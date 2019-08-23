package io.anuke.mnet.broadcast;

import io.anuke.arc.collection.*;
import io.anuke.arc.util.async.*;
import io.anuke.mnet.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class BroadcastServlet{
    private static int threadCounter;

    private final DatagramSocket socket;
    private final DatagramPacket sendingPacket;
    private final DatagramPacket receivingPacket;
    private final byte[] uuid;
    private final MSerializer serializer;
    private final BroadcastProcessor processor;
    private final Thread thread;
    private final HashMap<Pack, byte[]> memory = new HashMap<>();
    private final AtomicQueue<Request> requestAtomicQueue = new AtomicQueue<Request>(1000);
    private volatile boolean enabled = true;


    /**
     * Creates a new BroadcastServlet instance which is able to send responses to Locator in LAN.
     * Creates new UDP-listening thread
     * @param port Port which Servlet is listening. Must be the same for Locator.
     * @param bufferSize Max size of requests and responses. Make sure It's above any byte[] you're trying to send
     * @param processor Processor that will process Locators requests and respond to them.
     * @throws Exception if address can't be parsed.
     */
    public BroadcastServlet(int port, int bufferSize, String uuid, MSerializer serializer, BroadcastProcessor processor) throws Exception{
        this(port, bufferSize, uuid.getBytes(), serializer, processor);
    }

    /**
     * Creates a new BroadcastServlet instance which is able to send responses to Locator in LAN.
     * Creates new UDP-listening thread
     * @param port Port which Servlet is listening. Must be the same for Locator.
     * @param bufferSize Max size of requests and responses. Make sure It's above any byte[] you're trying to send
     * @param uuid Unique id for application. So that no other apps that use this library could see your request.
     * {@link BroadcastSocket} must have the same UUID in oder to receive requests!
     * @param processor Processor that will process Locators requests and respond to them.
     * @throws Exception if address can't be parsed.
     */
    public BroadcastServlet(int port, int bufferSize, byte[] uuid, MSerializer serializer, BroadcastProcessor processor) throws Exception{
        socket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
        sendingPacket = new DatagramPacket(new byte[bufferSize], bufferSize);
        receivingPacket = new DatagramPacket(new byte[bufferSize], bufferSize);
        this.uuid = LocatorUtils.normalizeUUID(uuid);
        this.serializer = serializer;
        this.processor = processor;
        thread = Threads.daemon("BroadcastServlet-" + threadCounter++, BroadcastServlet.this::run);
    }


    /**
     * Servlet must be enabled in order to function and trigger {@link BroadcastProcessor}.
     */
    public boolean isEnabled(){
        return enabled;
    }

    /**
     * Servlet must be enabled in order to function and trigger {@link BroadcastProcessor}.
     */
    public void enable(){
        enabled = true;
    }

    public void disable(){
        memory.clear();
        enabled = false;
    }

    public void update(){
        if(isClosed() || !isEnabled()) return;

        Request req = requestAtomicQueue.poll();
        if(req == null) return;

        while(req != null){
            Pack pack = req.pack;

            byte[] oldResponse = memory.get(pack);
            if(oldResponse == null){
                if(memory.size() > 16) memory.clear();
                byte[] serialized = serializer.serialize(processor.process(pack.address, pack.port, req.request));
                oldResponse = LocatorUtils.createResponse(uuid, pack.seq, serialized);
                memory.put(pack, oldResponse);
            }
            sendData(pack.address, pack.port, oldResponse);
            if(isClosed() || !isEnabled()) return;
            req = requestAtomicQueue.poll();
        }
    }

    /**
     * Closes UDP socket and related Thread
     */
    public void close(){
        socket.close();
    }

    public boolean isClosed(){
        return socket.isClosed();
    }


    private void run(){
        DatagramPacket receivingPacket = this.receivingPacket;
        DatagramSocket socket = this.socket;
        byte[] receivingBuffer = receivingPacket.getData();
        MSerializer serializer = this.serializer;

        while(!socket.isClosed()){

            try{
                socket.receive(receivingPacket);
            }catch(IOException e){
                if(socket.isClosed())
                    break;
                else
                    continue;
            }

            if(!enabled){
                continue;
            }

            int length = receivingPacket.getLength();
            if(length < LocatorUtils.minMsgLength){
                continue;
            }
            boolean startsWithUUID = LocatorUtils.startsWithUUID(receivingBuffer, uuid);
            if(!startsWithUUID || !LocatorUtils.isRequest(receivingBuffer)){
                continue;
            }

            final int seq = LocatorUtils.getSeq(receivingBuffer);
            final InetAddress address = receivingPacket.getAddress();
            final int port = receivingPacket.getPort();


            Object userRequest;
            try{
                userRequest = serializer.deserialize(receivingBuffer, 21, length - 21);
            }catch(Exception ignore){
                byte[] userData = new byte[length - 21];
                System.arraycopy(receivingBuffer, 21, userData, 0, length - 21);
                System.err.println("Failed to deserialize broadcast request: " + Arrays.toString(userData));
                continue;
            }

            Pack pack = new Pack(address, port, seq);
            requestAtomicQueue.put(new Request(pack, userRequest));
        }
    }

    private void sendData(InetAddress address, int port, byte[] data){
        DatagramPacket packet = this.sendingPacket;
        packet.setData(data);
        packet.setAddress(address);
        packet.setPort(port);
        try{
            socket.send(packet);
        }catch(IOException ignore){
        }
    }

    private class Pack{
        private final InetAddress address;
        private final int port;
        private final int seq;

        public Pack(InetAddress address, int port, int seq){
            this.address = address;
            this.port = port;
            this.seq = seq;
        }

        @Override
        public boolean equals(Object o){
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;

            Pack pack = (Pack)o;

            if(port != pack.port) return false;
            if(seq != pack.seq) return false;
            return address.equals(pack.address);
        }

        @Override
        public int hashCode(){
            int result = address.hashCode();
            result = 31 * result + port;
            result = 31 * result + seq;
            return result;
        }
    }

    private class Request{
        Pack pack;
        Object request;

        public Request(Pack pack, Object request){
            this.pack = pack;
            this.request = request;
        }
    }
}

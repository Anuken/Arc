package io.anuke.mnet.broadcast;


import io.anuke.mnet.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.*;

public class BroadcastSocket{

    private final UDPSocket udp;
    private final DatagramPacket sendingPacket;
    private final DatagramPacket receivingPacket;
    private final byte[] uuid;
    private final InetAddress address;
    private final int port;
    private final AtomicInteger seqCounter;
    private final MSerializer serializer;
    private final Object monitor;
    private volatile ReceiverBuffer receiver;
    private volatile Thread searchingThread;
    private volatile int currentSeq;


    /**
     * @param port {@link BroadcastServlet} must use the same port to communicate
     * @param bufferSize max byte[] size that is possible to send over UDP. Recommended to use 512
     * @param uuid Unique id for application or application version. So that no other apps that use this library could see your request.
     * {@link BroadcastServlet} must have the same UUID in oder to receive your requests!
     */
    public BroadcastSocket(int port, int bufferSize, String uuid, MSerializer serializer) throws SocketException, UnknownHostException{
        this(new JavaUDPSocket(), "255.255.255.255", port, bufferSize, uuid.getBytes(), serializer);
    }

    /**
     * @param socket Use {@link JavaUDPSocket} for actual connection
     * @param address target broadcast address. Use default 255.255.255.255 if you don't know what to put in here.
     * @param port {@link BroadcastServlet} must use the same port to communicate
     * @param bufferSize max byte[] size that is possible to send over UDP. Recommended to use 512
     * @param uuid Unique id for application or application version. So that no other apps that use this library could see your request.
     * {@link BroadcastServlet} must have the same UUID in oder to receive your requests!
     */
    public BroadcastSocket(UDPSocket socket, String address, int port, int bufferSize, byte[] uuid, MSerializer serializer) throws UnknownHostException, SocketException{
        this.udp = socket;
        this.address = InetAddress.getByName(address);
        this.port = port;
        this.seqCounter = new AtomicInteger();
        this.serializer = serializer;
        this.uuid = LocatorUtils.normalizeUUID(uuid);
        this.sendingPacket = new DatagramPacket(new byte[bufferSize], bufferSize);
        this.receivingPacket = new DatagramPacket(new byte[bufferSize], bufferSize);
        this.monitor = new Object();

        socket.setBroadcast(true);
        new Thread(new Runnable(){
            @Override
            public void run(){
                BroadcastSocket.this.run();
            }
        }).start();
    }

    public boolean isClosed(){
        return udp.isClosed();
    }

    public void close(){
        interrupt();
        udp.close();
    }

    /**
     * Interrupts current search if it's in action.
     * @return true if search was actually interrupted.
     */
    public boolean interrupt(){
        synchronized(monitor){
            if(receiver != null){
                receiver.receiver.finished(true);
                searchingThread.interrupt();
                searchingThread = null;
                receiver = null;
                return true;
            }
        }
        return false;
    }

    /**
     * Starts async search. Don't forget to synchronzie anything you get onto BroadcastReceiver!
     */
    public boolean search(Object data, final int timeMillis, final int resends, final BroadcastReceiver receiver){
        synchronized(monitor){
            if(this.receiver != null) return false;
            final byte[] serialized = serializer.serialize(data);
            final int seq = currentSeq = seqCounter.getAndIncrement();
            final byte[] fullPackage = LocatorUtils.createRequest(uuid, seq, serialized);

            this.receiver = new ReceiverBuffer(receiver);
            searchingThread = new Thread(new Runnable(){
                @Override
                public void run(){
                    final int resendCD = timeMillis / resends;

                    for(int i = 0; i < resends; i++){
                        sendData(fullPackage);
                        try{
                            Thread.sleep(resendCD);
                        }catch(InterruptedException e){
                            return;
                        }
                    }

                    synchronized(monitor){
                        ReceiverBuffer receiverBuffer = BroadcastSocket.this.receiver;
                        if(receiverBuffer != null){
                            receiverBuffer.receiver.finished(false);
                            BroadcastSocket.this.receiver = null;
                            BroadcastSocket.this.searchingThread = null;
                        }
                    }


                }
            });
            searchingThread.start();
        }
        return true;
    }

    public boolean isSearching(){
        synchronized(monitor){
            return receiver != null;
        }
    }

    private void run(){
        UDPSocket udp = this.udp;
        DatagramPacket receivingPacket = this.receivingPacket;
        byte[] buffer = receivingPacket.getData();
        byte[] uuid = this.uuid;

        while(!udp.isClosed()){
            try{
                udp.receive(receivingPacket);
            }catch(IOException e){
                if(udp.isClosed()){
                    break;
                }else{
                    continue;
                }
            }

            int length = receivingPacket.getLength();
            if(length < LocatorUtils.minMsgLength){
                continue;
            }

            boolean startsWithUUID = LocatorUtils.startsWithUUID(buffer, uuid);
            if(!startsWithUUID
            || !LocatorUtils.isResponse(buffer)){
                continue;
            }

            synchronized(monitor){
                ReceiverBuffer receiver = this.receiver;
                if(LocatorUtils.getSeq(buffer) != currentSeq || receiver == null) continue;

                InetAddress address = receivingPacket.getAddress();
                int port = receivingPacket.getPort();
                Address addr = new Address(address, port);
                if(!receiver.cointains(addr)){
                    receiver.add(addr);

                    Object userResponse = serializer.deserialize(buffer, 21, length - 21);
                    receiver.receiver.receive(new BroadcastResponse(address, port, userResponse));
                }
            }
        }
    }

    private void sendData(byte[] data){
        DatagramPacket sendingPacket = this.sendingPacket;
        sendingPacket.setData(data);
        sendingPacket.setAddress(address);
        sendingPacket.setPort(port);
        try{
            udp.send(sendingPacket);
        }catch(IOException e){
        }
    }


    private static class ReceiverBuffer{
        BroadcastReceiver receiver;
        ArrayList<Address> respondedAddresses;

        public ReceiverBuffer(BroadcastReceiver receiver){
            this.receiver = receiver;
            this.respondedAddresses = new ArrayList<Address>();
        }

        public boolean cointains(Address address){
            for(Address respondedAddress : respondedAddresses){
                if(address.address.equals(respondedAddress.address)
                && address.port == respondedAddress.port) return true;
            }
            return false;
        }

        public void add(Address address){
            respondedAddresses.add(address);
        }
    }

    private static class Address{
        InetAddress address;
        int port;

        public Address(InetAddress address, int port){
            this.address = address;
            this.port = port;
        }

    }
}

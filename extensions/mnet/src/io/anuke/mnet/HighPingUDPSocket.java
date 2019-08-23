package io.anuke.mnet;

import io.anuke.arc.collection.*;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * Adds additional delay between sending and receiving data over UDP
 */
public class HighPingUDPSocket implements UDPSocket, Runnable{

    private final UDPSocket delegate;
    private final int sendingPing;
    private final int receivingPing;
    private final Sender sender;
    private final LinkedBlockingQueue<DataTriplet> receiver = new LinkedBlockingQueue<DataTriplet>();
    private final DatagramPacket receivingPacket;
    private final Thread sendT;
    private final Thread recT;

    public HighPingUDPSocket(UDPSocket delegate, int additionalPing){
        this(delegate, additionalPing / 2, additionalPing / 2);
    }

    public HighPingUDPSocket(UDPSocket delegate, int sendingPing, int receivingPing){
        this.delegate = delegate;
        this.sendingPing = sendingPing;
        this.receivingPing = receivingPing;
        receivingPacket = new DatagramPacket(new byte[1500], 1500);
        recT = new Thread(this);
        sender = new Sender();
        sendT = new Thread(sender);

        recT.start();
        sendT.start();
    }

    @Override
    public int getLocalPort(){
        return delegate.getLocalPort();
    }

    @Override
    public void send(final DatagramPacket packet) throws IOException{
        final InetAddress address = packet.getAddress();
        final int port = packet.getPort();
        final byte[] packetBuffer = packet.getData();
        final int packetLength = packet.getLength();
        final byte[] data = new byte[packetLength];
        System.arraycopy(packetBuffer, packet.getOffset(), data, 0, packetLength);
        sender.add(new DataTriplet(address, port, data));
    }

    @Override
    public void receive(DatagramPacket packet) throws IOException{
        try{
            final DataTriplet take = receiver.take();
            packet.setAddress(take.address);
            packet.setPort(take.port);
            System.arraycopy(take.data, 0, packet.getData(), packet.getOffset(), take.data.length);
            packet.setLength(take.data.length);
            long currentTime = System.currentTimeMillis();
            while(currentTime - take.creationTime < receivingPing){
                Thread.sleep(1);
                currentTime = System.currentTimeMillis();
            }
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run(){

        try{
            while(true){
                delegate.receive(receivingPacket);
                final InetAddress address = receivingPacket.getAddress();
                final int port = receivingPacket.getPort();
                final byte[] data = new byte[receivingPacket.getLength()];
                System.arraycopy(receivingPacket.getData(), receivingPacket.getOffset(), data, 0, data.length);
                final DataTriplet triplet = new DataTriplet(address, port, data);
                receiver.offer(triplet);
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void close(){
        delegate.close();
        try{
            sendT.interrupt();
            recT.interrupt();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void setReceiveTimeout(int millis) throws SocketException{
        delegate.setReceiveTimeout(millis);
    }

    @Override
    public boolean isClosed(){
        return delegate.isClosed();
    }

    @Override
    public void connect(InetAddress address, int port){
        delegate.connect(address, port);
    }

    @Override
    public void setBroadcast(boolean enabled) throws SocketException{
        delegate.setBroadcast(enabled);
    }

    private class DataTriplet{


        private final InetAddress address;
        private final int port;
        private final byte[] data;
        private final long creationTime;

        public DataTriplet(InetAddress address, int port, byte[] data){
            this.creationTime = System.currentTimeMillis();
            this.address = address;
            this.port = port;
            this.data = data;
        }
    }

    private class Sender implements Runnable{


        private final AtomicQueue<DataTriplet> sendingQueue = new AtomicQueue<DataTriplet>(15000);

        @Override
        public void run(){
            AtomicQueue<DataTriplet> sendingQueue = this.sendingQueue;
            final DatagramPacket sendingPacket = new DatagramPacket(new byte[1400], 1400);

            try{
                while(!Thread.interrupted()){

                    DataTriplet poll = sendingQueue.poll();

                    while(poll != null){
                        long creationTime = poll.creationTime;
                        long l = System.currentTimeMillis();
                        while(l - creationTime < sendingPing){
                            Thread.sleep(1);
                            l = System.currentTimeMillis();
                        }
                        send(poll, sendingPacket);
                        poll = sendingQueue.poll();
                    }

                    Thread.sleep(1);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }


        public void add(DataTriplet triplet){
            sendingQueue.put(triplet);
        }

        private void send(DataTriplet triplet, DatagramPacket packet) throws Exception{
            packet.setAddress(triplet.address);
            packet.setPort(triplet.port);
            System.arraycopy(triplet.data, 0, packet.getData(), packet.getOffset(), triplet.data.length);
            packet.setLength(triplet.data.length);
            delegate.send(packet);
        }

    }
}

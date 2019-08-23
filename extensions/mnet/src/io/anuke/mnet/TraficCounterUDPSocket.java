package io.anuke.mnet;

import io.anuke.arc.util.async.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicLong;

public class TraficCounterUDPSocket implements UDPSocket{

    private final UDPSocket delegate;
    AtomicLong sendPacketCounter = new AtomicLong();
    AtomicLong sendSizeCounter = new AtomicLong();

    AtomicLong receivePacketCounter = new AtomicLong();
    AtomicLong receiveSizeCounter = new AtomicLong();

    public TraficCounterUDPSocket(final UDPSocket delegate, final int refreshRate, final Listener listener){
        this.delegate = delegate;

        Threads.daemon(() -> {
            try{

                while(!delegate.isClosed()){
                    long before = System.currentTimeMillis();
                    Thread.sleep(refreshRate);
                    long timePassed = System.currentTimeMillis() - before;

                    listener.tick(sendPacketCounter.getAndSet(0), sendSizeCounter.getAndSet(0), receivePacketCounter.getAndSet(0), receiveSizeCounter.getAndSet(0), timePassed);
                }
            }catch(InterruptedException e){
                e.printStackTrace();
            }

        });
    }


    @Override
    public int getLocalPort(){
        return delegate.getLocalPort();
    }

    @Override
    public void send(DatagramPacket packet) throws IOException{
        delegate.send(packet);
        sendPacketCounter.incrementAndGet();
        sendSizeCounter.addAndGet(packet.getLength());
    }

    @Override
    public void receive(DatagramPacket packet) throws IOException{
        delegate.receive(packet);
        receivePacketCounter.incrementAndGet();
        receiveSizeCounter.addAndGet(packet.getLength());
    }

    @Override
    public void setReceiveTimeout(int millis) throws SocketException{
        delegate.setReceiveTimeout(millis);
    }

    @Override
    public void connect(InetAddress address, int port){
        delegate.connect(address, port);
    }

    @Override
    public void close(){
        delegate.close();
    }

    @Override
    public boolean isClosed(){
        return delegate.isClosed();
    }

    @Override
    public void setBroadcast(boolean enabled) throws SocketException{
        delegate.setBroadcast(enabled);
    }

    public interface Listener{

        void tick(long packetsSent, long sentPacketsSize, long packetsReceived, long receivedPacketsSize, long timePassed);
    }


    public abstract static class DefaultListener implements Listener{

        long total;

        @Override
        public void tick(long sendPackets, long sendSize, long receivePackets, long receiveSize, long timePassed){
            if(sendPackets + receivePackets == 0) return;

            //28 - UDP header size
            long bytesSent = sendSize + (28 * sendPackets);
            long bytesReceived = receiveSize + (28 * receivePackets);

            total += bytesSent + bytesReceived;
            double bytesPerSecondSend = bytesSent / (((double)timePassed) / 1000);
            double bytesPerSecondReceived = bytesReceived / (((double)timePassed) / 1000);
            tick((float)bytesPerSecondSend / 1024, (float)bytesPerSecondReceived / 1024, total);
        }

        public abstract void tick(float kbpsSendSpeed, float kbpsReceiveSpeed, long totalBytes);
    }
}

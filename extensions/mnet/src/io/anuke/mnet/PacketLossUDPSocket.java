package io.anuke.mnet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Simulates packet loss over UDP
 */
public class PacketLossUDPSocket implements UDPSocket{

    private final UDPSocket delegate;
    private final double sendLoss;
    private final double receiveLoss;

    /**
     * Creates {@link UDPSocket} out of existing {@link UDPSocket}, but this one gets a chanse of <b>not</b>
     * sending a {@link DatagramPacket} when asked to. Used for testing
     * @param delegate which socket to use to send Datagrams
     * @param sendPacketLossChance Probability of a packet loss while sending data. Must be >=0 and <=100. Measured in percents %
     */
    public PacketLossUDPSocket(UDPSocket delegate, double sendPacketLossChance){
        this(delegate, sendPacketLossChance, 0);
    }

    /**
     * Creates {@link UDPSocket} out of existing {@link UDPSocket}, but this one gets a chanse of <b>not</b>
     * sending a {@link DatagramPacket} when asked to. Used for testing
     * @param delegate which socket to use to send Datagrams
     * @param sendPacketLossChance Probability of a packet loss while sending data. Must be >=0 and <=100. Measured in percents %
     * @param receivePacketLossChance Probability of a packet loss while receiving data. Must be >=0 and <=100. Measured in percents %
     */
    public PacketLossUDPSocket(UDPSocket delegate, double sendPacketLossChance, double receivePacketLossChance){
        if(sendPacketLossChance < 0 || sendPacketLossChance > 100
        || receivePacketLossChance < 0 || receivePacketLossChance > 100){
            throw new RuntimeException("Packet loss chance must be from 0 to 100%");
        }
        this.delegate = delegate;
        this.sendLoss = sendPacketLossChance / 100d;
        this.receiveLoss = receivePacketLossChance / 100d;
    }

    @Override
    public int getLocalPort(){
        return delegate.getLocalPort();
    }

    @Override
    public void send(DatagramPacket packet) throws IOException{
        if(Math.random() > sendLoss)
            delegate.send(packet);
    }

    @Override
    public void receive(DatagramPacket packet) throws IOException{
        if(receiveLoss == 0){
            delegate.receive(packet);
        }else{
            while(true){
                delegate.receive(packet);
                if(Math.random() > receiveLoss){
                    return;
                }
            }
        }
    }

    @Override
    public void setReceiveTimeout(int millis) throws SocketException{
        delegate.setReceiveTimeout(millis);
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
    public void connect(InetAddress address, int port){
        delegate.connect(address, port);
    }

    @Override
    public void setBroadcast(boolean enabled) throws SocketException{
        delegate.setBroadcast(enabled);
    }
}

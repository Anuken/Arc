package io.anuke.mnet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by amaklakov on 02.11.2017.
 * Interface, abstracting DatagramSocket and allowing to replace implementation with some custom code
 */
public interface UDPSocket{

    /**
     * Local port to which this socket is bind
     */
    int getLocalPort();

    /**
     * Sends containment of {@link DatagramPacket}. Throws {@link IOException} in bad cases
     */
    void send(DatagramPacket packet) throws IOException;

    /**
     * Blocks until the next datagram is received.
     * Throws {@link SocketException} if socket is getting close
     */
    void receive(DatagramPacket packet) throws IOException;

    /**
     * Timeout after which socket.receive will throw SocketException
     * @param millis time in milliseconds
     */
    void setReceiveTimeout(int millis) throws SocketException;

    /**
     * Any thread currently blocked in {@link #receive} upon this socket
     * will throw a {@link SocketException}.
     */
    void close();

    /**
     * @return Whether this socket is closed.
     */
    boolean isClosed();

    /**
     * Binds socket to the desired address, all datagrams will be send to and received from this address
     */
    void connect(InetAddress address, int port);

    /**
     * Enable/disable SO_BROADCAST.
     *
     * <p> Some operating systems may require that the Java virtual machine be
     * started with implementation specific privileges to enable this option or
     * send broadcast datagrams.
     * @param enabled whether or not to have broadcast turned on.
     * @throws SocketException if there is an error in the underlying protocol, such as an UDP
     * error.
     */
    void setBroadcast(boolean enabled) throws SocketException;
}

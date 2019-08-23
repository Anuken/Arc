package io.anuke.mnet;

import java.io.IOException;
import java.net.InetAddress;

public interface MSocket{

    /**
     * <p>Tries to establish connection to the server with custom user request.
     * Blocks thread for specified time until connected/rejected/timeout</p>
     * @param request Custom data (request data). Can be anything. For example login + password for validation.
     * Serialized maximum size is bufferSize - 1
     * @param timeout Blocking time in milliseconds
     * @return Connection response containing connection {@link ServerResponse result} and response data
     * @throws RuntimeException If this socket was created by server. Changing sub-server socket connections is forbidden
     */
    ServerResponse connect(Object request, int timeout) throws IOException;

    /**
     * Asynchronous connection establishment. Consumer will be called from another thread, so don't forget to sycnhronize
     */
    void connectAsync(Object request, int timeout, ServerResponseHandler handler);

    /**
     * <p>Sends data to connected socket if current state == CONNECTED</p>
     * <p>This method provides reliable, and ordered Object sending. Objects will be delivered in the order of sending.
     * Objects will be resent over and over until socket on the other end received it or disconnection occurs
     * </p>
     * @param o Object to be send. Max serialized size == bufferSize - 5. Object can be changed after calling this method
     */
    void send(Object o);


    /**
     * <p>Sends data to connected socket if current state == CONNECTED</p>
     * <p>This method provides reliable, and ordered Object sending. Objects will be delivered in the order of sending.
     * Objects will be resent over and over until socket on the other end received it or disconnection occurs
     * </p>
     * @param o Object to be send. Max serialized size == (bufferSize - 9) * 2^16. Object can be changed after calling this method
     */
    void sendBig(Object o);

    /**
     * <p>Sends Object to connected socket if current state == CONNECTED</p>
     * <p>This method sends data immediately to a socket on the other end and <b>does not provide reliability nor ordering</b>.
     * this method uses plain UDP, so Object might not be delivered or delivered in wrong order.
     * </p>
     * @param o Object to be send. Max serialized size == bufferSize - 1. Data will be copied, so can be changed after method returns
     */
    void sendUnreliable(Object o);

    /**
     * Sends already serialized data unreliably
     */
    void sendSerUnrel(byte[] data);

    /**
     * <p>Sends batch to connected socket if current state == CONNECTED</p>
     * <p>This method provides reliable, and ordered Object sending. Batches and Objects inside of the batch will be delivered in the order of sending.
     * Objects will be resent over and over until socket on the other end received it or disconnection occurs
     * </p>
     * @param batch Batch to be send.
     */
    void send(NetBatch batch);


    /**
     * <p>Receives data onto the {@link SocketProcessor}.
     * This process <b>must not be used by different threads or recursively at the same time</b> as it will throw an Exception.
     * Works better if you implement this method by one of your classes and pass the same instance every time, rather than instantiating
     * </p>
     * <p>If socket on the other end disconnects, this method might receive this Disconnection event and will trigger listeners.</p>
     * @param processor Instance that is going to receive all pending Objects in the order which they were sent.
     * Unreliable packets will also be consumed by this SocketProcessor. Receiving of the packets
     * can be temporarily stopped with {@link #stop()}
     */
    void update(SocketProcessor processor);

    /**
     * Currently working congestion manager
     */
    CongestionManager getCongestionManager();

    /**
     * Set congestion manager for controlling resend cooldown.
     */
    void setCongestionManager(CongestionManager cm);

    /**
     * Stops receiving objects
     */
    void stop();

    /**
     * Current value of how many milliseconds socket wait to resend presumably lost packet
     */
    long getResendDelay();

    /**
     * Sets current resend delay. Will determine how many milliseconds socket waits to resend presumably lost packet.
     * <b>If {@link #isCongestionControlEnabled()} feature enabled, then this field will be overwritten in the nearest future.</b>
     */
    void setResendDelay(long delay);

    /**
     * All listeners are removed when socket is closed
     */
    void addDcListener(DCListener listener);

    /**
     * All listeners are removed when socket is closed
     */
    void addPingListener(PingListener listener);

    void removeDcListener(DCListener listener);

    void removePingListener(PingListener listener);

    void removeAllListeners();

    /**
     * Sets how often ping should be updated in milliseconds. Note that ping frequency acts like a protocol heartbeat, so it must be
     * <li>1. Be less than inactivity timeout</li>
     * <li>2. Be less than 10 seconds, or UDP channel could be closed by NAT</li>
     */
    void setPingFrequency(int millis);

    /**
     * @return UserData of this socket. Uses auto-cast
     * Useful when you use single instance of SocketProcessor for processing multiple sockets.
     */
    <T> T getUserData();

    /**
     * Store your custom data in socket
     * Useful when you use single instance of SocketProcessor for processing multiple sockets.
     * Use {@link #getUserData()} to retrieve userData
     * @param userData Any object
     * @return userData object that was replaced (null for the 1st time)
     */
    <T> T setUserData(Object userData);

    /**
     * @return Current state of the socket
     */
    SocketState getState();

    /**
     * @return <b>True</b> if this socket is connected to any remote Socket. Same as getState() == CONNECTED
     */
    boolean isConnected();

    /**
     * @return <b>True</b> if currently {@link #update(SocketProcessor)} method is running.
     */
    boolean isProcessing();

    /**
     * @return Ping value after last ping update
     */
    float getPing();

    /**
     * @return Whether this socket is closed (after connection)
     */
    boolean isClosed();

    MSerializer getSerializer();

    /**
     * Same as {@link #send(Object)}.
     * Send already serialized data.
     */
    void sendSerialized(byte[] data);

    /**
     * Performs disconnect if at that time socket was connected, stops any internal threads associated with this socket and then closes the UDPSocket. After this method,
     * socket becomes unusable and you have to create new instance to establish connection.
     */
    boolean close(String msg);

    /**
     * Performs disconnect if at that time socket was connected, stops any internal threads associated with this socket and then closes the UDPSocket. After this method,
     * socket becomes unusable and you have to create new instance to establish connection.
     */
    boolean close();

    /**
     * @return InetAddress of the connected (or prevoiusly connected) remote socket. Value might be null if socket was never connected.
     */
    InetAddress getRemoteAddress();

    /**
     * @return port this socket is connected (was connected) to. Value might be -1 if socket was never connected.
     */
    int getRemotePort();

    /**
     * @return UDP channel of this socket
     */
    UDPSocket getUdp();

    /**
     * @return Local port of UDP socket
     */
    int getLocalPort();
}

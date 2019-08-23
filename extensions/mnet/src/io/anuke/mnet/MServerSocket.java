package io.anuke.mnet;

import io.anuke.arc.collection.*;
import io.anuke.arc.function.*;
import io.anuke.arc.util.async.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Job of a server socket is to accept new connections and handle subsockets.
 */
public class MServerSocket{
    final UDPSocket udp;
    final SocketMap socketMap;
    private final ServerAuthenticator authenticator;
    private DiscoveryHandler discoverer;
    DatagramPacket sendPacket; //update thread
    int inactivityTimeout;
    int bufferSize;
    int pingFrequency;
    int resendFrequency;
    MSerializer serializer;
    Supplier<MSerializer> serializerSupplier;
    private AtomicQueue<ConnectionRequest> connectionRequests;

    public MServerSocket(int port, ServerAuthenticator authenticator, Supplier<MSerializer> serializerSupplier, DiscoveryHandler discoverer) throws SocketException{
        this(new JavaUDPSocket(port), 1024, 15000, 2500, 125, authenticator, serializerSupplier, discoverer);
    }

    public MServerSocket(UDPSocket udp, int bufferSize, int inactivityTimeout, int pingFrequency, int resendFrequency, ServerAuthenticator authenticator, Supplier<MSerializer> serializerSupplier, DiscoveryHandler discoverer){
        this.udp = udp;
        this.bufferSize = bufferSize;
        this.inactivityTimeout = inactivityTimeout;
        this.pingFrequency = pingFrequency;
        this.resendFrequency = resendFrequency;
        this.authenticator = authenticator;
        this.socketMap = new SocketMap();
        this.connectionRequests = new AtomicQueue<>(1000);
        this.sendPacket = new DatagramPacket(new byte[0], 0);
        this.serializerSupplier = serializerSupplier;
        this.serializer = serializerSupplier.get();
        this.discoverer = discoverer;
        Threads.daemon("MServerSocket Thread", this::run);
    }

    void run(){
        UDPSocket udp = this.udp;
        byte[] buffer = new byte[bufferSize];
        DatagramPacket packet = new DatagramPacket(buffer, bufferSize);
        int len;
        SocketMap socketMap = this.socketMap;

        while(true){
            try{
                udp.receive(packet);
            }catch(IOException e){
                if(udp.isClosed()){
                    break;
                }
                continue;
            }
            len = packet.getLength();
            byte type = buffer[0];
            if(type == PacketType.discovery){
                if(discoverer == null){
                    continue;
                }

                DatagramPacket dpacket = discoverer.writeDiscoveryData();
                dpacket.setSocketAddress(packet.getSocketAddress());
                try{
                    udp.send(dpacket);
                }catch(IOException ignored){}

                continue;
            }
            if(len <= 5) continue;

            MSocket mSocket = socketMap.get(packet);
            if(mSocket != null){
                mSocket.receiveData(buffer, type, len);
            }else if(type == PacketType.connectionRequest){
                Object req;
                try{
                    req = serializer.deserialize(buffer, 1, len - 1);
                }catch(Exception e){
                    e.printStackTrace();
                    req = null;
                }
                connectionRequests.put(new ConnectionRequest(packet.getAddress(), packet.getPort(), req));
            }
        }
    }

    public void update(){
        updateDCAndSockets();
        processAuth();
    }

    /**
     * Calls ConnectionProcessor to accept new connections as they arrive
     */
    private void processAuth(){
        ConnectionRequest poll = connectionRequests.poll();
        while(poll != null){
            if (socketMap.get(poll.address, poll.port) != null) {
                poll = connectionRequests.poll();
                continue; //Отбрасываем если кто-то уже коннектился и подтвердился.
            }

            //Создаём полупустой сокет
            MSocket socket = new MSocket(udp, poll.address, poll.port, bufferSize);
            //Авторизация
            Connection conn = new Connection(this, socket, poll.userRequest);
            authenticator.acceptConnection(conn);
            if(!conn.isMadeChoice()){
                conn.reject(null);
            }

            poll = connectionRequests.poll();
        }
    }

    /**
     * Отключает сокеты которые давно не отвечали
     */
    private void updateDCAndSockets(){
        long now = System.currentTimeMillis();
        synchronized(socketMap){
            for(SocketMap.SocketWrap wrap : socketMap.sockets){
                MSocket socket = wrap.socket;
                if(now - socket.lastTimeReceivedMsg > socket.inactivityTimeout){
                    socket.queue.put(new MSocket.DisconnectionPacket(MSocket.DisconnectionPacket.TIMED_OUT, DCType.TIME_OUT));
                }else{
                    if(socket.isConnected()){
                        socket.checkResendAndPing();
                    }
                }
            }
        }
    }

    /**
     * @return how many sockets are connected right now
     */
    public int getSize(){
        return socketMap.size();
    }


    //***********//
    //* GET-SET *//
    //***********//

    public boolean isClosed(){
        return udp.isClosed();
    }

    public UDPSocket getUdp(){
        return udp;
    }

    public ArrayList<MSocket> getSockets(){
        return getSockets(new ArrayList<MSocket>());
    }

    public ArrayList<MSocket> getSockets(ArrayList<MSocket> sockets){
        if(sockets.size() > 0) sockets.clear();
        synchronized(socketMap){
            for(SocketMap.SocketWrap socket : socketMap.sockets){
                sockets.add(socket.socket);
            }
        }
        return sockets;
    }

    void removeMe(MSocket socket){
        socketMap.remove(socket);
    }

    public void close(){
        ArrayList<MSocket> sockets = getSockets();
        for(MSocket socket : sockets){
            socket.close(DCType.SERVER_SHUTDOWN);
        }
        udp.close();
    }

    private class ConnectionRequest{
        InetAddress address;
        int port;
        Object userRequest;

        public ConnectionRequest(InetAddress address, int port, Object userRequest){
            this.address = address;
            this.port = port;
            this.userRequest = userRequest;
        }
    }
}

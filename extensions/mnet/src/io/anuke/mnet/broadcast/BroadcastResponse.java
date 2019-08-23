package io.anuke.mnet.broadcast;


import java.net.InetAddress;

public class BroadcastResponse{

    private final InetAddress address;
    private final int port;
    private final Object data;

    BroadcastResponse(InetAddress address, int port, Object data){
        this.address = address;
        this.port = port;
        this.data = data;
    }

    public InetAddress getAddress(){
        return address;
    }

    public int getPort(){
        return port;
    }

    public Object getData(){
        return data;
    }


    @Override
    public String toString(){
        return "LocatorResponse{" +
        "address=" + address.getHostAddress() + ':' + port +
        ", data=" + data +
        '}';
    }
}

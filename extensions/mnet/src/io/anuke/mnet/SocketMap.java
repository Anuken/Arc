package io.anuke.mnet;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;

class SocketMap{

    ArrayList<SocketWrap> sockets = new ArrayList<SocketWrap>();

    public void put(MSocketImpl socket){
        put(socket.address, socket.port, socket);
    }

    public synchronized void put(InetAddress address, int port, MSocketImpl socket){
        sockets.add(new SocketWrap(address, port, socket));
    }

    public MSocketImpl get(DatagramPacket packet){
        return get(packet.getAddress(), packet.getPort());
    }

    public synchronized MSocketImpl get(InetAddress address, int port){
        for(SocketWrap socket : sockets){
            if(socket.address.equals(address) && socket.port == port){
                return socket.socket;
            }
        }
        return null;
    }

    public synchronized void remove(MSocketImpl socket){
        for(Iterator<SocketWrap> iter = sockets.iterator(); iter.hasNext(); ){
            SocketWrap wrap = iter.next();
            if(wrap.socket == socket){
                iter.remove();
                return;
            }
        }
    }

    public synchronized void clear(){
        sockets.clear();
    }

    public synchronized int size(){
        return sockets.size();
    }

    static class SocketWrap{
        InetAddress address;
        int port;
        MSocketImpl socket;

        public SocketWrap(InetAddress address, int port, MSocketImpl socket){
            this.address = address;
            this.port = port;
            this.socket = socket;
        }
    }

}

package io.anuke.mnet;


public interface SocketProcessor{

    /**
     * Receives next object from connected socket. Acts like iterator (socket.forEachNextPacket( (data) -> {}))
     * can be interrupted with help of SocketIterator.
     * @param o data that's received
     * @param socket socket from which data has come.
     */
    void process(MSocket socket, Object o);

}

package io.anuke.mnet;

public enum SocketState{

    /**
     * Socket has never been connected to desired source
     */
    NOT_CONNECTED,

    /**
     * Socket is connecting to it's destination right now
     */
    CONNECTING,

    /**
     * Socket is successfully connected. You can send data over it!
     */
    CONNECTED,

    /**
     * Socket was closed. No data will be sent or recevied. Socket can't be reused.
     */
    CLOSED


}

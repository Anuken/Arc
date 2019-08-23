package io.anuke.mnet;

public enum ResponseType{

    /**
     * Server accepted your request. You are now connected
     */
    ACCEPTED,

    /**
     * Server rejected your request. You're still Disconnected
     */
    REJECTED,

    /**
     * No response, Looks like server is down
     */
    NO_RESPONSE,

    /**
     * Check state of socket. Looks like you were already connected/connecting or socket was closed.
     */
    WRONG_STATE

}

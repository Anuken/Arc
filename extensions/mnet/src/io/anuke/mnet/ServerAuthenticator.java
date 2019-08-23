package io.anuke.mnet;

public interface ServerAuthenticator{

    /**
     * Make a decision if you want to accept new connection here.
     */
    void acceptConnection(Connection conn);

}

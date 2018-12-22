package io.anuke.arc.net;

import io.anuke.arc.Net.Protocol;
import io.anuke.arc.util.Disposable;
import io.anuke.arc.util.ArcRuntimeException;

/**
 * A server socket that accepts new incoming connections, returning {@link Socket} instances. The {@link #accept(SocketHints)}
 * method should preferably be called in a separate thread as it is blocking.
 * @author mzechner
 * @author noblemaster
 */
public interface ServerSocket extends Disposable{

    /** @return the Protocol used by this socket */
    Protocol getProtocol();

    /**
     * Accepts a new incoming connection from a client {@link Socket}. The given hints will be applied to the accepted socket.
     * Blocking, call on a separate thread.
     * @param hints additional {@link SocketHints} applied to the accepted {@link Socket}. Input null to use the default setting
     * provided by the system.
     * @return the accepted {@link Socket}
     * @throws ArcRuntimeException in case an error occurred
     */
    Socket accept(SocketHints hints);
}

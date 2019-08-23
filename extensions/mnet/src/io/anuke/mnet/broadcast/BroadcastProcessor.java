package io.anuke.mnet.broadcast;

import java.net.InetAddress;

public interface BroadcastProcessor{
    Object process(InetAddress address, int port, Object request);
}

package io.anuke.mnet;

import java.net.DatagramPacket;

public interface DiscoveryHandler{
    DatagramPacket writeDiscoveryData();
}

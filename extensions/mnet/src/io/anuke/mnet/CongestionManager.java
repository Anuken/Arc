package io.anuke.mnet;

public interface CongestionManager{
    long calculateDelay(MSocketImpl.ResendPacket respondedPacket, long currentTime, long currentDelay);
}

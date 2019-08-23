package io.anuke.mnet;

public interface CongestionManager{
    long calculateDelay(MSocket.ResendPacket respondedPacket, long currentTime, long currentDelay);
}

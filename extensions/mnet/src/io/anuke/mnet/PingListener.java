package io.anuke.mnet;

public interface PingListener{

    void pingChanged(MSocket socket, float ping);

}

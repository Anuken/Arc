package io.anuke.mnet;

public class BigStorage{

    public SortedIntList<byte[]> parts;
    private int totalPackets;

    public BigStorage(int totalPackets){
        this.totalPackets = totalPackets;
        parts = new SortedIntList<byte[]>();
    }

    public boolean put(int id, byte[] part){
        parts.insert(id, part);
        return parts.size >= totalPackets;
    }


}

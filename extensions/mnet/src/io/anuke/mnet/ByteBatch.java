package io.anuke.mnet;

import java.util.ArrayList;

/**
 * Used to send packets in a batch. All individual packets still must of size less than bufferSize.
 */
public class ByteBatch{

    final ArrayList<byte[]> array;

    public ByteBatch(int minSize){
        this.array = new ArrayList<byte[]>(minSize);
    }

    public ByteBatch(){
        this.array = new ArrayList<byte[]>();
    }

    public void add(byte[] bytes){
        array.add(bytes);
    }

    public void clear(){
        array.clear();
    }

    public int size(){
        return array.size();
    }

    public byte[] get(int i){
        return array.get(i);
    }

    public void remove(int i){
        array.remove(i);
    }

    public int calculateSize(){
        int sum = 6;
        for(byte[] bytes : array){
            sum += bytes.length;
        }
        return sum;
    }

}

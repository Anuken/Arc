package io.anuke.arc.util.io;

import java.io.ByteArrayOutputStream;

public class ReusableByteOutStream extends ByteArrayOutputStream{

    public int position(){
        return count;
    }

    public void position(int position){
        count = position;
    }

    public byte[] getBytes(){
        return buf;
    }
}

package io.anuke.arc.util.io;

import java.io.ByteArrayOutputStream;

public class CountableByteArrayOutputStream extends ByteArrayOutputStream{

    public int position(){
        return count;
    }
}

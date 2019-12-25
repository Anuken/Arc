package arc.util.io;

import java.io.ByteArrayOutputStream;

public class ReusableByteOutStream extends ByteArrayOutputStream{
    public byte[] getBytes(){
        return buf;
    }
}

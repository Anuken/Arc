package arc.util.io;

import java.io.*;

public class CounterInputStream extends FilterInputStream{
    protected int count;

    public CounterInputStream(InputStream inputStream){
        super(inputStream);
    }

    public int count(){
        return count;
    }

    public void resetCount(){
        count = 0;
    }

    @Override
    public long skip(long l) throws IOException{
        count += l;
        return super.skip(l);
    }

    @Override
    public int read() throws IOException{
        count ++;
        return in.read();
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        int total = in.read(bytes, offset, length);
        count += total;
        return total;
    }
}

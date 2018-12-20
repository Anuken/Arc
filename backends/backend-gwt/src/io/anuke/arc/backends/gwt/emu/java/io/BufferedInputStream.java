package java.io;

public class BufferedInputStream extends FilterInputStream{
    public BufferedInputStream(InputStream in){
        super(in);
    }

    public BufferedInputStream(InputStream in, int size){
        super(in);
    }
}

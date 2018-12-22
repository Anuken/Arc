package java.util.zip;

import io.anuke.arc.util.ArcRuntimeException;
import io.anuke.arc.util.io.StreamUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Dummy emulation. Throws a ArcRuntimeException on first read.
 * @author hneuer
 */
public class InflaterInputStream extends InputStream{
    private InputStream in;

    public InflaterInputStream(InputStream in){
        this.in = in;
    }

    @Override
    public int read(){
        throw new ArcRuntimeException("InflaterInputStream not supported in GWT");
    }

    @Override
    public void close() throws IOException{
        super.close();
        StreamUtils.closeQuietly(in);
    }
}

import arc.math.*;
import arc.util.*;
import org.junit.*;

import java.nio.*;

public class BufferSpeedTest{

    @Test
    public void bufferBenchmark(){
        ArcNativesLoader.load();
        int size = 1024 * 1024 * 1024;

        byte[] bytes = new byte[size];
        Mathf.random.nextBytes(bytes);

        ByteBuffer buffer = Buffers.newUnsafeByteBuffer(size);

        bench("unsafe copy", () -> {
            buffer.position(0);
            Buffers.copy(bytes, 0, buffer, size);
        });

        bench("unsafe put", () -> {
            buffer.position(0);
            buffer.put(bytes);
        });

        bench("unsafe copy 2", () -> {
            buffer.position(0);
            Buffers.copy(bytes, 0, buffer, size);
        });

        bench("unsafe put 2", () -> {
            buffer.position(0);
            buffer.put(bytes);
        });

        ByteBuffer safe = ByteBuffer.allocateDirect(size);

        bench("safe put", () -> {
            safe.position(0);
            safe.put(bytes);
        });

        bench("unsafe copy 3", () -> {
            buffer.position(0);
            Buffers.copy(bytes, 0, buffer, size);
        });

        bench("unsafe put 3", () -> {
            buffer.position(0);
            buffer.put(bytes);
        });

        bench("safe put 2", () -> {
            safe.position(0);
            safe.put(bytes);
        });
    }

    void bench(String name, Runnable run){
        Time.mark();
        run.run();
        Log.info("{0}: {1}", name, Time.elapsed());
    }
}

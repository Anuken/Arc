package arc.backend.teavm.emu;

import org.teavm.backend.javascript.codegen.*;
import org.teavm.backend.javascript.spi.*;
import org.teavm.model.*;

import java.io.*;


public class PixmapNativeGenerator implements Generator{
    @Override
    public void generate(GeneratorContext context, SourceWriter writer, MethodReference methodRef) throws IOException{
        if(methodRef.getName().equals("bufferAsArray")){
            String param = context.getParameterName(1);
            writer.append("return $rt_wrapArray($rt_bytecls(), new Int8Array(" + param + "));").softNewLine();
        }
    }
}

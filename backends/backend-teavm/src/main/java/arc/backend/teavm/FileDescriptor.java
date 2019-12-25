package arc.backend.teavm;

import org.teavm.jso.*;
import org.teavm.jso.core.*;


public interface FileDescriptor extends JSObject{
    @JSProperty
    String getName();

    @JSProperty
    boolean isDirectory();

    @JSProperty
    JSArrayReader<FileDescriptor> getChildFiles();
}

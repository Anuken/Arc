package io.anuke.arc.backends.teavm;

import org.teavm.jso.core.JSArrayReader;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

/**
 *
 * @author Alexey Andreev
 */
public interface FileDescriptor extends JSObject {
    @JSProperty
    String getName();

    @JSProperty
    boolean isDirectory();

    @JSProperty
    JSArrayReader<FileDescriptor> getChildFiles();
}

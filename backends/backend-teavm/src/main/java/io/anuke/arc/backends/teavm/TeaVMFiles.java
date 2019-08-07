package io.anuke.arc.backends.teavm;

import io.anuke.arc.*;
import io.anuke.arc.files.*;
import io.anuke.arc.util.*;
import org.teavm.jso.browser.*;

/**
 *
 * @author Alexey Andreev
 */
public class TeaVMFiles implements Files {
    public static final Storage localStorage = Storage.getLocalStorage();

    @Override
    public FileHandle getFileHandle(String path, FileType type) {
        if (type != FileType.Internal) {
            throw new ArcRuntimeException("FileType '" + type + "' not supported in GWT backend");
        }
        return new TeaVMFileHandle(path, type);
    }

    @Override
    public FileHandle classpath(String path) {
        return new TeaVMFileHandle(path, FileType.Classpath);
    }

    @Override
    public FileHandle internal(String path) {
        return new TeaVMFileHandle(path, FileType.Internal);
    }

    @Override
    public FileHandle external(String path) {
        throw new ArcRuntimeException("External files not supported in GWT backend");
    }

    @Override
    public FileHandle absolute(String path) {
        throw new ArcRuntimeException("Absolute files not supported in GWT backend");
    }

    @Override
    public FileHandle local(String path) {
        throw new ArcRuntimeException("local files not supported in GWT backend");
    }

    @Override
    public String getExternalStoragePath() {
        return null;
    }

    @Override
    public boolean isExternalStorageAvailable() {
        return false;
    }

    @Override
    public String getLocalStoragePath() {
        return null;
    }

    @Override
    public boolean isLocalStorageAvailable() {
        return false;
    }
}

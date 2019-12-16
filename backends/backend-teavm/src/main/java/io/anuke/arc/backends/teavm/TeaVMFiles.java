package io.anuke.arc.backends.teavm;

import io.anuke.arc.*;
import io.anuke.arc.files.*;
import io.anuke.arc.util.*;
import org.teavm.jso.browser.*;


public class TeaVMFiles implements Files{
    public static final Storage localStorage = Storage.getLocalStorage();

    @Override
    public Fi getFileHandle(String path, FileType type){
        if(type != FileType.Internal){
            throw new ArcRuntimeException("FileType '" + type + "' not supported in GWT backend");
        }
        return new TeaVMFi(path, type);
    }

    @Override
    public Fi classpath(String path){
        return new TeaVMFi(path, FileType.Classpath);
    }

    @Override
    public Fi internal(String path){
        return new TeaVMFi(path, FileType.Internal);
    }

    @Override
    public Fi external(String path){
        throw new ArcRuntimeException("External files not supported in GWT backend");
    }

    @Override
    public Fi absolute(String path){
        throw new ArcRuntimeException("Absolute files not supported in GWT backend");
    }

    @Override
    public Fi local(String path){
        throw new ArcRuntimeException("local files not supported in GWT backend");
    }

    @Override
    public String getExternalStoragePath(){
        return null;
    }

    @Override
    public boolean isExternalStorageAvailable(){
        return false;
    }

    @Override
    public String getLocalStoragePath(){
        return null;
    }

    @Override
    public boolean isLocalStorageAvailable(){
        return false;
    }
}

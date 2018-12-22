package io.anuke.arc.backends.gwt;

import com.google.gwt.storage.client.Storage;
import io.anuke.arc.Files;
import io.anuke.arc.Files.FileType;
import io.anuke.arc.backends.gwt.preloader.Preloader;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.util.ArcRuntimeException;

public class GwtFiles implements Files{

    public static final Storage LocalStorage = Storage.getLocalStorageIfSupported();

    final Preloader preloader;

    public GwtFiles(Preloader preloader){
        this.preloader = preloader;
    }

    @Override
    public FileHandle getFileHandle(String path, FileType type){
        if(type != FileType.Internal)
            throw new ArcRuntimeException("FileType '" + type + "' not supported in GWT backend");
        return new GwtFileHandle(preloader, path, type);
    }

    @Override
    public FileHandle classpath(String path){
        return new GwtFileHandle(preloader, path, FileType.Classpath);
    }

    @Override
    public FileHandle internal(String path){
        return new GwtFileHandle(preloader, path, FileType.Internal);
    }

    @Override
    public FileHandle external(String path){
        throw new ArcRuntimeException("Not supported in GWT backend");
    }

    @Override
    public FileHandle absolute(String path){
        throw new ArcRuntimeException("Not supported in GWT backend");
    }

    @Override
    public FileHandle local(String path){
        throw new ArcRuntimeException("Not supported in GWT backend");
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

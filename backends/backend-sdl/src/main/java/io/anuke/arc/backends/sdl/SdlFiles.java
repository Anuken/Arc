package io.anuke.arc.backends.sdl;

import io.anuke.arc.*;
import io.anuke.arc.files.*;
import io.anuke.arc.util.*;

import java.io.*;

public final class SdlFiles implements Files{
    public static final String externalPath = System.getProperty("user.home") + File.separator;
    public static final String localPath = new File("").getAbsolutePath() + File.separator;

    @Override
    public Fi getFileHandle(String fileName, FileType type){
        return new SdlFi(fileName, type);
    }

    @Override
    public Fi classpath(String path){
        return new SdlFi(path, FileType.Classpath);
    }

    @Override
    public Fi internal(String path){
        return new SdlFi(path, FileType.Internal);
    }

    @Override
    public Fi external(String path){
        return new SdlFi(path, FileType.External);
    }

    @Override
    public Fi absolute(String path){
        return new SdlFi(path, FileType.Absolute);
    }

    @Override
    public Fi local(String path){
        return new SdlFi(path, FileType.Local);
    }

    @Override
    public String getExternalStoragePath(){
        return externalPath;
    }

    @Override
    public boolean isExternalStorageAvailable(){
        return true;
    }

    @Override
    public String getLocalStoragePath(){
        return localPath;
    }

    @Override
    public boolean isLocalStorageAvailable(){
        return true;
    }

    public static final class SdlFi extends Fi{
        public SdlFi(String fileName, FileType type){
            super(fileName, type);
        }
    
        public SdlFi(File file, FileType type){
            super(file, type);
        }
    
        public Fi child(String name){
            if(file.getPath().length() == 0) return new SdlFi(new File(name), type);
            return new SdlFi(new File(file, name), type);
        }
    
        public Fi sibling(String name){
            if(file.getPath().length() == 0) throw new ArcRuntimeException("Cannot get the sibling of the root.");
            return new SdlFi(new File(file.getParent(), name), type);
        }
    
        public File file(){
            if(type == FileType.External) return new File(externalPath, file.getPath());
            if(type == FileType.Local) return new File(localPath, file.getPath());
            return file;
        }
    }
}

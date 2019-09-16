package io.anuke.arc.backends.sdl;

import io.anuke.arc.*;
import io.anuke.arc.files.*;
import io.anuke.arc.util.*;

import java.io.*;

public final class SdlFiles implements Files{
    public static final String externalPath = System.getProperty("user.home") + File.separator;
    public static final String localPath = new File("").getAbsolutePath() + File.separator;

    @Override
    public FileHandle getFileHandle(String fileName, FileType type){
        return new SdlFileHandle(fileName, type);
    }

    @Override
    public FileHandle classpath(String path){
        return new SdlFileHandle(path, FileType.Classpath);
    }

    @Override
    public FileHandle internal(String path){
        return new SdlFileHandle(path, FileType.Internal);
    }

    @Override
    public FileHandle external(String path){
        return new SdlFileHandle(path, FileType.External);
    }

    @Override
    public FileHandle absolute(String path){
        return new SdlFileHandle(path, FileType.Absolute);
    }

    @Override
    public FileHandle local(String path){
        return new SdlFileHandle(path, FileType.Local);
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

    /**
     * @author mzechner
     * @author Nathan Sweet
     */
    public static final class SdlFileHandle extends FileHandle{
        public SdlFileHandle(String fileName, FileType type){
            super(fileName, type);
        }
    
        public SdlFileHandle(File file, FileType type){
            super(file, type);
        }
    
        public FileHandle child(String name){
            if(file.getPath().length() == 0) return new SdlFileHandle(new File(name), type);
            return new SdlFileHandle(new File(file, name), type);
        }
    
        public FileHandle sibling(String name){
            if(file.getPath().length() == 0) throw new ArcRuntimeException("Cannot get the sibling of the root.");
            return new SdlFileHandle(new File(file.getParent(), name), type);
        }
    
        public File file(){
            if(type == FileType.External) return new File(externalPath, file.getPath());
            if(type == FileType.Local) return new File(localPath, file.getPath());
            return file;
        }
    }
}

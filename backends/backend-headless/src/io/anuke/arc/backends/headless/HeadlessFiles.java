package io.anuke.arc.backends.headless;

import io.anuke.arc.Files;
import io.anuke.arc.files.FileHandle;

import java.io.File;

/**
 * @author mzechner
 * @author Nathan Sweet
 */
public final class HeadlessFiles implements Files{
    static public final String externalPath = System.getProperty("user.home") + File.separator;
    static public final String localPath = new File("").getAbsolutePath() + File.separator;

    @Override
    public FileHandle getFileHandle(String fileName, FileType type){
        return new HeadlessFileHandle(fileName, type);
    }

    @Override
    public FileHandle classpath(String path){
        return new HeadlessFileHandle(path, FileType.Classpath);
    }

    @Override
    public FileHandle internal(String path){
        return new HeadlessFileHandle(path, FileType.Internal);
    }

    @Override
    public FileHandle external(String path){
        return new HeadlessFileHandle(path, FileType.External);
    }

    @Override
    public FileHandle absolute(String path){
        return new HeadlessFileHandle(path, FileType.Absolute);
    }

    @Override
    public FileHandle local(String path){
        return new HeadlessFileHandle(path, FileType.Local);
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
}

package io.anuke.arc.files;

import io.anuke.arc.Files.FileType;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A FileHandle intended to be subclassed for the purpose of implementing {@link #read()} and/or {@link #write(boolean)}. Methods
 * that would manipulate the file instead throw UnsupportedOperationException.
 * @author Nathan Sweet
 */
public abstract class FileHandleStream extends FileHandle{
    /** Create an {@link FileType#Absolute} file at the given location. */
    public FileHandleStream(String path){
        super(new File(path), FileType.Absolute);
    }

    public boolean isDirectory(){
        return false;
    }

    public long length(){
        return 0;
    }

    public boolean exists(){
        return true;
    }

    public FileHandle child(String name){
        throw new UnsupportedOperationException();
    }

    public FileHandle sibling(String name){
        throw new UnsupportedOperationException();
    }

    public FileHandle parent(){
        throw new UnsupportedOperationException();
    }

    public InputStream read(){
        throw new UnsupportedOperationException();
    }

    public OutputStream write(boolean overwrite){
        throw new UnsupportedOperationException();
    }

    public FileHandle[] list(){
        throw new UnsupportedOperationException();
    }

    public void mkdirs(){
        throw new UnsupportedOperationException();
    }

    public boolean delete(){
        throw new UnsupportedOperationException();
    }

    public boolean deleteDirectory(){
        throw new UnsupportedOperationException();
    }

    public void copyTo(FileHandle dest){
        throw new UnsupportedOperationException();
    }

    public void moveTo(FileHandle dest){
        throw new UnsupportedOperationException();
    }
}

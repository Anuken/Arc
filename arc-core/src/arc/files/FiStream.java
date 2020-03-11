package arc.files;

import arc.Files.FileType;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A FileHandle intended to be subclassed for the purpose of implementing {@link #read()} and/or {@link #write(boolean)}. Methods
 * that would manipulate the file instead throw UnsupportedOperationException.
 * @author Nathan Sweet
 */
public abstract class FiStream extends Fi{
    /** Create an {@link FileType#absolute} file at the given location. */
    public FiStream(String path){
        super(new File(path), FileType.absolute);
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

    public Fi child(String name){
        throw new UnsupportedOperationException();
    }

    public Fi sibling(String name){
        throw new UnsupportedOperationException();
    }

    public Fi parent(){
        throw new UnsupportedOperationException();
    }

    public InputStream read(){
        throw new UnsupportedOperationException();
    }

    public OutputStream write(boolean overwrite){
        throw new UnsupportedOperationException();
    }

    public Fi[] list(){
        throw new UnsupportedOperationException();
    }

    public boolean mkdirs(){
        throw new UnsupportedOperationException();
    }

    public boolean delete(){
        throw new UnsupportedOperationException();
    }

    public boolean deleteDirectory(){
        throw new UnsupportedOperationException();
    }

    public void copyTo(Fi dest){
        throw new UnsupportedOperationException();
    }

    public void moveTo(Fi dest){
        throw new UnsupportedOperationException();
    }
}

package io.anuke.arc.backends.lwjgl3;

import io.anuke.arc.Files;
import io.anuke.arc.files.Fi;
import io.anuke.arc.util.*;

import java.io.File;

/**
 * @author mzechner
 * @author Nathan Sweet
 */
public final class Lwjgl3Files implements Files{
    public static final String externalPath = System.getProperty("user.home") + File.separator;
    public static final String localPath = new File("").getAbsolutePath() + File.separator;

    @Override
    public Fi getFileHandle(String fileName, FileType type){
        return new Lwjgl3Fi(fileName, type);
    }

    @Override
    public Fi classpath(String path){
        return new Lwjgl3Fi(path, FileType.Classpath);
    }

    @Override
    public Fi internal(String path){
        return new Lwjgl3Fi(path, FileType.Internal);
    }

    @Override
    public Fi external(String path){
        return new Lwjgl3Fi(path, FileType.External);
    }

    @Override
    public Fi absolute(String path){
        return new Lwjgl3Fi(path, FileType.Absolute);
    }

    @Override
    public Fi local(String path){
        return new Lwjgl3Fi(path, FileType.Local);
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
    public static final class Lwjgl3Fi extends Fi{
        public Lwjgl3Fi(String fileName, FileType type){
            super(fileName, type);
        }

        public Lwjgl3Fi(File file, FileType type){
            super(file, type);
        }

        public Fi child(String name){
            if(file.getPath().length() == 0) return new Lwjgl3Fi(new File(name), type);
            return new Lwjgl3Fi(new File(file, name), type);
        }

        public Fi sibling(String name){
            if(file.getPath().length() == 0) throw new ArcRuntimeException("Cannot get the sibling of the root.");
            return new Lwjgl3Fi(new File(file.getParent(), name), type);
        }

        public Fi parent(){
            File parent = file.getParentFile();
            if(parent == null){
                if(type == FileType.Absolute)
                    parent = new File("/");
                else
                    parent = new File("");
            }
            return new Lwjgl3Fi(parent, type);
        }

        public File file(){
            if(type == FileType.External) return new File(externalPath, file.getPath());
            if(type == FileType.Local) return new File(localPath, file.getPath());
            return file;
        }
    }
}

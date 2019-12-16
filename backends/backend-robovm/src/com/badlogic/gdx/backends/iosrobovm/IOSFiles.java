package com.badlogic.gdx.backends.iosrobovm;

import io.anuke.arc.Files;
import io.anuke.arc.files.Fi;
import org.robovm.apple.foundation.NSBundle;

public class IOSFiles implements Files{
    // TODO: Use NSSearchPathForDirectoriesInDomains instead?
    // $HOME should point to the app root dir.
    static final String appDir = System.getenv("HOME");
    static final String externalPath = appDir + "/Documents/";
    static final String localPath = appDir + "/Library/local/";
    static final String internalPath = NSBundle.getMainBundle().getBundlePath();

    public IOSFiles(){
        new Fi(externalPath).mkdirs();
        new Fi(localPath).mkdirs();
    }

    @Override
    public Fi getFileHandle(String fileName, FileType type){
        return new IOSFi(fileName, type);
    }

    @Override
    public Fi classpath(String path){
        return new IOSFi(path, FileType.Classpath);
    }

    @Override
    public Fi internal(String path){
        return new IOSFi(path, FileType.Internal);
    }

    @Override
    public Fi external(String path){
        return new IOSFi(path, FileType.External);
    }

    @Override
    public Fi absolute(String path){
        return new IOSFi(path, FileType.Absolute);
    }

    @Override
    public Fi local(String path){
        return new IOSFi(path, FileType.Local);
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

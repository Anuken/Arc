package io.anuke.arc.backends.headless;

import io.anuke.arc.Files.FileType;
import io.anuke.arc.files.Fi;
import io.anuke.arc.util.ArcRuntimeException;

import java.io.File;

/**
 * @author mzechner
 * @author Nathan Sweet
 */
public final class HeadlessFi extends Fi{
    public HeadlessFi(String fileName, FileType type){
        super(fileName, type);
    }

    public HeadlessFi(File file, FileType type){
        super(file, type);
    }

    public Fi child(String name){
        if(file.getPath().length() == 0) return new HeadlessFi(new File(name), type);
        return new HeadlessFi(new File(file, name), type);
    }

    public Fi sibling(String name){
        if(file.getPath().length() == 0) throw new ArcRuntimeException("Cannot get the sibling of the root.");
        return new HeadlessFi(new File(file.getParent(), name), type);
    }

    public Fi parent(){
        File parent = file.getParentFile();
        if(parent == null){
            if(type == FileType.Absolute)
                parent = new File("/");
            else
                parent = new File("");
        }
        return new HeadlessFi(parent, type);
    }

    public File file(){
        if(type == FileType.External) return new File(HeadlessFiles.externalPath, file.getPath());
        if(type == FileType.Local) return new File(HeadlessFiles.localPath, file.getPath());
        return file;
    }
}

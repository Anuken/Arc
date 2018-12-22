package io.anuke.arc.backends.headless;

import io.anuke.arc.Files.FileType;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.util.ArcRuntimeException;

import java.io.File;

/**
 * @author mzechner
 * @author Nathan Sweet
 */
public final class HeadlessFileHandle extends FileHandle{
    public HeadlessFileHandle(String fileName, FileType type){
        super(fileName, type);
    }

    public HeadlessFileHandle(File file, FileType type){
        super(file, type);
    }

    public FileHandle child(String name){
        if(file.getPath().length() == 0) return new HeadlessFileHandle(new File(name), type);
        return new HeadlessFileHandle(new File(file, name), type);
    }

    public FileHandle sibling(String name){
        if(file.getPath().length() == 0) throw new ArcRuntimeException("Cannot get the sibling of the root.");
        return new HeadlessFileHandle(new File(file.getParent(), name), type);
    }

    public FileHandle parent(){
        File parent = file.getParentFile();
        if(parent == null){
            if(type == FileType.Absolute)
                parent = new File("/");
            else
                parent = new File("");
        }
        return new HeadlessFileHandle(parent, type);
    }

    public File file(){
        if(type == FileType.External) return new File(HeadlessFiles.externalPath, file.getPath());
        if(type == FileType.Local) return new File(HeadlessFiles.localPath, file.getPath());
        return file;
    }
}

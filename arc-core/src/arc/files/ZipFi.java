package arc.files;

import arc.Files.*;
import arc.struct.*;
import arc.util.*;
import arc.util.ArcAnnotate.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/** A FileHandle meant for easily representing and reading the contents of a zip/jar file.*/
public class ZipFi extends Fi{
    private ZipFi[] children = {};
    private ZipFi parent;

    private final @Nullable ZipEntry entry;
    private final @NonNull ZipFile zip;

    public ZipFi(Fi zipFileLoc){
        super(new File(""), FileType.absolute);
        entry = null;

        try{
            zip = new ZipFile(zipFileLoc.file());

            Array<String> names = Array.with(Collections.list(zip.entries())).map(ZipEntry::getName);
            ObjectSet<String> paths = new ObjectSet<>();

            for(String path : names){
                paths.add(path);
                while(path.contains("/") && !path.equals("/") && path.substring(0, path.length() - 1).contains("/")){
                    int index = path.endsWith("/") ? path.substring(0, path.length() - 1).lastIndexOf('/') : path.lastIndexOf('/');
                    path = path.substring(0, index);
                    paths.add(path.endsWith("/") ? path : path + "/");
                }
            }

            if(paths.contains("/")){
                file = new File("/");
                paths.remove("/");
            }

            Array<ZipFi> files = Array.with(paths).map(s -> zip.getEntry(s) != null ?
                new ZipFi(zip.getEntry(s), zip) : new ZipFi(s, zip));

            files.add(this);

            //find parents
            files.each(file -> file.parent = files.find(other -> other.isDirectory() && other != file
                && file.path().startsWith(other.path()) && !file.path().substring(1 + other.path().length()).contains("/")));
            //transform parents into children
            files.each(file -> file.children = files.select(f -> f.parent == file).toArray(ZipFi.class));

            parent = null;
        }catch(IOException e){
            throw new ArcRuntimeException(e);
        }
    }

    private ZipFi(ZipEntry entry, ZipFile file){
        super(new File(entry.getName()), FileType.absolute);
        this.entry = entry;
        this.zip = file;
    }

    private ZipFi(String path, ZipFile file){
        super(new File(path), FileType.absolute);
        this.entry = null;
        this.zip = file;
    }

    @Override
    public boolean delete(){
        try{
            zip.close();
        }catch(IOException e){
            Log.err(e);
            return false;
        }

        return super.delete();
    }

    @Override
    public boolean exists(){
        return true;
    }

    @Override
    public Fi child(String name){
        for(ZipFi child : children){
            if(child.name().equals(name)){
                return child;
            }
        }
        return new Fi(new File(file, name)){
            @Override
            public boolean exists(){
                return false;
            }
        };
    }

    @Override
    public String name(){
        return file.getName();
    }

    @Override
    public Fi parent(){
        return parent;
    }

    @Override
    public Fi[] list(){
        return children;
    }

    @Override
    public boolean isDirectory(){
        return entry == null || entry.isDirectory();
    }

    @Override
    public InputStream read(){
        if(entry == null) throw new RuntimeException("Not permitted.");
        try{
            return zip.getInputStream(entry);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public long length(){
        return isDirectory() ? 0 : entry.getSize();
    }
}
package arc.files;

import arc.Files.*;
import arc.struct.*;
import arc.util.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/** A FileHandle meant for easily representing and reading the contents of a zip/jar file.*/
public class ZipFi extends Fi{
    private ZipFi[] children = {};
    private ZipFi parent;
    private String path;

    private final @Nullable ZipEntry entry;
    private final ZipFile zip;

    public ZipFi(Fi zipFileLoc){
        super(new File(""), FileType.absolute);
        entry = null;

        try{
            zip = new ZipFile(zipFileLoc.file());
            path = "";

            Seq<String> names = Seq.with(Collections.list(zip.entries())).map(z -> z.getName().replace('\\', '/'));
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

            Seq<ZipFi> files = Seq.with(paths).map(s -> zip.getEntry(s) != null ?
                new ZipFi(zip.getEntry(s), zip) : new ZipFi(s, zip));

            files.add(this);

            //find parents
            files.each(file -> file.parent = files.find(other -> other.isDirectory() && other != file
                && file.path().startsWith(other.path())
                && (!file.path().substring(1 + other.path().length()).contains("/") || //do not allow extra slashes in the path
                    (file.path().endsWith("/") && countSlahes(file.path().substring(1 + other.path().length())) == 1)))); //unless it's a directory
            //transform parents into children
            files.each(file -> file.children = files.select(f -> f.parent == file).toArray(ZipFi.class));

            parent = null;
        }catch(IOException e){
            throw new ArcRuntimeException(e);
        }
    }

    private int countSlahes(String str){
        int sum = 0;
        for(int i = 0; i < str.length(); i++){
            if(str.charAt(i) == '/') sum ++;
        }
        return sum;
    }

    private ZipFi(ZipEntry entry, ZipFile file){
        super(new File(entry.getName()), FileType.absolute);
        this.path = entry.getName().replace('\\', '/');
        this.entry = entry;
        this.zip = file;
    }

    private ZipFi(String path, ZipFile file){
        super(new File(path), FileType.absolute);
        this.path = path.replace('\\', '/');
        this.entry = null;
        this.zip = file;
    }

    @Override
    public boolean delete(){
        try{
            zip.close();
            return true;
        }catch(IOException e){
            Log.err(e);
            return false;
        }
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
    public String path(){
        return path;
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

    @Override
    public String toString(){
        return path();
    }
}
package arc.backend.teavm;

import arc.Files.*;
import arc.struct.*;
import arc.files.*;
import arc.util.*;
import org.teavm.jso.dom.html.*;

import java.io.*;
import java.util.*;

public class TeaFi extends Fi{
    public static final FSEntry root = new FSEntry();
    private final String file;
    private final FileType type;

    public static class FSEntry{
        public final ObjectMap<String, FSEntry> childEntries = new ObjectMap<>();
        public byte[] data;
        public long lastModified;
        public boolean directory;
        public HTMLImageElement imageElem;
    }

    public TeaFi(String fileName, FileType type){
        if(type != FileType.internal && type != FileType.classpath){
            throw new ArcRuntimeException("FileType '" + type + "' Not supported in GWT backend");
        }
        this.file = fixSlashes(fileName);
        this.type = type;
    }

    public TeaFi(String path){
        this.type = FileType.internal;
        this.file = fixSlashes(path);
    }

    @Override
    public String path(){
        return file;
    }

    @Override
    public String name(){
        int index = file.lastIndexOf('/');
        if(index < 0){
            return file;
        }
        return file.substring(index + 1);
    }

    @Override
    public String extension(){
        String name = name();
        int dotIndex = name.lastIndexOf('.');
        if(dotIndex == -1){
            return "";
        }
        return name.substring(dotIndex + 1);
    }

    @Override
    public String nameWithoutExtension(){
        String name = name();
        int dotIndex = name.lastIndexOf('.');
        if(dotIndex == -1){
            return name;
        }
        return name.substring(0, dotIndex);
    }

    @Override
    public String pathWithoutExtension(){
        String path = file;
        int dotIndex = path.lastIndexOf('.');
        if(dotIndex == -1){
            return path;
        }
        return path.substring(0, dotIndex);
    }

    @Override
    public FileType type(){
        return type;
    }

    @Override
    public InputStream read(){
        FSEntry entry = entry();
        if(entry == null || entry.data == null){
            throw new ArcRuntimeException(file + " does not exist");
        }
        return new ByteArrayInputStream(entry.data);
    }

    public FSEntry entry(){
        FSEntry entry = root;
        for(String part : split()){
            entry = entry.childEntries.get(part);
            if(entry == null){
                break;
            }
        }
        return entry;
    }

    private String[] split(){
        List<String> result = new ArrayList<>();
        int index = 0;
        while(index < file.length()){
            int next = file.indexOf('/', index);
            if(next == -1){
                break;
            }
            addPart(index, next, result);
            index = next + 1;
        }
        addPart(index, file.length(), result);
        return result.toArray(new String[result.size()]);
    }

    private void addPart(int index, int next, List<String> result){
        String part = file.substring(index, next);
        if(!part.isEmpty() && !part.equals(".")){
            if(part.equals("..")){
                result.remove(result.size() - 1);
            }else{
                result.add(part);
            }
        }
    }

    @Override
    public BufferedInputStream read(int bufferSize){
        return new BufferedInputStream(read(), bufferSize);
    }

    @Override
    public Reader reader(){
        return new InputStreamReader(read());
    }

    @Override
    public String readString(){
        return new String(readBytes());
    }

    @Override
    public byte[] readBytes(){
        FSEntry entry = entry();
        if(entry == null || entry.data == null){
            throw new ArcRuntimeException("File does not exist: " + file);
        }
        return Arrays.copyOf(entry.data, entry.data.length);
    }

    @Override
    public int readBytes(byte[] bytes, int offset, int size){
        FSEntry entry = entry();
        if(entry == null || entry.data == null){
            throw new ArcRuntimeException("File does not exist: " + file);
        }
        size = Math.min(size, entry.data.length);
        System.arraycopy(entry.data, 0, bytes, offset, size);
        return size;
    }

    @Override
    public Fi[] list(){
        FSEntry entry = entry();
        if(entry == null){
            throw new ArcRuntimeException("File does not exist: " + file);
        }
        Fi[] result = new Fi[entry.childEntries.size];
        int index = 0;
        for(String childName : entry.childEntries.keys()){
            result[index++] = new TeaFi(file + "/" + childName, type);
        }
        return result;
    }

    @Override
    public Fi[] list(String suffix){
        FSEntry entry = entry();
        if(entry == null){
            throw new ArcRuntimeException("File does not exist: " + file);
        }
        Fi[] result = new Fi[entry.childEntries.size];
        int index = 0;
        for(String childName : entry.childEntries.keys()){
            if(childName.endsWith(suffix)){
                result[index++] = new TeaFi(file + "/" + childName, type);
            }
        }
        return index == result.length ? result : Arrays.copyOf(result, index);
    }

    @Override
    public boolean isDirectory(){
        FSEntry entry = entry();
        return entry != null && entry.data == null;
    }

    @Override
    public Fi child(String name){
        return new TeaFi(file + "/" + fixSlashes(name), type);
    }

    @Override
    public Fi parent(){
        int index = file.lastIndexOf('/', file.endsWith("/") ? file.length() - 1 : file.length());
        return index > 1 ? new TeaFi(file.substring(0, index), type) : this;
    }

    @Override
    public Fi sibling(String name){
        return parent().child(fixSlashes(name));
    }

    @Override
    public boolean exists(){
        return entry() != null;
    }

    @Override
    public long length(){
        FSEntry entry = entry();
        return entry != null && entry.data != null ? entry.data.length : 0;
    }

    @Override
    public long lastModified(){
        FSEntry entry = entry();
        return entry != null ? entry.lastModified : 0;
    }

    @Override
    public String toString(){
        return file;
    }

    private static String fixSlashes(String path){
        path = path.replace('\\', '/');
        if(path.endsWith("/")){
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }
}

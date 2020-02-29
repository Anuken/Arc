package arc;

import arc.struct.*;
import arc.struct.ObjectMap.*;
import arc.files.*;
import arc.func.*;
import arc.util.*;
import arc.util.io.*;
import arc.util.io.Streams.*;
import arc.util.serialization.*;

import java.io.*;

import static arc.Core.keybinds;

@SuppressWarnings("unchecked")
public class Settings{
    protected final static byte TYPE_BOOL = 0, TYPE_INT = 1, TYPE_LONG = 2, TYPE_FLOAT = 3, TYPE_STRING = 4, TYPE_BINARY = 5;

    //general state data
    protected Fi dataDirectory;
    protected String appName;
    protected ObjectMap<String, Object> defaults = new ObjectMap<>();
    protected ObjectMap<String, Object> values = new ObjectMap<>();
    protected Cons<Throwable> errorHandler;
    protected boolean hasErrored;

    //IO utility objects
    protected ByteArrayOutputStream byteStream = new OptimizedByteArrayOutputStream(16);
    protected ReusableByteInStream byteInputStream = new ReusableByteInStream();
    protected DataOutputStream dataOutput = new DataOutputStream(byteStream);
    protected DataInputStream dataInput = new DataInputStream(byteInputStream);
    protected ObjectMap<Class<?>, TypeSerializer<?>> serializers = new ObjectMap<>();
    protected UBJsonReader ureader = new UBJsonReader();
    protected Json json = new Json();

    public Settings(){
        DefaultSerializers.register(this);
    }

    public TypeSerializer getSerializer(Class type){
        if(type.isAnonymousClass()) type = type.getSuperclass();
        Class ftype = type;

        if(!serializers.containsKey(type)){
            return new TypeSerializer(){
                @Override
                public void write(DataOutput stream, Object object) throws IOException{
                    json.toUBJson(object, ftype, (OutputStream)stream);
                }

                @Override
                public Object read(DataInput stream) throws IOException{
                    JsonValue value = ureader.parse((InputStream)stream);
                    return json.readValue(ftype, value);
                }
            };
        }
        return serializers.get(type);
    }

    public <T> void setSerializer(Class<T> type, TypeWriter<T> writer, TypeReader<T> reader){
        serializers.put(type, new TypeSerializer<T>(){
            @Override public void write(DataOutput stream, T object) throws IOException{ writer.write(stream, object); }
            @Override public T read(DataInput stream) throws IOException{ return reader.read(stream); }
        });
    }

    public <T> void setSerializer(Class<?> type, TypeSerializer<T> ser){
        serializers.put(type, ser);
    }

    public String getAppName(){
        return appName;
    }

    public void setAppName(String name){
        appName = name;
    }

    /**Sets the error handler function.
     * This function gets called when {@link #save} or {@link #load} fails. This can occur most often on browsers,
     * where extensions can block writing to local storage.*/
    public void setErrorHandler(Cons<Throwable> handler){
        errorHandler = handler;
    }

    /** Loads all values and keybinds. */
    public void load(){
        try{
            loadValues();
            keybinds.load();
        }catch(Throwable error){
            if(errorHandler != null){
                if(!hasErrored) errorHandler.get(error);
            }else{
                throw error;
            }
            hasErrored = true;
        }
    }

    /** Saves all values and keybinds. */
    public void save(){
        try{
            keybinds.save();
            saveValues();
        }catch(Throwable error){
            if(errorHandler != null){
                if(!hasErrored) errorHandler.get(error);
            }else{
                throw error;
            }
            hasErrored = true;
        }
    }

    /** Loads a settings file into {@link #values} using the specified appName. */
    public void loadValues(){
        //don't load settings files if neither of them exist
        if(!getSettingsFile().exists() && !getBackupSettingsFile().exists()){
            return;
        }

        try{
            loadValues(getSettingsFile());

            //back up the save file, as the values have now been loaded successfully
            getSettingsFile().copyTo(getBackupSettingsFile());
        }catch(Exception e){
            Log.err("Failed to load base settings file, attempting to load backup.", e);
            try{
                //attempt to load backup
                loadValues(getBackupSettingsFile());
                //copy to normal settings file for future use
                getBackupSettingsFile().copyTo(getSettingsFile());
                Log.info("Loaded backup settings file.");
            }catch(Exception e2){
                Log.err("Failed to load backup settings file.", e2);
            }
        }
    }

    public void loadValues(Fi file) throws IOException{
        try(DataInputStream stream = new DataInputStream(file.read(8192))){
            int amount = stream.readInt();
            for(int i = 0; i < amount; i++){
                String key = stream.readUTF();
                byte type = stream.readByte();

                switch(type){
                    case TYPE_BOOL:
                        values.put(key, stream.readBoolean());
                        break;
                    case TYPE_INT:
                        values.put(key, stream.readInt());
                        break;
                    case TYPE_LONG:
                        values.put(key, stream.readLong());
                        break;
                    case TYPE_FLOAT:
                        values.put(key, stream.readFloat());
                        break;
                    case TYPE_STRING:
                        values.put(key, stream.readUTF());
                        break;
                    case TYPE_BINARY:
                        int length = stream.readInt();
                        byte[] bytes = new byte[length];
                        stream.read(bytes);
                        values.put(key, bytes);
                        break;
                }
            }
        }
    }

    /** Saves all entries from {@link #values} into the correct location. */
    public void saveValues(){
        Fi file = getSettingsFile();

        try(DataOutputStream stream = new DataOutputStream(file.write(false, 8192))){
            stream.writeInt(values.size);

            for(Entry<String, Object> entry : values.entries()){
                stream.writeUTF(entry.key);

                Object value = entry.value;

                if(value instanceof Boolean){
                    stream.writeByte(TYPE_BOOL);
                    stream.writeBoolean((Boolean)value);
                }else if(value instanceof Integer){
                    stream.writeByte(TYPE_INT);
                    stream.writeInt((Integer)value);
                }else if(value instanceof Long){
                    stream.writeByte(TYPE_LONG);
                    stream.writeLong((Long)value);
                }else if(value instanceof Float){
                    stream.writeByte(TYPE_FLOAT);
                    stream.writeFloat((Float)value);
                }else if(value instanceof String){
                    stream.writeByte(TYPE_STRING);
                    stream.writeUTF((String)value);
                }else if(value instanceof byte[]){
                    stream.writeByte(TYPE_BINARY);
                    stream.writeInt(((byte[])value).length);
                    stream.write((byte[])value);
                }
            }
        }catch(Throwable e){
            //file is now corrupt, delete it
            file.delete();
            throw new RuntimeException("Error writing preferences: " + file, e);
        }
    }

    /** Returns the file used for writing settings to. Not available on all platforms! */
    public Fi getSettingsFile(){
        return getDataDirectory().child("settings.bin");
    }

    public Fi getBackupSettingsFile(){
        return getDataDirectory().child("settings_backup.bin");
    }

    /** Returns the directory where all settings and data is placed. */
    public Fi getDataDirectory(){
        return dataDirectory == null ? Core.files.absolute(OS.getAppDataDirectoryString(appName)) : dataDirectory;
    }

    /** Sets the settings file where everything is written to. */
    public void setDataDirectory(Fi file){
        this.dataDirectory = file;
    }

    /**
     * Set up a list of defaults values.
     * Format: name1, default1, name2, default2, etc
     */
    public void defaults(Object... objects){
        for(int i = 0; i < objects.length; i += 2){
            defaults.put((String)objects[i], objects[i + 1]);
        }
    }

    /** Clears all prefence values. */
    public void clear(){
        values.clear();
    }

    public Object getDefault(String name){
        return defaults.get(name);
    }

    public boolean has(String name){
        return values.containsKey(name);
    }

    public Object get(String name, Object def){
        return values.get(name, def);
    }

    public void putObject(String name, Object value){
        putObject(name, value, value.getClass());
    }

    @SuppressWarnings("unchecked")
    public void putObject(String name, Object value, Class<?> type){
        getSerializer(type);
        if(!serializers.containsKey(type)){
            throw new IllegalArgumentException(type + " does not have a serializer registered!");
        }
        byteStream.reset();

        TypeSerializer serializer = serializers.get(type);
        try{
            serializer.write(dataOutput, value);
            put(name, byteStream.toByteArray());
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject(String name, Class<T> type, Prov<T> def){
        getSerializer(type);
        if(!serializers.containsKey(type)){
            throw new IllegalArgumentException("Type " + type + " does not have a serializer registered!");
        }

        TypeSerializer serializer = serializers.get(type);

        try{
            byteInputStream.setBytes(getBytes(name));
            Object obj = serializer.read(dataInput);
            if(obj == null) return def.get();
            return (T)obj;
        }catch(Exception e){
            return def.get();
        }
    }

    public float getFloat(String name, float def){
        return (float)values.get(name, def);
    }

    public long getLong(String name, long def){
        return (long)values.get(name, def);
    }

    public Long getLong(String name){
        return getLong(name, 0);
    }

    public int getInt(String name, int def){
        return (int)values.get(name, def);
    }

    public boolean getBool(String name, boolean def){
        return (boolean)values.get(name, def);
    }

    public byte[] getBytes(String name, byte[] def){
        return (byte[])values.get(name, def);
    }

    public String getString(String name, String def){
        return (String)values.get(name, def);
    }

    public float getFloat(String name){
        return getFloat(name, (float)defaults.get(name, 0f));
    }

    public int getInt(String name){
        return getInt(name, (int)defaults.get(name, 0));
    }

    public boolean getBool(String name){
        return getBool(name, (boolean)defaults.get(name, false));
    }

    /** Runs the specified code once, and never again. */
    public void getBoolOnce(String name, Runnable run){
        if(!getBool(name, false)){
            run.run();
            put(name, true);
            save();
        }
    }

    public byte[] getBytes(String name){
        return getBytes(name, (byte[])defaults.get(name, null));
    }

    public String getString(String name){
        return getString(name, (String)defaults.get(name, null));
    }

    public void putAll(ObjectMap<String, Object> map){
        for(Entry<String, Object> entry : map.entries()){
            put(entry.key, entry.value);
        }
    }

    /** Stores an object in the preference map and saves. */
    public void putSave(String name, Object object){
        put(name, object);
        save();
    }

    /** Stores an object in the preference map. */
    public void put(String name, Object object){
        if(object instanceof Float || object instanceof Integer || object instanceof Boolean || object instanceof Long
        || object instanceof String || object instanceof byte[]){
            values.put(name, object);
        }else{
            throw new IllegalArgumentException("Invalid object stored: " + (object == null ? null : object.getClass()) + ". Use putObject() for serialization.");
        }
    }

    public void remove(String name){
        values.remove(name);
    }

    public Iterable<String> keys(){
        return values.keys();
    }

    public interface TypeSerializer<T>{
        void write(DataOutput stream, T object) throws IOException;
        T read(DataInput stream) throws IOException;
    }

    public interface TypeWriter<T>{
        void write(DataOutput stream, T object) throws IOException;
    }

    public interface TypeReader<T>{
        T read(DataInput stream) throws IOException;
    }
}

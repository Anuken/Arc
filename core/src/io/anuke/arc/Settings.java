package io.anuke.arc;

import io.anuke.arc.collection.ObjectMap;
import io.anuke.arc.collection.ObjectMap.Entry;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.util.OS;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static io.anuke.arc.Core.keybinds;

public class Settings{
    protected final static byte TYPE_BOOL = 0, TYPE_INT = 1, TYPE_LONG = 2, TYPE_FLOAT = 3, TYPE_STRING = 4, TYPE_BINARY = 5;

    protected FileHandle dataDirectory;
    protected String appName;
    protected ObjectMap<String, Object> defaults = new ObjectMap<>();
    protected ObjectMap<String, Object> values = new ObjectMap<>();

    public String getAppName(){
        return appName;
    }

    public void setAppName(String name){
        appName = name;
    }

    /** Loads all values and keybinds. */
    public void load(){
        keybinds.save();
        saveValues();
    }

    /** Saves all values and keybinds. */
    public void save(){
        loadValues();
        keybinds.load();
    }

    /** Loads a settings file into {@link values} using the specified appName. */
    public void loadValues(){
        FileHandle file = getSettingsFile();

        try(DataInputStream stream = new DataInputStream(file.read())){
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
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /** Saves all entries from {@link values} into the correct location. */
    public void saveValues(){
        FileHandle file = getSettingsFile();

        try(DataOutputStream stream = new DataOutputStream(file.write(false))){
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
        }catch(IOException e){
            throw new RuntimeException("Error writing preferences: " + file, e);
        }
    }

    /** Returns the file used for writing settings to. Not available on all platforms! */
    public FileHandle getSettingsFile(){
        return getDataDirectory().child("settings.bin");
    }

    /** Returns the directory where all settings and data is placed. */
    public FileHandle getDataDirectory(){
        return dataDirectory == null ? Core.files.absolute(OS.getAppDataDirectoryString(appName)) : dataDirectory;
    }

    /** Sets the settings file where everything is written to. */
    public void setDataDirectory(FileHandle file){
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

    public float getFloat(String name, float def){
        return (float)values.get(name, def);
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

    public byte[] getBytes(String name){
        return getBytes(name, (byte[])defaults.get(name, null));
    }

    public String getString(String name){
        return getString(name, (String)defaults.get(name, null));
    }

    /** Stores an object in the preference map. */
    public void put(String name, Object object){
        if(object instanceof Float || object instanceof Integer || object instanceof Boolean
        || object instanceof String || object instanceof byte[]){
            values.put(name, object);
        }else{
            throw new IllegalArgumentException("Invalid object stored: " + object);
        }
    }
}

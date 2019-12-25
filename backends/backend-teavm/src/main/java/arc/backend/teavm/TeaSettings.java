package arc.backend.teavm;

import arc.*;
import arc.util.*;

public class TeaSettings extends Settings{
    String prefix;

    @Override
    public void setAppName(String name){
        super.setAppName(name);
        prefix = name + ":";
    }

    @Override
    public void saveValues(){

        try{
            // remove all old values
            for(int i = 0; i < TeaFiles.localStorage.getLength(); i++){
                String key = TeaFiles.localStorage.key(i);
                if(key.startsWith(prefix)) TeaFiles.localStorage.removeItem(key);
            }

            // push new values to LocalStorage
            for(String key : values.keys()){
                String storageKey = toStorageKey(key, values.get(key));
                String storageValue = "" + values.get(key).toString();
                TeaFiles.localStorage.setItem(storageKey, storageValue);
            }

        }catch(Exception e){
            throw new ArcRuntimeException("Couldn't flush preferences");
        }
    }

    @Override
    public void loadValues(){
        try{
            for(int i = 0; i < TeaFiles.localStorage.getLength(); i++){
                String key = TeaFiles.localStorage.key(i);
                if(key.startsWith(prefix)){
                    String value = TeaFiles.localStorage.getItem(key);
                    values.put(key.substring(prefix.length(), key.length() - 1), toObject(key, value));
                }
            }
        }catch(Exception e){
            values.clear();
        }
    }

    private Object toObject(String key, String value){
        if(key.endsWith("b")) return (Boolean.parseBoolean(value));
        if(key.endsWith("i")) return (Integer.parseInt(value));
        if(key.endsWith("l")) return (Long.parseLong(value));
        if(key.endsWith("f")) return (Float.parseFloat(value));
        return value;
    }

    private String toStorageKey(String key, Object value){
        if(value instanceof Boolean) return prefix + key + "b";
        if(value instanceof Integer) return prefix + key + "i";
        if(value instanceof Long) return prefix + key + "l";
        if(value instanceof Float) return prefix + key + "f";
        return prefix + key + "s";
    }
}

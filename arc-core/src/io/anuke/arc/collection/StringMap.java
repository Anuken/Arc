package io.anuke.arc.collection;

import io.anuke.arc.util.Strings;

/** An ObjectMap with string keys and values. Comes with extra parsing utilities.*/
public class StringMap extends ObjectMap<String, String>{

    public int getInt(String name){
        return Strings.parseInt(get(name), 0);
    }

    public float getFloat(String name){
        return Strings.parseFloat(get(name), 0f);
    }

    public long getLong(String name){
        return Strings.parseLong(get(name), 0L);
    }
}

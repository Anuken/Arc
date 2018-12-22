package io.anuke.arc.maps;

import io.anuke.arc.collection.Array;
import io.anuke.arc.util.reflect.ClassReflection;

import java.util.Iterator;

/** @brief Collection of MapObject instances */
public class MapObjects implements Iterable<MapObject>{

    private Array<MapObject> objects;

    /** Creates an empty set of MapObject instances */
    public MapObjects(){
        objects = new Array<>();
    }

    /**
     * @return the MapObject at the specified index
     */
    public MapObject get(int index){
        return objects.get(index);
    }

    /**
     * @return the first object having the specified name, if one exists, otherwise null
     */
    public MapObject get(String name){
        for(int i = 0, n = objects.size; i < n; i++){
            MapObject object = objects.get(i);
            if(name.equals(object.getName())){
                return object;
            }
        }
        return null;
    }

    /** Get the index of the object having the specified name, or -1 if no such object exists. */
    public int getIndex(String name){
        return getIndex(get(name));
    }

    /** Get the index of the object in the collection, or -1 if no such object exists. */
    public int getIndex(MapObject object){
        return objects.indexOf(object, true);
    }

    /** @return number of objects in the collection */
    public int getCount(){
        return objects.size;
    }

    /** @param object instance to be added to the collection */
    public void add(MapObject object){
        this.objects.add(object);
    }

    /** @param index removes MapObject instance at index */
    public void remove(int index){
        objects.removeAt(index);
    }

    /** @param object instance to be removed */
    public void remove(MapObject object){
        objects.removeValue(object, true);
    }

    /**
     * @param type class of the objects we want to retrieve
     * @return array filled with all the objects in the collection matching type
     */
    public <T extends MapObject> Array<T> getByType(Class<T> type){
        return getByType(type, new Array<>());
    }

    /**
     * @param type class of the objects we want to retrieve
     * @param fill collection to put the returned objects in
     * @return array filled with all the objects in the collection matching type
     */
    @SuppressWarnings("unchecked")
    public <T extends MapObject> Array<T> getByType(Class<T> type, Array<T> fill){
        fill.clear();
        for(int i = 0, n = objects.size; i < n; i++){
            MapObject object = objects.get(i);
            if(ClassReflection.isInstance(type, object)){
                fill.add((T)object);
            }
        }
        return fill;
    }

    /** @return iterator for the objects within the collection */
    @Override
    public Iterator<MapObject> iterator(){
        return objects.iterator();
    }

}

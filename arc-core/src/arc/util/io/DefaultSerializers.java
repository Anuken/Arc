package arc.util.io;


import arc.Settings;
import arc.Settings.TypeSerializer;
import arc.struct.*;
import arc.struct.ObjectMap.Entry;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@SuppressWarnings("unchecked")
public class DefaultSerializers{
    public static StringMap typeMappings = StringMap.of();
    public static String[] typeReplacements = {"io.anuke.", "", "arc.collection", "arc.struct"};

    public static void register(Settings settings){
        settings.setSerializer(IntArray.class, new TypeSerializer<IntArray>(){
            @Override
            public void write(DataOutput stream, IntArray object) throws IOException{
                stream.writeInt(object.size);
                for(int i = 0; i < object.size; i++){
                    stream.writeInt(object.get(i));
                }
            }

            @Override
            public IntArray read(DataInput stream) throws IOException{
                int size = stream.readInt();
                IntArray a = new IntArray(size);
                for(int i = 0; i < size; i++){
                    a.add(stream.readInt());
                }
                return a;
            }
        });

        settings.setSerializer(String.class, new TypeSerializer<String>(){
            @Override
            public void write(DataOutput stream, String object) throws IOException{
                stream.writeUTF(object == null ? "" : object);
            }

            @Override
            public String read(DataInput stream) throws IOException{
                return stream.readUTF();
            }
        });

        settings.setSerializer(Array.class, new TypeSerializer<Array>(){
            @Override
            public void write(DataOutput stream, Array object) throws IOException{
                stream.writeInt(object.size);
                if(object.size != 0){
                    TypeSerializer ser = settings.getSerializer(object.get(0).getClass());
                    if(ser == null) throw new IllegalArgumentException(object.get(0).getClass() + " does not have a serializer registered!");

                    stream.writeUTF(object.get(0).getClass().getName());

                    for(Object element : object){
                        ser.write(stream, element);
                    }
                }
            }

            @Override
            public Array read(DataInput stream) throws IOException{
                try{
                    int size = stream.readInt();
                    Array arr = new Array(size);

                    if(size == 0) return arr;

                    String type = stream.readUTF();

                    TypeSerializer ser = settings.getSerializer(lookup(type));
                    if(ser == null) throw new IllegalArgumentException(type + " does not have a serializer registered!");


                    for(int i = 0; i < size; i++){
                        arr.add(ser.read(stream));
                    }

                    return arr;
                }catch(ClassNotFoundException e){
                    e.printStackTrace();
                    return null;
                }
            }
        });

        settings.setSerializer(ObjectSet.class, new TypeSerializer<ObjectSet>(){
            @Override
            public void write(DataOutput stream, ObjectSet object) throws IOException{
                stream.writeInt(object.size);
                if(object.size != 0){
                    TypeSerializer ser = settings.getSerializer(object.first().getClass());
                    if(ser == null) throw new IllegalArgumentException(object.first().getClass() + " does not have a serializer registered!");

                    stream.writeUTF(object.first().getClass().getName());

                    for(Object element : object){
                        ser.write(stream, element);
                    }
                }
            }

            @Override
            public ObjectSet read(DataInput stream) throws IOException{
                try{
                    int size = stream.readInt();
                    ObjectSet arr = new ObjectSet();

                    if(size == 0) return arr;

                    String type = stream.readUTF();

                    TypeSerializer ser = settings.getSerializer(lookup(type));
                    if(ser == null)
                        throw new IllegalArgumentException(type + " does not have a serializer registered!");

                    for(int i = 0; i < size; i++){
                        arr.add(ser.read(stream));
                    }

                    return arr;
                }catch(ClassNotFoundException e){
                    e.printStackTrace();
                    return null;
                }
            }
        });

        settings.setSerializer(ObjectMap.class, new TypeSerializer<ObjectMap>(){
            @Override
            public void write(DataOutput stream, ObjectMap map) throws IOException{
                stream.writeInt(map.size);
                if(map.size == 0) return;
                Entry entry = map.entries().next();

                TypeSerializer keyser = settings.getSerializer(entry.key.getClass());
                TypeSerializer valser = settings.getSerializer(entry.value.getClass());
                if(keyser == null) throw new IllegalArgumentException(entry.key.getClass() + " does not have a serializer registered!");
                if(valser == null) throw new IllegalArgumentException(entry.value.getClass() + " does not have a serializer registered!");

                stream.writeUTF(entry.key.getClass().getName());
                stream.writeUTF(entry.value.getClass().getName());

                for(Object e : map.entries()){
                    Entry en = (Entry)e;
                    keyser.write(stream, en.key);
                    valser.write(stream, en.value);
                }
            }

            @Override
            public ObjectMap read(DataInput stream) throws IOException{
                try{
                    int size = stream.readInt();
                    ObjectMap map = new ObjectMap();
                    if(size == 0) return map;

                    String keyt = stream.readUTF();
                    String valt = stream.readUTF();

                    TypeSerializer keyser = settings.getSerializer(lookup(keyt));
                    TypeSerializer valser = settings.getSerializer(lookup(valt));
                    if(keyser == null) throw new IllegalArgumentException(keyt + " does not have a serializer registered!");
                    if(valser == null) throw new IllegalArgumentException(valt + " does not have a serializer registered!");

                    for(int i = 0; i < size; i++){
                        Object key = keyser.read(stream);
                        Object val = valser.read(stream);
                        map.put(key, val);
                    }

                    return map;
                }catch(ClassNotFoundException e){
                    e.printStackTrace();
                    return null;
                }
            }
        });

        settings.setSerializer(ObjectIntMap.class, new TypeSerializer<ObjectIntMap>(){
            @Override
            public void write(DataOutput stream, ObjectIntMap map) throws IOException{
                stream.writeInt(map.size);
                if(map.size == 0) return;
                ObjectIntMap.Entry entry = map.entries().next();

                TypeSerializer keyser = settings.getSerializer(entry.key.getClass());
                if(keyser == null) throw new IllegalArgumentException(entry.key.getClass() + " does not have a serializer registered!");

                stream.writeUTF(entry.key.getClass().getName());

                for(Object e : map.entries()){
                    ObjectIntMap.Entry en = (ObjectIntMap.Entry)e;
                    keyser.write(stream, en.key);
                    stream.writeInt(en.value);
                }
            }

            @Override
            public ObjectIntMap read(DataInput stream) throws IOException{
                try{
                    int size = stream.readInt();
                    ObjectIntMap map = new ObjectIntMap();
                    if(size == 0) return map;

                    String keyt = stream.readUTF();

                    TypeSerializer keyser = settings.getSerializer(lookup(keyt));
                    if(keyser == null) throw new IllegalArgumentException(keyt + " does not have a serializer registered!");

                    for(int i = 0; i < size; i++){
                        Object key = keyser.read(stream);
                        int val = stream.readInt();
                        map.put(key, val);
                    }

                    return map;
                }catch(ClassNotFoundException e){
                    e.printStackTrace();
                    return null;
                }
            }
        });
    }
    
    private static Class<?> lookup(String name) throws ClassNotFoundException{
        for(int i = 0; i < typeReplacements.length; i += 2){
            name = name.replace(typeReplacements[i], typeReplacements[i + 1]);
        }
        return Class.forName(typeMappings.get(name, name));
    }
}

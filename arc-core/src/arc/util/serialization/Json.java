package arc.util.serialization;

import arc.files.*;
import arc.struct.*;
import arc.struct.IntSet.*;
import arc.struct.ObjectMap.*;
import arc.struct.Queue;
import arc.struct.OrderedMap.*;
import arc.util.*;
import arc.util.io.*;
import arc.util.serialization.JsonValue.*;
import arc.util.serialization.JsonWriter.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Reads/writes Java objects to/from JSON, automatically.
 * TODO remove and replace with own implementation
 * @author Nathan Sweet
 */
@SuppressWarnings("unchecked")
public class Json{
    private static final boolean debug = false;
    private final ObjectMap<Class, OrderedMap<String, FieldMetadata>> typeToFields = new ObjectMap();
    private final ObjectMap<String, Class> tagToClass = new ObjectMap();
    private final ObjectMap<Class, String> classToTag = new ObjectMap();
    private final ObjectMap<Class, Serializer> classToSerializer = new ObjectMap();
    private final ObjectMap<Class, Object[]> classToDefaultValues = new ObjectMap();
    private final Object[] equals1 = {null}, equals2 = {null};
    private BaseJsonWriter writer;
    private String typeName = "class";
    private boolean usePrototypes = true;
    private OutputType outputType;
    private boolean quoteLongValues;
    private boolean ignoreUnknownFields = true;
    private boolean ignoreDeprecated;
    private boolean readDeprecated;
    private boolean enumNames = true;
    private Serializer defaultSerializer;

    public Json(){
        outputType = OutputType.minimal;
    }

    public Json(OutputType outputType){
        this.outputType = outputType;
    }

    public boolean getIgnoreUnknownFields(){
        return ignoreUnknownFields;
    }

    /**
     * When true, fields in the JSON that are not found on the class will not throw a {@link SerializationException}. Default is true.
     */
    public void setIgnoreUnknownFields(boolean ignoreUnknownFields){
        this.ignoreUnknownFields = ignoreUnknownFields;
    }

    /** When true, fields with the {@link Deprecated} annotation will not be serialized. */
    public void setIgnoreDeprecated(boolean ignoreDeprecated){
        this.ignoreDeprecated = ignoreDeprecated;
    }

    /**
     * When true, fields with the {@link Deprecated} annotation will be read (but not written) when
     * {@link #setIgnoreDeprecated(boolean)} is true.
     */
    public void setReadDeprecated(boolean readDeprecated){
        this.readDeprecated = readDeprecated;
    }

    /** @see JsonWriter#setOutputType(OutputType) */
    public void setOutputType(OutputType outputType){
        this.outputType = outputType;
    }

    /** @see JsonWriter#setQuoteLongValues(boolean) */
    public void setQuoteLongValues(boolean quoteLongValues){
        this.quoteLongValues = quoteLongValues;
    }

    /**
     * When true, {@link Enum#name()} is used to write enum values. When false, {@link Enum#toString()} is used which may not be
     * unique. Default is true.
     */
    public void setEnumNames(boolean enumNames){
        this.enumNames = enumNames;
    }

    /** Sets a tag to use instead of the fully qualifier class name. This can make the JSON easier to read. */
    public void addClassTag(String tag, Class type){
        tagToClass.put(tag, type);
        classToTag.put(type, tag);
    }

    /** Returns the class for the specified tag, or null. */
    public Class getClass(String tag){
        return tagToClass.get(tag);
    }

    /** Returns the tag for the specified class, or null. */
    public String getTag(Class type){
        return classToTag.get(type);
    }

    /**
     * Sets the name of the JSON field to store the Java class name or class tag when required to avoid ambiguity during
     * deserialization. Set to null to never output this information, but be warned that deserialization may fail. Default is
     * "class".
     */
    public void setTypeName(String typeName){
        this.typeName = typeName;
    }

    /**
     * Sets the serializer to use when the type being deserialized is not known (null).
     * @param defaultSerializer May be null.
     */
    public void setDefaultSerializer(Serializer defaultSerializer){
        this.defaultSerializer = defaultSerializer;
    }

    /**
     * Registers a serializer to use for the specified type instead of the default behavior of serializing all of an objects
     * fields.
     */
    public <T> void setSerializer(Class<T> type, Serializer<T> serializer){
        classToSerializer.put(type, serializer);
    }

    public <T> Serializer<T> getSerializer(Class<T> type){
        return classToSerializer.get(type);
    }

    /** When true, field values that are identical to a newly constructed instance are not written. Default is true. */
    public void setUsePrototypes(boolean usePrototypes){
        this.usePrototypes = usePrototypes;
    }

    /**
     * Sets the type of elements in a struct. When the element type is known, the class for each element in the struct
     * does not need to be written unless different from the element type.
     */
    public void setElementType(Class type, String fieldName, Class elementType){
        ObjectMap<String, FieldMetadata> fields = getFields(type);
        FieldMetadata metadata = fields.get(fieldName);
        if(metadata == null)
            throw new SerializationException("Field not found: " + fieldName + " (" + type.getName() + ")");
        metadata.elementType = elementType;
    }

    @SuppressWarnings("deprecation")
    public OrderedMap<String, FieldMetadata> getFields(Class type){
        OrderedMap<String, FieldMetadata> fields = typeToFields.get(type);
        if(fields != null) return fields;

        Seq<Class> classHierarchy = new Seq();
        Class nextClass = type;
        while(nextClass != Object.class){
            classHierarchy.add(nextClass);
            nextClass = nextClass.getSuperclass();
        }
        Seq<Field> allFields = new Seq<>();
        for(int i = classHierarchy.size - 1; i >= 0; i--)
            allFields.addAll(classHierarchy.get(i).getDeclaredFields());

        OrderedMap<String, FieldMetadata> nameToField = new OrderedMap(allFields.size);

        for(Field field: allFields){
            if(Modifier.isTransient(field.getModifiers())) continue;
            if(Modifier.isStatic(field.getModifiers())) continue;
            if(field.isSynthetic() || type.isEnum() || Reflect.isWrapper(type)) continue;

            //this is deprecated, but I know what I'm doing
            if(!field.isAccessible()){
                try{
                    field.setAccessible(true);
                }catch(Exception ex){
                    continue;
                }
            }

            if(ignoreDeprecated && !readDeprecated && field.isAnnotationPresent(Deprecated.class)) continue;
            FieldMetadata data = new FieldMetadata(field);

            nameToField.put(field.getName(), data);
        }
        typeToFields.put(type, nameToField);
        return nameToField;
    }


    public String toJson(Object object){
        return toJson(object, object == null ? null : object.getClass(), (Class)null);
    }

    public String toJson(Object object, Class knownType){
        return toJson(object, knownType, (Class)null);
    }

    public void toUBJson(Object object, Class knownType, OutputStream stream){
        this.writer = new UBJsonWriter(stream);
        toJson(object, knownType, (Class)null);
    }

    /**
     * @param knownType May be null if the type is unknown.
     * @param elementType May be null if the type is unknown.
     */
    public String toJson(Object object, Class knownType, Class elementType){
        StringWriter buffer = new StringWriter();
        toJson(object, knownType, elementType, buffer);
        return buffer.toString();
    }

    public void toJson(Object object, Fi file){
        toJson(object, object == null ? null : object.getClass(), null, file);
    }

    /** @param knownType May be null if the type is unknown. */
    public void toJson(Object object, Class knownType, Fi file){
        toJson(object, knownType, null, file);
    }

    /**
     * @param knownType May be null if the type is unknown.
     * @param elementType May be null if the type is unknown.
     */
    public void toJson(Object object, Class knownType, Class elementType, Fi file){
        Writer writer = null;
        try{
            writer = file.writer(false, "UTF-8");
            toJson(object, knownType, elementType, writer);
        }catch(Exception ex){
            throw new SerializationException("Error writing file: " + file, ex);
        }finally{
            Streams.close(writer);
        }
    }

    public void toJson(Object object, Writer writer){
        toJson(object, object == null ? null : object.getClass(), null, writer);
    }

    /** @param knownType May be null if the type is unknown. */
    public void toJson(Object object, Class knownType, Writer writer){
        toJson(object, knownType, null, writer);
    }

    /**
     * @param knownType May be null if the type is unknown.
     * @param elementType May be null if the type is unknown.
     */
    public void toJson(Object object, Class knownType, Class elementType, Writer writer){
        setWriter(new JsonWriter(writer));
        try{
            writeValue(object, knownType, elementType);
        }finally{
            Streams.close(this.writer);
            this.writer = null;
        }
    }

    public BaseJsonWriter getWriter(){
        return writer;
    }

    /** Sets the writer where JSON output will be written. This is only necessary when not using the toJson methods. */
    public void setWriter(BaseJsonWriter writer){
        //if(!(writer instanceof BaseJsonWriter)) writer = new JsonWriter(writer);
        this.writer = writer;
        this.writer.setOutputType(outputType);
        this.writer.setQuoteLongValues(quoteLongValues);
    }

    /** Writes all fields of the specified object to the current JSON object. */
    public void writeFields(Object object){
        Class type = object.getClass();

        Object[] defaultValues = getDefaultValues(type);

        OrderedMap<String, FieldMetadata> fields = getFields(type);
        int i = 0;
        for(FieldMetadata metadata : new OrderedMapValues<>(fields)){
            Field field = metadata.field;
            if(readDeprecated && ignoreDeprecated && field.isAnnotationPresent(Deprecated.class)) continue;
            try{
                Object value = field.get(object);
                if(defaultValues != null){
                    Object defaultValue = defaultValues[i++];
                    if(value == null && defaultValue == null) continue;
                    if(value != null && defaultValue != null){
                        if(value.equals(defaultValue)) continue;
                        if(value.getClass().isArray() && defaultValue.getClass().isArray()){
                            equals1[0] = value;
                            equals2[0] = defaultValue;
                            if(Arrays.deepEquals(equals1, equals2)) continue;
                        }
                    }
                }

                if(debug) System.out.println("Writing field: " + field.getName() + " (" + type.getName() + ")");
                writer.name(field.getName());
                writeValue(value, field.getType(), metadata.elementType);
            }catch(IllegalAccessException ex){
                throw new SerializationException("Error accessing field: " + field.getName() + " (" + type.getName() + ")", ex);
            }catch(SerializationException ex){
                ex.addTrace(field + " (" + type.getName() + ")");
                throw ex;
            }catch(Exception runtimeEx){
                SerializationException ex = new SerializationException(runtimeEx);
                ex.addTrace(field + " (" + type.getName() + ")");
                throw ex;
            }
        }
    }

    private Object[] getDefaultValues(Class type){
        if(!usePrototypes) return null;
        if(classToDefaultValues.containsKey(type)) return classToDefaultValues.get(type);
        Object object;
        try{
            object = newInstance(type);
        }catch(Exception ex){
            classToDefaultValues.put(type, null);
            return null;
        }

        ObjectMap<String, FieldMetadata> fields = getFields(type);
        Object[] values = new Object[fields.size];
        classToDefaultValues.put(type, values);

        int i = 0;
        for(FieldMetadata metadata : fields.values()){
            Field field = metadata.field;
            if(readDeprecated && ignoreDeprecated && field.isAnnotationPresent(Deprecated.class)) continue;
            try{
                values[i++] = field.get(object);
            }catch(IllegalAccessException ex){
                throw new SerializationException("Error accessing field: " + field.getName() + " (" + type.getName() + ")", ex);
            }catch(SerializationException ex){
                ex.addTrace(field + " (" + type.getName() + ")");
                throw ex;
            }catch(RuntimeException runtimeEx){
                SerializationException ex = new SerializationException(runtimeEx);
                ex.addTrace(field + " (" + type.getName() + ")");
                throw ex;
            }
        }
        return values;
    }

    /** @see #writeField(Object, String, String, Class) */
    public void writeField(Object object, String name){
        writeField(object, name, name, null);
    }

    /**
     * @param elementType May be null if the type is unknown.
     * @see #writeField(Object, String, String, Class)
     */
    public void writeField(Object object, String name, Class elementType){
        writeField(object, name, name, elementType);
    }

    /** @see #writeField(Object, String, String, Class) */
    public void writeField(Object object, String fieldName, String jsonName){
        writeField(object, fieldName, jsonName, null);
    }

    /**
     * Writes the specified field to the current JSON object.
     * @param elementType May be null if the type is unknown.
     */
    public void writeField(Object object, String fieldName, String jsonName, Class elementType){
        Class type = object.getClass();
        ObjectMap<String, FieldMetadata> fields = getFields(type);
        FieldMetadata metadata = fields.get(fieldName);
        if(metadata == null)
            throw new SerializationException("Field not found: " + fieldName + " (" + type.getName() + ")");
        Field field = metadata.field;
        if(elementType == null) elementType = metadata.elementType;
        try{
            if(debug) System.out.println("Writing field: " + field.getName() + " (" + type.getName() + ")");
            writer.name(jsonName);
            writeValue(field.get(object), field.getType(), elementType);
        }catch(IllegalAccessException ex){
            throw new SerializationException("Error accessing field: " + field.getName() + " (" + type.getName() + ")", ex);
        }catch(SerializationException ex){
            ex.addTrace(field + " (" + type.getName() + ")");
            throw ex;
        }catch(Exception runtimeEx){
            SerializationException ex = new SerializationException(runtimeEx);
            ex.addTrace(field + " (" + type.getName() + ")");
            throw ex;
        }
    }

    /**
     * Writes the value as a field on the current JSON object, without writing the actual class.
     * @param value May be null.
     * @see #writeValue(String, Object, Class, Class)
     */
    public void writeValue(String name, Object value){
        try{
            writer.name(name);
        }catch(IOException ex){
            throw new SerializationException(ex);
        }
        if(value == null)
            writeValue(value, null, null);
        else
            writeValue(value, value.getClass(), null);
    }

    /**
     * Writes the value as a field on the current JSON object, writing the class of the object if it differs from the specified
     * known type.
     * @param value May be null.
     * @param knownType May be null if the type is unknown.
     * @see #writeValue(String, Object, Class, Class)
     */
    public void writeValue(String name, Object value, Class knownType){
        try{
            writer.name(name);
        }catch(IOException ex){
            throw new SerializationException(ex);
        }
        writeValue(value, knownType, null);
    }

    /**
     * Writes the value as a field on the current JSON object, writing the class of the object if it differs from the specified
     * known type. The specified element type is used as the default type for collections.
     * @param value May be null.
     * @param knownType May be null if the type is unknown.
     * @param elementType May be null if the type is unknown.
     */
    public void writeValue(String name, Object value, Class knownType, Class elementType){
        try{
            writer.name(name);
        }catch(IOException ex){
            throw new SerializationException(ex);
        }
        writeValue(value, knownType, elementType);
    }

    /**
     * Writes the value, without writing the class of the object.
     * @param value May be null.
     */
    public void writeValue(Object value){
        if(value == null)
            writeValue(value, null, null);
        else
            writeValue(value, value.getClass(), null);
    }

    /**
     * Writes the value, writing the class of the object if it differs from the specified known type.
     * @param value May be null.
     * @param knownType May be null if the type is unknown.
     */
    public void writeValue(Object value, Class knownType){
        writeValue(value, knownType, null);
    }

    /**
     * Writes the value, writing the class of the object if it differs from the specified known type. The specified element type
     * is used as the default type for collections.
     * @param value May be null.
     * @param knownType May be null if the type is unknown.
     * @param elementType May be null if the type is unknown.
     */
    public void writeValue(Object value, Class knownType, Class elementType){
        if(knownType != null && knownType.isAnonymousClass()){
            knownType = knownType.getSuperclass();
        }

        try{
            if(value == null){
                writer.value(null);
                return;
            }

            if((knownType != null && knownType.isPrimitive()) || knownType == String.class || Reflect.isWrapper(knownType)){
                writer.value(value);
                return;
            }

            Class actualType = value.getClass().isAnonymousClass() ? value.getClass().getSuperclass() : value.getClass();

            if(actualType.isPrimitive() || actualType == String.class || Reflect.isWrapper(actualType)){
                writeObjectStart(actualType, null);
                writeValue("value", value);
                writeObjectEnd();
                return;
            }

            if(value instanceof JsonSerializable){
                writeObjectStart(actualType, knownType);
                ((JsonSerializable)value).write(this);
                writeObjectEnd();
                return;
            }

            Serializer serializer = classToSerializer.get(actualType);
            if(serializer != null){
                serializer.write(this, value, knownType);
                return;
            }

            // JSON array special cases.
            if(value instanceof Seq){
                if(knownType != null && actualType != knownType && actualType != Seq.class)
                    throw new SerializationException("Serialization of an Array other than the known type is not supported.\n"
                    + "Known type: " + knownType + "\nActual type: " + actualType);
                writeArrayStart();
                Seq array = (Seq)value;
                for(int i = 0, n = array.size; i < n; i++)
                    writeValue(array.get(i), elementType, null);
                writeArrayEnd();
                return;
            }
            if(value instanceof ObjectSet){
                if(knownType == null) knownType = ObjectSet.class;
                writeObjectStart(actualType, knownType);
                writer.name("values");
                writeArrayStart();
                for(Object entry : (ObjectSet)value)
                    writeValue(entry, elementType, null);
                writeArrayEnd();
                writeObjectEnd();
                return;
            }
            if(value instanceof IntSet){
                if(knownType == null) knownType = IntSet.class;
                writeObjectStart(actualType, knownType);
                writer.name("values");
                writeArrayStart();
                for(IntSetIterator iter = ((IntSet)value).iterator(); iter.hasNext; )
                    writeValue(iter.next(), Integer.class, null);
                writeArrayEnd();
                writeObjectEnd();
                return;
            }
            if(value instanceof arc.struct.Queue){
                if(knownType != null && actualType != knownType && actualType != arc.struct.Queue.class)
                    throw new SerializationException("Serialization of a Queue other than the known type is not supported.\n"
                    + "Known type: " + knownType + "\nActual type: " + actualType);
                writeArrayStart();
                arc.struct.Queue queue = (arc.struct.Queue)value;
                for(int i = 0, n = queue.size; i < n; i++)
                    writeValue(queue.get(i), elementType, null);
                writeArrayEnd();
                return;
            }
            if(value instanceof Collection){
                if(typeName != null && actualType != ArrayList.class && (knownType == null || knownType != actualType)){
                    writeObjectStart(actualType, knownType);
                    writeArrayStart("items");
                    for(Object item : (Collection)value)
                        writeValue(item, elementType, null);
                    writeArrayEnd();
                    writeObjectEnd();
                }else{
                    writeArrayStart();
                    for(Object item : (Collection)value)
                        writeValue(item, elementType, null);
                    writeArrayEnd();
                }
                return;
            }
            if(actualType.isArray()){
                if(elementType == null) elementType = actualType.getComponentType();
                int length = java.lang.reflect.Array.getLength(value);
                writeArrayStart();
                for(int i = 0; i < length; i++)
                    writeValue(java.lang.reflect.Array.get(value, i), elementType, null);
                writeArrayEnd();
                return;
            }

            // JSON object special cases.
            if(value instanceof ObjectMap){
                if(knownType == null) knownType = ObjectMap.class;
                writeObjectStart(actualType, knownType);
                for(Entry entry : ((ObjectMap<?, ?>)value).entries()){
                    writer.name(convertToString(entry.key));
                    writeValue(entry.value, elementType, null);
                }
                writeObjectEnd();
                return;
            }

            if(value instanceof ObjectIntMap){
                if(knownType == null) knownType = ObjectIntMap.class;
                writeObjectStart(actualType, knownType);
                for(ObjectIntMap.Entry entry : ((ObjectIntMap<?>)value).entries()){
                    writer.name(convertToString(entry.key));
                    writer.value(entry.value);
                }
                writeObjectEnd();
                return;
            }
            if(value instanceof ArrayMap){
                if(knownType == null) knownType = ArrayMap.class;
                writeObjectStart(actualType, knownType);
                ArrayMap map = (ArrayMap)value;
                for(int i = 0, n = map.size; i < n; i++){
                    writer.name(convertToString(map.keys[i]));
                    writeValue(map.values[i], elementType, null);
                }
                writeObjectEnd();
                return;
            }
            if(value instanceof Map){
                if(knownType == null) knownType = HashMap.class;
                writeObjectStart(actualType, knownType);
                for(Map.Entry entry : ((Map<?, ?>)value).entrySet()){
                    writer.name(convertToString(entry.getKey()));
                    writeValue(entry.getValue(), elementType, null);
                }
                writeObjectEnd();
                return;
            }

            // Enum special case.
            if(Enum.class.isAssignableFrom(actualType)){
                if(typeName != null && (knownType == null || knownType != actualType)){
                    // Ensures that enums with specific implementations (abstract logic) serialize correctly.
                    if(actualType.getEnumConstants() == null) actualType = actualType.getSuperclass();

                    writeObjectStart(actualType, null);
                    writer.name("value");
                    writer.value(convertToString((Enum)value));
                    writeObjectEnd();
                }else{
                    writer.value(convertToString((Enum)value));
                }
                return;
            }

            writeObjectStart(actualType, knownType);
            writeFields(value);
            writeObjectEnd();
        }catch(IOException ex){
            throw new SerializationException(ex);
        }
    }

    public void writeObjectStart(String name){
        try{
            writer.name(name);
        }catch(IOException ex){
            throw new SerializationException(ex);
        }
        writeObjectStart();
    }

    /** @param knownType May be null if the type is unknown. */
    public void writeObjectStart(String name, Class actualType, Class knownType){
        try{
            writer.name(name);
        }catch(IOException ex){
            throw new SerializationException(ex);
        }
        writeObjectStart(actualType, knownType);
    }

    public void writeObjectStart(){
        try{
            writer.object();
        }catch(IOException ex){
            throw new SerializationException(ex);
        }
    }

    /**
     * Starts writing an object, writing the actualType to a field if needed.
     * @param knownType May be null if the type is unknown.
     */
    public void writeObjectStart(Class actualType, Class knownType){
        try{
            writer.object();
        }catch(IOException ex){
            throw new SerializationException(ex);
        }
        if(knownType == null || knownType != actualType) writeType(actualType);
    }

    public void writeObjectEnd(){
        try{
            writer.pop();
        }catch(IOException ex){
            throw new SerializationException(ex);
        }
    }

    public void writeArrayStart(String name){
        try{
            writer.name(name);
            writer.array();
        }catch(IOException ex){
            throw new SerializationException(ex);
        }
    }

    public void writeArrayStart(){
        try{
            writer.array();
        }catch(IOException ex){
            throw new SerializationException(ex);
        }
    }

    public void writeArrayEnd(){
        try{
            writer.pop();
        }catch(IOException ex){
            throw new SerializationException(ex);
        }
    }

    public void writeType(Class type){
        if(typeName == null) return;
        String className = getTag(type);
        if(className == null) className = type.getName();
        try{
            writer.set(typeName, className);
        }catch(IOException ex){
            throw new SerializationException(ex);
        }
        if(debug) System.out.println("Writing type: " + type.getName());
    }

    /**
     * @param type May be null if the type is unknown.
     * @return May be null.
     */
    public <T> T fromJson(Class<T> type, Reader reader){
        return readValue(type, null, new JsonReader().parse(reader));
    }

    /**
     * @param type May be null if the type is unknown.
     * @param elementType May be null if the type is unknown.
     * @return May be null.
     */
    public <T> T fromJson(Class<T> type, Class elementType, Reader reader){
        return readValue(type, elementType, new JsonReader().parse(reader));
    }

    /**
     * @param type May be null if the type is unknown.
     * @return May be null.
     */
    public <T> T fromJson(Class<T> type, InputStream input){
        return readValue(type, null, new JsonReader().parse(input));
    }

    /**
     * @param type May be null if the type is unknown.
     * @param elementType May be null if the type is unknown.
     * @return May be null.
     */
    public <T> T fromJson(Class<T> type, Class elementType, InputStream input){
        return readValue(type, elementType, new JsonReader().parse(input));
    }

    /**
     * @param type May be null if the type is unknown.
     * @return May be null.
     */
    public <T> T fromJson(Class<T> type, Fi file){
        try{
            return readValue(type, null, new JsonReader().parse(file));
        }catch(Exception ex){
            throw new SerializationException("Error reading file: " + file, ex);
        }
    }

    /**
     * @param type May be null if the type is unknown.
     * @param elementType May be null if the type is unknown.
     * @return May be null.
     */
    public <T> T fromJson(Class<T> type, Class elementType, Fi file){
        try{
            return readValue(type, elementType, new JsonReader().parse(file));
        }catch(Exception ex){
            throw new SerializationException("Error reading file: " + file, ex);
        }
    }

    /**
     * @param type May be null if the type is unknown.
     * @return May be null.
     */
    public <T> T fromJson(Class<T> type, char[] data, int offset, int length){
        return readValue(type, null, new JsonReader().parse(data, offset, length));
    }

    /**
     * @param type May be null if the type is unknown.
     * @param elementType May be null if the type is unknown.
     * @return May be null.
     */
    public <T> T fromJson(Class<T> type, Class elementType, char[] data, int offset, int length){
        return readValue(type, elementType, new JsonReader().parse(data, offset, length));
    }

    /**
     * @param type May be null if the type is unknown.
     * @return May be null.
     */
    public <T> T fromJson(Class<T> type, String json){
        return readValue(type, null, new JsonReader().parse(json));
    }

    /**
     * @param type May be null if the type is unknown.
     * @return May be null.
     */
    public <T> T fromJson(Class<T> type, Class elementType, String json){
        return readValue(type, elementType, new JsonReader().parse(json));
    }

    public void readField(Object object, String name, JsonValue jsonData){
        readField(object, name, name, null, jsonData);
    }

    public void readField(Object object, String name, Class elementType, JsonValue jsonData){
        readField(object, name, name, elementType, jsonData);
    }

    public void readField(Object object, String fieldName, String jsonName, JsonValue jsonData){
        readField(object, fieldName, jsonName, null, jsonData);
    }

    /** @param elementType May be null if the type is unknown. */
    public void readField(Object object, String fieldName, String jsonName, Class elementType, JsonValue jsonMap){
        Class type = object.getClass();
        ObjectMap<String, FieldMetadata> fields = getFields(type);
        FieldMetadata metadata = fields.get(fieldName);
        if(metadata == null)
            throw new SerializationException("Field not found: " + fieldName + " (" + type.getName() + ")");
        Field field = metadata.field;
        if(elementType == null) elementType = metadata.elementType;
        readField(object, field, jsonName, elementType, jsonMap);
    }

    /**
     * @param object May be null if the field is static.
     * @param elementType May be null if the type is unknown.
     */
    public void readField(Object object, Field field, String jsonName, Class elementType, JsonValue jsonMap){
        JsonValue jsonValue = jsonMap.get(jsonName);
        if(jsonValue == null) return;
        try{
            field.set(object, readValue(field.getType(), elementType, jsonValue));
        }catch(IllegalAccessException ex){
            throw new SerializationException(
            "Error accessing field: " + field.getName() + " (" + field.getDeclaringClass().getName() + ")", ex);
        }catch(SerializationException ex){
            ex.addTrace(field.getName() + " (" + field.getDeclaringClass().getName() + ")");
            throw ex;
        }catch(RuntimeException runtimeEx){
            SerializationException ex = new SerializationException(runtimeEx);
            ex.addTrace(jsonValue.trace());
            ex.addTrace(field.getName() + " (" + field.getDeclaringClass().getName() + ")");
            throw ex;
        }
    }

    public void readFields(Object object, JsonValue jsonMap){
        Class type = object.getClass();
        ObjectMap<String, FieldMetadata> fields = getFields(type);
        for(JsonValue child = jsonMap.child; child != null; child = child.next){
            FieldMetadata metadata = fields.get(child.name().replace(" ", "_"));
            if(metadata == null){
                if(child.name.equals(typeName)) continue;
                if(ignoreUnknownFields || ignoreUnknownField(type, child.name)){
                    if(debug) Log.warn("Ignoring unknown field: " + child.name + " (" + type.getName() + ")");
                    continue;
                }else{
                    SerializationException ex = new SerializationException("Field not found: " + child.name + " (" + type.getName() + ")");
                    ex.addTrace(child.trace());
                    throw ex;
                }
            }
            Field field = metadata.field;
            try{
                field.set(object, readValue(field.getType(), metadata.elementType, child, metadata.keyType));
            }catch(IllegalAccessException ex){
                throw new SerializationException("Error accessing field: " + field.getName() + " (" + type.getName() + ")", ex);
            }catch(SerializationException ex){
                ex.addTrace(field.getName() + " (" + type.getName() + ")");
                throw ex;
            }catch(RuntimeException runtimeEx){
                SerializationException ex = new SerializationException(runtimeEx);
                ex.addTrace(child.trace());
                ex.addTrace(field.getName() + " (" + type.getName() + ")");
                throw ex;
            }
        }
    }

    /**
     * Called for each unknown field name encountered by {@link #readFields(Object, JsonValue)} when {@link #ignoreUnknownFields}
     * is false to determine whether the unknown field name should be ignored.
     * @param type The object type being read.
     * @param fieldName A field name encountered in the JSON for which there is no matching class field.
     * @return true if the field name should be ignored and an exception won't be thrown by
     * {@link #readFields(Object, JsonValue)}.
     */
    protected boolean ignoreUnknownField(Class type, String fieldName){
        return false;
    }

    /**
     * @param type May be null if the type is unknown.
     * @return May be null.
     */
    public <T> T readValue(String name, Class<T> type, JsonValue jsonMap){
        return readValue(type, null, jsonMap.get(name));
    }

    /**
     * @param type May be null if the type is unknown.
     * @return May be null.
     */
    public <T> T readValue(String name, Class<T> type, T defaultValue, JsonValue jsonMap){
        JsonValue jsonValue = jsonMap.get(name);
        if(jsonValue == null) return defaultValue;
        return readValue(type, null, jsonValue);
    }

    /**
     * @param type May be null if the type is unknown.
     * @param elementType May be null if the type is unknown.
     * @return May be null.
     */
    public <T> T readValue(String name, Class<T> type, Class elementType, JsonValue jsonMap){
        return readValue(type, elementType, jsonMap.get(name));
    }

    /**
     * @param type May be null if the type is unknown.
     * @param elementType May be null if the type is unknown.
     * @return May be null.
     */
    public <T> T readValue(String name, Class<T> type, Class elementType, T defaultValue, JsonValue jsonMap){
        JsonValue jsonValue = jsonMap.get(name);
        return readValue(type, elementType, defaultValue, jsonValue);
    }

    /**
     * @param type May be null if the type is unknown.
     * @param elementType May be null if the type is unknown.
     * @return May be null.
     */
    public <T> T readValue(Class<T> type, Class elementType, T defaultValue, JsonValue jsonData){
        if(jsonData == null) return defaultValue;
        return readValue(type, elementType, jsonData);
    }

    /**
     * @param type May be null if the type is unknown.
     * @return May be null.
     */
    public <T> T readValue(Class<T> type, JsonValue jsonData){
        return readValue(type, null, jsonData);
    }

    public <T> T readValue(Class<T> type, Class elementType, JsonValue jsonData){
        return readValue(type, elementType, jsonData, null);
    }

    /**
     * @param type May be null if the type is unknown.
     * @param elementType May be null if the type is unknown.
     * @return May be null.
     */
    public <T> T readValue(Class<T> type, Class elementType, JsonValue jsonData, Class keytype){
        if(jsonData == null) return null;

        if(jsonData.isObject()){
            String className = typeName == null ? null : jsonData.getString(typeName, null);
            if(className != null){
                type = getClass(className);
                if(type == null){
                    try{
                        type = (Class<T>)Class.forName(className);
                    }catch(Throwable ex){
                        throw new SerializationException(ex);
                    }
                }
            }

            if(type == null){
                if(defaultSerializer != null) return (T)defaultSerializer.read(this, jsonData, type);
                return (T)jsonData;
            }

            if(typeName != null && Collection.class.isAssignableFrom(type)){
                // JSON object wrapper to specify type.
                jsonData = jsonData.get("items");
                if(jsonData == null) throw new SerializationException(
                "Unable to convert object to struct: " + jsonData + " (" + type.getName() + ")");
            }else{
                Serializer serializer = classToSerializer.get(type);
                if(serializer != null) return (T)serializer.read(this, jsonData, type);

                if(type == String.class || Reflect.isWrapper(type) || Enum.class.isAssignableFrom(type)){
                    return readValue("value", type, jsonData);
                }

                Object object = newInstance(type);

                if(object instanceof JsonSerializable){
                    ((JsonSerializable)object).read(this, jsonData);
                    return (T)object;
                }

                // JSON object special cases.
                if(object instanceof ObjectMap){
                    ObjectMap result = (ObjectMap)object;
                    for(JsonValue child = jsonData.child; child != null; child = child.next){

                        result.put(keytype != null ? readValue(keytype, null, new JsonValue(child.name)) : child.name, readValue(elementType, null, child));
                    }

                    return (T)result;
                }
                if(object instanceof ObjectIntMap){
                    ObjectIntMap result = (ObjectIntMap)object;
                    for(JsonValue child = jsonData.child; child != null; child = child.next){
                        result.put(elementType != null ? readValue(elementType, null, new JsonValue(child.name)) : child.name, child.asInt());
                    }

                    return (T)result;
                }
                if(object instanceof ObjectSet){
                    ObjectSet result = (ObjectSet)object;
                    for(JsonValue child = jsonData.getChild("values"); child != null; child = child.next)
                        result.add(readValue(elementType, null, child));
                    return (T)result;
                }
                if(object instanceof IntSet){
                    IntSet result = (IntSet)object;
                    for(JsonValue child = jsonData.getChild("values"); child != null; child = child.next)
                        result.add(child.asInt());
                    return (T)result;
                }
                if(object instanceof ArrayMap){
                    ArrayMap result = (ArrayMap)object;
                    for(JsonValue child = jsonData.child; child != null; child = child.next)
                        result.put(child.name, readValue(elementType, null, child));

                    return (T)result;
                }
                if(object instanceof Map){
                    Map result = (Map)object;
                    for(JsonValue child = jsonData.child; child != null; child = child.next){
                        if(child.name.equals(typeName)){
                            continue;
                        }
                        result.put(child.name, readValue(elementType, null, child));
                    }
                    return (T)result;
                }

                readFields(object, jsonData);
                return (T)object;
            }
        }

        if(type != null){
            Serializer serializer = classToSerializer.get(type);
            if(serializer != null) return (T)serializer.read(this, jsonData, type);

            if(JsonSerializable.class.isAssignableFrom(type)){
                // A Serializable may be read as an array, string, etc, even though it will be written as an object.
                Object object = newInstance(type);
                ((JsonSerializable)object).read(this, jsonData);
                return (T)object;
            }
        }

        if(jsonData.isArray()){
            // JSON array special cases.
            if(type == null || type == Object.class) type = (Class<T>)Seq.class;
            if(Seq.class.isAssignableFrom(type)){
                Seq result = type == Seq.class ? new Seq() : (Seq)newInstance(type);
                for(JsonValue child = jsonData.child; child != null; child = child.next)
                    result.add(readValue(elementType, null, child));
                return (T)result;
            }
            if(ObjectSet.class.isAssignableFrom(type)){
                ObjectSet result = type == ObjectSet.class ? new ObjectSet() : (ObjectSet)newInstance(type);
                for(JsonValue child = jsonData.child; child != null; child = child.next)
                    result.add(readValue(elementType, null, child));
                return (T)result;
            }
            if(arc.struct.Queue.class.isAssignableFrom(type)){
                arc.struct.Queue result = type == arc.struct.Queue.class ? new arc.struct.Queue() : (Queue)newInstance(type);
                for(JsonValue child = jsonData.child; child != null; child = child.next)
                    result.addLast(readValue(elementType, null, child));
                return (T)result;
            }
            if(Collection.class.isAssignableFrom(type)){
                Collection result = type.isInterface() ? new ArrayList() : (Collection)newInstance(type);
                for(JsonValue child = jsonData.child; child != null; child = child.next)
                    result.add(readValue(elementType, null, child));
                return (T)result;
            }
            if(type.isArray()){
                Class componentType = type.getComponentType();
                if(elementType == null) elementType = componentType;
                Object result = java.lang.reflect.Array.newInstance(componentType, jsonData.size);
                int i = 0;
                for(JsonValue child = jsonData.child; child != null; child = child.next)
                    java.lang.reflect.Array.set(result, i++, readValue(elementType, null, child));
                return (T)result;
            }
            throw new SerializationException("Unable to convert value to required type: " + jsonData + " (" + type.getName() + ")");
        }

        if(jsonData.isNumber()){
            try{
                if(type == null || type == float.class || type == Float.class) return (T)(Float)jsonData.asFloat();
                if(type == int.class || type == Integer.class) return (T)(Integer)jsonData.asInt();
                if(type == long.class || type == Long.class) return (T)(Long)jsonData.asLong();
                if(type == double.class || type == Double.class) return (T)(Double)jsonData.asDouble();
                if(type == String.class) return (T)jsonData.asString();
                if(type == short.class || type == Short.class) return (T)(Short)jsonData.asShort();
                if(type == byte.class || type == Byte.class) return (T)(Byte)jsonData.asByte();
            }catch(NumberFormatException ignored){
            }
            jsonData = new JsonValue(jsonData.asString());
        }

        if(jsonData.isBoolean()){
            try{
                if(type == null || type == boolean.class || type == Boolean.class)
                    return (T)(Boolean)jsonData.asBoolean();
            }catch(NumberFormatException ignored){
            }
            jsonData = new JsonValue(jsonData.asString());
        }

        if(jsonData.isString()){
            String string = jsonData.asString();
            if(type == null || type == String.class) return (T)string;
            try{
                if(type == int.class || type == Integer.class) return (T)Integer.valueOf(string);
                if(type == float.class || type == Float.class) return string.endsWith("f") ? (T)Float.valueOf(string.substring(0, string.length() - 1)) : string.endsWith("f,") ? (T)Float.valueOf(string.substring(0, string.length() - 2)) : (T)Float.valueOf(string);
                if(type == long.class || type == Long.class) return (T)Long.valueOf(string);
                if(type == double.class || type == Double.class) return (T)Double.valueOf(string);
                if(type == short.class || type == Short.class) return (T)Short.valueOf(string);
                if(type == byte.class || type == Byte.class) return (T)Byte.valueOf(string);
            }catch(NumberFormatException ignored){
            }
            if(type == boolean.class || type == Boolean.class) return (T)Boolean.valueOf(string);
            if(type == char.class || type == Character.class) return (T)(Character)string.charAt(0);
            if(Enum.class.isAssignableFrom(type)){
                Enum[] constants = (Enum[])type.getEnumConstants();
                for(int i = 0, n = constants.length; i < n; i++){
                    Enum e = constants[i];
                    if(string.equals(convertToString(e))) return (T)e;
                }
            }
            if(type == CharSequence.class) return (T)string;
            throw new SerializationException("Unable to convert value to required type: " + jsonData + " (" + type.getName() + ")");
        }

        return null;
    }

    /**
     * Each field on the <code>to</code> object is set to the value for the field with the same name on the <code>from</code>
     * object. The <code>to</code> object must have at least all the fields of the <code>from</code> object with the same name and
     * type.
     */
    public void copyFields(Object from, Object to){
        copyFields(from, to, false);
    }

    public void copyFields(Object from, Object to, boolean setFinals){
        ObjectMap<String, FieldMetadata> toFields = getFields(from.getClass());
        for(ObjectMap.Entry<String, FieldMetadata> entry : getFields(from.getClass())){
            FieldMetadata toField = toFields.get(entry.key);
            Field fromField = entry.value.field;
            if(Modifier.isFinal(fromField.getModifiers()) && !setFinals) continue;

            if(toField == null) throw new SerializationException("To object is missing field" + entry.key);
            try{
                toField.field.set(to, fromField.get(from));
            }catch(IllegalAccessException ex){
                throw new SerializationException("Error copying field: " + fromField.getName(), ex);
            }
        }
    }

    private String convertToString(Enum e){
        return enumNames ? e.name() : e.toString();
    }

    protected String convertToString(Object object){
        if(object instanceof Enum) return convertToString((Enum)object);
        if(object instanceof Class) return ((Class)object).getName();
        return String.valueOf(object);
    }

    protected Object newInstance(Class type){
        try{
            return type.getDeclaredConstructor().newInstance();
        }catch(Exception ex){
            try{
                // Try a private constructor.
                Constructor constructor = type.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            }catch(SecurityException ignored){
            }catch(IllegalAccessException ignored){
                if(Enum.class.isAssignableFrom(type)){
                    if(type.getEnumConstants() == null) type = type.getSuperclass();
                    return type.getEnumConstants()[0];
                }
                if(type.isArray())
                    throw new SerializationException("Encountered JSON object when expected array of type: " + type.getName(), ex);
                else if(type.isMemberClass() && !Modifier.isStatic(type.getModifiers()))
                    throw new SerializationException("Class cannot be created (non-static member class): " + type.getName(), ex);
                else
                    throw new SerializationException("Class cannot be created (missing no-arg constructor): " + type.getName(), ex);
            }catch(Exception privateConstructorException){
                ex = privateConstructorException;
            }
            throw new SerializationException("Error constructing instance of class: " + type.getName(), ex);
        }
    }

    public String prettyPrint(Object object){
        return prettyPrint(object, 0);
    }

    public String prettyPrint(String json){
        return prettyPrint(json, 0);
    }

    public String prettyPrint(Object object, int singleLineColumns){
        return prettyPrint(toJson(object), singleLineColumns);
    }

    public String prettyPrint(String json, int singleLineColumns){
        return new JsonReader().parse(json).prettyPrint(outputType, singleLineColumns);
    }

    public String prettyPrint(Object object, PrettyPrintSettings settings){
        return prettyPrint(toJson(object), settings);
    }

    public String prettyPrint(String json, PrettyPrintSettings settings){
        return new JsonReader().parse(json).prettyPrint(settings);
    }

    public interface Serializer<T>{
        void write(Json json, T object, Class knownType);
        T read(Json json, JsonValue jsonData, Class type);
    }

    public interface JsonSerializable{
        void write(Json json);
        void read(Json json, JsonValue jsonData);
    }

    public static class FieldMetadata{
        public final Field field;
        public @Nullable Class elementType;
        public @Nullable Class keyType;

        public FieldMetadata(Field field){
            boolean isMap = ObjectMap.class.isAssignableFrom(field.getType())
            || Map.class.isAssignableFrom(field.getType());

            this.field = field;
            this.elementType = getElementType(field, isMap ? 1 : 0);
            keyType = isMap ? getElementType(field, 0) : null;
        }
    }

    static Class getElementType(Field field, int index){
        Type genericType = field.getGenericType();
        if(genericType instanceof ParameterizedType){
            Type[] actualTypes = ((ParameterizedType)genericType).getActualTypeArguments();
            if(actualTypes.length - 1 >= index){
                Type actualType = actualTypes[index];
                if(actualType instanceof Class)
                    return (Class)actualType;
                else if(actualType instanceof ParameterizedType)
                    return (Class)((ParameterizedType)actualType).getRawType();
                else if(actualType instanceof GenericArrayType){
                    Type componentType = ((GenericArrayType)actualType).getGenericComponentType();
                    if(componentType instanceof Class)
                        return java.lang.reflect.Array.newInstance((Class)componentType, 0).getClass();
                }
            }
        }
        return null;
    }
}

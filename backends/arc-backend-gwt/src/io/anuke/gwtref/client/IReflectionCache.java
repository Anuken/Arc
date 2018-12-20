package io.anuke.gwtref.client;

public interface IReflectionCache{
    // Class level methods
    Type forName(String name);

    Object newArray(Type componentType, int size);

    int getArrayLength(Type type, Object obj);

    Object getArrayElement(Type type, Object obj, int i);

    void setArrayElement(Type type, Object obj, int i, Object value);

    // Field Methods
    Object get(Field field, Object obj) throws IllegalAccessException;

    void set(Field field, Object obj, Object value) throws IllegalAccessException;

    // Method Methods :p
    Object invoke(Method m, Object obj, Object[] params);
}

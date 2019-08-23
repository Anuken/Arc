package io.anuke.mnet;

public interface MSerializer{

    /**
     * Serializes object into byte array.
     * @param o object to serialize
     * @return byte[] representing serialized object. It should also contain data about Type (class) of this object
     * so it can be successfully deserialized on the other end. Must be new instance
     */
    byte[] serialize(Object o);

    /**
     * Serializes object into byte array. First 'offset' bytes of which are empty or not used.
     * Must be new object
     */
    byte[] serialize(Object o, int offset);

    /**
     * serializes Object into buffer, presuming that buffer size is bigger than serialized object.
     * @param o object to serialize
     * @param buffer buffer to which do serialization
     * @param offset where to put object in buffer
     * @return serialized object length
     */
    int serialize(Object o, byte[] buffer, int offset);

    /**
     * Deserialize object from byte[]. Throw Runtime exception to receive that this byte[] can't be serialized.
     * It will print stack trace on System.err
     * @param bytes serialzied object
     * @return deserialized instance
     */
    Object deserialize(byte[] bytes);

    /**
     * Deserialize object from byte[]. Throw Runtime exception to receive that this byte[] can't be serialized.
     * It will print stack trace on System.err
     * @param bytes serialzied object
     * @param offset serialization start
     * @return deserialized instance
     */
    Object deserialize(byte[] bytes, int offset, int length);
}

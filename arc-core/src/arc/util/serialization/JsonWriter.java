package arc.util.serialization;

import java.io.*;

public interface JsonWriter extends Closeable{
    JsonWriter name(String name) throws IOException;

    JsonWriter object() throws IOException;

    JsonWriter array() throws IOException;

    JsonWriter value(Object value) throws IOException;

    JsonWriter object(String name) throws IOException;

    JsonWriter array(String name) throws IOException;

    JsonWriter set(String name, Object value) throws IOException;

    JsonWriter pop() throws IOException;
}
package arc.util.serialization;

import arc.util.serialization.JsonWriter.*;

import java.io.*;

public interface BaseJsonWriter extends Closeable{
    void setOutputType(OutputType outputType);

    void setQuoteLongValues(boolean quoteLongValues);

    BaseJsonWriter name(String name) throws IOException;

    BaseJsonWriter object() throws IOException;

    BaseJsonWriter array() throws IOException;

    BaseJsonWriter value(Object value) throws IOException;

    BaseJsonWriter object(String name) throws IOException;

    BaseJsonWriter array(String name) throws IOException;

    BaseJsonWriter set(String name, Object value) throws IOException;

    BaseJsonWriter pop() throws IOException;
}

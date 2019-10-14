package io.anuke.arc.util.serialization;

import io.anuke.arc.files.FileHandle;

import java.io.InputStream;

public interface BaseJsonReader{
    JsonValue parse(InputStream input);

    default JsonValue parse(FileHandle file){
        return parse(file.read());
    }
}

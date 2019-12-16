package io.anuke.arc.util.serialization;

import io.anuke.arc.files.Fi;

import java.io.InputStream;

public interface BaseJsonReader{
    JsonValue parse(InputStream input);

    default JsonValue parse(Fi file){
        return parse(file.read());
    }
}

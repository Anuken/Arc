package io.anuke.arc.util.serialization;

import io.anuke.arc.files.FileHandle;

import java.io.InputStream;

public interface BaseJsonReader{
    JsonValue parse(InputStream input);

    JsonValue parse(FileHandle file);
}

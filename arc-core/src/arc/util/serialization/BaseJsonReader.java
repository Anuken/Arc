package arc.util.serialization;

import arc.files.Fi;

import java.io.InputStream;

public interface BaseJsonReader{
    JsonValue parse(InputStream input);

    default JsonValue parse(Fi file){
        return parse(file.read());
    }
}

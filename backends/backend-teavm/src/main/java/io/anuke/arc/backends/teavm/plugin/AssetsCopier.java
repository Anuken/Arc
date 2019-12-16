package io.anuke.arc.backends.teavm.plugin;

import io.anuke.arc.collection.*;
import io.anuke.arc.files.*;
import io.anuke.arc.util.serialization.*;
import io.anuke.arc.util.serialization.JsonWriter.*;
import org.teavm.backend.javascript.rendering.*;
import org.teavm.vm.*;
import org.teavm.vm.spi.*;

import java.io.*;


public class AssetsCopier implements RendererListener{
    private Json json = new Json(OutputType.json);

    @Override
    public void begin(RenderingManager context, BuildTarget buildTarget) throws IOException{
        json.setElementType(FileDescriptor.class, "childFiles", FileDescriptor.class);
    }

    @Override
    public void complete() throws IOException{
        Fi main = new Fi("teavm/build/teavm");
        Fi assets = main.child("assets");
        main.child("filesystem.json").writeString("[" + Array.with(assets.list()).toString(",\n", s -> json.toJson(new FileDescriptor(s.file()))) + "]");
    }

    class FileDescriptor{
        FileDescriptor[] childFiles = new FileDescriptor[0];
        String name;
        boolean directory;

        FileDescriptor(File file){
            this.name = file.getName();
            this.directory = file.isDirectory();
            if(directory){
                this.childFiles = Array.with(file.listFiles()).map(FileDescriptor::new).toArray(FileDescriptor.class);
            }
        }
    }
}

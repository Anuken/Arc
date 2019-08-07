/*
 *  Copyright 2015 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.anuke.arc.backends.teavm.plugin;

import io.anuke.arc.collection.*;
import io.anuke.arc.files.*;
import io.anuke.arc.util.serialization.*;
import io.anuke.arc.util.serialization.JsonWriter.*;
import org.teavm.backend.javascript.rendering.*;
import org.teavm.vm.*;
import org.teavm.vm.spi.*;

import java.io.*;

/**
 *
 * @author Alexey Andreev
 */
public class AssetsCopier implements RendererListener {
    private Json json = new Json(OutputType.json);

    @Override
    public void begin(RenderingManager context, BuildTarget buildTarget) throws IOException {
        json.setElementType(FileDescriptor.class, "childFiles", FileDescriptor.class);
    }

    @Override
    public void complete() throws IOException {
        FileHandle main = new FileHandle("teavm/build/teavm");
        FileHandle assets = main.child("assets");
        main.child("filesystem.json").writeString("[" + Array.with(assets.list()).toString(",\n", s -> json.toJson(new FileDescriptor(s.file()))) + "]");
    }

    class FileDescriptor {
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

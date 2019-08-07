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
    private RenderingManager context;
    private FileDescriptor rootFileDescriptor = new FileDescriptor();
    private Json json = new Json(OutputType.json);

    @Override
    public void begin(RenderingManager context, BuildTarget buildTarget) throws IOException {
        this.context = context;
        json.setElementType(FileDescriptor.class, "childFiles", FileDescriptor.class);
    }

    @Override
    public void complete() throws IOException {
        String dirName = context.getProperties().getProperty("teavm.libgdx.genAssetsDirectory", "");
        if (!dirName.isEmpty()) {
            File dir = new File(dirName);
            dir.mkdirs();
            createFSDescriptor(dir);
        } else {
            createFSDescriptor(null);
        }
    }

    private void createFSDescriptor(File dir) throws IOException {
        String path = context.getProperties().getProperty("teavm.libgdx.fsJsonPath", "");
        if (path.isEmpty()) {
            return;
        }
        if (dir != null) {
            processFile(dir, rootFileDescriptor);
        }

        String dirName = context.getProperties().getProperty("teavm.libgdx.warAssetsDirectory", "");
        if (!dirName.isEmpty()) {
            dir = new File(dirName);
            processFile(dir, rootFileDescriptor);
        }

        try (DataOutputStream output = new DataOutputStream(new FileOutputStream(new File(path)))) {
            writeJsonFS(output);
        }
    }

    private void processFile(File file, FileDescriptor desc) {
        desc.setName(file.getName());
        desc.setDirectory(file.isDirectory());
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                FileDescriptor childDesc = new  FileDescriptor();
                processFile(child, childDesc);
                desc.getChildFiles().add(childDesc);
            }
        }
    }

    private void writeJsonFS(DataOutputStream output) throws IOException {
        boolean first = true;
        output.write((byte)'[');
        for (FileDescriptor desc : rootFileDescriptor.getChildFiles()) {
            if (!first) {
                output.write((byte)',');
            }
            first = false;

            output.writeUTF(json.toJson(desc));
        }
        output.write((byte)']');
    }
}

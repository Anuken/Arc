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
package io.anuke.arc.backends.teavm.emu;

import io.anuke.arc.files.FileHandle;
import io.anuke.arc.graphics.Pixmap;
import io.anuke.arc.graphics.Pixmap.Format;
import io.anuke.arc.graphics.TextureData;
import io.anuke.arc.graphics.glutils.FileTextureData;
import io.anuke.arc.backends.teavm.plugin.Annotations.Emulate;

/**
 *
 * @author Alexey Andreev
 */
@Emulate(TextureData.Factory.class)
@SuppressWarnings("unused")
public class TextureDataEmulator {
    public static TextureData loadFromFile(FileHandle file, Format format, boolean useMipMaps) {
        if (file == null) {
            return null;
        }
        return new FileTextureData(file, new Pixmap(file), format, useMipMaps);
    }
}

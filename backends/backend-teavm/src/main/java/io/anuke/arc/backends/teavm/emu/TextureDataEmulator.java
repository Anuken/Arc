package io.anuke.arc.backends.teavm.emu;

import io.anuke.arc.backends.teavm.plugin.Annotations.*;
import io.anuke.arc.files.*;
import io.anuke.arc.graphics.*;
import io.anuke.arc.graphics.Pixmap.*;
import io.anuke.arc.graphics.glutils.*;


@Emulate(TextureData.Factory.class)
@SuppressWarnings("unused")
public class TextureDataEmulator{
    public static TextureData loadFromFile(Fi file, Format format, boolean useMipMaps){
        if(file == null){
            return null;
        }
        return new FileTextureData(file, new Pixmap(file), format, useMipMaps);
    }
}

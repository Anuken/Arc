package arc.backend.teavm.emu;

import arc.backend.teavm.plugin.Annotations.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.Pixmap.*;
import arc.graphics.gl.*;


@Emulate(TextureData.Factory.class)
@SuppressWarnings("unused")
public class TextureDataEmu{
    public static TextureData loadFromFile(Fi file, Format format, boolean useMipMaps){
        return file == null ? null : new FileTextureData(file, new Pixmap(file), format, useMipMaps);
    }
}

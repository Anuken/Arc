package io.anuke.arc.backends.sdl;

import io.anuke.arc.Files.*;
import io.anuke.arc.graphics.*;
import io.anuke.arc.graphics.glutils.*;

public class SdlConfig{
    public boolean disableAudio = false;
    public int audioDeviceSimultaneousSources = 64;
    public int audioDeviceBufferSize = 1024;
    public int audioDeviceBufferCount = 9;

    public int r = 8, g = 8, b = 8, a = 8;
    public int depth = 16, stencil = 0;
    public int samples = 0;
    public HdpiMode hdpiMode = HdpiMode.Logical;

    public int width = 640;
    public int height = 480;
    public boolean resizable = true;
    public boolean decorated = true;
    public boolean maximized = false;

    public String title = "Arc Application";
    public Color initialBackgroundColor = Color.BLACK;
    public boolean initialVisible = true;
    public boolean vSyncEnabled = true;

    FileType windowIconFileType;
    String[] windowIconPaths;

    public void setWindowIcon(FileType fileType, String... filePaths){
        windowIconFileType = fileType;
        windowIconPaths = filePaths;
    }
}

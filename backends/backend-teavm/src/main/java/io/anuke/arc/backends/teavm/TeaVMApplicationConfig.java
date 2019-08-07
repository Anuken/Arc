package io.anuke.arc.backends.teavm;

import org.teavm.jso.dom.html.HTMLCanvasElement;

public class TeaVMApplicationConfig {
    public HTMLCanvasElement canvas;
    public boolean antialiasEnabled = false;
    public boolean stencilEnabled = false;
    public boolean alphaEnabled = false;
    public boolean premultipliedAlpha = true;
    public boolean drawingBufferPreserved = false;
}

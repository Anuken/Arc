package io.anuke.arc.backends.teavm;

import org.teavm.jso.dom.html.HTMLCanvasElement;

public class TeaVMApplicationConfig {
    public HTMLCanvasElement canvas;
    public boolean antialiasEnabled;
    public boolean stencilEnabled;
    public boolean alphaEnabled = true;
    public boolean premultipliedAlpha;
    public boolean drawingBufferPreserved;
}

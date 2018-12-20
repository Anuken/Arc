package io.anuke.arc.backends.lwjgl3;

import io.anuke.arc.Core;
import io.anuke.arc.graphics.Pixmap;
import io.anuke.arc.utils.Clipboard;
import org.lwjgl.glfw.GLFW;

/**
 * Clipboard implementation for desktop that uses the system clipboard via GLFW.
 * @author mzechner
 */
public class Lwjgl3Clipboard implements Clipboard{
    @Override
    public String getContents(){
        return GLFW.glfwGetClipboardString(((Lwjgl3Graphics)Core.graphics).getWindow().getWindowHandle());
    }

    @Override
    public void setContents(String content){
        GLFW.glfwSetClipboardString(((Lwjgl3Graphics)Core.graphics).getWindow().getWindowHandle(), content);
    }

    @Override
    public void setContents(Pixmap pixmap){

    }
}

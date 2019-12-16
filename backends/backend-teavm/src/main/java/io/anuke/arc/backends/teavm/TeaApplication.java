package io.anuke.arc.backends.teavm;

import io.anuke.arc.*;
import io.anuke.arc.collection.*;
import io.anuke.arc.func.*;
import io.anuke.arc.util.*;
import io.anuke.arc.util.Log.*;
import org.teavm.jso.*;
import org.teavm.jso.browser.*;
import org.teavm.jso.dom.html.*;

public class TeaApplication implements Application{
    private TeaApplicationConfig config;
    private HTMLCanvasElement canvas;
    private TeaGraphics graphics;
    private TeaInput input;
    private int lastWidth = -1, lastHeight = 1;
    private String clipboard = "";

    private final TaskQueue queue = new TaskQueue();
    private final Array<ApplicationListener> listeners = new Array<>();

    public TeaApplication(ApplicationListener listener, TeaApplicationConfig config){
        this.listeners.add(listener);
        this.config = config;
    }

    public void start(){
        TeaFileLoader.loadFiles(new TeaFileLoader.TeaVMFilePreloadListener(){
            @Override
            public void error(){
            }

            @Override
            public void complete(){
                startArc();
            }
        });
    }

    private void startArc(){
        canvas = config.canvas;

        Log.setLogger(new TeaLogger());

        Core.app = this;
        Core.graphics = graphics = new TeaGraphics(canvas, config);
        Core.gl = graphics.getGL20();
        Core.gl20 = graphics.getGL20();
        Core.files = new TeaFiles();
        Core.net = new TeaNet();
        Core.audio = new TeaAudio();
        Core.input = input = new TeaInput(canvas);
        Core.settings = new TeaSettings();
        listen(ApplicationListener::init);
        listen(l -> l.resize(canvas.getWidth(), canvas.getHeight()));
        delayedStep();
    }

    private void delayedStep(){
        Window.requestAnimationFrame(d -> step());
    }

    private void step(){
        graphics.update();
        graphics.frameId++;

        queue.run();

        Window window = Window.current();

        if(window.getInnerWidth() != canvas.getWidth() || window.getInnerHeight() != canvas.getHeight()){
            canvas.setWidth(window.getInnerWidth());
            canvas.setHeight(window.getInnerHeight());
        }

        if(lastWidth != canvas.getWidth() || lastHeight != canvas.getHeight()){
            listen(l -> l.resize(canvas.getWidth(), canvas.getHeight()));
            lastWidth = canvas.getWidth();
            lastHeight = canvas.getHeight();
        }

        listen(ApplicationListener::update);
        input.prepareNext();
        delayedStep();
    }

    private void listen(Cons<ApplicationListener> cons){
        for(ApplicationListener l : listeners){
            cons.get(l);
        }
    }

    @Override
    public Array<ApplicationListener> getListeners(){
        return listeners;
    }

    @Override
    public String getClipboardText(){
        return clipboard;
    }

    @Override
    public void setClipboardText(String text){
        this.clipboard = text;
    }

    @Override
    public void post(Runnable runnable){
        queue.post(runnable);
    }

    @Override
    public ApplicationType getType(){
        return ApplicationType.WebGL;
    }

    @Override
    public void exit(){
    }

    public static class TeaApplicationConfig{
        public HTMLCanvasElement canvas;
        public boolean antialiasEnabled = false;
        public boolean stencilEnabled = false;
        public boolean alphaEnabled = false;
        public boolean premultipliedAlpha = true;
        public boolean drawingBufferPreserved = false;
    }

    public static class TeaLogger implements LogHandler{

        @Override
        public void log(LogLevel level, String text, Object... args){
            consoleLog("[" + level.name() + "]: " + Strings.format(text, args));
        }

        @JSBody(params = "message", script = "console.log(\"Arc: \" + message);")
        native static public void consoleLog(String message);
    }
}

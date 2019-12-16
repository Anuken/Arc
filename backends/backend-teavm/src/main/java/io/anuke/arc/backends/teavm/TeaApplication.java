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
    private TeaVMApplicationConfig config;
    private HTMLCanvasElement canvas;
    private TeaGraphics graphics;
    private TeaInput input;
    private int lastWidth = -1, lastHeight = 1;
    private String clipboard = "";

    private final Array<Runnable> runnables = new Array<>();
    private final Array<Runnable> executedRunnables = new Array<>();
    private final Array<ApplicationListener> listeners = new Array<>();

    public TeaApplication(ApplicationListener listener, TeaVMApplicationConfig config){
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

        Log.setLogger(new TeaVMLogger());

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
        Window.setTimeout(this::step, 10);
    }

    private void step(){
        graphics.update();
        graphics.frameId++;

        executedRunnables.clear();
        executedRunnables.addAll(runnables);
        runnables.clear();

        for(Runnable runnable : executedRunnables){
            runnable.run();
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
        runnables.add(runnable);
    }

    @Override
    public ApplicationType getType(){
        return ApplicationType.WebGL;
    }

    @Override
    public long getJavaHeap(){
        return 0;
    }

    @Override
    public void exit(){
    }

    public static class TeaVMApplicationConfig{
        public HTMLCanvasElement canvas;
        public boolean antialiasEnabled = false;
        public boolean stencilEnabled = false;
        public boolean alphaEnabled = false;
        public boolean premultipliedAlpha = true;
        public boolean drawingBufferPreserved = false;
    }

    public static class TeaVMLogger implements LogHandler{

        @Override
        public void log(LogLevel level, String text, Object... args){
            consoleLog("[" + level.name() + "]: " + Strings.format(text, args));
        }

        @JSBody(params = "message", script = "console.log(\"Arc: \" + message);")
        native static public void consoleLog(String message);
    }
}

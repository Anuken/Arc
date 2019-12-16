package io.anuke.arc.backends.teavm;

import io.anuke.arc.*;
import io.anuke.arc.collection.*;
import io.anuke.arc.func.*;
import io.anuke.arc.util.*;
import org.teavm.jso.browser.*;
import org.teavm.jso.dom.html.*;

public class TeaVMApplication implements Application{
    private TeaVMApplicationConfig config;
    private HTMLCanvasElement canvas;
    private TeaVMGraphics graphics;
    private TeaVMInput input;
    private int lastWidth = -1, lastHeight = 1;
    private String clipboard = "";

    private final Array<Runnable> runnables = new Array<>();
    private final Array<Runnable> executedRunnables = new Array<>();
    private final Array<ApplicationListener> listeners = new Array<>();

    public TeaVMApplication(ApplicationListener listener, TeaVMApplicationConfig config){
        this.listeners.add(listener);
        this.config = config;
    }

    public void start(){
        TeaVMFileLoader.loadFiles(new TeaVMFilePreloadListener(){
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
        Core.graphics = graphics = new TeaVMGraphics(canvas, config);
        Core.gl = graphics.getGL20();
        Core.gl20 = graphics.getGL20();
        Core.files = new TeaVMFiles();
        Core.net = new TeaVMNet();
        Core.audio = new TeaVMAudio();
        Core.input = input = new TeaVMInput(canvas);
        Core.settings = new TeaVMSettings();
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

}

/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package io.anuke.arc.backends.gwt;

import io.anuke.arc.Application;
import io.anuke.arc.ApplicationListener;
import io.anuke.arc.Core;
import io.anuke.arc.backends.gwt.preloader.Preloader;
import io.anuke.arc.backends.gwt.preloader.Preloader.PreloaderCallback;
import io.anuke.arc.backends.gwt.preloader.Preloader.PreloaderState;
import io.anuke.arc.backends.gwt.soundmanager2.SoundManager;
import io.anuke.arc.collection.Array;
import io.anuke.arc.utils.Clipboard;
import io.anuke.arc.utils.Log;
import io.anuke.arc.utils.Log.LogLevel;
import io.anuke.arc.utils.TimeUtils;
import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.*;

/**
 * Implementation of an {@link Application} based on GWT. Clients have to override {@link #getConfig()} and
 * {@link #createApplicationListener()}. Clients can override the default loading screen via
 * {@link #getPreloaderCallback()} and implement any loading screen drawing via GWT widgets.
 * @author mzechner
 */
public abstract class GwtApplication implements EntryPoint, Application{
    private static AgentInfo agentInfo;
    protected TextArea log = null;
    GwtApplicationConfiguration config;
    GwtGraphics graphics;
    Preloader preloader;
    LoadingListener loadingListener;
    long loadStart = TimeUtils.nanoTime();
    private GwtInput input;
    private GwtNet net;
    private Panel root = null;
    private Array<Runnable> runnables = new Array<>();
    private Array<Runnable> runnablesHelper = new Array<>();
    private Array<ApplicationListener> listeners = new Array<ApplicationListener>();
    private int lastWidth;
    private int lastHeight;
    private Clipboard clipboard;

    /**
     * Contains precomputed information on the user-agent. Useful for dealing with browser and OS behavioral differences. ""Kindly""
     * borrowed from PlayN
     */
    public static AgentInfo agentInfo(){
        return agentInfo;
    }

    /** kindly borrowed from PlayN **/
    private static native AgentInfo computeAgentInfo() /*-{
        var userAgent = navigator.userAgent.toLowerCase();
        return {
            // browser type flags
            isFirefox : userAgent.indexOf("firefox") != -1,
            isChrome : userAgent.indexOf("chrome") != -1,
            isSafari : userAgent.indexOf("safari") != -1,
            isOpera : userAgent.indexOf("opera") != -1,
            isIE : userAgent.indexOf("msie") != -1 || userAgent.indexOf("trident") != -1,
            // OS type flags
            isMacOS : userAgent.indexOf("mac") != -1,
            isLinux : userAgent.indexOf("linux") != -1,
            isWindows : userAgent.indexOf("win") != -1
        };
    }-*/;

    native static public void consoleLog(String message) /*-{
		console.log( "GWT: " + message );
	}-*/;

    /** @return the configuration for the {@link GwtApplication}. */
    public abstract GwtApplicationConfiguration getConfig();

    @Override
    public Array<ApplicationListener> getListeners(){
        return listeners;
    }

    public String getPreloaderBaseURL(){
        return GWT.getHostPageBaseURL() + "assets/";
    }

    public abstract ApplicationListener createApplicationListener();

    @Override
    public void onModuleLoad(){
        GwtApplication.agentInfo = computeAgentInfo();
        addListener(createApplicationListener());
        this.config = getConfig();
        Log.setLogger(new GwtApplicationLogger(this.config.log));
        Log.setLogLevel(LogLevel.none);

        if(config.rootPanel != null){
            this.root = config.rootPanel;
        }else{
            Element element = Document.get().getElementById("embed-" + GWT.getModuleName());
            if(element == null){
                VerticalPanel panel = new VerticalPanel();
                panel.setWidth("" + config.width + "px");
                panel.setHeight("" + config.height + "px");
                panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
                panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
                RootPanel.get().add(panel);
                RootPanel.get().setWidth("" + config.width + "px");
                RootPanel.get().setHeight("" + config.height + "px");
                this.root = panel;
            }else{
                VerticalPanel panel = new VerticalPanel();
                panel.setWidth("" + config.width + "px");
                panel.setHeight("" + config.height + "px");
                panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
                panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
                element.appendChild(panel.getElement());
                root = panel;
            }
        }


        if(config.disableAudio){
            preloadAssets();
        }else{
            // initialize SoundManager2
            SoundManager.init(GWT.getModuleBaseURL(), 9, config.preferFlash, new SoundManager.SoundManagerCallback(){

                @Override
                public void onready(){
                    preloadAssets();
                }

                @Override
                public void ontimeout(String status, String errorType){
                    Log.err("[SoundManager] " + status + " " + errorType);
                }

            });
        }
    }

    void preloadAssets(){
        final PreloaderCallback callback = getPreloaderCallback();
        preloader = createPreloader();
        preloader.preload("assets.txt", new PreloaderCallback(){
            @Override
            public void error(String file){
                callback.error(file);
            }

            @Override
            public void update(PreloaderState state){
                callback.update(state);
                if(state.hasEnded()){
                    getRootPanel().clear();
                    if(loadingListener != null)
                        loadingListener.beforeSetup();
                    setupLoop();
                    addEventListeners();
                    if(loadingListener != null)
                        loadingListener.afterSetup();
                }
            }
        });
    }

    /**
     * Override this method to return a custom widget informing the that their browser lacks support of WebGL.
     * @return Widget to display when WebGL is not supported.
     */
    public Widget getNoWebGLSupportWidget(){
        return new Label("Sorry, your browser doesn't seem to support WebGL");
    }

    void setupLoop(){
        // setup modules
        try{
            graphics = new GwtGraphics(root, config);
        }catch(Throwable e){
            root.clear();
            root.add(getNoWebGLSupportWidget());
            return;
        }
        lastWidth = graphics.getWidth();
        lastHeight = graphics.getHeight();
        Core.app = this;

        if(config.disableAudio){
            Core.audio = null;
        }else{
            Core.audio = new GwtAudio();
        }
        Core.graphics = graphics;
        Core.gl20 = graphics.getGL20();
        Core.settings = new GwtSettings();
        Core.gl = Core.gl20;
        Core.files = new GwtFiles(preloader);
        this.input = new GwtInput(graphics.canvas);
        Core.input = this.input;
        this.net = new GwtNet(config);
        Core.net = this.net;
        this.clipboard = new GwtClipboard();
        updateLogLabelSize();

        // tell listener about app creation
        try{
            for(ApplicationListener listener : listeners){
                listener.create();
                listener.resize(graphics.getWidth(), graphics.getHeight());
            }
        }catch(Throwable t){
            Log.err("[GwtApplication] exception: " + t.getMessage(), t);
            t.printStackTrace();
            throw new RuntimeException(t);
        }

        AnimationScheduler.get().requestAnimationFrame(new AnimationCallback(){
            @Override
            public void execute(double timestamp){
                try{
                    mainLoop();
                }catch(Throwable t){
                    Log.err("[GwtApplication] exception: " + t.getMessage(), t);
                    throw new RuntimeException(t);
                }
                AnimationScheduler.get().requestAnimationFrame(this, graphics.canvas);
            }
        }, graphics.canvas);
    }

    void mainLoop(){
        graphics.update();
        if(Core.graphics.getWidth() != lastWidth || Core.graphics.getHeight() != lastHeight){
            lastWidth = graphics.getWidth();
            lastHeight = graphics.getHeight();
            Core.gl.glViewport(0, 0, lastWidth, lastHeight);
            for(ApplicationListener listener : listeners){
                listener.resize(lastWidth, lastHeight);
            }
        }
        runnablesHelper.addAll(runnables);
        runnables.clear();
        for(int i = 0; i < runnablesHelper.size; i++){
            runnablesHelper.get(i).run();
        }
        runnablesHelper.clear();
        graphics.frameId++;
        for(ApplicationListener listener : listeners){
            listener.update();
        }
        input.reset();
    }

    public Panel getRootPanel(){
        return root;
    }

    public Preloader createPreloader(){
        return new Preloader(getPreloaderBaseURL());
    }

    public PreloaderCallback getPreloaderCallback(){
        final Panel preloaderPanel = new VerticalPanel();
        preloaderPanel.setStyleName("gdx-preloader");
        final Image logo = new Image(GWT.getModuleBaseURL() + "logo.png");
        logo.setStyleName("logo");
        preloaderPanel.add(logo);
        final Panel meterPanel = new SimplePanel();
        meterPanel.setStyleName("gdx-meter");
        meterPanel.addStyleName("red");
        final InlineHTML meter = new InlineHTML();
        final Style meterStyle = meter.getElement().getStyle();
        meterStyle.setWidth(0, Unit.PCT);
        meterPanel.add(meter);
        preloaderPanel.add(meterPanel);
        getRootPanel().add(preloaderPanel);
        return new PreloaderCallback(){

            @Override
            public void error(String file){
                System.out.println("error: " + file);
            }

            @Override
            public void update(PreloaderState state){
                meterStyle.setWidth(100f * state.getProgress(), Unit.PCT);
            }

        };
    }

    private void updateLogLabelSize(){
        if(log != null){
            if(graphics != null){
                log.setSize(graphics.getWidth() + "px", "200px");
            }else{
                log.setSize("400px", "200px"); // Should not happen at this point, use dummy value
            }
        }
    }

    @Override
    public ApplicationType getType(){
        return ApplicationType.WebGL;
    }

    @Override
    public int getVersion(){
        return 0;
    }

    @Override
    public long getJavaHeap(){
        return 0;
    }

    @Override
    public long getNativeHeap(){
        return 0;
    }

    @Override
    public Clipboard getClipboard(){
        return clipboard;
    }

    @Override
    public void post(Runnable runnable){
        runnables.add(runnable);
    }

    @Override
    public void exit(){
    }

    public String getBaseUrl(){
        return preloader.baseUrl;
    }

    public Preloader getPreloader(){
        return preloader;
    }

    public CanvasElement getCanvasElement(){
        return graphics.canvas;
    }

    public LoadingListener getLoadingListener(){
        return loadingListener;
    }

    public void setLoadingListener(LoadingListener loadingListener){
        this.loadingListener = loadingListener;
    }

    private native void addEventListeners() /*-{
		var self = this;

		var eventName = null;
		if ("hidden" in $doc) {
			eventName = "visibilitychange"
		} else if ("webkitHidden" in $doc) {
			eventName = "webkitvisibilitychange"
		} else if ("mozHidden" in $doc) {
			eventName = "mozvisibilitychange"
		} else if ("msHidden" in $doc) {
			eventName = "msvisibilitychange"
		}

		if (eventName !== null) {
			$doc.addEventListener(eventName, function(e) {
				self.@io.anuke.arc.backends.gwt.GwtApplication::onVisibilityChange(Z)($doc['hidden'] !== true);
			});
		}
	}-*/;

    private void onVisibilityChange(boolean visible){
        if(visible){
            for(ApplicationListener listener : listeners){
                listener.resume();
            }
        }else{
            for(ApplicationListener listener : listeners){
                listener.pause();
            }
        }
    }

    /**
     * LoadingListener interface main purpose is to do some things before or after {@link GwtApplication#setupLoop()}
     */
    public interface LoadingListener{
        /**
         * Method called before the setup
         */
        void beforeSetup();

        /**
         * Method called after the setup
         */
        void afterSetup();
    }

    /** Returned by {@link #agentInfo}. Kindly borrowed from PlayN. */
    public static class AgentInfo extends JavaScriptObject{
        protected AgentInfo(){
        }

        public final native boolean isFirefox() /*-{
            return this.isFirefox;
        }-*/;

        public final native boolean isChrome() /*-{
            return this.isChrome;
        }-*/;

        public final native boolean isSafari() /*-{
            return this.isSafari;
        }-*/;

        public final native boolean isOpera() /*-{
            return this.isOpera;
        }-*/;

        public final native boolean isIE() /*-{
            return this.isIE;
        }-*/;

        public final native boolean isMacOS() /*-{
            return this.isMacOS;
        }-*/;

        public final native boolean isLinux() /*-{
            return this.isLinux;
        }-*/;

        public final native boolean isWindows() /*-{
            return this.isWindows;
        }-*/;
    }
}

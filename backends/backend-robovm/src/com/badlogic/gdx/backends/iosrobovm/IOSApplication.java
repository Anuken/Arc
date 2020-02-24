package com.badlogic.gdx.backends.iosrobovm;

import arc.*;
import arc.graphics.*;
import arc.struct.*;
import arc.util.*;
import com.badlogic.gdx.backends.iosrobovm.objectal.*;
import org.robovm.apple.coregraphics.*;
import org.robovm.apple.foundation.*;
import org.robovm.apple.uikit.*;
import org.robovm.rt.bro.*;

import java.lang.Runtime;

import static org.robovm.apple.foundation.NSPathUtilities.getDocumentsDirectory;

public class IOSApplication implements Application{
    UIApplication uiApp;
    UIWindow uiWindow;
    IOSViewControllerListener viewControllerListener;
    IOSApplicationConfiguration config;
    IOSGraphics graphics;
    IOSAudio audio;
    IOSFiles files;
    IOSInput input;
    IOSNet net;

    /** The display scale factor (1.0f for normal; 2.0f to use retina coordinates/dimensions). */
    float displayScaleFactor;
    Array<ApplicationListener> listeners = new Array<>();
    Array<Runnable> runnables = new Array<>();
    Array<Runnable> executedRunnables = new Array<>();
    private CGRect lastScreenBounds = null;


    public IOSApplication(ApplicationListener listener, IOSApplicationConfiguration config){
        addListener(listener);
        this.config = config;
    }

    final boolean didFinishLaunching(UIApplication uiApp, UIApplicationLaunchOptions options){
        Core.app = this;
        this.uiApp = uiApp;

        // enable or disable screen dimming
        UIApplication.getSharedApplication().setIdleTimerDisabled(config.preventScreenDimming);

        Log.info("[IOSApplication] iOS version: " + UIDevice.getCurrentDevice().getSystemVersion());
        // fix the scale factor if we have a retina device (NOTE: iOS screen sizes are in "points" not pixels by default!)

        Log.info("[IOSApplication] Running in " + (Bro.IS_64BIT ? "64-bit" : "32-bit") + " mode");

        float scale = (float)(getIosVersion() >= 8 ? UIScreen.getMainScreen().getNativeScale() : UIScreen.getMainScreen()
        .getScale());
        if(scale >= 2.0f){
            Log.info("[IOSApplication] scale: " + scale);
            if(UIDevice.getCurrentDevice().getUserInterfaceIdiom() == UIUserInterfaceIdiom.Pad){
                // it's an iPad!
                displayScaleFactor = config.displayScaleLargeScreenIfRetina * scale;
            }else{
                // it's an iPod or iPhone
                displayScaleFactor = config.displayScaleSmallScreenIfRetina * scale;
            }
        }else{
            // no retina screen: no scaling!
            if(UIDevice.getCurrentDevice().getUserInterfaceIdiom() == UIUserInterfaceIdiom.Pad){
                // it's an iPad!
                displayScaleFactor = config.displayScaleLargeScreenIfNonRetina;
            }else{
                // it's an iPod or iPhone
                displayScaleFactor = config.displayScaleSmallScreenIfNonRetina;
            }
        }

        // setup libgdx
        this.input = createInput();
        this.graphics = createGraphics(scale);
        Core.gl = Core.gl20 = graphics.gl20;
        Core.gl30 = graphics.gl30;
        this.files = new IOSFiles();
        this.audio = new IOSAudio(config);
        this.net = new IOSNet(this);

        Core.settings = new Settings();
        Core.files = this.files;
        Core.graphics = this.graphics;
        Core.audio = this.audio;
        Core.input = this.input;
        Core.net = this.net;

        this.input.setupPeripherals();

        this.uiWindow = new UIWindow(UIScreen.getMainScreen().getBounds());
        this.uiWindow.setRootViewController(this.graphics.viewController);
        this.uiWindow.makeKeyAndVisible();
        Log.info("[IOSApplication] created");
        return true;
    }

    protected IOSGraphics createGraphics(float scale){
        return new IOSGraphics(scale, this, config, input, config.useGL30);
    }

    protected IOSInput createInput(){
        return new IOSInput(this);
    }

    int getIosVersion(){
        String systemVersion = UIDevice.getCurrentDevice().getSystemVersion();
        return Integer.parseInt(systemVersion.split("\\.")[0]);
    }

    /**
     * Return the UI view controller of IOSApplication
     * @return the view controller of IOSApplication
     */
    public UIViewController getUIViewController(){
        return graphics.viewController;
    }

    /**
     * Return the UI Window of IOSApplication
     * @return the window
     */
    public UIWindow getUIWindow(){
        return uiWindow;
    }

    /**
     * GL View spans whole screen, that is, even under the status bar. iOS can also rotate the screen, which is not handled
     * consistently over iOS versions. This method returns, in pixels, rectangle in which libGDX draws.
     * @return dimensions of space we draw to, adjusted for device orientation
     */
    protected CGRect getBounds(){
        final CGRect screenBounds = UIScreen.getMainScreen().getBounds();
        final CGRect statusBarFrame = uiApp.getStatusBarFrame();
        final UIInterfaceOrientation statusBarOrientation = uiApp.getStatusBarOrientation();

        double statusBarHeight = Math.min(statusBarFrame.getWidth(), statusBarFrame.getHeight());

        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        // Make sure that the orientation is consistent with ratios. Should be, but may not be on older iOS versions
        switch(statusBarOrientation){
            case LandscapeLeft:
            case LandscapeRight:
                if(screenHeight > screenWidth){
                    Log.info("[IOSApplication] Switching reported width and height (w=" + screenWidth + " h=" + screenHeight + ")");
                    double tmp = screenHeight;
                    // noinspection SuspiciousNameCombination
                    screenHeight = screenWidth;
                    screenWidth = tmp;
                }
        }

        // update width/height depending on display scaling selected
        screenWidth *= displayScaleFactor;
        screenHeight *= displayScaleFactor;

        if(statusBarHeight != 0.0){
            Log.info("[IOSApplication] Status bar is visible (height = " + statusBarHeight + ")");
            statusBarHeight *= displayScaleFactor;
            screenHeight -= statusBarHeight;
        }else{
            Log.info("[IOSApplication] Status bar is not visible");
        }

        Log.info("[IOSApplication] Total computed bounds are w=" + screenWidth + " h=" + screenHeight);

        return lastScreenBounds = new CGRect(0.0, statusBarHeight, screenWidth, screenHeight);
    }

    protected CGRect getCachedBounds(){
        if(lastScreenBounds == null)
            return getBounds();
        else
            return lastScreenBounds;
    }

    final void didBecomeActive(UIApplication uiApp){
        Log.info("[IOSApplication] resumed");
        // workaround for ObjectAL crash problem
        // see: https://groups.google.com/forum/?fromgroups=#!topic/objectal-for-iphone/ubRWltp_i1Q
        OALAudioSession audioSession = OALAudioSession.sharedInstance();
        if(audioSession != null){
            audioSession.forceEndInterruption();
        }
        if(config.allowIpod){
            OALSimpleAudio audio = OALSimpleAudio.sharedInstance();
            if(audio != null){
                audio.setUseHardwareIfAvailable(false);
            }
        }
        graphics.makeCurrent();
        graphics.resume();
    }

    final void willEnterForeground(UIApplication uiApp){
        // workaround for ObjectAL crash problem
        // see: https://groups.google.com/forum/?fromgroups=#!topic/objectal-for-iphone/ubRWltp_i1Q
        OALAudioSession audioSession = OALAudioSession.sharedInstance();
        if(audioSession != null){
            audioSession.forceEndInterruption();
        }
    }

    final void willResignActive(UIApplication uiApp){
        Log.info("[IOSApplication] paused");
        graphics.makeCurrent();
        graphics.pause();
        Gl.finish();
    }

    final void willTerminate(UIApplication uiApp){
        Log.info("[IOSApplication] disposed");
        graphics.makeCurrent();
        Array<ApplicationListener> listeners = this.listeners;
        synchronized(listeners){
            for(ApplicationListener listener : listeners){
                listener.pause();
            }
        }
        Gl.finish();
    }

    @Override
    public ApplicationType getType(){
        return ApplicationType.iOS;
    }

    @Override
    public int getVersion(){
        return Integer.parseInt(UIDevice.getCurrentDevice().getSystemVersion().split("\\.")[0]);
    }

    @Override
    public long getJavaHeap(){
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    @Override
    public long getNativeHeap(){
        return getJavaHeap();
    }

    @Override
    public void post(Runnable runnable){
        synchronized(runnables){
            runnables.add(runnable);
            Core.graphics.requestRendering();
        }
    }

    public void processRunnables(){
        synchronized(runnables){
            executedRunnables.clear();
            executedRunnables.addAll(runnables);
            runnables.clear();
        }
        for(int i = 0; i < executedRunnables.size; i++){
            executedRunnables.get(i).run();
        }
    }

    @Override
    public void exit(){
        NSThread.exit();
    }

    @Override
    public String getClipboardText(){
        return UIPasteboard.getGeneralPasteboard().getString();
    }

    @Override
    public void setClipboardText(String text){
        UIPasteboard.getGeneralPasteboard().setString(text);
    }

    @Override
    public Array<ApplicationListener> getListeners(){
        return listeners;
    }

    /**
     * Add a listener to handle events from the libgdx root view controller
     * @param listener The {#link IOSViewControllerListener} to add
     */
    public void addViewControllerListener(IOSViewControllerListener listener){
        viewControllerListener = listener;
    }

    public static abstract class Delegate extends UIApplicationDelegateAdapter{
        private IOSApplication app;

        protected abstract IOSApplication createApplication();

        @Override
        public boolean didFinishLaunching(UIApplication application, UIApplicationLaunchOptions options){
            application.addStrongRef(this); // Prevent this from being GCed until the ObjC UIApplication is deallocated
            this.app = createApplication();

            boolean result = app.didFinishLaunching(application, options);
            if(options != null && options.has(UIApplicationLaunchOptions.Keys.URL())){
                openURL(((NSURL)options.get(UIApplicationLaunchOptions.Keys.URL())));
            }
            return result;
        }

        @Override
        public void didBecomeActive(UIApplication application){
            app.didBecomeActive(application);
        }

        @Override
        public void willEnterForeground(UIApplication application){
            app.willEnterForeground(application);
        }

        @Override
        public void willResignActive(UIApplication application){
            app.willResignActive(application);
        }

        @Override
        public void willTerminate(UIApplication application){
            app.willTerminate(application);
        }

        @Override
        public boolean openURL(UIApplication app, NSURL url, UIApplicationOpenURLOptions options) {
            openURL(url);
            return false;
        }

        void openURL(NSURL url){
            if(Core.app == null) return;
            Core.app.post(() -> {
                for(ApplicationListener list : Core.app.getListeners()){
                    list.fileDropped(Core.files.absolute(getDocumentsDirectory()).child(url.getLastPathComponent()));
                }
            });
        }
    }
}

package arc.backend.android;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.content.res.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import arc.Application;
import arc.*;
import arc.backend.android.surfaceview.*;
import arc.func.*;
import arc.struct.*;
import arc.util.*;

import java.util.concurrent.*;

/**
 * An implementation of the {@link Application} interface for Android. Create an {@link Activity} that derives from this class. In
 * the {@link Activity#onCreate(Bundle)} method call the {@link #initialize(ApplicationListener)} method specifying the
 * configuration for the GLSurfaceView.
 * @author mzechner
 */
public class AndroidApplication extends Activity implements AndroidApplicationBase{
    protected final Array<ApplicationListener> listeners = new Array<>();
    protected final Array<Runnable> runnables = new Array<>();
    protected final Array<Runnable> executedRunnables = new Array<>();
    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private final IntMap<AndroidEventListener> eventListeners = new IntMap<>();
    private int lastEventNumber = 43;
    public Handler handler;
    protected AndroidGraphics graphics;
    protected AndroidInput input;
    protected AndroidAudio audio;
    protected AndroidFiles files;
    protected AndroidNet net;
    protected Settings settings;
    protected ClipboardManager honeycombClipboard;
    protected boolean firstResume = true;
    protected boolean useImmersiveMode = false;
    protected boolean hideStatusBar = false;
    private int wasFocusChanged = -1;
    private boolean isWaitingForAudio = false;

    static{
        ArcNativesLoader.load();
        Log.setLogger(new AndroidApplicationLogger());
    }

    /**
     * This method has to be called in the {@link Activity#onCreate(Bundle)} method. It sets up all the things necessary to get
     * input, render via OpenGL and so on. Uses a default {@link AndroidApplicationConfiguration}.
     * @param listener the {@link ApplicationListener} implementing the program logic
     **/
    public void initialize(ApplicationListener listener){
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        initialize(listener, config);
    }

    /**
     * This method has to be called in the {@link Activity#onCreate(Bundle)} method. It sets up all the things necessary to get
     * input, render via OpenGL and so on. You can configure other aspects of the application with the rest of the fields in the
     * {@link AndroidApplicationConfiguration} instance.
     * @param listener the {@link ApplicationListener} implementing the program logic
     * @param config the {@link AndroidApplicationConfiguration}, defining various settings of the application (use accelerometer,
     * etc.).
     */
    public void initialize(ApplicationListener listener, AndroidApplicationConfiguration config){
        init(listener, config, false);
    }

    /**
     * This method has to be called in the {@link Activity#onCreate(Bundle)} method. It sets up all the things necessary to get
     * input, render via OpenGL and so on. Uses a default {@link AndroidApplicationConfiguration}.
     * <p>
     * Note: you have to add the returned view to your layout!
     * @param listener the {@link ApplicationListener} implementing the program logic
     * @return the GLSurfaceView of the application
     */
    public View initializeForView(ApplicationListener listener){
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        return initializeForView(listener, config);
    }

    /**
     * This method has to be called in the {@link Activity#onCreate(Bundle)} method. It sets up all the things necessary to get
     * input, render via OpenGL and so on. You can configure other aspects of the application with the rest of the fields in the
     * {@link AndroidApplicationConfiguration} instance.
     * <p>
     * Note: you have to add the returned view to your layout!
     * @param listener the {@link ApplicationListener} implementing the program logic
     * @param config the {@link AndroidApplicationConfiguration}, defining various settings of the application (use accelerometer,
     * etc.).
     * @return the GLSurfaceView of the application
     */
    public View initializeForView(ApplicationListener listener, AndroidApplicationConfiguration config){
        init(listener, config, true);
        return graphics.getView();
    }

    private void init(ApplicationListener listener, AndroidApplicationConfiguration config, boolean isForView){
        if(this.getVersion() < MINIMUM_SDK){
            throw new ArcRuntimeException("Arc requires Android API Level " + MINIMUM_SDK + " or later.");
        }
        graphics = new AndroidGraphics(this, config, config.resolutionStrategy == null ? new FillResolutionStrategy() : config.resolutionStrategy);
        input = new AndroidInput(this, this, graphics.view, config);
        audio = new AndroidAudio(this, config);
        this.getFilesDir(); // workaround for Android bug #10515463
        files = new AndroidFiles(this.getAssets(), this.getFilesDir().getAbsolutePath());
        net = new AndroidNet(this);
        settings = new Settings();
        addListener(listener);
        this.handler = new Handler();
        this.useImmersiveMode = config.useImmersiveMode;
        this.hideStatusBar = config.hideStatusBar;
        this.honeycombClipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);

        // Add a specialized audio lifecycle listener
        addListener(new ApplicationListener(){
            @Override
            public void pause(){
                audio.pause();
            }

            @Override
            public void dispose(){
                audio.dispose();
                graphics.dispose();
            }
        });

        Core.app = this;
        Core.settings = settings;
        Core.input = input;
        Core.audio = audio;
        Core.files = files;
        Core.graphics = graphics;
        Core.net = net;

        if(!isForView){
            try{
                requestWindowFeature(Window.FEATURE_NO_TITLE);
            }catch(Exception ex){
                Log.err("[AndroidApplication] Content already displayed, cannot request FEATURE_NO_TITLE", ex);
            }
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            setContentView(graphics.getView(), createLayoutParams());
        }

        createWakeLock(config.useWakelock);
        hideStatusBar(this.hideStatusBar);
        useImmersiveMode(this.useImmersiveMode);
        if(this.useImmersiveMode && getVersion() >= Build.VERSION_CODES.KITKAT){
            try{
                View rootView = getApplicationWindow().getDecorView();
                rootView.setOnSystemUiVisibilityChangeListener(arg0 -> getHandler().post(() -> useImmersiveMode(true)));
            }catch(Throwable e){
                Log.err("[AndroidApplication] Failed to create AndroidVisibilityListener", e);
            }
        }

        // detect an already connected bluetooth keyboardAvailable
        if(getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS){
            input.keyboardAvailable = true;
        }
    }

    protected FrameLayout.LayoutParams createLayoutParams(){
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
        android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        return layoutParams;
    }

    protected void createWakeLock(boolean use){
        if(use){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void hideStatusBar(boolean hide){
        if(!hide) return;

        getWindow().getDecorView().setSystemUiVisibility(0x1);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        useImmersiveMode(this.useImmersiveMode);
        hideStatusBar(this.hideStatusBar);
        if(hasFocus){
            this.wasFocusChanged = 1;
            if(this.isWaitingForAudio){
                this.audio.resume();
                this.isWaitingForAudio = false;
            }
        }else{
            this.wasFocusChanged = 0;
        }
    }

    @TargetApi(19)
    @Override
    public void useImmersiveMode(boolean use){
        if(!use || getVersion() < Build.VERSION_CODES.KITKAT) return;

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onPause(){
        //fixes obscure destruction bug
        if(useImmersiveMode){
            exec.submit(() -> {
                try{
                    Thread.sleep(100);
                }catch(InterruptedException ignored){
                }
                graphics.onDrawFrame(null);
            });
        }

        boolean isContinuous = graphics.isContinuousRendering();
        boolean isContinuousEnforced = AndroidGraphics.enforceContinuousRendering;

        // from here we don't want non continuous rendering
        AndroidGraphics.enforceContinuousRendering = true;
        graphics.setContinuousRendering(true);
        // calls to setContinuousRendering(false) from other thread (ex: GLThread)
        // will be ignored at this point...
        graphics.pause();

        input.onPause();

        if(isFinishing()){
            graphics.clearManagedCaches();
            graphics.destroy();
        }

        AndroidGraphics.enforceContinuousRendering = isContinuousEnforced;
        graphics.setContinuousRendering(isContinuous);

        graphics.onPauseGLSurfaceView();

        super.onPause();
    }

    @Override
    protected void onResume(){
        Core.app = this;
        Core.settings = settings;
        Core.input = input;
        Core.audio = audio;
        Core.files = files;
        Core.graphics = graphics;
        Core.net = net;

        input.onResume();

        if(graphics != null){
            graphics.onResumeGLSurfaceView();
        }

        if(!firstResume){
            graphics.resume();
        }else{
            firstResume = false;
        }

        this.isWaitingForAudio = true;
        if(this.wasFocusChanged == 1 || this.wasFocusChanged == -1){
            this.audio.resume();
            this.isWaitingForAudio = false;
        }
        super.onResume();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    public boolean openFolder(String file){
        Log.info(file);
        Uri selectedUri = Uri.parse(file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(selectedUri, "resource/folder");

        if(intent.resolveActivityInfo(getPackageManager(), 0) != null){
            startActivity(intent);
            return true;
        }else{
            runOnUiThread(() -> {
                Toast toast = Toast.makeText(getContext(), "Unable to open folder (missing valid file manager?)\n" + file, Toast.LENGTH_LONG);
                toast.show();
            });
            return false;
        }
    }

    @Override
    public ApplicationType getType(){
        return ApplicationType.Android;
    }

    @Override
    public int getVersion(){
        return android.os.Build.VERSION.SDK_INT;
    }

    @Override
    public long getJavaHeap(){
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    @Override
    public long getNativeHeap(){
        return Debug.getNativeHeapAllocatedSize();
    }

    @Override
    public String getClipboardText(){
        ClipData clip = honeycombClipboard.getPrimaryClip();
        if(clip == null) return null;
        CharSequence text = clip.getItemAt(0).getText();
        if(text == null) return null;
        return text.toString();
    }

    @Override
    public void setClipboardText(String contents){
        ClipData data = ClipData.newPlainText(contents, contents);
        honeycombClipboard.setPrimaryClip(data);
    }

    @Override
    public void post(Runnable runnable){
        synchronized(runnables){
            runnables.add(runnable);
            Core.graphics.requestRendering();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration config){
        super.onConfigurationChanged(config);
        boolean keyboardAvailable = false;
        if(config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) keyboardAvailable = true;
        input.keyboardAvailable = keyboardAvailable;
    }

    @Override
    public void exit(){
        handler.post(AndroidApplication.this::finish);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        // forward events to our listeners if there are any installed
        synchronized(eventListeners){
            if(eventListeners.containsKey(requestCode)){
                eventListeners.get(requestCode).onActivityResult(resultCode, data);
            }
        }

        if(data != null && data.getData() != null){
            String scheme = data.getData().getScheme();
            if(scheme.equals("file")){
                String fileName = data.getData().getEncodedPath();
                synchronized(listeners){
                    for(ApplicationListener list : listeners){
                        Core.app.post(() -> list.fileDropped(Core.files.absolute(fileName)));
                    }
                }
            }
        }
    }

    /** Adds an event listener for Android specific event such as onActivityResult(...). */
    public void addResultListener(Intc runner, AndroidEventListener listener){
        synchronized(eventListeners){
            int id = lastEventNumber++;
            eventListeners.put(id, listener);
            runner.get(id);
        }
    }

    @Override
    public Array<ApplicationListener> getListeners(){
        return listeners;
    }

    @Override
    public Context getContext(){
        return this;
    }

    @Override
    public Array<Runnable> getRunnables(){
        return runnables;
    }

    @Override
    public Array<Runnable> getExecutedRunnables(){
        return executedRunnables;
    }

    @Override
    public Window getApplicationWindow(){
        return this.getWindow();
    }

    @Override
    public Handler getHandler(){
        return this.handler;
    }

    /**
     * A listener for special Android events such onActivityResult(...). This can be used by e.g. extensions to plug into the Android
     * system.
     * @author noblemaster
     */
    public interface AndroidEventListener{

        /** Will be called if the application's onActivityResult(...) method is called. */
        void onActivityResult(int resultCode, Intent data);
    }
}

package arc.backend.robovm;

import arc.*;
import arc.func.*;
import arc.util.*;
import com.badlogic.gdx.backends.iosrobovm.bindings.metalangle.*;
import org.robovm.apple.uikit.*;

public class IOSApplicationConfiguration{
    /** whether to enable screen dimming. */
    public boolean preventScreenDimming = true;
    /** whether or not portrait orientation is supported. */
    public boolean orientationPortrait = true;
    /** whether or not landscape orientation is supported. */
    public boolean orientationLandscape = true;

    /** the color format, RGB565 is the default **/
    public MGLDrawableColorFormat colorFormat = MGLDrawableColorFormat.RGBA8888;

    /** the depth buffer format, Format16 is default **/
    public MGLDrawableDepthFormat depthFormat = MGLDrawableDepthFormat._16;

    /** the stencil buffer format, None is default **/
    public MGLDrawableStencilFormat stencilFormat = MGLDrawableStencilFormat.None;

    /** the multisample format, None is default **/
    public MGLDrawableMultisample multisample = MGLDrawableMultisample.None;

    /** number of frames per second, 60 is default **/
    public int preferredFramesPerSecond = 60;

    /** handles any errors in the main loop.*/
    @Nullable
    public Cons<Throwable> errorHandler;

    /** whether to use the accelerometer, default true **/
    public boolean useAccelerometer = true;
    /** the update interval to poll the accelerometer with, in seconds **/
    public float accelerometerUpdate = 0.05f;
    /** whether or not the onScreenKeyboard should be closed on return key **/
    public boolean keyboardCloseOnReturn = true;

    /**
     * Whether to enable OpenGL ES 3 if supported. If not supported it will fall-back to OpenGL ES 2.0.
     * When GLES3 is enabled, {@link Core#gl30} can be used to access its functionality.
     */
    public boolean useGL30 = false;

    /** whether the home indicator should be hidden or not **/
    public boolean hideHomeIndicator = true;

    /**
     * Edges where app gestures must be fired over system gestures.
     * Prior to iOS 11, UIRectEdge.All was default behaviour if status bar hidden, see #5110
     **/
    public UIRectEdge screenEdgesDeferringSystemGestures = UIRectEdge.None;
}

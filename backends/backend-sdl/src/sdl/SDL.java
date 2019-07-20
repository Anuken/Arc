package sdl;

import io.anuke.arc.util.*;

final public class SDL {
    /*JNI

    #include "SDL.h"

    */

    static {
        new SharedLibraryLoader().load("sdl-arc");
    }

    //core SDL methods

    public static int
    SDL_INIT_TIMER = SDL_INIT_TIMER(),
    SDL_INIT_AUDIO = SDL_INIT_AUDIO(),
    SDL_INIT_VIDEO = SDL_INIT_VIDEO(),
    SDL_INIT_JOYSTICK = SDL_INIT_JOYSTICK(),
    SDL_INIT_HAPTIC = SDL_INIT_HAPTIC(),
    SDL_INIT_GAMECONTROLLER = SDL_INIT_GAMECONTROLLER(),
    SDL_INIT_EVENTS = SDL_INIT_EVENTS(),
    SDL_INIT_SENSOR = SDL_INIT_SENSOR(),
    SDL_INIT_EVERYTHING = SDL_INIT_EVERYTHING();

    private static native int SDL_INIT_TIMER(); /* return SDL_INIT_TIMER;*/
    private static native int SDL_INIT_AUDIO(); /* return SDL_INIT_AUDIO; */
    private static native int SDL_INIT_VIDEO(); /* return SDL_INIT_VIDEO; */
    private static native int SDL_INIT_JOYSTICK(); /* return SDL_INIT_JOYSTICK; */
    private static native int SDL_INIT_HAPTIC(); /* return SDL_INIT_HAPTIC; */
    private static native int SDL_INIT_GAMECONTROLLER(); /* return SDL_INIT_GAMECONTROLLER; */
    private static native int SDL_INIT_EVENTS(); /* return SDL_INIT_EVENTS; */
    private static native int SDL_INIT_SENSOR(); /* return SDL_INIT_SENSOR; */
    private static native int SDL_INIT_EVERYTHING(); /* return SDL_INIT_EVERYTHING; */

    public static native int SDL_Init(int flags); /*
        return SDL_Init(flags);
    */

    public static native int SDL_InitSubSystem(int flags); /*
        return SDL_InitSubSystem(flags);
    */


    public static native void SDL_QuitSubSystem(int flags); /*
        SDL_QuitSubSystem(flags);
    */

    public static native int SDL_WasInit(int flags); /*
        return SDL_WasInit(flags);
    */

    public static native void SDL_Quit(); /*
        SDL_Quit();
    */

    public static native boolean SDL_SetHint(String name, String value); /*
       return (SDL_SetHint(name, value)==SDL_TRUE);
    */

    public static native String SDL_GetError(); /*
        return env->NewStringUTF(SDL_GetError());
    */

    public static native int SDL_SetClipboardText(String text); /*
        return SDL_SetClipboardText(text);
    */

    public static native String SDL_GetClipboardText(); /*
        return env->NewStringUTF(SDL_GetClipboardText());
    */

    //video-related methods

    public static final int
    SDL_WINDOW_FULLSCREEN = SDL_WINDOW_FULLSCREEN(),
    SDL_WINDOW_FULLSCREEN_DESKTOP = SDL_WINDOW_FULLSCREEN_DESKTOP(),
    SDL_WINDOW_OPENGL = SDL_WINDOW_OPENGL(),
    SDL_WINDOW_SHOWN = SDL_WINDOW_SHOWN(),
    SDL_WINDOW_HIDDEN = SDL_WINDOW_HIDDEN(),
    SDL_WINDOW_BORDERLESS = SDL_WINDOW_BORDERLESS(),
    SDL_WINDOW_RESIZABLE = SDL_WINDOW_RESIZABLE(),
    SDL_WINDOW_MINIMIZED = SDL_WINDOW_MINIMIZED(),
    SDL_WINDOW_MAXIMIZED = SDL_WINDOW_MAXIMIZED();

    private static native int SDL_WINDOW_FULLSCREEN(); /* return SDL_WINDOW_FULLSCREEN;*/
    private static native int SDL_WINDOW_FULLSCREEN_DESKTOP(); /* return SDL_WINDOW_FULLSCREEN_DESKTOP;*/
    private static native int SDL_WINDOW_OPENGL(); /* return SDL_WINDOW_OPENGL;*/
    private static native int SDL_WINDOW_SHOWN(); /* return SDL_WINDOW_SHOWN;*/
    private static native int SDL_WINDOW_HIDDEN(); /* return SDL_WINDOW_HIDDEN;*/
    private static native int SDL_WINDOW_BORDERLESS(); /* return SDL_WINDOW_BORDERLESS;*/
    private static native int SDL_WINDOW_RESIZABLE(); /* return SDL_WINDOW_RESIZABLE;*/
    private static native int SDL_WINDOW_MINIMIZED(); /* return SDL_WINDOW_MINIMIZED;*/
    private static native int SDL_WINDOW_MAXIMIZED(); /* return SDL_WINDOW_MAXIMIZED;*/

    public static final int
    SDL_WINDOWEVENT_SHOWN = SDL_WINDOWEVENT_SHOWN(),
    SDL_WINDOWEVENT_HIDDEN = SDL_WINDOWEVENT_HIDDEN(),
    SDL_WINDOWEVENT_RESIZED = SDL_WINDOWEVENT_RESIZED(),
    SDL_WINDOWEVENT_SIZE_CHANGED = SDL_WINDOWEVENT_SIZE_CHANGED(),
    SDL_WINDOWEVENT_MINIMIZED = SDL_WINDOWEVENT_MINIMIZED(),
    SDL_WINDOWEVENT_MAXIMIZED = SDL_WINDOWEVENT_MAXIMIZED(),
    SDL_WINDOWEVENT_RESTORED = SDL_WINDOWEVENT_RESTORED();

    private static native int SDL_WINDOWEVENT_SHOWN(); /* return SDL_WINDOWEVENT_SHOWN;*/
    private static native int SDL_WINDOWEVENT_HIDDEN(); /* return SDL_WINDOWEVENT_HIDDEN;*/
    private static native int SDL_WINDOWEVENT_RESIZED(); /* return SDL_WINDOWEVENT_RESIZED;*/
    private static native int SDL_WINDOWEVENT_SIZE_CHANGED(); /* return SDL_WINDOWEVENT_SIZE_CHANGED;*/
    private static native int SDL_WINDOWEVENT_MINIMIZED(); /* return SDL_WINDOWEVENT_MINIMIZED;*/
    private static native int SDL_WINDOWEVENT_MAXIMIZED(); /* return SDL_WINDOWEVENT_MAXIMIZED;*/
    private static native int SDL_WINDOWEVENT_RESTORED(); /* return SDL_WINDOWEVENT_RESTORED;*/


    public static native long SDL_CreateWindow(String title, int w, int h, int flags); /*
        return (jlong)SDL_CreateWindow(title, SDL_WINDOWPOS_UNDEFINED, SDL_WINDOWPOS_UNDEFINED, w, h, flags);
    */

    public static native void SDL_DestroyWindow(long handle); /*
        SDL_DestroyWindow((SDL_Window*)handle);
    */

    public static native int SDL_SetWindowFullscreen(long handle, int flags); /*
        return SDL_SetWindowFullscreen((SDL_Window*)handle, flags);
    */

    public static native void SDL_SetWindowTitle(long handle, String title); /*
        SDL_SetWindowTitle((SDL_Window*)handle, title);
    */

    public static final int
    SDL_BUTTON_LEFT = SDL_BUTTON_LEFT(),
    SDL_BUTTON_MIDDLE = SDL_BUTTON_MIDDLE(),
    SDL_BUTTON_RIGHT = SDL_BUTTON_RIGHT();

    private static native int SDL_BUTTON_LEFT(); /* return SDL_BUTTON_LEFT;*/
    private static native int SDL_BUTTON_MIDDLE(); /* return SDL_BUTTON_MIDDLE;*/
    private static native int SDL_BUTTON_RIGHT(); /* return SDL_BUTTON_RIGHT;*/

    public static final int
    SDL_EVENT_QUIT = 0,
    SDL_EVENT_WINDOW = 1,
    SDL_EVENT_MOUSE_MOTION = 2,
    SDL_EVENT_MOUSE_BUTTON = 3,
    SDL_EVENT_MOUSE_WHEEL = 4,
    SDL_EVENT_KEYBOARD = 5,
    SDL_EVENT_OTHER = 6;

    /** Since passing in or returning a class here would be a pain, I have to resort to an int array.
     * @return whether or not the event was processed.
     * If true is returned, the input data array is filled with the event data.*/
    public static native boolean SDL_PollEvent(int[] data); /*
        SDL_Event e;
        if(SDL_PollEvent(&e)){
            switch(e.type){
                case SDL_QUIT:
                    data[0] = 0;
                    break;
                case SDL_WINDOWEVENT:
                    data[0] = 1;
                    data[1] = e.window.event;
                    data[2] = e.window.data1;
                    data[3] = e.window.data2;
                    break;
                case SDL_MOUSEMOTION:
                    data[0] = 2;
                    data[1] = e.motion.x;
                    data[2] = e.motion.y;
                    break;
                case SDL_MOUSEBUTTONDOWN:
                case SDL_MOUSEBUTTONUP:
                    data[0] = 3;
                    data[1] = (e.type == SDL_MOUSEBUTTONDOWN);
                    data[2] = e.button.x;
                    data[3] = e.button.y;
                    data[4] = e.button.button;
                    break;
                case SDL_MOUSEWHEEL:
                    data[0] = 4;
                    data[1] = e.wheel.x;
                    data[2] = e.wheel.y;
                    break;
                case SDL_KEYDOWN:
                case SDL_KEYUP:
                    data[0] = 5;
                    data[1] = (e.type == SDL_KEYDOWN);
                    data[2] = e.key.keysym.sym;
                    data[3] = e.key.repeat;
                    break;
                default:
                    data[0] = 6;
                    break;
            }
            return 1;
        }
        return 0;
    */


    //openGL stuff

    public static final int
    SDL_GL_RED_SIZE = SDL_GL_RED_SIZE(),
    SDL_GL_GREEN_SIZE = SDL_GL_GREEN_SIZE(),
    SDL_GL_BLUE_SIZE = SDL_GL_BLUE_SIZE(),
    SDL_GL_DEPTH_SIZE = SDL_GL_DEPTH_SIZE(),
    SDL_GL_DOUBLEBUFFER = SDL_GL_DOUBLEBUFFER(),
    SDL_GL_CONTEXT_MAJOR_VERSION = SDL_GL_CONTEXT_MAJOR_VERSION(),
    SDL_GL_CONTEXT_MINOR_VERSION = SDL_GL_CONTEXT_MINOR_VERSION();

    private static native int SDL_GL_RED_SIZE(); /* return SDL_GL_RED_SIZE; */
    private static native int SDL_GL_GREEN_SIZE(); /* return SDL_GL_GREEN_SIZE; */
    private static native int SDL_GL_BLUE_SIZE(); /* return SDL_GL_BLUE_SIZE; */
    private static native int SDL_GL_DEPTH_SIZE(); /* return SDL_GL_DEPTH_SIZE; */
    private static native int SDL_GL_DOUBLEBUFFER(); /* return SDL_GL_DOUBLEBUFFER; */
    private static native int SDL_GL_CONTEXT_MAJOR_VERSION(); /* return SDL_GL_CONTEXT_MAJOR_VERSION; */
    private static native int SDL_GL_CONTEXT_MINOR_VERSION(); /* return SDL_GL_CONTEXT_MINOR_VERSION; */

    public static native int SDL_GL_SetAttribute(int attribute, int value); /*
        return SDL_GL_SetAttribute((SDL_GLattr)attribute, value);
    */

    public static native long SDL_GL_CreateContext(long window); /*
        return (jlong)SDL_GL_CreateContext((SDL_Window*)window);
    */

    public static native int SDL_GL_SetSwapInterval(int on); /*
        return SDL_GL_SetSwapInterval(on);
    */

    public static native void SDL_GL_SwapWindow(long window); /*
        SDL_GL_SwapWindow((SDL_Window*)window);
    */
}

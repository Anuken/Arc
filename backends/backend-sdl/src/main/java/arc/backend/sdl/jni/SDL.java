package arc.backend.sdl.jni;

import arc.util.*;

import java.io.*;
import java.nio.*;

final public class SDL{
    /*JNI

    #ifdef __APPLE__

    #include "SDL2/SDL.h"

    #else

    #include "SDL.h"

    #endif

    */

    static{
        if(OS.isWindows){
            new SharedLibraryLoader(){
                @Override
                public String mapLibraryName(String libraryName){
                    return libraryName + ".dll";
                }

                @Override
                protected InputStream readFile(String path){
                    return super.readFile(OS.is64Bit ? "OpenAL.dll" : "OpenAL32.dll");
                }
            }.load("OpenAL32");
        }else if(OS.isLinux){
            new SharedLibraryLoader(){
                @Override public String mapLibraryName(String libraryName){ return "lib" +libraryName + ".so"; }
            }.load("openal");
        }
        new SharedLibraryLoader(){
            @Override
            protected Throwable loadFile(String sourcePath, String sourceCrc, File extractedFile){
                if(OS.isWindows){
                    try{
                        extractFile(OS.is64Bit ? "OpenAL.dll" : "OpenAL32.dll", sourceCrc,
                        new File(extractedFile.getParentFile() == null ? "OpenAL32.dll" : (extractedFile.getParentFile() + "/OpenAL32.dll")));
                    }catch(Throwable ignored){
                    }
                }
                return super.loadFile(sourcePath, sourceCrc, extractedFile);
            }
        }.load("sdl-arc");
    }

    //core SDL methods
    public static final int
    SDL_INIT_TIMER = 0x00000001,
    SDL_INIT_AUDIO = 0x00000010,
    SDL_INIT_VIDEO = 0x00000020,
    SDL_INIT_JOYSTICK = 0x00000200,
    SDL_INIT_HAPTIC = 0x00001000,
    SDL_INIT_GAMECONTROLLER = 0x00002000,
    SDL_INIT_EVENTS = 0x00004000,
    SDL_INIT_NOPARACHUTE = 0x00100000,
    SDL_INIT_EVERYTHING = SDL_INIT_TIMER | SDL_INIT_AUDIO | SDL_INIT_VIDEO | SDL_INIT_EVENTS | SDL_INIT_JOYSTICK | SDL_INIT_HAPTIC | SDL_INIT_GAMECONTROLLER;

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
    SDL_WINDOW_FULLSCREEN = 0x00000001,         /**< fullscreen window */
    SDL_WINDOW_OPENGL = 0x00000002,             /**< window usable with OpenGL context */
    SDL_WINDOW_SHOWN = 0x00000004,              /**< window is visible */
    SDL_WINDOW_HIDDEN = 0x00000008,             /**< window is not visible */
    SDL_WINDOW_BORDERLESS = 0x00000010,         /**< no window decoration */
    SDL_WINDOW_RESIZABLE = 0x00000020,          /**< window can be resized */
    SDL_WINDOW_MINIMIZED = 0x00000040,          /**< window is minimized */
    SDL_WINDOW_MAXIMIZED = 0x00000080,          /**< window is maximized */
    SDL_WINDOW_INPUT_GRABBED = 0x00000100,      /**< window has grabbed input focus */
    SDL_WINDOW_INPUT_FOCUS = 0x00000200,        /**< window has input focus */
    SDL_WINDOW_MOUSE_FOCUS = 0x00000400,        /**< window has mouse focus */
    SDL_WINDOW_FULLSCREEN_DESKTOP = (SDL_WINDOW_FULLSCREEN | 0x00001000),
    SDL_WINDOW_FOREIGN = 0x00000800,            /**< window not created by SDL */
    SDL_WINDOW_ALLOW_HIGHDPI = 0x00002000,      /**< window should be created in high-DPI mode if supported */
    SDL_WINDOW_MOUSE_CAPTURE = 0x00004000;       /**< window has mouse captured (unrelated to INPUT_GRABBED) */

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

    public static final int
    SDL_SYSTEM_CURSOR_ARROW = SDL_SYSTEM_CURSOR_ARROW(),
    SDL_SYSTEM_CURSOR_IBEAM = SDL_SYSTEM_CURSOR_IBEAM(),
    SDL_SYSTEM_CURSOR_WAIT = SDL_SYSTEM_CURSOR_WAIT(),
    SDL_SYSTEM_CURSOR_CROSSHAIR = SDL_SYSTEM_CURSOR_CROSSHAIR(),
    SDL_SYSTEM_CURSOR_WAITARROW = SDL_SYSTEM_CURSOR_WAITARROW(),
    SDL_SYSTEM_CURSOR_SIZEALL = SDL_SYSTEM_CURSOR_SIZEALL(),
    SDL_SYSTEM_CURSOR_NO = SDL_SYSTEM_CURSOR_NO(),
    SDL_SYSTEM_CURSOR_SIZENS = SDL_SYSTEM_CURSOR_SIZENS(),
    SDL_SYSTEM_CURSOR_SIZEWE = SDL_SYSTEM_CURSOR_SIZEWE(),
    SDL_SYSTEM_CURSOR_HAND = SDL_SYSTEM_CURSOR_HAND();

    private static native int SDL_SYSTEM_CURSOR_ARROW(); /* return SDL_SYSTEM_CURSOR_ARROW;*/
    private static native int SDL_SYSTEM_CURSOR_IBEAM(); /* return SDL_SYSTEM_CURSOR_IBEAM;*/
    private static native int SDL_SYSTEM_CURSOR_WAIT(); /* return SDL_SYSTEM_CURSOR_WAIT;*/
    private static native int SDL_SYSTEM_CURSOR_CROSSHAIR(); /* return SDL_SYSTEM_CURSOR_CROSSHAIR;*/
    private static native int SDL_SYSTEM_CURSOR_WAITARROW(); /* return SDL_SYSTEM_CURSOR_WAITARROW;*/
    private static native int SDL_SYSTEM_CURSOR_SIZEALL(); /* return SDL_SYSTEM_CURSOR_SIZEALL;*/
    private static native int SDL_SYSTEM_CURSOR_NO(); /* return SDL_SYSTEM_CURSOR_NO;*/
    private static native int SDL_SYSTEM_CURSOR_SIZENS(); /* return SDL_SYSTEM_CURSOR_SIZENS;*/
    private static native int SDL_SYSTEM_CURSOR_SIZEWE(); /* return SDL_SYSTEM_CURSOR_SIZEWE;*/
    private static native int SDL_SYSTEM_CURSOR_HAND(); /* return SDL_SYSTEM_CURSOR_HAND;*/


    public static native long SDL_CreateWindow(String title, int w, int h, int flags); /*
        return (jlong)SDL_CreateWindow(title, SDL_WINDOWPOS_UNDEFINED, SDL_WINDOWPOS_UNDEFINED, w, h, flags);
    */

    public static native void SDL_DestroyWindow(long handle); /*
        SDL_DestroyWindow((SDL_Window*)handle);
    */

    public static native void SDL_SetWindowIcon(long handle, long surface); /*
        SDL_SetWindowIcon((SDL_Window*)handle, (SDL_Surface*)surface);
    */

    public static native void SDL_RestoreWindow(long handle); /*
        SDL_RestoreWindow((SDL_Window*)handle);
    */

    public static native void SDL_MaximizeWindow(long handle); /*
        SDL_MaximizeWindow((SDL_Window*)handle);
    */

    public static native void SDL_MinimizeWindow(long handle); /*
        SDL_MinimizeWindow((SDL_Window*)handle);
    */

    public static native int SDL_SetWindowFullscreen(long handle, int flags); /*
        return SDL_SetWindowFullscreen((SDL_Window*)handle, flags);
    */

    public static native void SDL_SetWindowBordered(long handle, boolean bordered); /*
        SDL_SetWindowBordered((SDL_Window*)handle, (SDL_bool)bordered);
    */

    public static native void SDL_SetWindowSize(long handle, int w, int h); /*
        SDL_SetWindowSize((SDL_Window*)handle, w, h);
    */

    public static native int SDL_GetWindowFlags(long handle); /*
        return SDL_GetWindowFlags((SDL_Window*)handle);
    */

    public static native void SDL_SetWindowTitle(long handle, String title); /*
        SDL_SetWindowTitle((SDL_Window*)handle, title);
    */

    //expects RGBA format of bytes.
    public static native long SDL_CreateRGBSurfaceFrom(ByteBuffer bytes, int width, int height); /*
        return (jlong)SDL_CreateRGBSurfaceFrom(bytes, width, height, 32, 4 * width, 0x000000ff, 0x0000ff00, 0x00ff0000, 0xff000000);
    */

    public static native long SDL_CreateColorCursor(long surface, int hotx, int hoty); /*
        return (jlong)SDL_CreateColorCursor((SDL_Surface*)surface, hotx, hoty);
    */

    public static native long SDL_CreateSystemCursor(int type); /*
        return (jlong)SDL_CreateSystemCursor((SDL_SystemCursor)type);
    */

    public static native void SDL_SetCursor(long handle); /*
        SDL_SetCursor((SDL_Cursor*)handle);
    */

    public static native void SDL_FreeCursor(long handle); /*
        SDL_FreeCursor((SDL_Cursor*)handle);
    */

    public static native void SDL_FreeSurface(long handle); /*
        SDL_FreeSurface((SDL_Surface*)handle);
     */

    public static native int SDL_ShowSimpleMessageBox(int flags, String title, String message); /*
        return SDL_ShowSimpleMessageBox(flags, title, message, NULL);
    */

    public static native void SDL_StartTextInput(); /*
        SDL_StartTextInput();
    */

    public static native void SDL_StopTextInput(); /*
        SDL_StopTextInput();
    */

    public static final int
    SDL_MESSAGEBOX_ERROR = 0x00000010,   /**< error dialog */
    SDL_MESSAGEBOX_WARNING = 0x00000020,   /**< warning dialog */
    SDL_MESSAGEBOX_INFORMATION = 0x00000040;    /**< informational dialog */

    public static final int
    SDL_BUTTON_LEFT = 1,
    SDL_BUTTON_MIDDLE = 2,
    SDL_BUTTON_RIGHT = 3,
    SDL_BUTTON_X1 = 4,
    SDL_BUTTON_X2 = 5;

    public static final int
    SDL_EVENT_QUIT = 0,
    SDL_EVENT_WINDOW = 1,
    SDL_EVENT_MOUSE_MOTION = 2,
    SDL_EVENT_MOUSE_BUTTON = 3,
    SDL_EVENT_MOUSE_WHEEL = 4,
    SDL_EVENT_KEYBOARD = 5,
    SDL_EVENT_TEXT_INPUT = 6,
    SDL_EVENT_OTHER = 7;

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
                    data[4] = e.key.keysym.scancode;
                    break;
                case SDL_TEXTINPUT:
                    data[0] = 6;
                    for(int i = 0; i < 32; i ++){
                        data[i + 1] = e.text.text[i];
                        if(e.text.text[i] == '\0'){
                            break;
                        }
                    }
                    break;
                default:
                    data[0] = 7;
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

    public static native boolean SDL_GL_ExtensionSupported(String exte); /*
        return SDL_GL_ExtensionSupported(exte);
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

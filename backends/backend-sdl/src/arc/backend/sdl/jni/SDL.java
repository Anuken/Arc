package arc.backend.sdl.jni;

import arc.util.*;

import java.io.*;
import java.nio.*;

public class SDL{
    /*JNI

    #ifdef __APPLE__

    #include "SDL2/SDL.h"

    #else

    #include "SDL.h"

    #endif

    */

    public static final int

    SDL_INIT_TIMER = 0x00000001,
    SDL_INIT_AUDIO = 0x00000010,
    SDL_INIT_VIDEO = 0x00000020,
    SDL_INIT_JOYSTICK = 0x00000200,
    SDL_INIT_HAPTIC = 0x00001000,
    SDL_INIT_GAMECONTROLLER = 0x00002000,
    SDL_INIT_EVENTS = 0x00004000,
    SDL_INIT_NOPARACHUTE = 0x00100000,
    SDL_INIT_EVERYTHING = SDL_INIT_TIMER | SDL_INIT_AUDIO | SDL_INIT_VIDEO | SDL_INIT_EVENTS | SDL_INIT_JOYSTICK | SDL_INIT_HAPTIC | SDL_INIT_GAMECONTROLLER,

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
    SDL_WINDOW_MOUSE_CAPTURE = 0x00004000,       /**< window has mouse captured (unrelated to INPUT_GRABBED) */

    SDL_WINDOWEVENT_NONE = 0,
    SDL_WINDOWEVENT_SHOWN = 1,
    SDL_WINDOWEVENT_HIDDEN = 2,
    SDL_WINDOWEVENT_EXPOSED = 3,
    SDL_WINDOWEVENT_MOVED = 4,
    SDL_WINDOWEVENT_RESIZED = 5,
    SDL_WINDOWEVENT_SIZE_CHANGED = 6,
    SDL_WINDOWEVENT_MINIMIZED = 7,
    SDL_WINDOWEVENT_MAXIMIZED = 8,
    SDL_WINDOWEVENT_RESTORED = 9,
    SDL_WINDOWEVENT_ENTER = 10,
    SDL_WINDOWEVENT_LEAVE = 11,
    SDL_WINDOWEVENT_FOCUS_GAINED = 12,
    SDL_WINDOWEVENT_FOCUS_LOST = 13,
    SDL_WINDOWEVENT_CLOSE = 14,

    SDL_SYSTEM_CURSOR_ARROW = 0,
    SDL_SYSTEM_CURSOR_IBEAM = 1,
    SDL_SYSTEM_CURSOR_WAIT = 2,
    SDL_SYSTEM_CURSOR_CROSSHAIR = 3,
    SDL_SYSTEM_CURSOR_WAITARROW = 4,
    SDL_SYSTEM_CURSOR_SIZENWSE = 5,
    SDL_SYSTEM_CURSOR_SIZENESW = 6,
    SDL_SYSTEM_CURSOR_SIZEWE = 7,
    SDL_SYSTEM_CURSOR_SIZENS = 8,
    SDL_SYSTEM_CURSOR_SIZEALL = 9,
    SDL_SYSTEM_CURSOR_NO = 10,
    SDL_SYSTEM_CURSOR_HAND = 11,
    SDL_NUM_SYSTEM_CURSORS = 12,

    SDL_MESSAGEBOX_ERROR = 0x00000010,
    SDL_MESSAGEBOX_WARNING = 0x00000020,
    SDL_MESSAGEBOX_INFORMATION = 0x00000040,

    SDL_BUTTON_LEFT = 1,
    SDL_BUTTON_MIDDLE = 2,
    SDL_BUTTON_RIGHT = 3,
    SDL_BUTTON_X1 = 4,
    SDL_BUTTON_X2 = 5,

    //fake constants, not part of SDL, just my wrapper
    SDL_EVENT_QUIT = 0,
    SDL_EVENT_WINDOW = 1,
    SDL_EVENT_MOUSE_MOTION = 2,
    SDL_EVENT_MOUSE_BUTTON = 3,
    SDL_EVENT_MOUSE_WHEEL = 4,
    SDL_EVENT_TOUCH = 5,
    SDL_EVENT_KEYBOARD = 6,
    SDL_EVENT_TEXT_INPUT = 7,
    SDL_EVENT_TEXT_EDIT = 9,
    SDL_EVENT_OTHER = 8,

    SDL_GL_RED_SIZE = 0,
    SDL_GL_GREEN_SIZE = 1,
    SDL_GL_BLUE_SIZE = 2,
    SDL_GL_ALPHA_SIZE = 3,
    SDL_GL_BUFFER_SIZE = 4,
    SDL_GL_DOUBLEBUFFER = 5,
    SDL_GL_DEPTH_SIZE = 6,
    SDL_GL_STENCIL_SIZE = 7,
    SDL_GL_CONTEXT_MAJOR_VERSION = 17,
    SDL_GL_CONTEXT_MINOR_VERSION = 18,
    SDL_GL_MULTISAMPLEBUFFERS = 13,
    SDL_GL_MULTISAMPLESAMPLES = 14,
    SDL_GL_CONTEXT_PROFILE_CORE = 1,
    SDL_GL_CONTEXT_PROFILE_MASK = 21,
    SDL_GL_CONTEXT_FLAGS = 20

    ;

    static{
        new SharedLibraryLoader(){
            @Override
            protected Throwable loadFile(String sourcePath, String sourceCrc, File extractedFile){
                //skip dynamic load for me because it crashes otherwise
                if(OS.isLinux && !OS.hasProp("SDL2_STATIC") && !OS.username.equals("anuke")){
                    //on linux, the SDL shared library isn't statically linked, try to load it first
                    try{
                        String name = "libSDL2.so";
                        File result = new File(extractedFile.getParentFile() == null ? name : (extractedFile.getParentFile() + "/" + name));
                        extractFile(name, crc(readFile(name)), result);
                        System.load(result.getAbsolutePath());
                    }catch(Throwable ignored){
                    }
                }
                return super.loadFile(sourcePath, sourceCrc, extractedFile);
            }
        }.load("sdl-arc");
    }

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

    public static native void SDL_GetCompiledVersion(int[] values); /*
        SDL_version compiled;
        SDL_VERSION(&compiled);
        values[0] = compiled.major;
        values[1] = compiled.minor;
        values[2] = compiled.patch;
    */

    public static native void SDL_GetVersion(int[] values); /*
        SDL_version compiled;
        SDL_GetVersion(&compiled);
        values[0] = compiled.major;
        values[1] = compiled.minor;
        values[2] = compiled.patch;
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

    public static native void SDL_SetWindowPosition(long handle, int x, int y); /*
        SDL_SetWindowPosition((SDL_Window*)handle, x, y);
    */

    public static native int SDL_GetWindowDisplayIndex(long handle); /*
        return SDL_GetWindowDisplayIndex((SDL_Window*)handle);
    */

    public static native int SDL_GetDisplayUsableBounds(int display, int[] xywh); /*
        SDL_Rect bounds;
        int result = SDL_GetDisplayUsableBounds(display, &bounds);

        xywh[0] = bounds.x;
        xywh[1] = bounds.y;
        xywh[2] = bounds.w;
        xywh[3] = bounds.h;

        return result;
    */

    public static native int SDL_GetDisplayBounds(int display, int[] xywh); /*
        SDL_Rect bounds;
        int result = SDL_GetDisplayBounds(display, &bounds);

        xywh[0] = bounds.x;
        xywh[1] = bounds.y;
        xywh[2] = bounds.w;
        xywh[3] = bounds.h;

        return result;
    */

    public static native int SDL_GetNumVideoDisplays(); /*
        return SDL_GetNumVideoDisplays();
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

    public static native void SDL_SetTextInputRect(int x, int y, int w, int h); /*
        SDL_Rect rect;
        rect.x = x;
        rect.y = y;
        rect.w = w;
        rect.h = h;
        SDL_SetTextInputRect(&rect);
    */

    private static native boolean SDL_IsTextInputActive(); /*
        return (jboolean)SDL_IsTextInputActive();
    */

    /** Since passing in or returning a class here would be a pain, I have to resort to an int array.
     * @return whether the event was processed.
     * If true is returned, the input data array is filled with the event data.*/
    public static native boolean SDL_PollEvent(int[] data, int width, int height); /*
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
                    if(e.motion.which == SDL_TOUCH_MOUSEID) break;
                    data[0] = 2;
                    data[1] = e.motion.x;
                    data[2] = (height - e.motion.y);
                    break;
                case SDL_MOUSEBUTTONDOWN:
                case SDL_MOUSEBUTTONUP:
                    if(e.button.which == SDL_TOUCH_MOUSEID) break;
                    data[0] = 3;
                    data[1] = (e.type == SDL_MOUSEBUTTONDOWN);
                    data[2] = e.button.x;
                    data[3] = (height - e.button.y);
                    data[4] = e.button.button;
                    break;
                case SDL_MOUSEWHEEL:
                    if(e.wheel.which == SDL_TOUCH_MOUSEID) break;
                    data[0] = 4;
                    data[1] = e.wheel.x;
                    data[2] = e.wheel.y;
                    break;
                case SDL_FINGERMOTION:
                case SDL_FINGERDOWN:
                case SDL_FINGERUP:
                    data[0] = 5;
                    // everything relating to touch (sans gestures) in SDL2 are under one data field,
                    // this mirrors that rather than splitting them apart.
                    // 0 - finger motion, 1 - finger down, 2 - finger up
                    if(e.type == SDL_FINGERMOTION){ data[1] = 0; }
                    else if(e.type == SDL_FINGERDOWN){ data[1] = 1; }
                    else { data[1] = 2; }
                    data[2] = e.tfinger.x * width;
                    data[3] = height - (e.tfinger.y * height);
                    data[4] = e.tfinger.dx;
                    data[5] = e.tfinger.dy;
                    data[6] = e.tfinger.pressure;
                    data[7] = e.tfinger.touchId;
                    data[8] = e.tfinger.fingerId;
                    break;
                case SDL_KEYDOWN:
                case SDL_KEYUP:
                    data[0] = 6;
                    data[1] = (e.type == SDL_KEYDOWN);
                    data[2] = e.key.keysym.sym;
                    data[3] = e.key.repeat;
                    data[4] = e.key.keysym.scancode;
                    data[5] = e.key.keysym.mod;
                    data[6] = e.key.timestamp;
                    break;
                case SDL_TEXTINPUT:
                    data[0] = 7;
                    for(int i = 0; i < 32; i ++){
                        data[i + 1] = e.text.text[i];
                        if(e.text.text[i] == '\0'){
                            break;
                        }
                    }
                    break;
                case SDL_TEXTEDITING:
                    data[0] = 9;
                    data[1] = e.edit.start;
                    data[2] = e.edit.length;
                    for(int i = 0; i < 32; i ++){
                        data[i + 3] = e.edit.text[i];
                        if(e.edit.text[i] == '\0'){
                            break;
                        }
                    }

                    break;
                default:
                    data[0] = 8;
                    break;
            }
            return 1;
        }
        return 0;
    */

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

    public static native void SDL_GL_GetDrawableSize(long window, int[] values); /*
        int w, h;
        SDL_GL_GetDrawableSize((SDL_Window*)window, &w, &h);
        values[0] = w;
        values[1] = h;
    */
}

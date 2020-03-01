package arc.backend.sdl;

import arc.backend.sdl.jni.SDL;

public class SDLError extends RuntimeException{
    public SDLError() {
        super(SDL.SDL_GetError());
    }
}

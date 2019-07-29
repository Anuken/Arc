package sdl;

public class SDLMixer{
    /*JNI

    #include "SDL_mixer.h"
    #include "SDL.h"

     */

    public static native long Mix_LoadWAVBytes(byte[] bytes, int length); /*
        return (jlong)Mix_LoadWAV("shoot.ogg");
    */

    public static native int Mix_PlayChannel(int channel, long handle, int loops); /*
        return Mix_PlayChannel(channel, (Mix_Chunk*)handle, loops);
    */

    public static native int Mix_Volume(int channel, int volume); /*
        return Mix_Volume(channel, volume);
     */

    public static native int Mix_SetPanning(int channel, int left, int right); /*
        return Mix_SetPanning(channel, (Uint8)left, (Uint8)right);
     */

    public static native void Mix_FreeChunk(long handle); /*
        Mix_FreeChunk((Mix_Chunk*)handle);
     */

    public static native long Mix_LoadMusicBytes(String path); /*
        return (jlong)Mix_LoadMUS(path);
    */

    public static native void Mix_FreeMusic(long handle); /*
        Mix_FreeMusic((Mix_Music*)handle);
     */

    public static native int Mix_OpenAudio(int frequency, int channels, int chunksize); /*
        return Mix_OpenAudio(frequency, MIX_DEFAULT_FORMAT, channels, chunksize);
    */

    public static native int Mix_Init(); /*
        return Mix_Init(MIX_INIT_OGG | MIX_INIT_MP3);
    */

    public static native void Mix_Quit(); /* Mix_Quit(); */

    public static native void Mix_CloseAudio(); /* Mix_CloseAudio(); */
}

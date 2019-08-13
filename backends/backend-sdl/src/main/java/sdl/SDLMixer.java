package sdl;

public class SDLMixer{
    /*JNI

    #include "SDL_mixer.h"
    #include "SDL.h"

    static jmethodID runid = 0;
    static jobject callback = 0;
    static JavaVM* staticVM;

    void musicDoneCallback(){
        JNIEnv * g_env;
        int getEnvStat = staticVM->GetEnv((void **)&g_env, JNI_VERSION_1_6);
        if(getEnvStat == JNI_EDETACHED){
            if(staticVM->AttachCurrentThread((void **) &g_env, NULL) != 0){
                printf("Failed to attach\n");
            }
        }else if(getEnvStat == JNI_OK){
        }else if(getEnvStat == JNI_EVERSION){
            printf("GetEnv: version not supported\n");
        }

        if(callback){
            g_env->CallVoidMethod(callback, runid);
        }

        staticVM->DetachCurrentThread();
    }

     */

    static{
        initJNI();
    }

    private static native void initJNI(); /*
        env->GetJavaVM(&staticVM);
        jclass exception = env->FindClass("java/lang/Exception");
        jclass runClass = env->FindClass("java/lang/Runnable");

        runid = env->GetMethodID(runClass, "run", "()V");

        if(!runClass) {
			env->ThrowNew(exception, "Couldn't find Runnable() class");
		}

        if(!runid) {
			env->ThrowNew(exception, "Couldn't find run() method");
		}
    */

    public static native long loadWAV(String path); /*
        return (jlong)Mix_LoadWAV(path);
    */

    public static native int playChannel(int channel, long handle, int loops); /*
        return Mix_PlayChannel(channel, (Mix_Chunk*)handle, loops);
    */

    public static native int haltChannel(int channel); /*
        return Mix_HaltChannel(channel);
    */

    public static native int volume(int channel, int volume); /*
        return Mix_Volume(channel, volume);
     */

    public static native int volumeChunk(long chunk, int volume); /*
        return Mix_VolumeChunk((Mix_Chunk*)chunk, volume);
    */

    public static native int setPanning(int channel, int left, int right); /*
        return Mix_SetPanning(channel, (Uint8)left, (Uint8)right);
     */

    public static native void freeChunk(long handle); /*
        Mix_FreeChunk((Mix_Chunk*)handle);
     */

    public static native long loadMusic(String path); /*
        return (jlong)Mix_LoadMUS(path);
    */

    public static native void hookMusicFinished(Runnable jcallback); /*
        if(callback){
			env->DeleteGlobalRef(callback);
			callback = 0;
		}
		if(jcallback) callback = env->NewGlobalRef(jcallback);
		Mix_HookMusicFinished(musicDoneCallback);
    */

    public static native int playMusic(long handle, int loops); /*
        return Mix_PlayMusic((Mix_Music*)handle, loops);
    */

    public static native boolean playingMusic(); /*
        return Mix_PlayingMusic();
    */

    public static native void pauseMusic(); /*
        Mix_PauseMusic();
    */

    public static native void volumeMusic(int volume); /*
        Mix_VolumeMusic(volume);
    */

    public static native void haltMusic(); /*
        Mix_HaltMusic();
    */

    public static native void setMusicPosition(double position); /*
        Mix_SetMusicPosition(position);
    */

    public static native void rewindMusic(); /*
        Mix_RewindMusic();
    */

    public static native void freeMusic(long handle); /*
        Mix_FreeMusic((Mix_Music*)handle);
     */

    public static native void allocateChannels(int channels); /*
        Mix_AllocateChannels(channels);
    */

    public static native int openAudio(int frequency, int channels, int chunksize); /*
        return Mix_OpenAudio(frequency, MIX_DEFAULT_FORMAT, channels, chunksize);
    */

    public static native int init(); /*
        return Mix_Init(MIX_INIT_OGG | MIX_INIT_MP3);
    */

    public static native void quit(); /* Mix_Quit(); */

    public static native void closeAudio(); /* Mix_CloseAudio(); */
}

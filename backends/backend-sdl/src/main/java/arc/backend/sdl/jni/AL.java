package arc.backend.sdl.jni;

import java.nio.*;

public class AL{

    /*JNI

    #ifdef __APPLE__

    #include "OpenAL/al.h"
    #include "OpenAL/alc.h"

    #else

    #include "AL/al.h"
    #include "AL/alc.h"

    #endif

     */

    //al
    public static final int AL_INVALID = -1;
    public static final int AL_NONE = 0;
    public static final int AL_FALSE = 0;
    public static final int AL_TRUE = 1;
    public static final int AL_SOURCE_RELATIVE = 514;
    public static final int AL_CONE_INNER_ANGLE = 4097;
    public static final int AL_CONE_OUTER_ANGLE = 4098;
    public static final int AL_PITCH = 4099;
    public static final int AL_POSITION = 4100;
    public static final int AL_DIRECTION = 4101;
    public static final int AL_VELOCITY = 4102;
    public static final int AL_LOOPING = 4103;
    public static final int AL_BUFFER = 4105;
    public static final int AL_GAIN = 4106;
    public static final int AL_MIN_GAIN = 4109;
    public static final int AL_MAX_GAIN = 4110;
    public static final int AL_ORIENTATION = 4111;
    public static final int AL_SOURCE_STATE = 4112;
    public static final int AL_INITIAL = 4113;
    public static final int AL_PLAYING = 4114;
    public static final int AL_PAUSED = 4115;
    public static final int AL_STOPPED = 4116;
    public static final int AL_BUFFERS_QUEUED = 4117;
    public static final int AL_BUFFERS_PROCESSED = 4118;
    public static final int AL_REFERENCE_DISTANCE = 4128;
    public static final int AL_ROLLOFF_FACTOR = 4129;
    public static final int AL_CONE_OUTER_GAIN = 4130;
    public static final int AL_MAX_DISTANCE = 4131;
    public static final int AL_SEC_OFFSET = 4132;
    public static final int AL_SAMPLE_OFFSET = 4133;
    public static final int AL_BYTE_OFFSET = 4134;
    public static final int AL_SOURCE_TYPE = 4135;
    public static final int AL_STATIC = 4136;
    public static final int AL_STREAMING = 4137;
    public static final int AL_UNDETERMINED = 4144;
    public static final int AL_FORMAT_MONO8 = 4352;
    public static final int AL_FORMAT_MONO16 = 4353;
    public static final int AL_FORMAT_STEREO8 = 4354;
    public static final int AL_FORMAT_STEREO16 = 4355;
    public static final int AL_FREQUENCY = 8193;
    public static final int AL_BITS = 8194;
    public static final int AL_CHANNELS = 8195;
    public static final int AL_SIZE = 8196;
    public static final int AL_UNUSED = 8208;
    public static final int AL_PENDING = 8209;
    public static final int AL_PROCESSED = 8210;
    public static final int AL_NO_ERROR = 0;
    public static final int AL_INVALID_NAME = 40961;
    public static final int AL_INVALID_ENUM = 40962;
    public static final int AL_INVALID_VALUE = 40963;
    public static final int AL_INVALID_OPERATION = 40964;
    public static final int AL_OUT_OF_MEMORY = 40965;
    public static final int AL_VENDOR = 45057;
    public static final int AL_VERSION = 45058;
    public static final int AL_RENDERER = 45059;
    public static final int AL_EXTENSIONS = 45060;
    public static final int AL_DOPPLER_FACTOR = 49152;
    public static native void alDopplerFactor(float value); /* alDopplerFactor((ALfloat)value); */
    public static final int AL_DOPPLER_VELOCITY = 49153;
    public static native void alDopplerVelocity(float value); /* alDopplerVelocity((ALfloat)value); */
    public static final int AL_SPEED_OF_SOUND = 49155;
    public static native void alSpeedOfSound(float value); /* alSpeedOfSound((ALfloat)value); */
    public static final int AL_DISTANCE_MODEL = 53248;
    public static native void alDistanceModel(int distanceModel); /* alDistanceModel((ALenum)distanceModel); */
    public static final int AL_INVERSE_DISTANCE = 53249;
    public static final int AL_INVERSE_DISTANCE_CLAMPED = 53250;
    public static final int AL_LINEAR_DISTANCE = 53251;
    public static final int AL_LINEAR_DISTANCE_CLAMPED = 53252;
    public static final int AL_EXPONENT_DISTANCE = 53253;
    public static final int AL_EXPONENT_DISTANCE_CLAMPED = 53254;
    public static native void alEnable(int capability); /* alEnable((ALenum)capability); */
    public static native void alDisable(int capability); /* alDisable((ALenum)capability); */
    public static native boolean alIsEnabled(int capability); /* return (jboolean)alIsEnabled((ALenum)capability); */
    public static native String alGetString(int param); /* return env->NewStringUTF((const char*)alGetString((ALenum)param)); */
    public static native boolean alGetBoolean(int param); /* return (jboolean)alGetBoolean((ALenum)param); */
    public static native int alGetInteger(int param); /* return (jint)alGetInteger((ALenum)param); */
    public static native float alGetFloat(int param); /* return (jfloat)alGetFloat((ALenum)param); */
    public static native double alGetDouble(int param); /* return (jdouble)alGetDouble((ALenum)param); */
    public static native int alGetError(); /* return (jint)alGetError(); */
    public static native boolean alIsExtensionPresent(String extname); /* return (jboolean)alIsExtensionPresent((ALchar*)extname); */
   // public static native void* alGetProcAddress(String fname); /* return (jvoid*)alGetProcAddress((ALchar*)fname); */
    public static native int alGetEnumValue(String ename); /* return (jint)alGetEnumValue((ALchar*)ename); */
    public static native void alListenerf(int param, float value); /* alListenerf((ALenum)param, (ALfloat)value); */
    public static native void alListener3f(int param, float value1, float value2, float value3); /* alListener3f((ALenum)param, (ALfloat)value1, (ALfloat)value2, (ALfloat)value3); */
    public static native void alListenerfv(int param, FloatBuffer values); /* alListenerfv((ALenum)param, (ALfloat*)values); */
    public static native void alListeneri(int param, int value); /* alListeneri((ALenum)param, (ALint)value); */
    public static native void alListener3i(int param, int value1, int value2, int value3); /* alListener3i((ALenum)param, (ALint)value1, (ALint)value2, (ALint)value3); */
    public static native void alListeneriv(int param, IntBuffer values); /* alListeneriv((ALenum)param, (ALint*)values); */
    public static native void alGetListenerf(int param, FloatBuffer value); /* alGetListenerf((ALenum)param, (ALfloat*)value); */
    public static native void alGetListener3f(int param, FloatBuffer value1, FloatBuffer value2, FloatBuffer value3); /* alGetListener3f((ALenum)param, (ALfloat*)value1, (ALfloat*)value2, (ALfloat*)value3); */
    public static native void alGetListenerfv(int param, FloatBuffer values); /* alGetListenerfv((ALenum)param, (ALfloat*)values); */
    public static native void alGetListeneri(int param, IntBuffer value); /* alGetListeneri((ALenum)param, (ALint*)value); */
    public static native void alGetListener3i(int param, IntBuffer value1, IntBuffer value2, IntBuffer value3); /* alGetListener3i((ALenum)param, (ALint*)value1, (ALint*)value2, (ALint*)value3); */
    public static native void alGetListeneriv(int param, IntBuffer values); /* alGetListeneriv((ALenum)param, (ALint*)values); */
    public static native int alGenSources(); /* ALuint result; alGenSources(1, &result); return (jint)result; */
    public static native void alDeleteSources(int sources); /* ALuint output = (ALuint)sources; alDeleteSources(1, &output); */
    public static native boolean alIsSource(int source); /* return (jboolean)alIsSource((ALuint)source); */
    public static native void alSourcef(int source, int param, float value); /* alSourcef((ALuint)source, (ALenum)param, (ALfloat)value); */
    public static native void alSource3f(int source, int param, float value1, float value2, float value3); /* alSource3f((ALuint)source, (ALenum)param, (ALfloat)value1, (ALfloat)value2, (ALfloat)value3); */
    public static native void alSourcefv(int source, int param, FloatBuffer values); /* alSourcefv((ALuint)source, (ALenum)param, (ALfloat*)values); */
    public static native void alSourcei(int source, int param, int value); /* alSourcei((ALuint)source, (ALenum)param, (ALint)value); */
    public static native void alSource3i(int source, int param, int value1, int value2, int value3); /* alSource3i((ALuint)source, (ALenum)param, (ALint)value1, (ALint)value2, (ALint)value3); */
    public static native void alSourceiv(int source, int param, IntBuffer values); /* alSourceiv((ALuint)source, (ALenum)param, (ALint*)values); */
    public static native float alGetSourcef(int source, int param); /* ALfloat result; alGetSourcef((ALuint)source, (ALenum)param, &result); return (jint)result; */
    public static native void alGetSource3f(int source, int param, FloatBuffer value1, FloatBuffer value2, FloatBuffer value3); /* alGetSource3f((ALuint)source, (ALenum)param, (ALfloat*)value1, (ALfloat*)value2, (ALfloat*)value3); */
    public static native void alGetSourcefv(int source, int param, FloatBuffer values); /* alGetSourcefv((ALuint)source, (ALenum)param, (ALfloat*)values); */
    public static native int alGetSourcei(int source, int param); /* ALint result; alGetSourcei((ALuint)source, (ALenum)param, &result); return (jint)result; */
    public static native void alGetSource3i(int source, int param, IntBuffer value1, IntBuffer value2, IntBuffer value3); /* alGetSource3i((ALuint)source, (ALenum)param, (ALint*)value1, (ALint*)value2, (ALint*)value3); */
    public static native void alGetSourceiv(int source, int param, IntBuffer values); /* alGetSourceiv((ALuint)source, (ALenum)param, (ALint*)values); */
    public static native void alSourcePlayv(int n, IntBuffer sources); /* alSourcePlayv((ALsizei)n, (ALuint*)sources); */
    public static native void alSourceStopv(int n, IntBuffer sources); /* alSourceStopv((ALsizei)n, (ALuint*)sources); */
    public static native void alSourceRewindv(int n, IntBuffer sources); /* alSourceRewindv((ALsizei)n, (ALuint*)sources); */
    public static native void alSourcePausev(int n, IntBuffer sources); /* alSourcePausev((ALsizei)n, (ALuint*)sources); */
    public static native void alSourcePlay(int source); /* alSourcePlay((ALuint)source); */
    public static native void alSourceStop(int source); /* alSourceStop((ALuint)source); */
    public static native void alSourceRewind(int source); /* alSourceRewind((ALuint)source); */
    public static native void alSourcePause(int source); /* alSourcePause((ALuint)source); */
    //public static native void alSourceQueueBuffers(int source, int nb, IntBuffer buffers); /* ALuint buffers; alSourceQueueBuffers((ALuint)source, 1, (ALuint*)buffers); */
    public static native void alSourceQueueBuffers(int source, int name); /* ALuint buffers = (ALuint)name; alSourceQueueBuffers((ALuint)source, 1, &buffers); */
    public static native void alSourceUnqueueBuffers(int source, int nb, IntBuffer buffers); /* alSourceUnqueueBuffers((ALuint)source, (ALsizei)nb, (ALuint*)buffers); */
    public static native void alSourceUnqueueBuffers(int source, IntBuffer buffers); /* alSourceUnqueueBuffers((ALuint)source, 1, (ALuint*)buffers); */
    public static native int alSourceUnqueueBuffers(int source); /* ALuint result; alSourceUnqueueBuffers((ALuint)source, 1, &result); return (jint)result; */
    public static native int alGenBuffers(); /* ALuint result; alGenBuffers(1, &result); return (jint)result;*/
    public static native void alGenBuffers(int n, IntBuffer buffers); /* alGenBuffers((ALsizei)n, (ALuint*)buffers); */
    public static native void alDeleteBuffers(IntBuffer buffers); /* alDeleteBuffers(1, (ALuint*)buffers); */
    public static native void alDeleteBuffers(int buffer); /* ALuint input = (ALuint)buffer; alDeleteBuffers(1, &input); */
    public static native boolean alIsBuffer(int buffer); /* return (jboolean)alIsBuffer((ALuint)buffer); */
    public static native void alBufferDataShort(int buffer, int format, ShortBuffer data, int size, int freq); /* alBufferData((ALuint)buffer, (ALenum)format, (ALvoid*)data, (ALsizei)size, (ALsizei)freq); */
    public static native void alBufferData(int buffer, int format, ByteBuffer data, int size, int freq); /* alBufferData((ALuint)buffer, (ALenum)format, (ALvoid*)data, (ALsizei)size, (ALsizei)freq); */
    public static native void alBufferf(int buffer, int param, float value); /* alBufferf((ALuint)buffer, (ALenum)param, (ALfloat)value); */
    public static native void alBuffer3f(int buffer, int param, float value1, float value2, float value3); /* alBuffer3f((ALuint)buffer, (ALenum)param, (ALfloat)value1, (ALfloat)value2, (ALfloat)value3); */
    public static native void alBufferfv(int buffer, int param, FloatBuffer values); /* alBufferfv((ALuint)buffer, (ALenum)param, (ALfloat*)values); */
    public static native void alBufferi(int buffer, int param, int value); /* alBufferi((ALuint)buffer, (ALenum)param, (ALint)value); */
    public static native void alBuffer3i(int buffer, int param, int value1, int value2, int value3); /* alBuffer3i((ALuint)buffer, (ALenum)param, (ALint)value1, (ALint)value2, (ALint)value3); */
    public static native void alBufferiv(int buffer, int param, IntBuffer values); /* alBufferiv((ALuint)buffer, (ALenum)param, (ALint*)values); */
    public static native void alGetBufferf(int buffer, int param, FloatBuffer value); /* alGetBufferf((ALuint)buffer, (ALenum)param, (ALfloat*)value); */
    public static native void alGetBuffer3f(int buffer, int param, FloatBuffer value1, FloatBuffer value2, FloatBuffer value3); /* alGetBuffer3f((ALuint)buffer, (ALenum)param, (ALfloat*)value1, (ALfloat*)value2, (ALfloat*)value3); */
    public static native void alGetBufferfv(int buffer, int param, FloatBuffer values); /* alGetBufferfv((ALuint)buffer, (ALenum)param, (ALfloat*)values); */
    public static native void alGetBufferi(int buffer, int param, IntBuffer value); /* alGetBufferi((ALuint)buffer, (ALenum)param, (ALint*)value); */
    public static native void alGetBuffer3i(int buffer, int param, IntBuffer value1, IntBuffer value2, IntBuffer value3); /* alGetBuffer3i((ALuint)buffer, (ALenum)param, (ALint*)value1, (ALint*)value2, (ALint*)value3); */
    public static native void alGetBufferiv(int buffer, int param, IntBuffer values); /* alGetBufferiv((ALuint)buffer, (ALenum)param, (ALint*)values); */

    //alc

    public static final int ALC_INVALID = 0;
    public static final int ALC_VERSION_0_1 = 1;
    public static final int ALC_FALSE = 0;
    public static final int ALC_TRUE = 1;
    public static final int ALC_FREQUENCY = 4103;
    public static final int ALC_REFRESH = 4104;
    public static final int ALC_SYNC = 4105;
    public static final int ALC_MONO_SOURCES = 4112;
    public static final int ALC_STEREO_SOURCES = 4113;
    public static final int ALC_NO_ERROR = 0;
    public static final int ALC_INVALID_DEVICE = 40961;
    public static final int ALC_INVALID_CONTEXT = 40962;
    public static final int ALC_INVALID_ENUM = 40963;
    public static final int ALC_INVALID_VALUE = 40964;
    public static final int ALC_OUT_OF_MEMORY = 40965;
    public static final int ALC_MAJOR_VERSION = 4096;
    public static final int ALC_MINOR_VERSION = 4097;
    public static final int ALC_ATTRIBUTES_SIZE = 4098;
    public static final int ALC_ALL_ATTRIBUTES = 4099;
    public static final int ALC_DEFAULT_DEVICE_SPECIFIER = 4100;
    public static final int ALC_DEVICE_SPECIFIER = 4101;
    public static final int ALC_EXTENSIONS = 4102;
    public static final int ALC_EXT_CAPTURE = 1;
    public static final int ALC_CAPTURE_DEVICE_SPECIFIER = 784;
    public static final int ALC_CAPTURE_DEFAULT_DEVICE_SPECIFIER = 785;
    public static final int ALC_CAPTURE_SAMPLES = 786;
    public static final int ALC_ENUMERATE_ALL_EXT = 1;
    public static final int ALC_DEFAULT_ALL_DEVICES_SPECIFIER = 4114;
    public static final int ALC_ALL_DEVICES_SPECIFIER = 4115;
    public static native long alcCreateContext(long device, IntBuffer attrlist); /* return (jlong)alcCreateContext((ALCdevice*)device, NULL); */
    public static native boolean alcMakeContextCurrent(long context); /* return (jboolean)alcMakeContextCurrent((ALCcontext*)context); */
    public static native void alcProcessContext(long context); /* alcProcessContext((ALCcontext*)context); */
    public static native void alcSuspendContext(long context); /* alcSuspendContext((ALCcontext*)context); */
    public static native void alcDestroyContext(long context); /* alcDestroyContext((ALCcontext*)context); */
    public static native long alcGetCurrentContext(); /* return (jlong)alcGetCurrentContext(); */
    public static native long alcGetContextsDevice(long context); /* return (jlong)alcGetContextsDevice((ALCcontext*)context); */
    public static native long alcOpenDevice(); /* return (jlong)alcOpenDevice(NULL); */
    public static native boolean alcCloseDevice(long device); /* return (jboolean)alcCloseDevice((ALCdevice*)device); */
    public static native int alcGetError(long device); /* return (jint)alcGetError((ALCdevice*)device); */
    public static native boolean alcIsExtensionPresent(long device, String extname); /* return (jboolean)alcIsExtensionPresent((ALCdevice*)device, (ALCchar*)extname); */
    //public static native void* alcGetProcAddress(long device, String funcname); /* return (jvoid*)alcGetProcAddress((ALCdevice*)device, (ALCchar*)funcname); */
    public static native int alcGetEnumValue(long device, String enumname); /* return (jint)alcGetEnumValue((ALCdevice*)device, (ALCchar*)enumname); */
    public static native String alcGetString(long device, int param); /* return env->NewStringUTF((const char*)alcGetString((ALCdevice*)device, (ALCenum)param)); */
    public static native void alcGetIntegerv(long device, int param, int size, IntBuffer values); /* alcGetIntegerv((ALCdevice*)device, (ALCenum)param, (ALCsizei)size, (ALCint*)values); */
    public static native long alcCaptureOpenDevice(String devicename, int frequency, int format, int buffersize); /* return (jlong)alcCaptureOpenDevice((ALCchar*)devicename, (ALCuint)frequency, (ALCenum)format, (ALCsizei)buffersize); */
    public static native boolean alcCaptureCloseDevice(long device); /* return (jboolean)alcCaptureCloseDevice((ALCdevice*)device); */
    public static native void alcCaptureStart(long device); /* alcCaptureStart((ALCdevice*)device); */
    public static native void alcCaptureStop(long device); /* alcCaptureStop((ALCdevice*)device); */
    public static native void alcCaptureSamples(long device, ByteBuffer buffer, int samples); /* alcCaptureSamples((ALCdevice*)device, (ALCvoid*)buffer, (ALCsizei)samples); */
}

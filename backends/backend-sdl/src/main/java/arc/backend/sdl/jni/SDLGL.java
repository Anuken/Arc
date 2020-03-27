package arc.backend.sdl.jni;

import arc.util.*;
import arc.util.ArcAnnotate.*;

import java.nio.*;

public class SDLGL{

    /*JNI

    #define GLEW_STATIC

    #include "GL/glew.h"

    //copied from ios openGL source, I have no idea what I'm doing

    static jclass bufferClass;
    static jclass byteBufferClass;
    static jclass charBufferClass;
    static jclass shortBufferClass;
    static jclass intBufferClass;
    static jclass longBufferClass;
    static jclass floatBufferClass;
    static jclass doubleBufferClass;
    static jclass OOMEClass;
    static jclass UOEClass;
    static jclass IAEClass;

    static jmethodID positionID;


    static void
    nativeClassInitBuffer(JNIEnv *_env)
    {
        jclass bufferClassLocal = _env->FindClass("java/nio/Buffer");
        bufferClass = (jclass) _env->NewGlobalRef(bufferClassLocal);

        byteBufferClass = (jclass) _env->NewGlobalRef(_env->FindClass("java/nio/ByteBuffer"));
        charBufferClass = (jclass) _env->NewGlobalRef(_env->FindClass("java/nio/CharBuffer"));
        shortBufferClass = (jclass) _env->NewGlobalRef(_env->FindClass("java/nio/ShortBuffer"));
        intBufferClass = (jclass) _env->NewGlobalRef(_env->FindClass("java/nio/IntBuffer"));
        longBufferClass = (jclass) _env->NewGlobalRef(_env->FindClass("java/nio/LongBuffer"));
        floatBufferClass = (jclass) _env->NewGlobalRef(_env->FindClass("java/nio/FloatBuffer"));
        doubleBufferClass = (jclass) _env->NewGlobalRef(_env->FindClass("java/nio/DoubleBuffer"));

        positionID = _env->GetMethodID(bufferClass, "position","()I");
        if(positionID == 0) _env->ThrowNew(IAEClass, "Couldn't fetch position() method");
    }

    static void
    nativeClassInit(JNIEnv *_env)
    {
        nativeClassInitBuffer(_env);

        jclass IAEClassLocal =
            _env->FindClass("java/lang/IllegalArgumentException");
        jclass OOMEClassLocal =
             _env->FindClass("java/lang/OutOfMemoryError");
        jclass UOEClassLocal =
             _env->FindClass("java/lang/UnsupportedOperationException");

        IAEClass = (jclass) _env->NewGlobalRef(IAEClassLocal);
        OOMEClass = (jclass) _env->NewGlobalRef(OOMEClassLocal);
        UOEClass = (jclass) _env->NewGlobalRef(UOEClassLocal);
    }

    static jint getElementSizeShift(JNIEnv *_env, jobject buffer) {
        if(_env->IsInstanceOf(buffer, byteBufferClass)) return 0;
        if(_env->IsInstanceOf(buffer, floatBufferClass)) return 2;
        if(_env->IsInstanceOf(buffer, shortBufferClass)) return 1;

        if(_env->IsInstanceOf(buffer, charBufferClass)) return 1;
        if(_env->IsInstanceOf(buffer, intBufferClass)) return 2;
        if(_env->IsInstanceOf(buffer, longBufferClass)) return 3;
        if(_env->IsInstanceOf(buffer, doubleBufferClass)) return 3;

        _env->ThrowNew(IAEClass, "buffer type unkown! (Not a ByteBuffer, ShortBuffer, etc.)");
        return 0;
    }

    inline jint getBufferPosition(JNIEnv *env, jobject buffer)
    {
        jint ret = env->CallIntMethodA(buffer, positionID, 0);
        return  ret;
    }

    static void *
    getDirectBufferPointer(JNIEnv *_env, jobject buffer) {
        if (!buffer) {
            return NULL;
        }
        void* buf = _env->GetDirectBufferAddress(buffer);
        if (buf) {
            jint position = getBufferPosition(_env, buffer);
            jint elementSizeShift = getElementSizeShift(_env, buffer);
            buf = ((char*) buf) + (position << elementSizeShift);
        } else {
            _env->ThrowNew(IAEClass, "Must use a native order direct Buffer");
        }
        return buf;
    }

    */

    static{
        String errorMessage = init();
        if(errorMessage != null){
            throw new ArcRuntimeException("GLEW failed to initialize: " + errorMessage);
        }
    }

    @Nullable
    private static native String init(); /*
        nativeClassInit(env);

        glewExperimental = GL_TRUE;

        GLenum glewError = glewInit();
        if(glewError != GLEW_OK){
            return env->NewStringUTF((const char*)glewGetErrorString(glewError));
        }

        if((glewIsSupported("GL_VERSION_3_0") || glewIsSupported("GL_EXT_framebuffer_object") || glewIsSupported("GL_ARB_framebuffer_object")) && glGenFramebuffers != 0){
            //no error message
            return NULL;
        }else{
            return env->NewStringUTF("Missing framebuffer_object extension.");
        }
    */

    public static native void glActiveTexture(int texture); /*
        glActiveTexture(texture);
    */

    public static native void glBindTexture(int target, int texture); /*
        glBindTexture(target, texture);
    */

    public static native void glBlendFunc(int sfactor, int dfactor); /*
        glBlendFunc(sfactor, dfactor);
    */

    public static native void glClear(int mask); /*
        glClear(mask);
    */

    public static native void glClearColor(float red, float green, float blue, float alpha); /*
        glClearColor(red, green, blue, alpha);
    */


    public static native void glClearDepthf(float depth); /*
        glClearDepthf(depth);
    */

    public static native void glClearStencil(int s); /*
        glClearStencil(s);
    */

    public static native void glColorMask(boolean red, boolean green, boolean blue, boolean alpha); /*
        glColorMask(red, green, blue, alpha);
    */

    public static native void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, int imageSize, Buffer data); /*
        glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
    */

    public static native void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int imageSize, Buffer data); /*
        glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
    */

    public static native void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height, int border); /*
        glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
    */

    public static native void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width, int height); /*
        glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
    */

    public static native void glCullFace(int mode); /*
        glCullFace(mode);
    */

    public static native void glDeleteTexture(int texture); /*
        GLuint b = texture;
        glDeleteTextures(1, &b);
    */

    public static native void glDepthFunc(int func); /*
        glDepthFunc(func);
    */

    public static native void glDepthMask(boolean flag); /*
        glDepthMask(flag);
    */

    public static native void glDepthRangef(float zNear, float zFar); /*
        glDepthRangef(zNear, zFar);
    */

    public static native void glDisable(int cap); /*
        glDisable(cap);
    */

    public static native void glDrawArrays(int mode, int first, int count); /*
        glDrawArrays(mode, first, count);
    */

    public static native void glDrawElements(int mode, int count, int type, Buffer indices); /*
        glDrawElements(mode, count, type, indices);
    */

    public static native void glEnable(int cap); /*
        glEnable(cap);
    */

    public static native void glFinish(); /*
        glFinish();
    */

    public static native void glFlush(); /*
        glFlush();
    */

    public static native void glFrontFace(int mode); /*
        glFrontFace(mode);
    */

    public static native int glGenTexture(); /*
        GLuint result;
        glGenTextures(1, &result);
        return result;
    */

    public static native int glGetError(); /*
        return glGetError();
    */

    public static native void glGetIntegerv(int pname, IntBuffer params); /*
        glGetIntegerv(pname, params);
    */

    public static native String glGetString(int name); /*
        return env->NewStringUTF((const char*)glGetString(name));
    */

    public static native void glHint(int target, int mode); /*
        glHint(target, mode);
    */

    public static native void glLineWidth(float width); /*
        glLineWidth(width);
    */

    public static native void glPixelStorei(int pname, int param); /*
        glPixelStorei(pname, param);
    */

    public static native void glPolygonOffset(float factor, float units); /*
        glPolygonOffset(factor, units);
    */

    public static native void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels); /*
        glReadPixels(x, y, width, height, format, type, pixels);
    */

    public static native void glScissor(int x, int y, int width, int height); /*
        glScissor(x, y, width, height);
    */

    public static native void glStencilFunc(int func, int ref, int mask); /*
        glStencilFunc(func, ref, mask);
    */

    public static native void glStencilMask(int mask); /*
        glStencilMask(mask);
    */

    public static native void glStencilOp(int fail, int zfail, int zpass); /*
        glStencilOp(fail, zfail, zpass);
    */

    public static native void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels); /*
        glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    */

    public static native void glTexParameterf(int target, int pname, float param); /*
        glTexParameterf(target, pname, param);
    */

    public static native void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels); /*
        glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    */

    public static native void glViewport(int x, int y, int width, int height); /*
        glViewport(x, y, width, height);
    */

    public static native void glAttachShader(int program, int shader); /*
        glAttachShader(program, shader);
    */

    public static native void glBindAttribLocation(int program, int index, String name); /*
        glBindAttribLocation(program, index, name);
    */

    public static native void glBindBuffer(int target, int buffer); /*
        glBindBuffer(target, buffer);
    */

    public static native void glBindFramebuffer(int target, int framebuffer); /*
        glBindFramebuffer(target, framebuffer);
    */

    public static native void glBindRenderbuffer(int target, int renderbuffer); /*
        glBindRenderbuffer(target, renderbuffer);
    */

    public static native void glBlendColor(float red, float green, float blue, float alpha); /*
        glBlendColor(red, green, blue, alpha);
    */

    public static native void glBlendEquation(int mode); /*
        glBlendEquation(mode);
    */

    public static native void glBlendEquationSeparate(int modeRGB, int modeAlpha); /*
        glBlendEquationSeparate(modeRGB, modeAlpha);
    */

    public static native void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha); /*
        glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
    */

    public static native void glBufferData(int target, int size, Buffer data, int usage); /*
        glBufferData(target, size, data, usage);
    */

    public static native void glBufferSubData(int target, int offset, int size, Buffer data); /*
        glBufferSubData(target, offset, size, data);
    */

    public static native int glCheckFramebufferStatus(int target); /*
        return glCheckFramebufferStatus(target);
    */

    public static native void glCompileShader(int shader); /*
        glCompileShader(shader);
    */

    public static native int glCreateProgram(); /*
        return glCreateProgram();
    */

    public static native int glCreateShader(int type); /*
        return glCreateShader(type);
    */

    public static native void glDeleteBuffer(int buffer); /*
        GLuint b = buffer;
        glDeleteBuffers(1, &b);
    */

    public static native void glDeleteFramebuffer(int framebuffer); /*
        GLuint b = framebuffer;
        glDeleteFramebuffers(1, &b);
    */

    public static native void glDeleteProgram(int program); /*
        glDeleteProgram(program);
    */

    public static native void glDeleteRenderbuffer(int renderbuffer); /*
        GLuint b = renderbuffer;
        glDeleteRenderbuffers(1, &b);
    */

    public static native void glDeleteShader(int shader); /*
        glDeleteShader(shader);
    */

    public static native void glDetachShader(int program, int shader); /*
        glDetachShader(program, shader);
    */

    public static native void glDisableVertexAttribArray(int index); /*
        glDisableVertexAttribArray(index);
    */

    public static native void glDrawElements(int mode, int count, int type, int indices); /*
        glDrawElements(mode, count, type, (const void*)indices);
    */

    public static native void glEnableVertexAttribArray(int index); /*
        glEnableVertexAttribArray(index);
    */

    public static native void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer); /*
        glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
    */

    public static native void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level); /*
        glFramebufferTexture2D(target, attachment, textarget, texture, level);
    */

    public static native int glGenBuffer(); /*
        GLuint result;
        glGenBuffers(1, &result);
        return result;
    */

    public static native void glGenerateMipmap(int target); /*
        glGenerateMipmap(target);
    */

    public static native int glGenFramebuffer(); /*
        GLuint result;
        glGenFramebuffers(1, &result);
        return result;
    */

    public static native int glGenRenderbuffer(); /*
        GLuint result;
        glGenRenderbuffers(1, &result);
        return result;
    */

    public static native String glGetActiveAttrib(int program, int index, Object size, Object type); /*
        char cname[2048];
	    void* sizePtr = getDirectBufferPointer( env, size );
	    void* typePtr = getDirectBufferPointer( env, type );
	    glGetActiveAttrib( program, index, 2048, NULL, (GLint*)sizePtr, (GLenum*)typePtr, cname );

        return env->NewStringUTF(cname);
    */

    public static native String glGetActiveUniform(int program, int index, Object size, Object type); /*
        char cname[2048];
        void* sizePtr = getDirectBufferPointer( env, size );
        void* typePtr = getDirectBufferPointer( env, type );
        glGetActiveUniform( program, index, 2048, NULL, (GLint*)sizePtr, (GLenum*)typePtr, cname );
        return env->NewStringUTF(cname);
    */

    public static native int glGetAttribLocation(int program, String name); /*
        return glGetAttribLocation(program, name);
    */

    public static native void glGetBooleanv(int pname, Buffer params); /*
        glGetBooleanv(pname, params);
    */

    public static native void glGetBufferParameteriv(int target, int pname, IntBuffer params); /*
        glGetBufferParameteriv(target, pname, params);
    */

    public static native void glGetFloatv(int pname, FloatBuffer params); /*
        glGetFloatv(pname, params);
    */

    public static native void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params); /*
        glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
    */

    public static native void glGetProgramiv(int program, int pname, IntBuffer params); /*
        glGetProgramiv(program, pname, params);
    */

    public static native String glGetProgramInfoLog(int program); /*
        char info[1024*10]; // FIXME 10k limit should suffice
        int length = 0;
        glGetProgramInfoLog( program, 1024*10, &length, info );
        return env->NewStringUTF(info);
    */

    public static native void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params); /*
        glGetRenderbufferParameteriv(target, pname, params);
    */

    public static native void glGetShaderiv(int shader, int pname, IntBuffer params); /*
        glGetShaderiv(shader, pname, params);
    */

    public static native String glGetShaderInfoLog(int shader); /*
        char info[1024*10]; // FIXME 10k limit should suffice
        int length = 0;
        glGetShaderInfoLog( shader, 1024*10, &length, info );
        return env->NewStringUTF( info );
    */

    public static native void glGetShaderPrecisionFormat(int shadertype, int precisiontype, IntBuffer range, IntBuffer precision); /*
        glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);
    */

    public static native void glGetTexParameterfv(int target, int pname, FloatBuffer params); /*
        glGetTexParameterfv(target, pname, params);
    */

    public static native void glGetTexParameteriv(int target, int pname, IntBuffer params); /*
        glGetTexParameteriv(target, pname, params);
    */

    public static native void glGetUniformfv(int program, int location, FloatBuffer params); /*
        glGetUniformfv(program, location, params);
    */

    public static native void glGetUniformiv(int program, int location, IntBuffer params); /*
        glGetUniformiv(program, location, params);
    */

    public static native int glGetUniformLocation(int program, String name); /*
        return glGetUniformLocation(program, name);
    */

    public static native void glGetVertexAttribfv(int index, int pname, FloatBuffer params); /*
        glGetVertexAttribfv(index, pname, params);
    */

    public static native void glGetVertexAttribiv(int index, int pname, IntBuffer params); /*
        glGetVertexAttribiv(index, pname, params);
    */

    public static native boolean glIsBuffer(int buffer); /*
        return glIsBuffer(buffer);
    */

    public static native boolean glIsEnabled(int cap); /*
        return glIsEnabled(cap);
    */

    public static native boolean glIsFramebuffer(int framebuffer); /*
        return glIsFramebuffer(framebuffer);
    */

    public static native boolean glIsProgram(int program); /*
        return glIsProgram(program);
    */

    public static native boolean glIsRenderbuffer(int renderbuffer); /*
        return glIsRenderbuffer(renderbuffer);
    */

    public static native boolean glIsShader(int shader); /*
        return glIsShader(shader);
    */

    public static native boolean glIsTexture(int texture); /*
        return glIsTexture(texture);
    */

    public static native void glLinkProgram(int program); /*
        glLinkProgram(program);
    */

    public static native void glReleaseShaderCompiler(); /*
        glReleaseShaderCompiler();
    */

    public static native void glRenderbufferStorage(int target, int internalformat, int width, int height); /*
        glRenderbufferStorage(target, internalformat, width, height);
    */

    public static native void glSampleCoverage(float value, boolean invert); /*
        glSampleCoverage(value, invert);
    */

    public static native void glShaderSource(int shader, String string); /*
        glShaderSource(shader, 1, &string, NULL);
    */

    public static native void glStencilFuncSeparate(int face, int func, int ref, int mask); /*
        glStencilFuncSeparate(face, func, ref, mask);
    */

    public static native void glStencilMaskSeparate(int face, int mask); /*
        glStencilMaskSeparate(face, mask);
    */

    public static native void glStencilOpSeparate(int face, int fail, int zfail, int zpass); /*
        glStencilOpSeparate(face, fail, zfail, zpass);
    */

    public static native void glTexParameterfv(int target, int pname, FloatBuffer params); /*
        glTexParameterfv(target, pname, params);
    */

    public static native void glTexParameteri(int target, int pname, int param); /*
        glTexParameteri(target, pname, param);
    */

    public static native void glTexParameteriv(int target, int pname, IntBuffer params); /*
        glTexParameteriv(target, pname, params);
    */

    public static native void glUniform1f(int location, float x); /*
        glUniform1f(location, x);
    */

    public static native void glUniform1fv(int location, int count, FloatBuffer v); /*
        glUniform1fv(location, count, v);
    */

    public static native void glUniform1fv(int location, int count, float[] v, int offset); /*
        glUniform1fv(location, count, (GLfloat*)&v[offset]);
    */

    public static native void glUniform1i(int location, int x); /*
        glUniform1i(location, x);
    */

    public static native void glUniform1iv(int location, int count, IntBuffer v); /*
        glUniform1iv(location, count, v);
    */

    public static native void glUniform1iv(int location, int count, int[] v, int offset); /*
        glUniform1iv(location, count, (GLint*)&v[offset]);
    */

    public static native void glUniform2f(int location, float x, float y); /*
        glUniform2f(location, x, y);
    */

    public static native void glUniform2fv(int location, int count, FloatBuffer v); /*
        glUniform2fv(location, count, v);
    */

    public static native void glUniform2fv(int location, int count, float[] v, int offset); /*
        glUniform2fv(location, count, (GLfloat*)&v[offset]);
    */

    public static native void glUniform2i(int location, int x, int y); /*
        glUniform2i(location, x, y);
    */

    public static native void glUniform2iv(int location, int count, IntBuffer v); /*
        glUniform2iv(location, count, v);
    */

    public static native void glUniform2iv(int location, int count, int[] v, int offset); /*
        glUniform2iv(location, count, (GLint*)&v[offset]);
    */

    public static native void glUniform3f(int location, float x, float y, float z); /*
        glUniform3f(location, x, y, z);
    */

    public static native void glUniform3fv(int location, int count, FloatBuffer v); /*
        glUniform3fv(location, count, v);
    */

    public static native void glUniform3fv(int location, int count, float[] v, int offset); /*
        glUniform3fv(location, count, (GLfloat*)&v[offset]);
    */

    public static native void glUniform3i(int location, int x, int y, int z); /*
        glUniform3i(location, x, y, z);
    */

    public static native void glUniform3iv(int location, int count, IntBuffer v); /*
        glUniform3iv(location, count, v);
    */

    public static native void glUniform3iv(int location, int count, int[] v, int offset); /*
        glUniform3iv(location, count, (GLint*)&v[offset]);
    */

    public static native void glUniform4f(int location, float x, float y, float z, float w); /*
        glUniform4f(location, x, y, z, w);
    */

    public static native void glUniform4fv(int location, int count, FloatBuffer v); /*
        glUniform4fv(location, count, v);
    */

    public static native void glUniform4fv(int location, int count, float[] v, int offset); /*
        glUniform4fv(location, count, (GLfloat*)&v[offset]);
    */

    public static native void glUniform4i(int location, int x, int y, int z, int w); /*
        glUniform4i(location, x, y, z, w);
    */

    public static native void glUniform4iv(int location, int count, IntBuffer v); /*
        glUniform4iv(location, count, v);
    */

    public static native void glUniform4iv(int location, int count, int[] v, int offset); /*
        glUniform4iv(location, count, (GLint*)&v[offset]);
    */

    public static native void glUniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer value); /*
        glUniformMatrix2fv(location, count, transpose, value);
    */

    public static native void glUniformMatrix2fv(int location, int count, boolean transpose, float[] value, int offset); /*
        glUniformMatrix2fv(location, count, transpose, (GLfloat*)&value[offset]);
    */

    public static native void glUniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer value); /*
        glUniformMatrix3fv(location, count, transpose, value);
    */

    public static native void glUniformMatrix3fv(int location, int count, boolean transpose, float[] value, int offset); /*
        glUniformMatrix3fv(location, count, transpose, (GLfloat*)&value[offset]);
    */

    public static native void glUniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer value); /*
        glUniformMatrix4fv(location, count, transpose, value);
    */

    public static native void glUniformMatrix4fv(int location, int count, boolean transpose, float[] value, int offset); /*
        glUniformMatrix4fv(location, count, transpose, (GLfloat*)&value[offset]);
    */

    public static native void glUseProgram(int program); /*
        glUseProgram(program);
    */

    public static native void glValidateProgram(int program); /*
        glValidateProgram(program);
    */

    public static native void glVertexAttrib1f(int indx, float x); /*
        glVertexAttrib1f(indx, x);
    */

    public static native void glVertexAttrib1fv(int indx, FloatBuffer values); /*
        glVertexAttrib1fv(indx, values);
    */

    public static native void glVertexAttrib2f(int indx, float x, float y); /*
        glVertexAttrib2f(indx, x, y);
    */

    public static native void glVertexAttrib2fv(int indx, FloatBuffer values); /*
        glVertexAttrib2fv(indx, values);
    */

    public static native void glVertexAttrib3f(int indx, float x, float y, float z); /*
        glVertexAttrib3f(indx, x, y, z);
    */

    public static native void glVertexAttrib3fv(int indx, FloatBuffer values); /*
        glVertexAttrib3fv(indx, values);
    */

    public static native void glVertexAttrib4f(int indx, float x, float y, float z, float w); /*
        glVertexAttrib4f(indx, x, y, z, w);
    */

    public static native void glVertexAttrib4fv(int indx, FloatBuffer values); /*
        glVertexAttrib4fv(indx, values);
    */

    public static native void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, Object ptr); /*
        void* dataPtr = getDirectBufferPointer( env, ptr );
        glVertexAttribPointer(indx, size, type, normalized, stride, dataPtr);
    */

    public static native void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, int ptr); /*
        glVertexAttribPointer(indx, size, type, normalized, stride, (const void*)ptr);
    */
}
package arc.backend.sdl.jni;

import arc.util.*;
import arc.util.ArcAnnotate.*;

import java.nio.*;

public class SDLGL{
    //region initialization

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

        if((glewIsSupported("GL_VERSION_3_0") || glewIsSupported("GL_EXT_framebuffer_object")) && (glGenFramebuffers != 0 || glGenFramebuffersEXT != 0)){
            //no error message
            return NULL;
        }else{
            return env->NewStringUTF("Missing framebuffer_object extension.");
        }
    */

    //endregion
    //region openGL 2.0

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
        if(glBindFramebuffer){
            glBindFramebuffer(target, framebuffer);
            return;
        }

        glBindFramebufferEXT(target, framebuffer);
    */

    public static native void glBindRenderbuffer(int target, int renderbuffer); /*
        if(glBindRenderbuffer){
            glBindRenderbuffer(target, renderbuffer);
            return;
        }

        glBindRenderbufferEXT(target, renderbuffer);
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
        if(glCheckFramebufferStatus){
            return glCheckFramebufferStatus(target);
        }

        return glCheckFramebufferStatusEXT(target);
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
        if(glDeleteFramebuffers){
            GLuint b = framebuffer;
            glDeleteFramebuffers(1, &b);
            return;
        }

        GLuint b = framebuffer;
        glDeleteFramebuffersEXT(1, &b);
    */

    public static native void glDeleteProgram(int program); /*
        glDeleteProgram(program);
    */

    public static native void glDeleteRenderbuffer(int renderbuffer); /*
        GLuint b = renderbuffer;

        if(glDeleteRenderbuffers){
            glDeleteRenderbuffers(1, &b);
            return;
        }

        glDeleteRenderbuffersEXT(1, &b);
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
        if(glFramebufferRenderbuffer){
            glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
            return;
        }

        glFramebufferRenderbufferEXT(target, attachment, renderbuffertarget, renderbuffer);
    */

    public static native void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level); /*
        if(glFramebufferTexture2D){
            glFramebufferTexture2D(target, attachment, textarget, texture, level);
            return;
        }

        glFramebufferTexture2DEXT(target, attachment, textarget, texture, level);
    */

    public static native int glGenBuffer(); /*
        GLuint result;
        glGenBuffers(1, &result);
        return result;
    */

    public static native void glGenerateMipmap(int target); /*
        if(glGenerateMipmap){
            glGenerateMipmap(target);
            return;
        }

        glGenerateMipmapEXT(target);
    */

    public static native int glGenFramebuffer(); /*
        if(glGenFramebuffers){
            GLuint result;
            glGenFramebuffers(1, &result);
            return result;
        }

        GLuint result;
        glGenFramebuffersEXT(1, &result);
        return result;
    */

    public static native int glGenRenderbuffer(); /*
        if(glGenRenderbuffers){
            GLuint result;
            glGenRenderbuffers(1, &result);
            return result;
        }

        GLuint result;
        glGenRenderbuffersEXT(1, &result);
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
        if(glGetFramebufferAttachmentParameteriv){
            glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
            return;
        }

        glGetFramebufferAttachmentParameterivEXT(target, attachment, pname, params);
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
        if(glGetRenderbufferParameteriv){
            glGetRenderbufferParameteriv(target, pname, params);
            return;
        }

        glGetRenderbufferParameterivEXT(target, pname, params);
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
        if(glIsFramebuffer){
            return glIsFramebuffer(framebuffer);
        }

        return glIsFramebufferEXT(framebuffer);
    */

    public static native boolean glIsProgram(int program); /*
        return glIsProgram(program);
    */

    public static native boolean glIsRenderbuffer(int renderbuffer); /*
        if(glIsRenderbuffer){
            return glIsRenderbuffer(renderbuffer);
        }

        return glIsRenderbufferEXT(renderbuffer);
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
        if(glRenderbufferStorage){
            glRenderbufferStorage(target, internalformat, width, height);
            return;
        }

        glRenderbufferStorageEXT(target, internalformat, width, height);
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

    //endregion
    //region openGL 3.0

    public static native void glReadBuffer(int mode); /*
        glReadBuffer(mode);
    */

    public static native void glDrawRangeElements(int mode, int start, int end, int count, int type, int offset); /*
        glDrawRangeElements(mode, start, end, count, type, (void*)offset);
    */

    public static native void glDrawRangeElements(int mode, int start, int end, int count, int type, Buffer indices); /*
        glDrawRangeElements(mode, start, end, count, type, indices);
    */

    public static native void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, int offset); /*
        glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, (void*)offset);
    */

    public static native void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, Buffer pixels); /*
        glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
    */

    public static native void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, int offset); /*
        glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, (void*)offset);
    */

    public static native void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, Buffer pixels); /*
        glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    */

    public static native void glCopyTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width, int height); /*
        glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height);
    */

    public static native void glGenQueries(int n, IntBuffer ids); /*
        glGenQueries(n, ids);
    */

    public static native void glDeleteQueries(int n, IntBuffer ids); /*
        glDeleteQueries(n, ids);
    */

    public static native boolean glIsQuery(int id); /*
        return glIsQuery(id);
    */

    public static native void glBeginQuery(int target, int id); /*
        glBeginQuery(target, id);
    */

    public static native void glEndQuery(int target); /*
        glEndQuery(target);
    */

    public static native void glGetQueryiv(int target, int pname, IntBuffer params); /*
        glGetQueryiv(target, pname, params);
    */

    public static native void glGetQueryObjectuiv(int id, int pname, IntBuffer params); /*
        glGetQueryObjectuiv(id, pname, params);
    */

    public static native boolean glUnmapBuffer(int target); /*
        return glUnmapBuffer(target);
    */

    public static native Buffer glGetBufferPointerv(int target, int pname); /*
        return glGetBufferPointerv(target, pname);
    */

    public static native void glDrawBuffers(int n, IntBuffer bufs); /*
        glDrawBuffers(n, bufs);
    */

    public static native void glUniformMatrix2x3fv(int location, int count, boolean transpose, FloatBuffer value); /*
        glUniformMatrix2x3fv(location, count, transpose, value);
    */

    public static native void glUniformMatrix3x2fv(int location, int count, boolean transpose, FloatBuffer value); /*
        glUniformMatrix3x2fv(location, count, transpose, value);
    */

    public static native void glUniformMatrix2x4fv(int location, int count, boolean transpose, FloatBuffer value); /*
        glUniformMatrix2x4fv(location, count, transpose, value);
    */

    public static native void glUniformMatrix4x2fv(int location, int count, boolean transpose, FloatBuffer value); /*
        glUniformMatrix4x2fv(location, count, transpose, value);
    */

    public static native void glUniformMatrix3x4fv(int location, int count, boolean transpose, FloatBuffer value); /*
        glUniformMatrix3x4fv(location, count, transpose, value);
    */

    public static native void glUniformMatrix4x3fv(int location, int count, boolean transpose, FloatBuffer value); /*
        glUniformMatrix4x3fv(location, count, transpose, value);
    */

    public static native void glBlitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter); /*
        glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
    */

    public static native void glRenderbufferStorageMultisample(int target, int samples, int internalformat, int width, int height); /*
        glRenderbufferStorageMultisample(target, samples, internalformat, width, height);
    */

    public static native void glFramebufferTextureLayer(int target, int attachment, int texture, int level, int layer); /*
        glFramebufferTextureLayer(target, attachment, texture, level, layer);
    */

    public static native void glFlushMappedBufferRange(int target, int offset, int length); /*
        glFlushMappedBufferRange(target, offset, length);
    */

    public static native void glBindVertexArray(int array); /*
        glBindVertexArray(array);
    */

    public static native void glDeleteVertexArrays(int n, IntBuffer arrays); /*
        glDeleteVertexArrays(n, arrays);
    */

    public static native void glGenVertexArrays(int n, IntBuffer arrays); /*
        glGenVertexArrays(n, arrays);
    */

    public static native boolean glIsVertexArray(int array); /*
        return glIsVertexArray(array);
    */

    public static native void glBeginTransformFeedback(int primitiveMode); /*
        glBeginTransformFeedback(primitiveMode);
    */

    public static native void glEndTransformFeedback(); /*
        glEndTransformFeedback();
    */

    public static native void glBindBufferRange(int target, int index, int buffer, int offset, int size); /*
        glBindBufferRange(target, index, buffer, offset, size);
    */

    public static native void glBindBufferBase(int target, int index, int buffer); /*
        glBindBufferBase(target, index, buffer);
    */

    public static native void glTransformFeedbackVaryings(int program, String[] varyings, int bufferMode); /*
        glTransformFeedbackVaryings(program, varyings, bufferMode);
    */

    public static native void glVertexAttribIPointer(int index, int size, int type, int stride, int offset); /*
        glVertexAttribIPointer(index, size, type, stride, offset);
    */

    public static native void glGetVertexAttribIiv(int index, int pname, IntBuffer params); /*
        glGetVertexAttribIiv(index, pname, params);
    */

    public static native void glGetVertexAttribIuiv(int index, int pname, IntBuffer params); /*
        glGetVertexAttribIuiv(index, pname, params);
    */

    public static native void glVertexAttribI4i(int index, int x, int y, int z, int w); /*
        glVertexAttribI4i(index, x, y, z, w);
    */

    public static native void glVertexAttribI4ui(int index, int x, int y, int z, int w); /*
        glVertexAttribI4ui(index, x, y, z, w);
    */

    public static native void glGetUniformuiv(int program, int location, IntBuffer params); /*
        glGetUniformuiv(program, location, params);
    */

    public static native int glGetFragDataLocation(int program, String name); /*
        return glGetFragDataLocation(program, name);
    */

    public static native void glUniform1uiv(int location, int count, IntBuffer value); /*
        glUniform1uiv(location, count, value);
    */

    public static native void glUniform3uiv(int location, int count, IntBuffer value); /*
        glUniform3uiv(location, count, value);
    */

    public static native void glUniform4uiv(int location, int count, IntBuffer value); /*
        glUniform4uiv(location, count, value);
    */

    public static native void glClearBufferiv(int buffer, int drawbuffer, IntBuffer value); /*
        glClearBufferiv(buffer, drawbuffer, value);
    */

    public static native void glClearBufferuiv(int buffer, int drawbuffer, IntBuffer value); /*
        glClearBufferuiv(buffer, drawbuffer, value);
    */

    public static native void glClearBufferfv(int buffer, int drawbuffer, FloatBuffer value); /*
        glClearBufferfv(buffer, drawbuffer, value);
    */

    public static native void glClearBufferfi(int buffer, int drawbuffer, float depth, int stencil); /*
        glClearBufferfi(buffer, drawbuffer, depth, stencil);
    */

    public static native String glGetStringi(int name, int index); /*
        return glGetStringi(name, index);
    */

    public static native void glCopyBufferSubData(int readTarget, int writeTarget, int readOffset, int writeOffset, int size); /*
        glCopyBufferSubData(readTarget, writeTarget, readOffset, writeOffset, size);
    */

    public static native void glGetUniformIndices(int program, String[] uniformNames, IntBuffer uniformIndices); /*
        glGetUniformIndices(program, uniformNames, uniformIndices);
    */

    public static native void glGetActiveUniformsiv(int program, int uniformCount, IntBuffer uniformIndices, int pname, IntBuffer params); /*
        glGetActiveUniformsiv(program, uniformCount, uniformIndices, pname, params);
    */

    public static native int glGetUniformBlockIndex(int program, String uniformBlockName); /*
        return glGetUniformBlockIndex(program, uniformBlockName);
    */

    public static native void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, IntBuffer params); /*
        glGetActiveUniformBlockiv(program, uniformBlockIndex, pname, params);
    */

    public static native String glGetActiveUniformBlockName(int program, int uniformBlockIndex); /*
        return glGetActiveUniformBlockName(program, uniformBlockIndex);
    */

    public static native void glGetActiveUniformBlockName(int program, int uniformBlockIndex, Buffer length, Buffer uniformBlockName); /*
        glGetActiveUniformBlockName(program, uniformBlockIndex, length, uniformBlockName);
    */

    public static native void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding); /*
        glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
    */

    public static native void glDrawArraysInstanced(int mode, int first, int count, int instanceCount); /*
        glDrawArraysInstanced(mode, first, count, instanceCount);
    */

    public static native void glDrawElementsInstanced(int mode, int count, int type, int indicesOffset, int instanceCount); /*
        glDrawElementsInstanced(mode, count, type, indicesOffset, instanceCount);
    */

    public static native void glGetInteger64v(int pname, LongBuffer params); /*
        glGetInteger64v(pname, params);
    */

    public static native void glGetBufferParameteri64v(int target, int pname, LongBuffer params); /*
        glGetBufferParameteri64v(target, pname, params);
    */

    public static native void glGenSamplers(int count, IntBuffer samplers); /*
        glGenSamplers(count, samplers);
    */

    public static native void glDeleteSamplers(int count, IntBuffer samplers); /*
        glDeleteSamplers(count, samplers);
    */

    public static native boolean glIsSampler(int sampler); /*
        return glIsSampler(sampler);
    */

    public static native void glBindSampler(int unit, int sampler); /*
        glBindSampler(unit, sampler);
    */

    public static native void glSamplerParameteri(int sampler, int pname, int param); /*
        glSamplerParameteri(sampler, pname, param);
    */

    public static native void glSamplerParameteriv(int sampler, int pname, IntBuffer param); /*
        glSamplerParameteriv(sampler, pname, param);
    */

    public static native void glSamplerParameterf(int sampler, int pname, float param); /*
        glSamplerParameterf(sampler, pname, param);
    */

    public static native void glSamplerParameterfv(int sampler, int pname, FloatBuffer param); /*
        glSamplerParameterfv(sampler, pname, param);
    */

    public static native void glGetSamplerParameteriv(int sampler, int pname, IntBuffer params); /*
        glGetSamplerParameteriv(sampler, pname, params);
    */

    public static native void glGetSamplerParameterfv(int sampler, int pname, FloatBuffer params); /*
        glGetSamplerParameterfv(sampler, pname, params);
    */

    public static native void glVertexAttribDivisor(int index, int divisor); /*
        glVertexAttribDivisor(index, divisor);
    */

    public static native void glBindTransformFeedback(int target, int id); /*
        glBindTransformFeedback(target, id);
    */

    public static native void glDeleteTransformFeedbacks(int n, IntBuffer ids); /*
        glDeleteTransformFeedbacks(n, ids);
    */

    public static native void glGenTransformFeedbacks(int n, IntBuffer ids); /*
        glGenTransformFeedbacks(n, ids);
    */

    public static native boolean glIsTransformFeedback(int id); /*
        return glIsTransformFeedback(id);
    */

    public static native void glPauseTransformFeedback(); /*
        glPauseTransformFeedback();
    */

    public static native void glResumeTransformFeedback(); /*
        glResumeTransformFeedback();
    */

    public static native void glProgramParameteri(int program, int pname, int value); /*
        glProgramParameteri(program, pname, value);
    */

    public static native void glInvalidateFramebuffer(int target, int numAttachments, IntBuffer attachments); /*
        glInvalidateFramebuffer(target, numAttachments, attachments);
    */

    public static native void glInvalidateSubFramebuffer(int target, int numAttachments, IntBuffer attachments, int x, int y, int width, int height); /*
        glInvalidateSubFramebuffer(target, numAttachments, attachments, x, y, width, height);
    */

    //endregion
}
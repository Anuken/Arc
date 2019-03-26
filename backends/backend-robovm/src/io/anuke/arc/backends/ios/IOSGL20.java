package io.anuke.arc.backends.ios;

import io.anuke.arc.graphics.GL20;
import io.anuke.arc.util.BufferUtils;
import org.robovm.rt.bro.Struct;
import org.robovm.rt.bro.ptr.BytePtr;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class IOSGL20 implements GL20{
    /** last viewport set, needed because GLKView resets the viewport on each call to render... amazing **/
    public static int x, y, width, height;

    private static final IntBuffer aint = IntBuffer.allocate(1);
    private static final FloatBuffer floats = FloatBuffer.allocate(128);
    private static final IntBuffer ints = IntBuffer.allocate(128);
    private static final int MAX_LOG_SIZE = 8192;

    private static String toString(ByteBuffer bbuf, int length){
        byte[] data = new byte[length];
        bbuf.get(data, 0, length);
        return new String(data);
    }

    @Override
    public void glActiveTexture(int texture){
        OpenGLES.glActiveTexture(texture);
    }

    @Override
    public void glAttachShader(int program, int shader){
        OpenGLES.glAttachShader(program, shader);
    }

    @Override
    public void glBindAttribLocation(int program, int index, String name){
        OpenGLES.glBindAttribLocation(program, index, name);
    }

    @Override
    public void glBindBuffer(int target, int buffer){
        OpenGLES.glBindBuffer(target, buffer);
    }

    @Override
    public void glBindFramebuffer(int target, int frameBuffer){
        OpenGLES.glBindFramebuffer(target, frameBuffer);
    }

    @Override
    public void glBindRenderbuffer(int target, int renderBuffer){
        OpenGLES.glBindRenderbuffer(target, renderBuffer);
    }

    @Override
    public void glBindTexture(int target, int texture){
        OpenGLES.glBindTexture(target, texture);
    }

    @Override
    public void glBlendColor(float red, float green, float blue, float alpha){
        OpenGLES.glBlendColor(red, green, blue, alpha);
    }

    @Override
    public void glBlendEquation(int mode){
        OpenGLES.glBlendEquation(mode);
    }

    @Override
    public void glBlendEquationSeparate(int modeRGB, int modeAlpha){
        OpenGLES.glBlendEquationSeparate(modeRGB, modeAlpha);
    }

    @Override
    public void glBlendFunc(int sfactor, int dfactor){
        OpenGLES.glBlendFunc(sfactor, dfactor);
    }

    @Override
    public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha){
        OpenGLES.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
    }

    @Override
    public void glBufferData(int target, int size, Buffer data, int usage){
        OpenGLES.glBufferData(target, size, data, usage);
    }

    @Override
    public void glBufferSubData(int target, int offset, int size, Buffer data){
        OpenGLES.glBufferSubData(target, offset, size, data);
    }

    @Override
    public int glCheckFramebufferStatus(int target){
        return OpenGLES.glCheckFramebufferStatus(target);
    }

    @Override
    public void glClear(int mask){
        OpenGLES.glClear(mask);
    }

    @Override
    public void glClearColor(float red, float green, float blue, float alpha){
        OpenGLES.glClearColor(red, green, blue, alpha);
    }

    @Override
    public void glClearDepthf(float depth){
        OpenGLES.glClearDepthf(depth);
    }

    @Override
    public void glClearStencil(int s){
        OpenGLES.glClearStencil(s);
    }

    @Override
    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha){
        OpenGLES.glColorMask(red, green, blue, alpha);
    }

    @Override
    public void glCompileShader(int shader){
        OpenGLES.glCompileShader(shader);
    }

    @Override
    public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, int imageSize, Buffer data){
        OpenGLES.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
    }

    @Override
    public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int imageSize, Buffer data){
        OpenGLES.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
    }

    @Override
    public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height, int border){
        OpenGLES.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
    }

    @Override
    public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width, int height){
        OpenGLES.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
    }

    @Override
    public int glCreateProgram(){
        return OpenGLES.glCreateProgram();
    }

    @Override
    public int glCreateShader(int type){
        return OpenGLES.glCreateShader(type);
    }

    @Override
    public void glCullFace(int mode){
        OpenGLES.glCullFace(mode);
    }

    @Override
    public void glDeleteBuffers(int n, IntBuffer buffers){
        OpenGLES.glDeleteBuffers(n, buffers);
    }

    @Override
    public void glDeleteFramebuffers(int n, IntBuffer framebuffers){
        OpenGLES.glDeleteFramebuffers(n, framebuffers);
    }

    @Override
    public void glDeleteProgram(int program){
        OpenGLES.glDeleteProgram(program);
    }

    @Override
    public void glDeleteRenderbuffers(int n, IntBuffer renderbuffers){
        OpenGLES.glDeleteRenderbuffers(n, renderbuffers);
    }

    @Override
    public void glDeleteShader(int shader){
        OpenGLES.glDeleteShader(shader);
    }

    @Override
    public void glDeleteTextures(int n, IntBuffer textures){
        OpenGLES.glDeleteTextures(n, textures);
    }

    @Override
    public void glDepthFunc(int func){
        OpenGLES.glDepthFunc(func);
    }

    @Override
    public void glDepthMask(boolean flag){
        OpenGLES.glDepthMask(flag);
    }

    @Override
    public void glDepthRangef(float zNear, float zFar){
        OpenGLES.glDepthRangef(zNear, zFar);
    }

    @Override
    public void glDetachShader(int program, int shader){
        OpenGLES.glDetachShader(program, shader);
    }

    @Override
    public void glDisable(int cap){
        OpenGLES.glDisable(cap);
    }

    @Override
    public void glDisableVertexAttribArray(int index){
        OpenGLES.glDisableVertexAttribArray(index);
    }

    @Override
    public void glDrawArrays(int mode, int first, int count){
        OpenGLES.glDrawArrays(mode, first, count);
    }

    @Override
    public void glDrawElements(int mode, int count, int type, Buffer indices){
        OpenGLES.glDrawElements(mode, count, type, indices);
    }

    @Override
    public void glDrawElements(int mode, int count, int type, int offset){
        OpenGLES.glDrawElements(mode, count, type, offset);
    }

    @Override
    public void glEnable(int cap){
        OpenGLES.glEnable(cap);
    }

    @Override
    public void glEnableVertexAttribArray(int index){
        OpenGLES.glEnableVertexAttribArray(index);
    }

    @Override
    public void glFinish(){
        OpenGLES.glFinish();
    }

    @Override
    public void glFlush(){
        OpenGLES.glFlush();
    }

    @Override
    public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer){
        OpenGLES.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
    }

    @Override
    public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level){
        OpenGLES.glFramebufferTexture2D(target, attachment, textarget, texture, level);
    }

    @Override
    public void glFrontFace(int mode){
        OpenGLES.glFrontFace(mode);
    }

    @Override
    public void glGenBuffers(int n, IntBuffer buffers){
        OpenGLES.glGenBuffers(n, buffers);
    }

    @Override
    public void glGenerateMipmap(int target){
        OpenGLES.glGenerateMipmap(target);
    }

    @Override
    public void glGenFramebuffers(int n, IntBuffer framebuffers){
        OpenGLES.glGenFramebuffers(n, framebuffers);
    }

    @Override
    public void glGenRenderbuffers(int n, IntBuffer renderbuffers){
        OpenGLES.glGenRenderbuffers(n, renderbuffers);
    }

    @Override
    public void glGenTextures(int n, IntBuffer textures){
        OpenGLES.glGenTextures(n, textures);
    }

    @Override
    public int glGetAttribLocation(int program, String name){
        return OpenGLES.glGetAttribLocation(program, name);
    }

    @Override
    public void glGetBufferParameteriv(int target, int pname, IntBuffer params){
        OpenGLES.glGetBufferParameteriv(target, pname, params);
    }

    @Override
    public int glGetError(){
        return OpenGLES.glGetError();
    }

    @Override
    public void glGetFloatv(int pname, FloatBuffer params){
        OpenGLES.glGetFloatv(pname, params);
    }

    @Override
    public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params){
        OpenGLES.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
    }

    @Override
    public void glGetIntegerv(int pname, IntBuffer params){
        OpenGLES.glGetIntegerv(pname, params);
    }

    @Override
    public String glGetProgramInfoLog(int program){
        ByteBuffer bbuf = BufferUtils.newByteBuffer(MAX_LOG_SIZE);
        OpenGLES.glGetProgramInfoLog(program, MAX_LOG_SIZE, aint, bbuf);
        return toString(bbuf, aint.get(0));
    }

    @Override
    public void glGetProgramiv(int program, int pname, IntBuffer params){
        OpenGLES.glGetProgramiv(program, pname, params);
    }

    @Override
    public void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params){
        OpenGLES.glGetRenderbufferParameteriv(target, pname, params);
    }

    @Override
    public String glGetShaderInfoLog(int shader){
        ByteBuffer bbuf = BufferUtils.newByteBuffer(MAX_LOG_SIZE);
        OpenGLES.glGetShaderInfoLog(shader, MAX_LOG_SIZE, aint, bbuf);
        return toString(bbuf, aint.get(0));
    }

    @Override
    public void glGetShaderiv(int shader, int pname, IntBuffer params){
        OpenGLES.glGetShaderiv(shader, pname, params);
    }

    @Override
    public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, IntBuffer range, IntBuffer precision){
        OpenGLES.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);
    }

    @Override
    public String glGetString(int name){
        return OpenGLES.glGetString(name);
    }

    @Override
    public void glGetTexParameterfv(int target, int pname, FloatBuffer params){
        OpenGLES.glGetTexParameterfv(target, pname, params);
    }

    @Override
    public void glGetTexParameteriv(int target, int pname, IntBuffer params){
        OpenGLES.glGetTexParameteriv(target, pname, params);
    }

    @Override
    public void glGetUniformfv(int program, int location, FloatBuffer params){
        OpenGLES.glGetUniformfv(program, location, params);
    }

    @Override
    public void glGetUniformiv(int program, int location, IntBuffer params){
        OpenGLES.glGetUniformiv(program, location, params);
    }

    @Override
    public int glGetUniformLocation(int program, String name){
        return OpenGLES.glGetUniformLocation(program, name);
    }

    @Override
    public void glGetVertexAttribfv(int index, int pname, FloatBuffer params){
        OpenGLES.glGetVertexAttribfv(index, pname, params);
    }

    @Override
    public void glGetVertexAttribiv(int index, int pname, IntBuffer params){
        OpenGLES.glGetVertexAttribiv(index, pname, params);
    }

    @Override
    public void glHint(int target, int mode){
        OpenGLES.glHint(target, mode);
    }

    @Override
    public boolean glIsBuffer(int buffer){
        return OpenGLES.glIsBuffer(buffer);
    }

    @Override
    public boolean glIsEnabled(int cap){
        return OpenGLES.glIsEnabled(cap);
    }

    @Override
    public boolean glIsFramebuffer(int framebuffer){
        return OpenGLES.glIsFramebuffer(framebuffer);
    }

    @Override
    public boolean glIsProgram(int program){
        return OpenGLES.glIsProgram(program);
    }

    @Override
    public boolean glIsRenderbuffer(int renderbuffer){
        return OpenGLES.glIsRenderbuffer(renderbuffer);
    }

    @Override
    public boolean glIsShader(int shader){
        return OpenGLES.glIsShader(shader);
    }

    @Override
    public boolean glIsTexture(int texture){
        return OpenGLES.glIsTexture(texture);
    }

    @Override
    public void glLineWidth(float width){
        OpenGLES.glLineWidth(width);
    }

    @Override
    public void glLinkProgram(int program){
        OpenGLES.glLinkProgram(program);
    }

    @Override
    public void glPixelStorei(int pname, int param){
        OpenGLES.glPixelStorei(pname, param);
    }

    @Override
    public void glPolygonOffset(float factor, float units){
        OpenGLES.glPolygonOffset(factor, units);
    }

    @Override
    public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels){
        OpenGLES.glReadPixels(x, y, width, height, format, type, pixels);
    }

    @Override
    public void glReleaseShaderCompiler(){
        OpenGLES.glReleaseShaderCompiler();
    }

    @Override
    public void glRenderbufferStorage(int target, int internalformat, int width, int height){
        OpenGLES.glRenderbufferStorage(target, internalformat, width, height);
    }

    @Override
    public void glSampleCoverage(float value, boolean invert){
        OpenGLES.glSampleCoverage(value, invert);
    }

    @Override
    public void glScissor(int x, int y, int width, int height){
        OpenGLES.glScissor(x, y, width, height);
    }

    @Override
    public void glShaderBinary(int n, IntBuffer shaders, int binaryformat, Buffer binary, int length){
        OpenGLES.glShaderBinary(n, shaders, binaryformat, binary, length);
    }

    @Override
    public void glShaderSource(int shader, String source){
        BytePtr.BytePtrPtr sources = Struct.allocate(BytePtr.BytePtrPtr.class, 1);
        sources.next(0).set(BytePtr.toBytePtrAsciiZ(source));
        OpenGLES.glShaderSource(shader, 1, sources, null);
    }

    @Override
    public void glStencilFunc(int func, int ref, int mask){
        OpenGLES.glStencilFunc(func, ref, mask);
    }

    @Override
    public void glStencilFuncSeparate(int face, int func, int ref, int mask){
        OpenGLES.glStencilFuncSeparate(face, func, ref, mask);
    }

    @Override
    public void glStencilMask(int mask){
        OpenGLES.glStencilMask(mask);
    }

    @Override
    public void glStencilMaskSeparate(int face, int mask){
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void glStencilOp(int fail, int zfail, int zpass){
        OpenGLES.glStencilOp(fail, zfail, zpass);
    }

    @Override
    public void glStencilOpSeparate(int face, int fail, int zfail, int zpass){
        OpenGLES.glStencilOpSeparate(face, fail, zfail, zpass);
    }

    @Override
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels){
        OpenGLES.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    @Override
    public void glTexParameterf(int target, int pname, float param){
        OpenGLES.glTexParameterf(target, pname, param);
    }

    @Override
    public void glTexParameterfv(int target, int pname, FloatBuffer params){
        OpenGLES.glTexParameterfv(target, pname, params);
    }

    @Override
    public void glTexParameteri(int target, int pname, int param){
        OpenGLES.glTexParameterf(target, pname, param);
    }

    @Override
    public void glTexParameteriv(int target, int pname, IntBuffer params){
        OpenGLES.glTexParameteriv(target, pname, params);
    }

    @Override
    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels){
        OpenGLES.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    @Override
    public void glUniform1f(int location, float x){
        OpenGLES.glUniform1f(location, x);
    }

    @Override
    public void glUniform1fv(int location, int count, FloatBuffer v){
        OpenGLES.glUniform1fv(location, count, v);
    }

    @Override
    public void glUniform1i(int location, int x){
        OpenGLES.glUniform1i(location, x);
    }

    @Override
    public void glUniform1iv(int location, int count, IntBuffer v){
        OpenGLES.glUniform1iv(location, count, v);
    }

    @Override
    public void glUniform2f(int location, float x, float y){
        OpenGLES.glUniform2f(location, x, y);
    }

    @Override
    public void glUniform2fv(int location, int count, FloatBuffer v){
        OpenGLES.glUniform2fv(location, count, v);
    }

    @Override
    public void glUniform2i(int location, int x, int y){
        OpenGLES.glUniform2i(location, x, y);
    }

    @Override
    public void glUniform2iv(int location, int count, IntBuffer v){
        OpenGLES.glUniform2iv(location, count, v);
    }

    @Override
    public void glUniform3f(int location, float x, float y, float z){
        OpenGLES.glUniform3f(location, x, y, z);
    }

    @Override
    public void glUniform3fv(int location, int count, FloatBuffer v){
        OpenGLES.glUniform3fv(location, count, v);
    }

    @Override
    public void glUniform3i(int location, int x, int y, int z){
        OpenGLES.glUniform3i(location, x, y, z);
    }

    @Override
    public void glUniform3iv(int location, int count, IntBuffer v){
        OpenGLES.glUniform3iv(location, count, v);
    }

    @Override
    public void glUniform4f(int location, float x, float y, float z, float w){
        OpenGLES.glUniform4f(location, x, y, z, w);
    }

    @Override
    public void glUniform4fv(int location, int count, FloatBuffer v){
        OpenGLES.glUniform4fv(location, count, v);
    }

    @Override
    public void glUniform4i(int location, int x, int y, int z, int w){
        OpenGLES.glUniform4i(location, x, y, z, w);
    }

    @Override
    public void glUniform4iv(int location, int count, IntBuffer v){
        OpenGLES.glUniform4iv(location, count, v);
    }

    @Override
    public void glUniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer value){
        OpenGLES.glUniformMatrix2fv(location, count, transpose, value);
    }

    @Override
    public void glUniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer value){
        OpenGLES.glUniformMatrix3fv(location, count, transpose, value);
    }

    @Override
    public void glUniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer value){
        OpenGLES.glUniformMatrix4fv(location, count, transpose, value);
    }

    @Override
    public void glUseProgram(int program){
        OpenGLES.glUseProgram(program);
    }

    @Override
    public void glValidateProgram(int program){
        OpenGLES.glValidateProgram(program);
    }

    @Override
    public void glVertexAttrib1f(int indx, float x){
        OpenGLES.glVertexAttrib1f(indx, x);
    }

    @Override
    public void glVertexAttrib1fv(int indx, FloatBuffer values){
        OpenGLES.glVertexAttrib1fv(indx, values);
    }

    @Override
    public void glVertexAttrib2f(int indx, float x, float y){
        OpenGLES.glVertexAttrib2f(indx, x, y);
    }

    @Override
    public void glVertexAttrib2fv(int indx, FloatBuffer values){
        OpenGLES.glVertexAttrib2fv(indx, values);
    }

    @Override
    public void glVertexAttrib3f(int indx, float x, float y, float z){
        OpenGLES.glVertexAttrib3f(indx, x, y, z);
    }

    @Override
    public void glVertexAttrib3fv(int indx, FloatBuffer values){
        OpenGLES.glVertexAttrib3fv(indx, values);
    }

    @Override
    public void glVertexAttrib4f(int indx, float x, float y, float z, float w){
        OpenGLES.glVertexAttrib4f(indx, x, y, z, w);
    }

    @Override
    public void glVertexAttrib4fv(int indx, FloatBuffer values){
        OpenGLES.glVertexAttrib4fv(indx, values);
    }

    @Override
    public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, Buffer ptr){
        OpenGLES.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
    }

    @Override
    public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, int ptr){
        OpenGLES.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
    }

    @Override
    public void glViewport(int x, int y, int width, int height){
        IOSGL20.x = x;
        IOSGL20.y = y;
        IOSGL20.width = width;
        IOSGL20.height = height;
        OpenGLES.glViewport(x, y, width, height);
    }

    @Override
    public void glDeleteTexture(int texture){
        aint.position(0);
        aint.put(texture);
        OpenGLES.glDeleteTextures(1, aint);
    }

    @Override
    public int glGenTexture(){
        OpenGLES.glGenTextures(1, aint);
        return aint.get(0);
    }

    @Override
    public void glDeleteBuffer(int buffer){
        aint.position(0);
        aint.put(buffer);
        OpenGLES.glDeleteBuffers(1, aint);
    }

    @Override
    public void glDeleteFramebuffer(int framebuffer){
        aint.position(0);
        aint.put(framebuffer);
        OpenGLES.glDeleteFramebuffers(1, aint);
    }

    @Override
    public void glDeleteRenderbuffer(int renderbuffer){
        aint.position(0);
        aint.put(renderbuffer);
        OpenGLES.glDeleteBuffers(1, aint);
    }

    @Override
    public int glGenBuffer(){
        OpenGLES.glGenBuffers(1, aint);
        return aint.get(0);
    }

    @Override
    public int glGenFramebuffer(){
        OpenGLES.glGenFramebuffers(1, aint);
        return aint.get(0);
    }

    @Override
    public int glGenRenderbuffer(){
        OpenGLES.glGenRenderbuffers(1, aint);
        return aint.get(0);
    }

    @Override
    public String glGetActiveAttrib(int program, int index, IntBuffer size, Buffer type){
        return OpenGLES.glGetActiveAttrib(program, index, size, type);
    }

    @Override
    public String glGetActiveUniform(int program, int index, IntBuffer size, Buffer type){
        return OpenGLES.glGetActiveUniform(program, index, size, type);
    }

    @Override
    public void glGetAttachedShaders(int program, int maxcount, Buffer count, IntBuffer shaders){
        OpenGLES.glGetAttachedShaders(program, maxcount, count, shaders);
    }

    @Override
    public void glGetBooleanv(int pname, Buffer params){
        OpenGLES.glGetBooleanv(pname, params);
    }

    @Override
    public void glGetVertexAttribPointerv(int index, int pname, Buffer pointer){
        OpenGLES.glGetVertexAttribPointerv(index, pname, pointer);
    }

    @Override
    public void glUniform1fv(int location, int count, float[] v, int offset){
        floats.position(0);
        floats.put(v, offset, count);
        OpenGLES.glUniform1fv(location, count, floats);
    }

    @Override
    public void glUniform1iv(int location, int count, int[] v, int offset){
        ints.position(0);
        ints.put(v, offset, count);
        OpenGLES.glUniform1iv(location, count, ints);
    }

    @Override
    public void glUniform2fv(int location, int count, float[] v, int offset){
        floats.position(0);
        floats.put(v, offset, count);
        OpenGLES.glUniform2fv(location, count, floats);
    }

    @Override
    public void glUniform2iv(int location, int count, int[] v, int offset){
        ints.position(0);
        ints.put(v, offset, count);
        OpenGLES.glUniform2iv(location, count, ints);
    }

    @Override
    public void glUniform3fv(int location, int count, float[] v, int offset){
        floats.position(0);
        floats.put(v, offset, count);
        OpenGLES.glUniform3fv(location, count, floats);
    }

    @Override
    public void glUniform3iv(int location, int count, int[] v, int offset){
        ints.position(0);
        ints.put(v, offset, count);
        OpenGLES.glUniform3iv(location, count, ints);
    }

    @Override
    public void glUniform4fv(int location, int count, float[] v, int offset){
        floats.position(0);
        floats.put(v, offset, count);
        OpenGLES.glUniform4fv(location, count, floats);
    }

    @Override
    public void glUniform4iv(int location, int count, int[] v, int offset){
        ints.position(0);
        ints.put(v, offset, count);
        OpenGLES.glUniform4iv(location, count, ints);
    }

    @Override
    public void glUniformMatrix2fv(int location, int count, boolean transpose, float[] value, int offset){
        floats.position(0);
        floats.put(value, offset, count);
        OpenGLES.glUniformMatrix2fv(location, count, transpose, floats);
    }

    @Override
    public void glUniformMatrix3fv(int location, int count, boolean transpose, float[] value, int offset){
        floats.position(0);
        floats.put(value, offset, count);
        OpenGLES.glUniformMatrix3fv(location, count, transpose, floats);
    }

    @Override
    public void glUniformMatrix4fv(int location, int count, boolean transpose, float[] value, int offset){
        floats.position(0);
        floats.put(value, offset, count);
        OpenGLES.glUniformMatrix4fv(location, count, transpose, floats);
    }
}

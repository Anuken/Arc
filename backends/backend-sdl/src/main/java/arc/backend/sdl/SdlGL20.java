package arc.backend.sdl;

import io.anuke.arc.backends.sdl.jni.SDLGL;
import arc.graphics.*;

import java.nio.*;

public class SdlGL20 implements GL20{
    @Override public void glActiveTexture(int texture){
        io.anuke.arc.backends.sdl.jni.SDLGL.glActiveTexture(texture);}

    @Override public void glBindTexture(int target, int texture){
        io.anuke.arc.backends.sdl.jni.SDLGL.glBindTexture(target, texture);}

    @Override public void glBlendFunc(int sfactor, int dfactor){
        io.anuke.arc.backends.sdl.jni.SDLGL.glBlendFunc(sfactor, dfactor);}

    @Override public void glClear(int mask){
        io.anuke.arc.backends.sdl.jni.SDLGL.glClear(mask);}

    @Override public void glClearColor(float red, float green, float blue, float alpha){
        io.anuke.arc.backends.sdl.jni.SDLGL.glClearColor(red, green, blue, alpha);}

    @Override public void glClearDepthf(float depth){
        io.anuke.arc.backends.sdl.jni.SDLGL.glClearDepthf(depth);}

    @Override public void glClearStencil(int s){
        io.anuke.arc.backends.sdl.jni.SDLGL.glClearStencil(s);}

    @Override public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha){
        io.anuke.arc.backends.sdl.jni.SDLGL.glColorMask(red, green, blue, alpha);}

    @Override public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, int imageSize, Buffer data){
        io.anuke.arc.backends.sdl.jni.SDLGL.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);}

    @Override public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int imageSize, Buffer data){
        io.anuke.arc.backends.sdl.jni.SDLGL.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);}

    @Override public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height, int border){
        io.anuke.arc.backends.sdl.jni.SDLGL.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);}

    @Override public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width, int height){
        io.anuke.arc.backends.sdl.jni.SDLGL.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);}

    @Override public void glCullFace(int mode){
        io.anuke.arc.backends.sdl.jni.SDLGL.glCullFace(mode);}

    @Override public void glDeleteTexture(int texture){
        io.anuke.arc.backends.sdl.jni.SDLGL.glDeleteTexture(texture);}

    @Override public void glDepthFunc(int func){
        io.anuke.arc.backends.sdl.jni.SDLGL.glDepthFunc(func);}

    @Override public void glDepthMask(boolean flag){
        io.anuke.arc.backends.sdl.jni.SDLGL.glDepthMask(flag);}

    @Override public void glDepthRangef(float zNear, float zFar){
        io.anuke.arc.backends.sdl.jni.SDLGL.glDepthRangef(zNear, zFar);}

    @Override public void glDisable(int cap){
        io.anuke.arc.backends.sdl.jni.SDLGL.glDisable(cap);}

    @Override public void glDrawArrays(int mode, int first, int count){
        io.anuke.arc.backends.sdl.jni.SDLGL.glDrawArrays(mode, first, count);}

    @Override public void glDrawElements(int mode, int count, int type, Buffer indices){
        io.anuke.arc.backends.sdl.jni.SDLGL.glDrawElements(mode, count, type, indices);}

    @Override public void glEnable(int cap){
        io.anuke.arc.backends.sdl.jni.SDLGL.glEnable(cap);}

    @Override public void glFinish(){
        io.anuke.arc.backends.sdl.jni.SDLGL.glFinish();}

    @Override public void glFlush(){
        io.anuke.arc.backends.sdl.jni.SDLGL.glFlush();}

    @Override public void glFrontFace(int mode){
        io.anuke.arc.backends.sdl.jni.SDLGL.glFrontFace(mode);}

    @Override public int glGenTexture(){return io.anuke.arc.backends.sdl.jni.SDLGL.glGenTexture();}

    @Override public int glGetError(){return io.anuke.arc.backends.sdl.jni.SDLGL.glGetError();}

    @Override public void glGetIntegerv(int pname, IntBuffer params){
        io.anuke.arc.backends.sdl.jni.SDLGL.glGetIntegerv(pname, params);}

    @Override public String glGetString(int name){return io.anuke.arc.backends.sdl.jni.SDLGL.glGetString(name);}

    @Override public void glHint(int target, int mode){
        io.anuke.arc.backends.sdl.jni.SDLGL.glHint(target, mode);}

    @Override public void glLineWidth(float width){
        io.anuke.arc.backends.sdl.jni.SDLGL.glLineWidth(width);}

    @Override public void glPixelStorei(int pname, int param){
        io.anuke.arc.backends.sdl.jni.SDLGL.glPixelStorei(pname, param);}

    @Override public void glPolygonOffset(float factor, float units){
        io.anuke.arc.backends.sdl.jni.SDLGL.glPolygonOffset(factor, units);}

    @Override public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels){
        io.anuke.arc.backends.sdl.jni.SDLGL.glReadPixels(x, y, width, height, format, type, pixels);}

    @Override public void glScissor(int x, int y, int width, int height){
        io.anuke.arc.backends.sdl.jni.SDLGL.glScissor(x, y, width, height);}

    @Override public void glStencilFunc(int func, int ref, int mask){
        io.anuke.arc.backends.sdl.jni.SDLGL.glStencilFunc(func, ref, mask);}

    @Override public void glStencilMask(int mask){
        io.anuke.arc.backends.sdl.jni.SDLGL.glStencilMask(mask);}

    @Override public void glStencilOp(int fail, int zfail, int zpass){
        io.anuke.arc.backends.sdl.jni.SDLGL.glStencilOp(fail, zfail, zpass);}

    @Override public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels){
        io.anuke.arc.backends.sdl.jni.SDLGL.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);}

    @Override public void glTexParameterf(int target, int pname, float param){
        io.anuke.arc.backends.sdl.jni.SDLGL.glTexParameterf(target, pname, param);}

    @Override public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels){
        io.anuke.arc.backends.sdl.jni.SDLGL.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);}

    @Override public void glViewport(int x, int y, int width, int height){
        io.anuke.arc.backends.sdl.jni.SDLGL.glViewport(x, y, width, height);}

    @Override public void glAttachShader(int program, int shader){
        io.anuke.arc.backends.sdl.jni.SDLGL.glAttachShader(program, shader);}

    @Override public void glBindAttribLocation(int program, int index, String name){
        io.anuke.arc.backends.sdl.jni.SDLGL.glBindAttribLocation(program, index, name);}

    @Override public void glBindBuffer(int target, int buffer){
        io.anuke.arc.backends.sdl.jni.SDLGL.glBindBuffer(target, buffer);}

    @Override public void glBindFramebuffer(int target, int framebuffer){
        io.anuke.arc.backends.sdl.jni.SDLGL.glBindFramebuffer(target, framebuffer);}

    @Override public void glBindRenderbuffer(int target, int renderbuffer){
        io.anuke.arc.backends.sdl.jni.SDLGL.glBindRenderbuffer(target, renderbuffer);}

    @Override public void glBlendColor(float red, float green, float blue, float alpha){
        io.anuke.arc.backends.sdl.jni.SDLGL.glBlendColor(red, green, blue, alpha);}

    @Override public void glBlendEquation(int mode){
        io.anuke.arc.backends.sdl.jni.SDLGL.glBlendEquation(mode);}

    @Override public void glBlendEquationSeparate(int modeRGB, int modeAlpha){
        io.anuke.arc.backends.sdl.jni.SDLGL.glBlendEquationSeparate(modeRGB, modeAlpha);}

    @Override public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha){
        io.anuke.arc.backends.sdl.jni.SDLGL.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);}

    @Override public void glBufferData(int target, int size, Buffer data, int usage){
        io.anuke.arc.backends.sdl.jni.SDLGL.glBufferData(target, size, data, usage);}

    @Override public void glBufferSubData(int target, int offset, int size, Buffer data){
        io.anuke.arc.backends.sdl.jni.SDLGL.glBufferSubData(target, offset, size, data);}

    @Override public int glCheckFramebufferStatus(int target){return io.anuke.arc.backends.sdl.jni.SDLGL.glCheckFramebufferStatus(target);}

    @Override public void glCompileShader(int shader){
        io.anuke.arc.backends.sdl.jni.SDLGL.glCompileShader(shader);}

    @Override public int glCreateProgram(){return io.anuke.arc.backends.sdl.jni.SDLGL.glCreateProgram();}

    @Override public int glCreateShader(int type){return io.anuke.arc.backends.sdl.jni.SDLGL.glCreateShader(type);}

    @Override public void glDeleteBuffer(int buffer){
        io.anuke.arc.backends.sdl.jni.SDLGL.glDeleteBuffer(buffer);}

    @Override public void glDeleteFramebuffer(int framebuffer){
        io.anuke.arc.backends.sdl.jni.SDLGL.glDeleteFramebuffer(framebuffer);}

    @Override public void glDeleteProgram(int program){
        io.anuke.arc.backends.sdl.jni.SDLGL.glDeleteProgram(program);}

    @Override public void glDeleteRenderbuffer(int renderbuffer){
        io.anuke.arc.backends.sdl.jni.SDLGL.glDeleteRenderbuffer(renderbuffer);}

    @Override public void glDeleteShader(int shader){
        io.anuke.arc.backends.sdl.jni.SDLGL.glDeleteShader(shader);}

    @Override public void glDetachShader(int program, int shader){
        io.anuke.arc.backends.sdl.jni.SDLGL.glDetachShader(program, shader);}

    @Override public void glDisableVertexAttribArray(int index){
        io.anuke.arc.backends.sdl.jni.SDLGL.glDisableVertexAttribArray(index);}

    @Override public void glDrawElements(int mode, int count, int type, int indices){
        io.anuke.arc.backends.sdl.jni.SDLGL.glDrawElements(mode, count, type, indices);}

    @Override public void glEnableVertexAttribArray(int index){
        io.anuke.arc.backends.sdl.jni.SDLGL.glEnableVertexAttribArray(index);}

    @Override public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer){
        io.anuke.arc.backends.sdl.jni.SDLGL.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);}

    @Override public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level){
        io.anuke.arc.backends.sdl.jni.SDLGL.glFramebufferTexture2D(target, attachment, textarget, texture, level);}

    @Override public int glGenBuffer(){return io.anuke.arc.backends.sdl.jni.SDLGL.glGenBuffer();}

    @Override public void glGenerateMipmap(int target){
        io.anuke.arc.backends.sdl.jni.SDLGL.glGenerateMipmap(target);}

    @Override public int glGenFramebuffer(){return io.anuke.arc.backends.sdl.jni.SDLGL.glGenFramebuffer();}

    @Override public int glGenRenderbuffer(){return io.anuke.arc.backends.sdl.jni.SDLGL.glGenRenderbuffer();}

    @Override public String glGetActiveAttrib(int program, int index, IntBuffer size, IntBuffer type){return io.anuke.arc.backends.sdl.jni.SDLGL.glGetActiveAttrib(program, index, size, type);}

    @Override public String glGetActiveUniform(int program, int index, IntBuffer size, IntBuffer type){return io.anuke.arc.backends.sdl.jni.SDLGL.glGetActiveUniform(program, index, size, type);}

    @Override public int glGetAttribLocation(int program, String name){return io.anuke.arc.backends.sdl.jni.SDLGL.glGetAttribLocation(program, name);}

    @Override public void glGetBooleanv(int pname, Buffer params){
        io.anuke.arc.backends.sdl.jni.SDLGL.glGetBooleanv(pname, params);}

    @Override public void glGetBufferParameteriv(int target, int pname, IntBuffer params){
        io.anuke.arc.backends.sdl.jni.SDLGL.glGetBufferParameteriv(target, pname, params);}

    @Override public void glGetFloatv(int pname, FloatBuffer params){
        io.anuke.arc.backends.sdl.jni.SDLGL.glGetFloatv(pname, params);}

    @Override public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params){
        io.anuke.arc.backends.sdl.jni.SDLGL.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);}

    @Override public void glGetProgramiv(int program, int pname, IntBuffer params){
        io.anuke.arc.backends.sdl.jni.SDLGL.glGetProgramiv(program, pname, params);}

    @Override public String glGetProgramInfoLog(int program){return io.anuke.arc.backends.sdl.jni.SDLGL.glGetProgramInfoLog(program);}

    @Override public void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params){
        io.anuke.arc.backends.sdl.jni.SDLGL.glGetRenderbufferParameteriv(target, pname, params);}

    @Override public void glGetShaderiv(int shader, int pname, IntBuffer params){
        io.anuke.arc.backends.sdl.jni.SDLGL.glGetShaderiv(shader, pname, params);}

    @Override public String glGetShaderInfoLog(int shader){return io.anuke.arc.backends.sdl.jni.SDLGL.glGetShaderInfoLog(shader);}

    @Override public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, IntBuffer range, IntBuffer precision){
        io.anuke.arc.backends.sdl.jni.SDLGL.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);}

    @Override public void glGetTexParameterfv(int target, int pname, FloatBuffer params){
        io.anuke.arc.backends.sdl.jni.SDLGL.glGetTexParameterfv(target, pname, params);}

    @Override public void glGetTexParameteriv(int target, int pname, IntBuffer params){
        io.anuke.arc.backends.sdl.jni.SDLGL.glGetTexParameteriv(target, pname, params);}

    @Override public void glGetUniformfv(int program, int location, FloatBuffer params){
        io.anuke.arc.backends.sdl.jni.SDLGL.glGetUniformfv(program, location, params);}

    @Override public void glGetUniformiv(int program, int location, IntBuffer params){
        io.anuke.arc.backends.sdl.jni.SDLGL.glGetUniformiv(program, location, params);}

    @Override public int glGetUniformLocation(int program, String name){return io.anuke.arc.backends.sdl.jni.SDLGL.glGetUniformLocation(program, name);}

    @Override public void glGetVertexAttribfv(int index, int pname, FloatBuffer params){
        io.anuke.arc.backends.sdl.jni.SDLGL.glGetVertexAttribfv(index, pname, params);}

    @Override public void glGetVertexAttribiv(int index, int pname, IntBuffer params){
        io.anuke.arc.backends.sdl.jni.SDLGL.glGetVertexAttribiv(index, pname, params);}

    @Override public boolean glIsBuffer(int buffer){return io.anuke.arc.backends.sdl.jni.SDLGL.glIsBuffer(buffer);}

    @Override public boolean glIsEnabled(int cap){return io.anuke.arc.backends.sdl.jni.SDLGL.glIsEnabled(cap);}

    @Override public boolean glIsFramebuffer(int framebuffer){return io.anuke.arc.backends.sdl.jni.SDLGL.glIsFramebuffer(framebuffer);}

    @Override public boolean glIsProgram(int program){return io.anuke.arc.backends.sdl.jni.SDLGL.glIsProgram(program);}

    @Override public boolean glIsRenderbuffer(int renderbuffer){return io.anuke.arc.backends.sdl.jni.SDLGL.glIsRenderbuffer(renderbuffer);}

    @Override public boolean glIsShader(int shader){return io.anuke.arc.backends.sdl.jni.SDLGL.glIsShader(shader);}

    @Override public boolean glIsTexture(int texture){return io.anuke.arc.backends.sdl.jni.SDLGL.glIsTexture(texture);}

    @Override public void glLinkProgram(int program){
        io.anuke.arc.backends.sdl.jni.SDLGL.glLinkProgram(program);}

    @Override public void glReleaseShaderCompiler(){
        io.anuke.arc.backends.sdl.jni.SDLGL.glReleaseShaderCompiler();}

    @Override public void glRenderbufferStorage(int target, int internalformat, int width, int height){
        io.anuke.arc.backends.sdl.jni.SDLGL.glRenderbufferStorage(target, internalformat, width, height);}

    @Override public void glSampleCoverage(float value, boolean invert){
        io.anuke.arc.backends.sdl.jni.SDLGL.glSampleCoverage(value, invert);}

    @Override public void glShaderSource(int shader, String string){
        io.anuke.arc.backends.sdl.jni.SDLGL.glShaderSource(shader, string);}

    @Override public void glStencilFuncSeparate(int face, int func, int ref, int mask){
        io.anuke.arc.backends.sdl.jni.SDLGL.glStencilFuncSeparate(face, func, ref, mask);}

    @Override public void glStencilMaskSeparate(int face, int mask){
        io.anuke.arc.backends.sdl.jni.SDLGL.glStencilMaskSeparate(face, mask);}

    @Override public void glStencilOpSeparate(int face, int fail, int zfail, int zpass){
        io.anuke.arc.backends.sdl.jni.SDLGL.glStencilOpSeparate(face, fail, zfail, zpass);}

    @Override public void glTexParameterfv(int target, int pname, FloatBuffer params){
        io.anuke.arc.backends.sdl.jni.SDLGL.glTexParameterfv(target, pname, params);}

    @Override public void glTexParameteri(int target, int pname, int param){
        io.anuke.arc.backends.sdl.jni.SDLGL.glTexParameteri(target, pname, param);}

    @Override public void glTexParameteriv(int target, int pname, IntBuffer params){
        io.anuke.arc.backends.sdl.jni.SDLGL.glTexParameteriv(target, pname, params);}

    @Override public void glUniform1f(int location, float x){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform1f(location, x);}

    @Override public void glUniform1fv(int location, int count, FloatBuffer v){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform1fv(location, count, v);}

    @Override public void glUniform1fv(int location, int count, float[] v, int offset){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform1fv(location, count, v, offset);}

    @Override public void glUniform1i(int location, int x){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform1i(location, x);}

    @Override public void glUniform1iv(int location, int count, IntBuffer v){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform1iv(location, count, v);}

    @Override public void glUniform1iv(int location, int count, int[] v, int offset){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform1iv(location, count, v, offset);}

    @Override public void glUniform2f(int location, float x, float y){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform2f(location, x, y);}

    @Override public void glUniform2fv(int location, int count, FloatBuffer v){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform2fv(location, count, v);}

    @Override public void glUniform2fv(int location, int count, float[] v, int offset){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform2fv(location, count, v, offset);}

    @Override public void glUniform2i(int location, int x, int y){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform2i(location, x, y);}

    @Override public void glUniform2iv(int location, int count, IntBuffer v){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform2iv(location, count, v);}

    @Override public void glUniform2iv(int location, int count, int[] v, int offset){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform2iv(location, count, v, offset);}

    @Override public void glUniform3f(int location, float x, float y, float z){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform3f(location, x, y, z);}

    @Override public void glUniform3fv(int location, int count, FloatBuffer v){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform3fv(location, count, v);}

    @Override public void glUniform3fv(int location, int count, float[] v, int offset){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform3fv(location, count, v, offset);}

    @Override public void glUniform3i(int location, int x, int y, int z){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform3i(location, x, y, z);}

    @Override public void glUniform3iv(int location, int count, IntBuffer v){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform3iv(location, count, v);}

    @Override public void glUniform3iv(int location, int count, int[] v, int offset){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform3iv(location, count, v, offset);}

    @Override public void glUniform4f(int location, float x, float y, float z, float w){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform4f(location, x, y, z, w);}

    @Override public void glUniform4fv(int location, int count, FloatBuffer v){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform4fv(location, count, v);}

    @Override public void glUniform4fv(int location, int count, float[] v, int offset){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform4fv(location, count, v, offset);}

    @Override public void glUniform4i(int location, int x, int y, int z, int w){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform4i(location, x, y, z, w);}

    @Override public void glUniform4iv(int location, int count, IntBuffer v){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform4iv(location, count, v);}

    @Override public void glUniform4iv(int location, int count, int[] v, int offset){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniform4iv(location, count, v, offset);}

    @Override public void glUniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer value){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniformMatrix2fv(location, count, transpose, value);}

    @Override public void glUniformMatrix2fv(int location, int count, boolean transpose, float[] value, int offset){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniformMatrix2fv(location, count, transpose, value, offset);}

    @Override public void glUniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer value){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniformMatrix3fv(location, count, transpose, value);}

    @Override public void glUniformMatrix3fv(int location, int count, boolean transpose, float[] value, int offset){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniformMatrix3fv(location, count, transpose, value, offset);}

    @Override public void glUniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer value){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniformMatrix4fv(location, count, transpose, value);}

    @Override public void glUniformMatrix4fv(int location, int count, boolean transpose, float[] value, int offset){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUniformMatrix4fv(location, count, transpose, value, offset);}

    @Override public void glUseProgram(int program){
        io.anuke.arc.backends.sdl.jni.SDLGL.glUseProgram(program);}

    @Override public void glValidateProgram(int program){
        io.anuke.arc.backends.sdl.jni.SDLGL.glValidateProgram(program);}

    @Override public void glVertexAttrib1f(int indx, float x){
        io.anuke.arc.backends.sdl.jni.SDLGL.glVertexAttrib1f(indx, x);}

    @Override public void glVertexAttrib1fv(int indx, FloatBuffer values){
        io.anuke.arc.backends.sdl.jni.SDLGL.glVertexAttrib1fv(indx, values);}

    @Override public void glVertexAttrib2f(int indx, float x, float y){
        io.anuke.arc.backends.sdl.jni.SDLGL.glVertexAttrib2f(indx, x, y);}

    @Override public void glVertexAttrib2fv(int indx, FloatBuffer values){
        io.anuke.arc.backends.sdl.jni.SDLGL.glVertexAttrib2fv(indx, values);}

    @Override public void glVertexAttrib3f(int indx, float x, float y, float z){
        io.anuke.arc.backends.sdl.jni.SDLGL.glVertexAttrib3f(indx, x, y, z);}

    @Override public void glVertexAttrib3fv(int indx, FloatBuffer values){
        io.anuke.arc.backends.sdl.jni.SDLGL.glVertexAttrib3fv(indx, values);}

    @Override public void glVertexAttrib4f(int indx, float x, float y, float z, float w){
        io.anuke.arc.backends.sdl.jni.SDLGL.glVertexAttrib4f(indx, x, y, z, w);}

    @Override public void glVertexAttrib4fv(int indx, FloatBuffer values){
        io.anuke.arc.backends.sdl.jni.SDLGL.glVertexAttrib4fv(indx, values);}

    @Override public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, Buffer ptr){
        io.anuke.arc.backends.sdl.jni.SDLGL.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);}

    @Override public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, int ptr){
        SDLGL.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);}}

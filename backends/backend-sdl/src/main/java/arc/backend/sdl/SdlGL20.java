package arc.backend.sdl;

import arc.backend.sdl.jni.SDLGL;
import arc.graphics.*;

import java.nio.*;

public class SdlGL20 implements GL20{
    @Override public void glActiveTexture(int texture){ SDLGL.glActiveTexture(texture);}
    @Override public void glBindTexture(int target, int texture){ SDLGL.glBindTexture(target, texture);}
    @Override public void glBlendFunc(int sfactor, int dfactor){ SDLGL.glBlendFunc(sfactor, dfactor);}
    @Override public void glClear(int mask){ SDLGL.glClear(mask);}
    @Override public void glClearColor(float red, float green, float blue, float alpha){ SDLGL.glClearColor(red, green, blue, alpha);}
    @Override public void glClearDepthf(float depth){ SDLGL.glClearDepthf(depth);}
    @Override public void glClearStencil(int s){ SDLGL.glClearStencil(s);}
    @Override public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha){ SDLGL.glColorMask(red, green, blue, alpha);}
    @Override public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, int imageSize, Buffer data){ SDLGL.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);}
    @Override public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int imageSize, Buffer data){ SDLGL.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);}
    @Override public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height, int border){ SDLGL.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);}
    @Override public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width, int height){ SDLGL.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);}
    @Override public void glCullFace(int mode){ SDLGL.glCullFace(mode);}
    @Override public void glDeleteTexture(int texture){ SDLGL.glDeleteTexture(texture);}
    @Override public void glDepthFunc(int func){ SDLGL.glDepthFunc(func);}
    @Override public void glDepthMask(boolean flag){ SDLGL.glDepthMask(flag);}
    @Override public void glDepthRangef(float zNear, float zFar){ SDLGL.glDepthRangef(zNear, zFar);}
    @Override public void glDisable(int cap){ SDLGL.glDisable(cap);}
    @Override public void glDrawArrays(int mode, int first, int count){ SDLGL.glDrawArrays(mode, first, count);}
    @Override public void glDrawElements(int mode, int count, int type, Buffer indices){ SDLGL.glDrawElements(mode, count, type, indices);}
    @Override public void glEnable(int cap){ SDLGL.glEnable(cap);}
    @Override public void glFinish(){ SDLGL.glFinish();}
    @Override public void glFlush(){ SDLGL.glFlush();}
    @Override public void glFrontFace(int mode){ SDLGL.glFrontFace(mode);}
    @Override public int glGenTexture(){return SDLGL.glGenTexture();}
    @Override public int glGetError(){return SDLGL.glGetError();}
    @Override public void glGetIntegerv(int pname, IntBuffer params){ SDLGL.glGetIntegerv(pname, params);}
    @Override public String glGetString(int name){return SDLGL.glGetString(name);}
    @Override public void glHint(int target, int mode){ SDLGL.glHint(target, mode);}
    @Override public void glLineWidth(float width){ SDLGL.glLineWidth(width);}
    @Override public void glPixelStorei(int pname, int param){ SDLGL.glPixelStorei(pname, param);}
    @Override public void glPolygonOffset(float factor, float units){ SDLGL.glPolygonOffset(factor, units);}
    @Override public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels){ SDLGL.glReadPixels(x, y, width, height, format, type, pixels);}
    @Override public void glScissor(int x, int y, int width, int height){ SDLGL.glScissor(x, y, width, height);}
    @Override public void glStencilFunc(int func, int ref, int mask){ SDLGL.glStencilFunc(func, ref, mask);}
    @Override public void glStencilMask(int mask){ SDLGL.glStencilMask(mask);}
    @Override public void glStencilOp(int fail, int zfail, int zpass){ SDLGL.glStencilOp(fail, zfail, zpass);}
    @Override public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels){ SDLGL.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);}
    @Override public void glTexParameterf(int target, int pname, float param){ SDLGL.glTexParameterf(target, pname, param);}
    @Override public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels){ SDLGL.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);}
    @Override public void glViewport(int x, int y, int width, int height){ SDLGL.glViewport(x, y, width, height);}
    @Override public void glAttachShader(int program, int shader){ SDLGL.glAttachShader(program, shader);}
    @Override public void glBindAttribLocation(int program, int index, String name){ SDLGL.glBindAttribLocation(program, index, name);}
    @Override public void glBindBuffer(int target, int buffer){ SDLGL.glBindBuffer(target, buffer);}
    @Override public void glBindFramebuffer(int target, int framebuffer){ SDLGL.glBindFramebuffer(target, framebuffer);}
    @Override public void glBindRenderbuffer(int target, int renderbuffer){ SDLGL.glBindRenderbuffer(target, renderbuffer);}
    @Override public void glBlendColor(float red, float green, float blue, float alpha){ SDLGL.glBlendColor(red, green, blue, alpha);}
    @Override public void glBlendEquation(int mode){ SDLGL.glBlendEquation(mode);}
    @Override public void glBlendEquationSeparate(int modeRGB, int modeAlpha){ SDLGL.glBlendEquationSeparate(modeRGB, modeAlpha);}
    @Override public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha){ SDLGL.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);}
    @Override public void glBufferData(int target, int size, Buffer data, int usage){ SDLGL.glBufferData(target, size, data, usage);}
    @Override public void glBufferSubData(int target, int offset, int size, Buffer data){ SDLGL.glBufferSubData(target, offset, size, data);}
    @Override public int glCheckFramebufferStatus(int target){return SDLGL.glCheckFramebufferStatus(target);}
    @Override public void glCompileShader(int shader){ SDLGL.glCompileShader(shader);}
    @Override public int glCreateProgram(){return SDLGL.glCreateProgram();}
    @Override public int glCreateShader(int type){return SDLGL.glCreateShader(type);}
    @Override public void glDeleteBuffer(int buffer){ SDLGL.glDeleteBuffer(buffer);}
    @Override public void glDeleteFramebuffer(int framebuffer){ SDLGL.glDeleteFramebuffer(framebuffer);}
    @Override public void glDeleteProgram(int program){ SDLGL.glDeleteProgram(program);}
    @Override public void glDeleteRenderbuffer(int renderbuffer){ SDLGL.glDeleteRenderbuffer(renderbuffer);}
    @Override public void glDeleteShader(int shader){ SDLGL.glDeleteShader(shader);}
    @Override public void glDetachShader(int program, int shader){ SDLGL.glDetachShader(program, shader);}
    @Override public void glDisableVertexAttribArray(int index){ SDLGL.glDisableVertexAttribArray(index);}
    @Override public void glDrawElements(int mode, int count, int type, int indices){ SDLGL.glDrawElements(mode, count, type, indices);}
    @Override public void glEnableVertexAttribArray(int index){ SDLGL.glEnableVertexAttribArray(index);}
    @Override public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer){ SDLGL.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);}
    @Override public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level){ SDLGL.glFramebufferTexture2D(target, attachment, textarget, texture, level);}
    @Override public int glGenBuffer(){return SDLGL.glGenBuffer();}
    @Override public void glGenerateMipmap(int target){ SDLGL.glGenerateMipmap(target);}
    @Override public int glGenFramebuffer(){return SDLGL.glGenFramebuffer();}
    @Override public int glGenRenderbuffer(){return SDLGL.glGenRenderbuffer();}
    @Override public String glGetActiveAttrib(int program, int index, IntBuffer size, IntBuffer type){return SDLGL.glGetActiveAttrib(program, index, size, type);}
    @Override public String glGetActiveUniform(int program, int index, IntBuffer size, IntBuffer type){return SDLGL.glGetActiveUniform(program, index, size, type);}
    @Override public int glGetAttribLocation(int program, String name){return SDLGL.glGetAttribLocation(program, name);}
    @Override public void glGetBooleanv(int pname, Buffer params){ SDLGL.glGetBooleanv(pname, params);}
    @Override public void glGetBufferParameteriv(int target, int pname, IntBuffer params){ SDLGL.glGetBufferParameteriv(target, pname, params);}
    @Override public void glGetFloatv(int pname, FloatBuffer params){ SDLGL.glGetFloatv(pname, params);}
    @Override public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params){ SDLGL.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);}
    @Override public void glGetProgramiv(int program, int pname, IntBuffer params){ SDLGL.glGetProgramiv(program, pname, params);}
    @Override public String glGetProgramInfoLog(int program){return SDLGL.glGetProgramInfoLog(program);}
    @Override public void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params){ SDLGL.glGetRenderbufferParameteriv(target, pname, params);}
    @Override public void glGetShaderiv(int shader, int pname, IntBuffer params){ SDLGL.glGetShaderiv(shader, pname, params);}
    @Override public String glGetShaderInfoLog(int shader){return SDLGL.glGetShaderInfoLog(shader);}
    @Override public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, IntBuffer range, IntBuffer precision){ SDLGL.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);}
    @Override public void glGetTexParameterfv(int target, int pname, FloatBuffer params){ SDLGL.glGetTexParameterfv(target, pname, params);}
    @Override public void glGetTexParameteriv(int target, int pname, IntBuffer params){ SDLGL.glGetTexParameteriv(target, pname, params);}
    @Override public void glGetUniformfv(int program, int location, FloatBuffer params){ SDLGL.glGetUniformfv(program, location, params);}
    @Override public void glGetUniformiv(int program, int location, IntBuffer params){ SDLGL.glGetUniformiv(program, location, params);}
    @Override public int glGetUniformLocation(int program, String name){return SDLGL.glGetUniformLocation(program, name);}
    @Override public void glGetVertexAttribfv(int index, int pname, FloatBuffer params){ SDLGL.glGetVertexAttribfv(index, pname, params);}
    @Override public void glGetVertexAttribiv(int index, int pname, IntBuffer params){ SDLGL.glGetVertexAttribiv(index, pname, params);}
    @Override public boolean glIsBuffer(int buffer){return SDLGL.glIsBuffer(buffer);}
    @Override public boolean glIsEnabled(int cap){return SDLGL.glIsEnabled(cap);}
    @Override public boolean glIsFramebuffer(int framebuffer){return SDLGL.glIsFramebuffer(framebuffer);}
    @Override public boolean glIsProgram(int program){return SDLGL.glIsProgram(program);}
    @Override public boolean glIsRenderbuffer(int renderbuffer){return SDLGL.glIsRenderbuffer(renderbuffer);}
    @Override public boolean glIsShader(int shader){return SDLGL.glIsShader(shader);}
    @Override public boolean glIsTexture(int texture){return SDLGL.glIsTexture(texture);}
    @Override public void glLinkProgram(int program){ SDLGL.glLinkProgram(program);}
    @Override public void glReleaseShaderCompiler(){ SDLGL.glReleaseShaderCompiler();}
    @Override public void glRenderbufferStorage(int target, int internalformat, int width, int height){ SDLGL.glRenderbufferStorage(target, internalformat, width, height);}
    @Override public void glSampleCoverage(float value, boolean invert){ SDLGL.glSampleCoverage(value, invert);}
    @Override public void glShaderSource(int shader, String string){ SDLGL.glShaderSource(shader, string);}
    @Override public void glStencilFuncSeparate(int face, int func, int ref, int mask){ SDLGL.glStencilFuncSeparate(face, func, ref, mask);}
    @Override public void glStencilMaskSeparate(int face, int mask){ SDLGL.glStencilMaskSeparate(face, mask);}
    @Override public void glStencilOpSeparate(int face, int fail, int zfail, int zpass){ SDLGL.glStencilOpSeparate(face, fail, zfail, zpass);}
    @Override public void glTexParameterfv(int target, int pname, FloatBuffer params){ SDLGL.glTexParameterfv(target, pname, params);}
    @Override public void glTexParameteri(int target, int pname, int param){ SDLGL.glTexParameteri(target, pname, param);}
    @Override public void glTexParameteriv(int target, int pname, IntBuffer params){ SDLGL.glTexParameteriv(target, pname, params);}
    @Override public void glUniform1f(int location, float x){ SDLGL.glUniform1f(location, x);}
    @Override public void glUniform1fv(int location, int count, FloatBuffer v){ SDLGL.glUniform1fv(location, count, v);}
    @Override public void glUniform1fv(int location, int count, float[] v, int offset){ SDLGL.glUniform1fv(location, count, v, offset);}
    @Override public void glUniform1i(int location, int x){ SDLGL.glUniform1i(location, x);}
    @Override public void glUniform1iv(int location, int count, IntBuffer v){ SDLGL.glUniform1iv(location, count, v);}
    @Override public void glUniform1iv(int location, int count, int[] v, int offset){ SDLGL.glUniform1iv(location, count, v, offset);}
    @Override public void glUniform2f(int location, float x, float y){ SDLGL.glUniform2f(location, x, y);}
    @Override public void glUniform2fv(int location, int count, FloatBuffer v){ SDLGL.glUniform2fv(location, count, v);}
    @Override public void glUniform2fv(int location, int count, float[] v, int offset){ SDLGL.glUniform2fv(location, count, v, offset);}
    @Override public void glUniform2i(int location, int x, int y){ SDLGL.glUniform2i(location, x, y);}
    @Override public void glUniform2iv(int location, int count, IntBuffer v){ SDLGL.glUniform2iv(location, count, v);}
    @Override public void glUniform2iv(int location, int count, int[] v, int offset){ SDLGL.glUniform2iv(location, count, v, offset);}
    @Override public void glUniform3f(int location, float x, float y, float z){ SDLGL.glUniform3f(location, x, y, z);}
    @Override public void glUniform3fv(int location, int count, FloatBuffer v){ SDLGL.glUniform3fv(location, count, v);}
    @Override public void glUniform3fv(int location, int count, float[] v, int offset){ SDLGL.glUniform3fv(location, count, v, offset);}
    @Override public void glUniform3i(int location, int x, int y, int z){ SDLGL.glUniform3i(location, x, y, z);}
    @Override public void glUniform3iv(int location, int count, IntBuffer v){ SDLGL.glUniform3iv(location, count, v);}
    @Override public void glUniform3iv(int location, int count, int[] v, int offset){ SDLGL.glUniform3iv(location, count, v, offset);}
    @Override public void glUniform4f(int location, float x, float y, float z, float w){ SDLGL.glUniform4f(location, x, y, z, w);}
    @Override public void glUniform4fv(int location, int count, FloatBuffer v){ SDLGL.glUniform4fv(location, count, v);}
    @Override public void glUniform4fv(int location, int count, float[] v, int offset){ SDLGL.glUniform4fv(location, count, v, offset);}
    @Override public void glUniform4i(int location, int x, int y, int z, int w){ SDLGL.glUniform4i(location, x, y, z, w);}
    @Override public void glUniform4iv(int location, int count, IntBuffer v){ SDLGL.glUniform4iv(location, count, v);}
    @Override public void glUniform4iv(int location, int count, int[] v, int offset){ SDLGL.glUniform4iv(location, count, v, offset);}
    @Override public void glUniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer value){ SDLGL.glUniformMatrix2fv(location, count, transpose, value);}
    @Override public void glUniformMatrix2fv(int location, int count, boolean transpose, float[] value, int offset){ SDLGL.glUniformMatrix2fv(location, count, transpose, value, offset);}
    @Override public void glUniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer value){ SDLGL.glUniformMatrix3fv(location, count, transpose, value);}
    @Override public void glUniformMatrix3fv(int location, int count, boolean transpose, float[] value, int offset){ SDLGL.glUniformMatrix3fv(location, count, transpose, value, offset);}
    @Override public void glUniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer value){ SDLGL.glUniformMatrix4fv(location, count, transpose, value);}
    @Override public void glUniformMatrix4fv(int location, int count, boolean transpose, float[] value, int offset){ SDLGL.glUniformMatrix4fv(location, count, transpose, value, offset);}
    @Override public void glUseProgram(int program){ SDLGL.glUseProgram(program);}
    @Override public void glValidateProgram(int program){ SDLGL.glValidateProgram(program);}
    @Override public void glVertexAttrib1f(int indx, float x){ SDLGL.glVertexAttrib1f(indx, x);}
    @Override public void glVertexAttrib1fv(int indx, FloatBuffer values){ SDLGL.glVertexAttrib1fv(indx, values);}
    @Override public void glVertexAttrib2f(int indx, float x, float y){ SDLGL.glVertexAttrib2f(indx, x, y);}
    @Override public void glVertexAttrib2fv(int indx, FloatBuffer values){ SDLGL.glVertexAttrib2fv(indx, values);}
    @Override public void glVertexAttrib3f(int indx, float x, float y, float z){ SDLGL.glVertexAttrib3f(indx, x, y, z);}
    @Override public void glVertexAttrib3fv(int indx, FloatBuffer values){ SDLGL.glVertexAttrib3fv(indx, values);}
    @Override public void glVertexAttrib4f(int indx, float x, float y, float z, float w){ SDLGL.glVertexAttrib4f(indx, x, y, z, w);}
    @Override public void glVertexAttrib4fv(int indx, FloatBuffer values){ SDLGL.glVertexAttrib4fv(indx, values);}
    @Override public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, Buffer ptr){ SDLGL.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);}
    @Override public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, int ptr){ SDLGL.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);}}

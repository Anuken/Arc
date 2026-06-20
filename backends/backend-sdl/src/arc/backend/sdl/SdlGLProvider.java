package arc.backend.sdl;

import arc.backend.sdl.jni.*;
import arc.graphics.gl.*;

import java.nio.*;

public class SdlGLProvider implements GLProvider{
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
    @Override public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, int ptr){ SDLGL.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);}

    @Override public void glReadBuffer(int mode){ SDLGL.glReadBuffer(mode); }
    @Override public void glDrawRangeElements(int mode, int start, int end, int count, int type, int offset){ SDLGL.glDrawRangeElements(mode, start, end, count, type, offset); }
    @Override public void glDrawRangeElements(int mode, int start, int end, int count, int type, Buffer indices){ SDLGL.glDrawRangeElements(mode, start, end, count, type, indices); }
    @Override public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, int offset){ SDLGL.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, offset); }
    @Override public void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int format, int type, Buffer pixels){ SDLGL.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels); }
    @Override public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, int offset){ SDLGL.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, offset); }
    @Override public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, Buffer pixels){ SDLGL.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels); }
    @Override public void glCopyTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width, int height){ SDLGL.glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height); }
    @Override public void glGenQueries(int n, IntBuffer ids){ SDLGL.glGenQueries(n, ids); }
    @Override public void glDeleteQueries(int n, IntBuffer ids){ SDLGL.glDeleteQueries(n, ids); }
    @Override public boolean glIsQuery(int id){ return SDLGL.glIsQuery(id); }
    @Override public void glBeginQuery(int target, int id){ SDLGL.glBeginQuery(target, id); }
    @Override public void glEndQuery(int target){ SDLGL.glEndQuery(target); }
    @Override public void glGetQueryiv(int target, int pname, IntBuffer params){ SDLGL.glGetQueryiv(target, pname, params); }
    @Override public void glGetQueryObjectuiv(int id, int pname, IntBuffer params){ SDLGL.glGetQueryObjectuiv(id, pname, params); }
    @Override public boolean glUnmapBuffer(int target){ return SDLGL.glUnmapBuffer(target); }
    @Override public Buffer glGetBufferPointerv(int target, int pname){ return SDLGL.glGetBufferPointerv(target, pname); }
    @Override public void glDrawBuffers(int n, IntBuffer bufs){ SDLGL.glDrawBuffers(n, bufs); }
    @Override public void glUniformMatrix2x3fv(int location, int count, boolean transpose, FloatBuffer value){ SDLGL.glUniformMatrix2x3fv(location, count, transpose, value); }
    @Override public void glUniformMatrix3x2fv(int location, int count, boolean transpose, FloatBuffer value){ SDLGL.glUniformMatrix3x2fv(location, count, transpose, value); }
    @Override public void glUniformMatrix2x4fv(int location, int count, boolean transpose, FloatBuffer value){ SDLGL.glUniformMatrix2x4fv(location, count, transpose, value); }
    @Override public void glUniformMatrix4x2fv(int location, int count, boolean transpose, FloatBuffer value){ SDLGL.glUniformMatrix4x2fv(location, count, transpose, value); }
    @Override public void glUniformMatrix3x4fv(int location, int count, boolean transpose, FloatBuffer value){ SDLGL.glUniformMatrix3x4fv(location, count, transpose, value); }
    @Override public void glUniformMatrix4x3fv(int location, int count, boolean transpose, FloatBuffer value){ SDLGL.glUniformMatrix4x3fv(location, count, transpose, value); }
    @Override public void glBlitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter){ SDLGL.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter); }
    @Override public void glRenderbufferStorageMultisample(int target, int samples, int internalformat, int width, int height){ SDLGL.glRenderbufferStorageMultisample(target, samples, internalformat, width, height); }
    @Override public void glFramebufferTextureLayer(int target, int attachment, int texture, int level, int layer){ SDLGL.glFramebufferTextureLayer(target, attachment, texture, level, layer); }
    @Override public void glFlushMappedBufferRange(int target, int offset, int length){ SDLGL.glFlushMappedBufferRange(target, offset, length); }
    @Override public void glBindVertexArray(int array){ SDLGL.glBindVertexArray(array); }
    @Override public void glDeleteVertexArrays(int n, IntBuffer arrays){ SDLGL.glDeleteVertexArrays(n, arrays); }
    @Override public void glGenVertexArrays(int n, IntBuffer arrays){ SDLGL.glGenVertexArrays(n, arrays); }
    @Override public boolean glIsVertexArray(int array){ return SDLGL.glIsVertexArray(array); }
    @Override public void glBeginTransformFeedback(int primitiveMode){ SDLGL.glBeginTransformFeedback(primitiveMode); }
    @Override public void glEndTransformFeedback(){ SDLGL.glEndTransformFeedback(); }
    @Override public void glBindBufferRange(int target, int index, int buffer, int offset, int size){ SDLGL.glBindBufferRange(target, index, buffer, offset, size); }
    @Override public void glBindBufferBase(int target, int index, int buffer){ SDLGL.glBindBufferBase(target, index, buffer); }
    @Override public void glTransformFeedbackVaryings(int program, String[] varyings, int bufferMode){ SDLGL.glTransformFeedbackVaryings(program, varyings, bufferMode); }
    @Override public void glVertexAttribIPointer(int index, int size, int type, int stride, int offset){ SDLGL.glVertexAttribIPointer(index, size, type, stride, offset); }
    @Override public void glGetVertexAttribIiv(int index, int pname, IntBuffer params){ SDLGL.glGetVertexAttribIiv(index, pname, params); }
    @Override public void glGetVertexAttribIuiv(int index, int pname, IntBuffer params){ SDLGL.glGetVertexAttribIuiv(index, pname, params); }
    @Override public void glVertexAttribI4i(int index, int x, int y, int z, int w){ SDLGL.glVertexAttribI4i(index, x, y, z, w); }
    @Override public void glVertexAttribI4ui(int index, int x, int y, int z, int w){ SDLGL.glVertexAttribI4ui(index, x, y, z, w); }
    @Override public void glGetUniformuiv(int program, int location, IntBuffer params){ SDLGL.glGetUniformuiv(program, location, params); }
    @Override public int glGetFragDataLocation(int program, String name){ return SDLGL.glGetFragDataLocation(program, name); }
    @Override public void glUniform1uiv(int location, int count, IntBuffer value){ SDLGL.glUniform1uiv(location, count, value); }
    @Override public void glUniform3uiv(int location, int count, IntBuffer value){ SDLGL.glUniform3uiv(location, count, value); }
    @Override public void glUniform4uiv(int location, int count, IntBuffer value){ SDLGL.glUniform4uiv(location, count, value); }
    @Override public void glClearBufferiv(int buffer, int drawbuffer, IntBuffer value){ SDLGL.glClearBufferiv(buffer, drawbuffer, value); }
    @Override public void glClearBufferuiv(int buffer, int drawbuffer, IntBuffer value){ SDLGL.glClearBufferuiv(buffer, drawbuffer, value); }
    @Override public void glClearBufferfv(int buffer, int drawbuffer, FloatBuffer value){ SDLGL.glClearBufferfv(buffer, drawbuffer, value); }
    @Override public void glClearBufferfi(int buffer, int drawbuffer, float depth, int stencil){ SDLGL.glClearBufferfi(buffer, drawbuffer, depth, stencil); }
    @Override public String glGetStringi(int name, int index){ return SDLGL.glGetStringi(name, index); }
    @Override public void glCopyBufferSubData(int readTarget, int writeTarget, int readOffset, int writeOffset, int size){ SDLGL.glCopyBufferSubData(readTarget, writeTarget, readOffset, writeOffset, size); }
    @Override public void glGetUniformIndices(int program, String[] uniformNames, IntBuffer uniformIndices){ SDLGL.glGetUniformIndices(program, uniformNames, uniformIndices); }
    @Override public void glGetActiveUniformsiv(int program, int uniformCount, IntBuffer uniformIndices, int pname, IntBuffer params){ SDLGL.glGetActiveUniformsiv(program, uniformCount, uniformIndices, pname, params); }
    @Override public int glGetUniformBlockIndex(int program, String uniformBlockName){ return SDLGL.glGetUniformBlockIndex(program, uniformBlockName); }
    @Override public void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, IntBuffer params){ SDLGL.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname, params); }
    @Override public void glGetActiveUniformBlockName(int program, int uniformBlockIndex, Buffer length, Buffer uniformBlockName){ SDLGL.glGetActiveUniformBlockName(program, uniformBlockIndex, length, uniformBlockName); }
    @Override public void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding){ SDLGL.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding); }
    @Override public void glDrawArraysInstanced(int mode, int first, int count, int instanceCount){ SDLGL.glDrawArraysInstanced(mode, first, count, instanceCount); }
    @Override public void glDrawElementsInstanced(int mode, int count, int type, int indicesOffset, int instanceCount){ SDLGL.glDrawElementsInstanced(mode, count, type, indicesOffset, instanceCount); }
    @Override public void glGetInteger64v(int pname, LongBuffer params){ SDLGL.glGetInteger64v(pname, params); }
    @Override public void glGetBufferParameteri64v(int target, int pname, LongBuffer params){ SDLGL.glGetBufferParameteri64v(target, pname, params); }
    @Override public void glGenSamplers(int count, IntBuffer samplers){ SDLGL.glGenSamplers(count, samplers); }
    @Override public void glDeleteSamplers(int count, IntBuffer samplers){ SDLGL.glDeleteSamplers(count, samplers); }
    @Override public boolean glIsSampler(int sampler){ return SDLGL.glIsSampler(sampler); }
    @Override public void glBindSampler(int unit, int sampler){ SDLGL.glBindSampler(unit, sampler); }
    @Override public void glSamplerParameteri(int sampler, int pname, int param){ SDLGL.glSamplerParameteri(sampler, pname, param); }
    @Override public void glSamplerParameteriv(int sampler, int pname, IntBuffer param){ SDLGL.glSamplerParameteriv(sampler, pname, param); }
    @Override public void glSamplerParameterf(int sampler, int pname, float param){ SDLGL.glSamplerParameterf(sampler, pname, param); }
    @Override public void glSamplerParameterfv(int sampler, int pname, FloatBuffer param){ SDLGL.glSamplerParameterfv(sampler, pname, param); }
    @Override public void glGetSamplerParameteriv(int sampler, int pname, IntBuffer params){ SDLGL.glGetSamplerParameteriv(sampler, pname, params); }
    @Override public void glGetSamplerParameterfv(int sampler, int pname, FloatBuffer params){ SDLGL.glGetSamplerParameterfv(sampler, pname, params); }
    @Override public void glVertexAttribDivisor(int index, int divisor){ SDLGL.glVertexAttribDivisor(index, divisor); }
    @Override public void glBindTransformFeedback(int target, int id){ SDLGL.glBindTransformFeedback(target, id); }
    @Override public void glDeleteTransformFeedbacks(int n, IntBuffer ids){ SDLGL.glDeleteTransformFeedbacks(n, ids); }
    @Override public void glGenTransformFeedbacks(int n, IntBuffer ids){ SDLGL.glGenTransformFeedbacks(n, ids); }
    @Override public boolean glIsTransformFeedback(int id){ return SDLGL.glIsTransformFeedback(id); }
    @Override public void glPauseTransformFeedback(){ SDLGL.glPauseTransformFeedback(); }
    @Override public void glResumeTransformFeedback(){ SDLGL.glResumeTransformFeedback(); }
    @Override public void glProgramParameteri(int program, int pname, int value){ SDLGL.glProgramParameteri(program, pname, value); }
    @Override public void glInvalidateFramebuffer(int target, int numAttachments, IntBuffer attachments){ SDLGL.glInvalidateFramebuffer(target, numAttachments, attachments); }
    @Override public void glInvalidateSubFramebuffer(int target, int numAttachments, IntBuffer attachments, int x, int y, int width, int height){ SDLGL.glInvalidateSubFramebuffer(target, numAttachments, attachments, x, y, width, height); }

}

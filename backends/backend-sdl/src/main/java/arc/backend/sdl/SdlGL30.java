package arc.backend.sdl;

import arc.backend.sdl.jni.*;
import arc.graphics.*;

import java.nio.*;

public class SdlGL30 extends SdlGL20 implements GL30{
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
    @Override public String glGetActiveUniformBlockName(int program, int uniformBlockIndex){ return SDLGL.glGetActiveUniformBlockName(program, uniformBlockIndex); }
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

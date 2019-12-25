/*
 **
 ** Copyright 2013, The Android Open Source Project
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **     http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 */

// This source file is automatically generated

package arc.graphics;

/** OpenGL ES 3.0 */
public interface GL30 extends GL20{
    int GL_READ_BUFFER = 0x0C02;
    int GL_UNPACK_ROW_LENGTH = 0x0CF2;
    int GL_UNPACK_SKIP_ROWS = 0x0CF3;
    int GL_UNPACK_SKIP_PIXELS = 0x0CF4;
    int GL_PACK_ROW_LENGTH = 0x0D02;
    int GL_PACK_SKIP_ROWS = 0x0D03;
    int GL_PACK_SKIP_PIXELS = 0x0D04;
    int GL_COLOR = 0x1800;
    int GL_DEPTH = 0x1801;
    int GL_STENCIL = 0x1802;
    int GL_RED = 0x1903;
    int GL_RGB8 = 0x8051;
    int GL_RGBA8 = 0x8058;
    int GL_RGB10_A2 = 0x8059;
    int GL_TEXTURE_BINDING_3D = 0x806A;
    int GL_UNPACK_SKIP_IMAGES = 0x806D;
    int GL_UNPACK_IMAGE_HEIGHT = 0x806E;
    int GL_TEXTURE_3D = 0x806F;
    int GL_TEXTURE_WRAP_R = 0x8072;
    int GL_MAX_3D_TEXTURE_SIZE = 0x8073;
    int GL_UNSIGNED_INT_2_10_10_10_REV = 0x8368;
    int GL_MAX_ELEMENTS_VERTICES = 0x80E8;
    int GL_MAX_ELEMENTS_INDICES = 0x80E9;
    int GL_TEXTURE_MIN_LOD = 0x813A;
    int GL_TEXTURE_MAX_LOD = 0x813B;
    int GL_TEXTURE_BASE_LEVEL = 0x813C;
    int GL_TEXTURE_MAX_LEVEL = 0x813D;
    int GL_MIN = 0x8007;
    int GL_MAX = 0x8008;
    int GL_DEPTH_COMPONENT24 = 0x81A6;
    int GL_MAX_TEXTURE_LOD_BIAS = 0x84FD;
    int GL_TEXTURE_COMPARE_MODE = 0x884C;
    int GL_TEXTURE_COMPARE_FUNC = 0x884D;
    int GL_CURRENT_QUERY = 0x8865;
    int GL_QUERY_RESULT = 0x8866;
    int GL_QUERY_RESULT_AVAILABLE = 0x8867;
    int GL_BUFFER_MAPPED = 0x88BC;
    int GL_BUFFER_MAP_POINTER = 0x88BD;
    int GL_STREAM_READ = 0x88E1;
    int GL_STREAM_COPY = 0x88E2;
    int GL_STATIC_READ = 0x88E5;
    int GL_STATIC_COPY = 0x88E6;
    int GL_DYNAMIC_READ = 0x88E9;
    int GL_DYNAMIC_COPY = 0x88EA;
    int GL_MAX_DRAW_BUFFERS = 0x8824;
    int GL_DRAW_BUFFER0 = 0x8825;
    int GL_DRAW_BUFFER1 = 0x8826;
    int GL_DRAW_BUFFER2 = 0x8827;
    int GL_DRAW_BUFFER3 = 0x8828;
    int GL_DRAW_BUFFER4 = 0x8829;
    int GL_DRAW_BUFFER5 = 0x882A;
    int GL_DRAW_BUFFER6 = 0x882B;
    int GL_DRAW_BUFFER7 = 0x882C;
    int GL_DRAW_BUFFER8 = 0x882D;
    int GL_DRAW_BUFFER9 = 0x882E;
    int GL_DRAW_BUFFER10 = 0x882F;
    int GL_DRAW_BUFFER11 = 0x8830;
    int GL_DRAW_BUFFER12 = 0x8831;
    int GL_DRAW_BUFFER13 = 0x8832;
    int GL_DRAW_BUFFER14 = 0x8833;
    int GL_DRAW_BUFFER15 = 0x8834;
    int GL_MAX_FRAGMENT_UNIFORM_COMPONENTS = 0x8B49;
    int GL_MAX_VERTEX_UNIFORM_COMPONENTS = 0x8B4A;
    int GL_SAMPLER_3D = 0x8B5F;
    int GL_SAMPLER_2D_SHADOW = 0x8B62;
    int GL_FRAGMENT_SHADER_DERIVATIVE_HINT = 0x8B8B;
    int GL_PIXEL_PACK_BUFFER = 0x88EB;
    int GL_PIXEL_UNPACK_BUFFER = 0x88EC;
    int GL_PIXEL_PACK_BUFFER_BINDING = 0x88ED;
    int GL_PIXEL_UNPACK_BUFFER_BINDING = 0x88EF;
    int GL_FLOAT_MAT2x3 = 0x8B65;
    int GL_FLOAT_MAT2x4 = 0x8B66;
    int GL_FLOAT_MAT3x2 = 0x8B67;
    int GL_FLOAT_MAT3x4 = 0x8B68;
    int GL_FLOAT_MAT4x2 = 0x8B69;
    int GL_FLOAT_MAT4x3 = 0x8B6A;
    int GL_SRGB = 0x8C40;
    int GL_SRGB8 = 0x8C41;
    int GL_SRGB8_ALPHA8 = 0x8C43;
    int GL_COMPARE_REF_TO_TEXTURE = 0x884E;
    int GL_MAJOR_VERSION = 0x821B;
    int GL_MINOR_VERSION = 0x821C;
    int GL_NUM_EXTENSIONS = 0x821D;
    int GL_RGBA32F = 0x8814;
    int GL_RGB32F = 0x8815;
    int GL_RGBA16F = 0x881A;
    int GL_RGB16F = 0x881B;
    int GL_VERTEX_ATTRIB_ARRAY_INTEGER = 0x88FD;
    int GL_MAX_ARRAY_TEXTURE_LAYERS = 0x88FF;
    int GL_MIN_PROGRAM_TEXEL_OFFSET = 0x8904;
    int GL_MAX_PROGRAM_TEXEL_OFFSET = 0x8905;
    int GL_MAX_VARYING_COMPONENTS = 0x8B4B;
    int GL_TEXTURE_2D_ARRAY = 0x8C1A;
    int GL_TEXTURE_BINDING_2D_ARRAY = 0x8C1D;
    int GL_R11F_G11F_B10F = 0x8C3A;
    int GL_UNSIGNED_INT_10F_11F_11F_REV = 0x8C3B;
    int GL_RGB9_E5 = 0x8C3D;
    int GL_UNSIGNED_INT_5_9_9_9_REV = 0x8C3E;
    int GL_TRANSFORM_FEEDBACK_VARYING_MAX_LENGTH = 0x8C76;
    int GL_TRANSFORM_FEEDBACK_BUFFER_MODE = 0x8C7F;
    int GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_COMPONENTS = 0x8C80;
    int GL_TRANSFORM_FEEDBACK_VARYINGS = 0x8C83;
    int GL_TRANSFORM_FEEDBACK_BUFFER_START = 0x8C84;
    int GL_TRANSFORM_FEEDBACK_BUFFER_SIZE = 0x8C85;
    int GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN = 0x8C88;
    int GL_RASTERIZER_DISCARD = 0x8C89;
    int GL_MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS = 0x8C8A;
    int GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS = 0x8C8B;
    int GL_INTERLEAVED_ATTRIBS = 0x8C8C;
    int GL_SEPARATE_ATTRIBS = 0x8C8D;
    int GL_TRANSFORM_FEEDBACK_BUFFER = 0x8C8E;
    int GL_TRANSFORM_FEEDBACK_BUFFER_BINDING = 0x8C8F;
    int GL_RGBA32UI = 0x8D70;
    int GL_RGB32UI = 0x8D71;
    int GL_RGBA16UI = 0x8D76;
    int GL_RGB16UI = 0x8D77;
    int GL_RGBA8UI = 0x8D7C;
    int GL_RGB8UI = 0x8D7D;
    int GL_RGBA32I = 0x8D82;
    int GL_RGB32I = 0x8D83;
    int GL_RGBA16I = 0x8D88;
    int GL_RGB16I = 0x8D89;
    int GL_RGBA8I = 0x8D8E;
    int GL_RGB8I = 0x8D8F;
    int GL_RED_INTEGER = 0x8D94;
    int GL_RGB_INTEGER = 0x8D98;
    int GL_RGBA_INTEGER = 0x8D99;
    int GL_SAMPLER_2D_ARRAY = 0x8DC1;
    int GL_SAMPLER_2D_ARRAY_SHADOW = 0x8DC4;
    int GL_SAMPLER_CUBE_SHADOW = 0x8DC5;
    int GL_UNSIGNED_INT_VEC2 = 0x8DC6;
    int GL_UNSIGNED_INT_VEC3 = 0x8DC7;
    int GL_UNSIGNED_INT_VEC4 = 0x8DC8;
    int GL_INT_SAMPLER_2D = 0x8DCA;
    int GL_INT_SAMPLER_3D = 0x8DCB;
    int GL_INT_SAMPLER_CUBE = 0x8DCC;
    int GL_INT_SAMPLER_2D_ARRAY = 0x8DCF;
    int GL_UNSIGNED_INT_SAMPLER_2D = 0x8DD2;
    int GL_UNSIGNED_INT_SAMPLER_3D = 0x8DD3;
    int GL_UNSIGNED_INT_SAMPLER_CUBE = 0x8DD4;
    int GL_UNSIGNED_INT_SAMPLER_2D_ARRAY = 0x8DD7;
    int GL_BUFFER_ACCESS_FLAGS = 0x911F;
    int GL_BUFFER_MAP_LENGTH = 0x9120;
    int GL_BUFFER_MAP_OFFSET = 0x9121;
    int GL_DEPTH_COMPONENT32F = 0x8CAC;
    int GL_DEPTH32F_STENCIL8 = 0x8CAD;
    int GL_FLOAT_32_UNSIGNED_INT_24_8_REV = 0x8DAD;
    int GL_FRAMEBUFFER_ATTACHMENT_COLOR_ENCODING = 0x8210;
    int GL_FRAMEBUFFER_ATTACHMENT_COMPONENT_TYPE = 0x8211;
    int GL_FRAMEBUFFER_ATTACHMENT_RED_SIZE = 0x8212;
    int GL_FRAMEBUFFER_ATTACHMENT_GREEN_SIZE = 0x8213;
    int GL_FRAMEBUFFER_ATTACHMENT_BLUE_SIZE = 0x8214;
    int GL_FRAMEBUFFER_ATTACHMENT_ALPHA_SIZE = 0x8215;
    int GL_FRAMEBUFFER_ATTACHMENT_DEPTH_SIZE = 0x8216;
    int GL_FRAMEBUFFER_ATTACHMENT_STENCIL_SIZE = 0x8217;
    int GL_FRAMEBUFFER_DEFAULT = 0x8218;
    int GL_FRAMEBUFFER_UNDEFINED = 0x8219;
    int GL_DEPTH_STENCIL_ATTACHMENT = 0x821A;
    int GL_DEPTH_STENCIL = 0x84F9;
    int GL_UNSIGNED_INT_24_8 = 0x84FA;
    int GL_DEPTH24_STENCIL8 = 0x88F0;
    int GL_UNSIGNED_NORMALIZED = 0x8C17;
    int GL_DRAW_FRAMEBUFFER_BINDING = GL_FRAMEBUFFER_BINDING;
    int GL_READ_FRAMEBUFFER = 0x8CA8;
    int GL_DRAW_FRAMEBUFFER = 0x8CA9;
    int GL_READ_FRAMEBUFFER_BINDING = 0x8CAA;
    int GL_RENDERBUFFER_SAMPLES = 0x8CAB;
    int GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LAYER = 0x8CD4;
    int GL_MAX_COLOR_ATTACHMENTS = 0x8CDF;
    int GL_COLOR_ATTACHMENT1 = 0x8CE1;
    int GL_COLOR_ATTACHMENT2 = 0x8CE2;
    int GL_COLOR_ATTACHMENT3 = 0x8CE3;
    int GL_COLOR_ATTACHMENT4 = 0x8CE4;
    int GL_COLOR_ATTACHMENT5 = 0x8CE5;
    int GL_COLOR_ATTACHMENT6 = 0x8CE6;
    int GL_COLOR_ATTACHMENT7 = 0x8CE7;
    int GL_COLOR_ATTACHMENT8 = 0x8CE8;
    int GL_COLOR_ATTACHMENT9 = 0x8CE9;
    int GL_COLOR_ATTACHMENT10 = 0x8CEA;
    int GL_COLOR_ATTACHMENT11 = 0x8CEB;
    int GL_COLOR_ATTACHMENT12 = 0x8CEC;
    int GL_COLOR_ATTACHMENT13 = 0x8CED;
    int GL_COLOR_ATTACHMENT14 = 0x8CEE;
    int GL_COLOR_ATTACHMENT15 = 0x8CEF;
    int GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE = 0x8D56;
    int GL_MAX_SAMPLES = 0x8D57;
    int GL_HALF_FLOAT = 0x140B;
    int GL_MAP_READ_BIT = 0x0001;
    int GL_MAP_WRITE_BIT = 0x0002;
    int GL_MAP_INVALIDATE_RANGE_BIT = 0x0004;
    int GL_MAP_INVALIDATE_BUFFER_BIT = 0x0008;
    int GL_MAP_FLUSH_EXPLICIT_BIT = 0x0010;
    int GL_MAP_UNSYNCHRONIZED_BIT = 0x0020;
    int GL_RG = 0x8227;
    int GL_RG_INTEGER = 0x8228;
    int GL_R8 = 0x8229;
    int GL_RG8 = 0x822B;
    int GL_R16F = 0x822D;
    int GL_R32F = 0x822E;
    int GL_RG16F = 0x822F;
    int GL_RG32F = 0x8230;
    int GL_R8I = 0x8231;
    int GL_R8UI = 0x8232;
    int GL_R16I = 0x8233;
    int GL_R16UI = 0x8234;
    int GL_R32I = 0x8235;
    int GL_R32UI = 0x8236;
    int GL_RG8I = 0x8237;
    int GL_RG8UI = 0x8238;
    int GL_RG16I = 0x8239;
    int GL_RG16UI = 0x823A;
    int GL_RG32I = 0x823B;
    int GL_RG32UI = 0x823C;
    int GL_VERTEX_ARRAY_BINDING = 0x85B5;
    int GL_R8_SNORM = 0x8F94;
    int GL_RG8_SNORM = 0x8F95;
    int GL_RGB8_SNORM = 0x8F96;
    int GL_RGBA8_SNORM = 0x8F97;
    int GL_SIGNED_NORMALIZED = 0x8F9C;
    int GL_PRIMITIVE_RESTART_FIXED_INDEX = 0x8D69;
    int GL_COPY_READ_BUFFER = 0x8F36;
    int GL_COPY_WRITE_BUFFER = 0x8F37;
    int GL_COPY_READ_BUFFER_BINDING = GL_COPY_READ_BUFFER;
    int GL_COPY_WRITE_BUFFER_BINDING = GL_COPY_WRITE_BUFFER;
    int GL_UNIFORM_BUFFER = 0x8A11;
    int GL_UNIFORM_BUFFER_BINDING = 0x8A28;
    int GL_UNIFORM_BUFFER_START = 0x8A29;
    int GL_UNIFORM_BUFFER_SIZE = 0x8A2A;
    int GL_MAX_VERTEX_UNIFORM_BLOCKS = 0x8A2B;
    int GL_MAX_FRAGMENT_UNIFORM_BLOCKS = 0x8A2D;
    int GL_MAX_COMBINED_UNIFORM_BLOCKS = 0x8A2E;
    int GL_MAX_UNIFORM_BUFFER_BINDINGS = 0x8A2F;
    int GL_MAX_UNIFORM_BLOCK_SIZE = 0x8A30;
    int GL_MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS = 0x8A31;
    int GL_MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS = 0x8A33;
    int GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT = 0x8A34;
    int GL_ACTIVE_UNIFORM_BLOCK_MAX_NAME_LENGTH = 0x8A35;
    int GL_ACTIVE_UNIFORM_BLOCKS = 0x8A36;
    int GL_UNIFORM_TYPE = 0x8A37;
    int GL_UNIFORM_SIZE = 0x8A38;
    int GL_UNIFORM_NAME_LENGTH = 0x8A39;
    int GL_UNIFORM_BLOCK_INDEX = 0x8A3A;
    int GL_UNIFORM_OFFSET = 0x8A3B;
    int GL_UNIFORM_ARRAY_STRIDE = 0x8A3C;
    int GL_UNIFORM_MATRIX_STRIDE = 0x8A3D;
    int GL_UNIFORM_IS_ROW_MAJOR = 0x8A3E;
    int GL_UNIFORM_BLOCK_BINDING = 0x8A3F;
    int GL_UNIFORM_BLOCK_DATA_SIZE = 0x8A40;
    int GL_UNIFORM_BLOCK_NAME_LENGTH = 0x8A41;
    int GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS = 0x8A42;
    int GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES = 0x8A43;
    int GL_UNIFORM_BLOCK_REFERENCED_BY_VERTEX_SHADER = 0x8A44;
    int GL_UNIFORM_BLOCK_REFERENCED_BY_FRAGMENT_SHADER = 0x8A46;
    // GL_INVALID_INDEX is defined as 0xFFFFFFFFu in C.
    int GL_INVALID_INDEX = -1;
    int GL_MAX_VERTEX_OUTPUT_COMPONENTS = 0x9122;
    int GL_MAX_FRAGMENT_INPUT_COMPONENTS = 0x9125;
    int GL_MAX_SERVER_WAIT_TIMEOUT = 0x9111;
    int GL_OBJECT_TYPE = 0x9112;
    int GL_SYNC_CONDITION = 0x9113;
    int GL_SYNC_STATUS = 0x9114;
    int GL_SYNC_FLAGS = 0x9115;
    int GL_SYNC_FENCE = 0x9116;
    int GL_SYNC_GPU_COMMANDS_COMPLETE = 0x9117;
    int GL_UNSIGNALED = 0x9118;
    int GL_SIGNALED = 0x9119;
    int GL_ALREADY_SIGNALED = 0x911A;
    int GL_TIMEOUT_EXPIRED = 0x911B;
    int GL_CONDITION_SATISFIED = 0x911C;
    int GL_WAIT_FAILED = 0x911D;
    int GL_SYNC_FLUSH_COMMANDS_BIT = 0x00000001;
    // GL_TIMEOUT_IGNORED is defined as 0xFFFFFFFFFFFFFFFFull in C.
    long GL_TIMEOUT_IGNORED = -1;
    int GL_VERTEX_ATTRIB_ARRAY_DIVISOR = 0x88FE;
    int GL_ANY_SAMPLES_PASSED = 0x8C2F;
    int GL_ANY_SAMPLES_PASSED_CONSERVATIVE = 0x8D6A;
    int GL_SAMPLER_BINDING = 0x8919;
    int GL_RGB10_A2UI = 0x906F;
    int GL_TEXTURE_SWIZZLE_R = 0x8E42;
    int GL_TEXTURE_SWIZZLE_G = 0x8E43;
    int GL_TEXTURE_SWIZZLE_B = 0x8E44;
    int GL_TEXTURE_SWIZZLE_A = 0x8E45;
    int GL_GREEN = 0x1904;
    int GL_BLUE = 0x1905;
    int GL_INT_2_10_10_10_REV = 0x8D9F;
    int GL_TRANSFORM_FEEDBACK = 0x8E22;
    int GL_TRANSFORM_FEEDBACK_PAUSED = 0x8E23;
    int GL_TRANSFORM_FEEDBACK_ACTIVE = 0x8E24;
    int GL_TRANSFORM_FEEDBACK_BINDING = 0x8E25;
    int GL_PROGRAM_BINARY_RETRIEVABLE_HINT = 0x8257;
    int GL_PROGRAM_BINARY_LENGTH = 0x8741;
    int GL_NUM_PROGRAM_BINARY_FORMATS = 0x87FE;
    int GL_PROGRAM_BINARY_FORMATS = 0x87FF;
    int GL_COMPRESSED_R11_EAC = 0x9270;
    int GL_COMPRESSED_SIGNED_R11_EAC = 0x9271;
    int GL_COMPRESSED_RG11_EAC = 0x9272;
    int GL_COMPRESSED_SIGNED_RG11_EAC = 0x9273;
    int GL_COMPRESSED_RGB8_ETC2 = 0x9274;
    int GL_COMPRESSED_SRGB8_ETC2 = 0x9275;
    int GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2 = 0x9276;
    int GL_COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2 = 0x9277;
    int GL_COMPRESSED_RGBA8_ETC2_EAC = 0x9278;
    int GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC = 0x9279;
    int GL_TEXTURE_IMMUTABLE_FORMAT = 0x912F;
    int GL_MAX_ELEMENT_INDEX = 0x8D6B;
    int GL_NUM_SAMPLE_COUNTS = 0x9380;
    int GL_TEXTURE_IMMUTABLE_LEVELS = 0x82DF;

    // C function void glReadBuffer ( GLenum mode )

    void glReadBuffer(int mode);

    // C function void glDrawRangeElements ( GLenum mode, GLuint start, GLuint end, GLsizei count, GLenum type, const GLvoid
// *indices )

    void glDrawRangeElements(int mode, int start, int end, int count, int type, java.nio.Buffer indices);

    // C function void glDrawRangeElements ( GLenum mode, GLuint start, GLuint end, GLsizei count, GLenum type, GLsizei offset )

    void glDrawRangeElements(int mode, int start, int end, int count, int type, int offset);

    // C function void glTexImage3D ( GLenum target, GLint level, GLint internalformat, GLsizei width, GLsizei height, GLsizei
// depth, GLint border, GLenum format, GLenum type, const GLvoid *pixels )

    void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int format,
                      int type, java.nio.Buffer pixels);

    // C function void glTexImage3D ( GLenum target, GLint level, GLint internalformat, GLsizei width, GLsizei height, GLsizei
// depth, GLint border, GLenum format, GLenum type, GLsizei offset )

    void glTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int format,
                      int type, int offset);

    // C function void glTexSubImage3D ( GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset, GLsizei width,
// GLsizei height, GLsizei depth, GLenum format, GLenum type, const GLvoid *pixels )

    void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
                         int format, int type, java.nio.Buffer pixels);

    // C function void glTexSubImage3D ( GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset, GLsizei width,
// GLsizei height, GLsizei depth, GLenum format, GLenum type, GLsizei offset )

    void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
                         int format, int type, int offset);

    // C function void glCopyTexSubImage3D ( GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset, GLint x,
// GLint y, GLsizei width, GLsizei height )

    void glCopyTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width,
                             int height);

// // C function void glCompressedTexImage3D ( GLenum target, GLint level, GLenum internalformat, GLsizei width, GLsizei height,
// GLsizei depth, GLint border, GLsizei imageSize, const GLvoid *data )
//
// public void glCompressedTexImage3D(
// int target,
// int level,
// int internalformat,
// int width,
// int height,
// int depth,
// int border,
// int imageSize,
// java.nio.Buffer data
// );
//
// // C function void glCompressedTexImage3D ( GLenum target, GLint level, GLenum internalformat, GLsizei width, GLsizei height,
// GLsizei depth, GLint border, GLsizei imageSize, GLsizei offset )
//
// public void glCompressedTexImage3D(
// int target,
// int level,
// int internalformat,
// int width,
// int height,
// int depth,
// int border,
// int imageSize,
// int offset
// );
//
// // C function void glCompressedTexSubImage3D ( GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset, GLsizei
// width, GLsizei height, GLsizei depth, GLenum format, GLsizei imageSize, const GLvoid *data )
//
// public void glCompressedTexSubImage3D(
// int target,
// int level,
// int xoffset,
// int yoffset,
// int zoffset,
// int width,
// int height,
// int depth,
// int format,
// int imageSize,
// java.nio.Buffer data
// );
//
// // C function void glCompressedTexSubImage3D ( GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset, GLsizei
// width, GLsizei height, GLsizei depth, GLenum format, GLsizei imageSize, GLsizei offset )
//
// public void glCompressedTexSubImage3D(
// int target,
// int level,
// int xoffset,
// int yoffset,
// int zoffset,
// int width,
// int height,
// int depth,
// int format,
// int imageSize,
// int offset
// );

    // C function void glGenQueries ( GLsizei n, GLuint *ids )

    void glGenQueries(int n, int[] ids, int offset);

    // C function void glGenQueries ( GLsizei n, GLuint *ids )

    void glGenQueries(int n, java.nio.IntBuffer ids);

    // C function void glDeleteQueries ( GLsizei n, const GLuint *ids )

    void glDeleteQueries(int n, int[] ids, int offset);

    // C function void glDeleteQueries ( GLsizei n, const GLuint *ids )

    void glDeleteQueries(int n, java.nio.IntBuffer ids);

    // C function GLboolean glIsQuery ( GLuint id )

    boolean glIsQuery(int id);

    // C function void glBeginQuery ( GLenum target, GLuint id )

    void glBeginQuery(int target, int id);

    // C function void glEndQuery ( GLenum target )

    void glEndQuery(int target);

// // C function void glGetQueryiv ( GLenum target, GLenum pname, GLint *params )
//
// public void glGetQueryiv(
// int target,
// int pname,
// int[] params,
// int offset
// );

    // C function void glGetQueryiv ( GLenum target, GLenum pname, GLint *params )

    void glGetQueryiv(int target, int pname, java.nio.IntBuffer params);

// // C function void glGetQueryObjectuiv ( GLuint id, GLenum pname, GLuint *params )
//
// public void glGetQueryObjectuiv(
// int id,
// int pname,
// int[] params,
// int offset
// );

    // C function void glGetQueryObjectuiv ( GLuint id, GLenum pname, GLuint *params )

    void glGetQueryObjectuiv(int id, int pname, java.nio.IntBuffer params);

    // C function GLboolean glUnmapBuffer ( GLenum target )

    boolean glUnmapBuffer(int target);

    // C function void glGetBufferPointerv ( GLenum target, GLenum pname, GLvoid** params )

    java.nio.Buffer glGetBufferPointerv(int target, int pname);

// // C function void glDrawBuffers ( GLsizei n, const GLenum *bufs )
//
// public void glDrawBuffers(
// int n,
// int[] bufs,
// int offset
// );

    // C function void glDrawBuffers ( GLsizei n, const GLenum *bufs )

    void glDrawBuffers(int n, java.nio.IntBuffer bufs);

// // C function void glUniformMatrix2x3fv ( GLint location, GLsizei count, GLboolean transpose, const GLfloat *value )
//
// public void glUniformMatrix2x3fv(
// int location,
// int count,
// boolean transpose,
// float[] value,
// int offset
// );

    // C function void glUniformMatrix2x3fv ( GLint location, GLsizei count, GLboolean transpose, const GLfloat *value )

    void glUniformMatrix2x3fv(int location, int count, boolean transpose, java.nio.FloatBuffer value);

// // C function void glUniformMatrix3x2fv ( GLint location, GLsizei count, GLboolean transpose, const GLfloat *value )
//
// public void glUniformMatrix3x2fv(
// int location,
// int count,
// boolean transpose,
// float[] value,
// int offset
// );

    // C function void glUniformMatrix3x2fv ( GLint location, GLsizei count, GLboolean transpose, const GLfloat *value )

    void glUniformMatrix3x2fv(int location, int count, boolean transpose, java.nio.FloatBuffer value);

// // C function void glUniformMatrix2x4fv ( GLint location, GLsizei count, GLboolean transpose, const GLfloat *value )
//
// public void glUniformMatrix2x4fv(
// int location,
// int count,
// boolean transpose,
// float[] value,
// int offset
// );

    // C function void glUniformMatrix2x4fv ( GLint location, GLsizei count, GLboolean transpose, const GLfloat *value )

    void glUniformMatrix2x4fv(int location, int count, boolean transpose, java.nio.FloatBuffer value);

// // C function void glUniformMatrix4x2fv ( GLint location, GLsizei count, GLboolean transpose, const GLfloat *value )
//
// public void glUniformMatrix4x2fv(
// int location,
// int count,
// boolean transpose,
// float[] value,
// int offset
// );

    // C function void glUniformMatrix4x2fv ( GLint location, GLsizei count, GLboolean transpose, const GLfloat *value )

    void glUniformMatrix4x2fv(int location, int count, boolean transpose, java.nio.FloatBuffer value);

// // C function void glUniformMatrix3x4fv ( GLint location, GLsizei count, GLboolean transpose, const GLfloat *value )
//
// public void glUniformMatrix3x4fv(
// int location,
// int count,
// boolean transpose,
// float[] value,
// int offset
// );

    // C function void glUniformMatrix3x4fv ( GLint location, GLsizei count, GLboolean transpose, const GLfloat *value )

    void glUniformMatrix3x4fv(int location, int count, boolean transpose, java.nio.FloatBuffer value);

// // C function void glUniformMatrix4x3fv ( GLint location, GLsizei count, GLboolean transpose, const GLfloat *value )
//
// public void glUniformMatrix4x3fv(
// int location,
// int count,
// boolean transpose,
// float[] value,
// int offset
// );

    // C function void glUniformMatrix4x3fv ( GLint location, GLsizei count, GLboolean transpose, const GLfloat *value )

    void glUniformMatrix4x3fv(int location, int count, boolean transpose, java.nio.FloatBuffer value);

    // C function void glBlitFramebuffer ( GLint srcX0, GLint srcY0, GLint srcX1, GLint srcY1, GLint dstX0, GLint dstY0, GLint
// dstX1, GLint dstY1, GLbitfield mask, GLenum filter )

    void glBlitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1,
                           int mask, int filter);

    // C function void glRenderbufferStorageMultisample ( GLenum target, GLsizei samples, GLenum internalformat, GLsizei width,
// GLsizei height )

    void glRenderbufferStorageMultisample(int target, int samples, int internalformat, int width, int height);

    // C function void glFramebufferTextureLayer ( GLenum target, GLenum attachment, GLuint texture, GLint level, GLint layer )

    void glFramebufferTextureLayer(int target, int attachment, int texture, int level, int layer);

// // C function GLvoid * glMapBufferRange ( GLenum target, GLintptr offset, GLsizeiptr length, GLbitfield access )
//
// public java.nio.Buffer glMapBufferRange(
// int target,
// int offset,
// int length,
// int access
// );

    // C function void glFlushMappedBufferRange ( GLenum target, GLintptr offset, GLsizeiptr length )

    void glFlushMappedBufferRange(int target, int offset, int length);

    // C function void glBindVertexArray ( GLuint array )

    void glBindVertexArray(int array);

    // C function void glDeleteVertexArrays ( GLsizei n, const GLuint *arrays )

    void glDeleteVertexArrays(int n, int[] arrays, int offset);

    // C function void glDeleteVertexArrays ( GLsizei n, const GLuint *arrays )

    void glDeleteVertexArrays(int n, java.nio.IntBuffer arrays);

    // C function void glGenVertexArrays ( GLsizei n, GLuint *arrays )

    void glGenVertexArrays(int n, int[] arrays, int offset);

    // C function void glGenVertexArrays ( GLsizei n, GLuint *arrays )

    void glGenVertexArrays(int n, java.nio.IntBuffer arrays);

    // C function GLboolean glIsVertexArray ( GLuint array )

    boolean glIsVertexArray(int array);

//
// // C function void glGetIntegeri_v ( GLenum target, GLuint index, GLint *data )
//
// public void glGetIntegeri_v(
// int target,
// int index,
// int[] data,
// int offset
// );
//
// // C function void glGetIntegeri_v ( GLenum target, GLuint index, GLint *data )
//
// public void glGetIntegeri_v(
// int target,
// int index,
// java.nio.IntBuffer data
// );

    // C function void glBeginTransformFeedback ( GLenum primitiveMode )

    void glBeginTransformFeedback(int primitiveMode);

    // C function void glEndTransformFeedback ( void )

    void glEndTransformFeedback();

    // C function void glBindBufferRange ( GLenum target, GLuint index, GLuint buffer, GLintptr offset, GLsizeiptr size )

    void glBindBufferRange(int target, int index, int buffer, int offset, int size);

    // C function void glBindBufferBase ( GLenum target, GLuint index, GLuint buffer )

    void glBindBufferBase(int target, int index, int buffer);

    // C function void glTransformFeedbackVaryings ( GLuint program, GLsizei count, const GLchar *varyings, GLenum bufferMode )

    void glTransformFeedbackVaryings(int program, String[] varyings, int bufferMode);

// // C function void glGetTransformFeedbackVarying ( GLuint program, GLuint index, GLsizei bufSize, GLsizei *length, GLint *size,
// GLenum *type, GLchar *name )
//
// public void glGetTransformFeedbackVarying(
// int program,
// int index,
// int bufsize,
// int[] length,
// int lengthOffset,
// int[] size,
// int sizeOffset,
// int[] type,
// int typeOffset,
// byte[] name,
// int nameOffset
// );
//
// // C function void glGetTransformFeedbackVarying ( GLuint program, GLuint index, GLsizei bufSize, GLsizei *length, GLint *size,
// GLenum *type, GLchar *name )
//
// public void glGetTransformFeedbackVarying(
// int program,
// int index,
// int bufsize,
// java.nio.IntBuffer length,
// java.nio.IntBuffer size,
// java.nio.IntBuffer type,
// byte name
// );
//
// // C function void glGetTransformFeedbackVarying ( GLuint program, GLuint index, GLsizei bufSize, GLsizei *length, GLint *size,
// GLenum *type, GLchar *name )
//
// public String glGetTransformFeedbackVarying(
// int program,
// int index,
// int[] size,
// int sizeOffset,
// int[] type,
// int typeOffset
// );
//
// // C function void glGetTransformFeedbackVarying ( GLuint program, GLuint index, GLsizei bufSize, GLsizei *length, GLint *size,
// GLenum *type, GLchar *name )
//
// public String glGetTransformFeedbackVarying(
// int program,
// int index,
// java.nio.IntBuffer size,
// java.nio.IntBuffer type
// );

    // C function void glVertexAttribIPointer ( GLuint index, GLint size, GLenum type, GLsizei stride, GLsizei offset )

    void glVertexAttribIPointer(int index, int size, int type, int stride, int offset);

// // C function void glGetVertexAttribIiv ( GLuint index, GLenum pname, GLint *params )
//
// public void glGetVertexAttribIiv(
// int index,
// int pname,
// int[] params,
// int offset
// );

    // C function void glGetVertexAttribIiv ( GLuint index, GLenum pname, GLint *params )

    void glGetVertexAttribIiv(int index, int pname, java.nio.IntBuffer params);

// // C function void glGetVertexAttribIuiv ( GLuint index, GLenum pname, GLuint *params )
//
// public void glGetVertexAttribIuiv(
// int index,
// int pname,
// int[] params,
// int offset
// );

    // C function void glGetVertexAttribIuiv ( GLuint index, GLenum pname, GLuint *params )

    void glGetVertexAttribIuiv(int index, int pname, java.nio.IntBuffer params);

    // C function void glVertexAttribI4i ( GLuint index, GLint x, GLint y, GLint z, GLint w )

    void glVertexAttribI4i(int index, int x, int y, int z, int w);

    // C function void glVertexAttribI4ui ( GLuint index, GLuint x, GLuint y, GLuint z, GLuint w )

    void glVertexAttribI4ui(int index, int x, int y, int z, int w);

// // C function void glVertexAttribI4iv ( GLuint index, const GLint *v )
//
// public void glVertexAttribI4iv(
// int index,
// int[] v,
// int offset
// );
//
// // C function void glVertexAttribI4iv ( GLuint index, const GLint *v )
//
// public void glVertexAttribI4iv(
// int index,
// java.nio.IntBuffer v
// );
//
// // C function void glVertexAttribI4uiv ( GLuint index, const GLuint *v )
//
// public void glVertexAttribI4uiv(
// int index,
// int[] v,
// int offset
// );
//
// // C function void glVertexAttribI4uiv ( GLuint index, const GLuint *v )
//
// public void glVertexAttribI4uiv(
// int index,
// java.nio.IntBuffer v
// );
//
// // C function void glGetUniformuiv ( GLuint program, GLint location, GLuint *params )
//
// public void glGetUniformuiv(
// int program,
// int location,
// int[] params,
// int offset
// );

    // C function void glGetUniformuiv ( GLuint program, GLint location, GLuint *params )

    void glGetUniformuiv(int program, int location, java.nio.IntBuffer params);

    // C function GLint glGetFragDataLocation ( GLuint program, const GLchar *name )

    int glGetFragDataLocation(int program, String name);

// // C function void glUniform1ui ( GLint location, GLuint v0 )
//
// public void glUniform1ui(
// int location,
// int v0
// );
//
// // C function void glUniform2ui ( GLint location, GLuint v0, GLuint v1 )
//
// public void glUniform2ui(
// int location,
// int v0,
// int v1
// );
//
// // C function void glUniform3ui ( GLint location, GLuint v0, GLuint v1, GLuint v2 )
//
// public void glUniform3ui(
// int location,
// int v0,
// int v1,
// int v2
// );
//
// // C function void glUniform4ui ( GLint location, GLuint v0, GLuint v1, GLuint v2, GLuint v3 )
//
// public void glUniform4ui(
// int location,
// int v0,
// int v1,
// int v2,
// int v3
// );
//
// // C function void glUniform1uiv ( GLint location, GLsizei count, const GLuint *value )
//
// public void glUniform1uiv(
// int location,
// int count,
// int[] value,
// int offset
// );

    // C function void glUniform1uiv ( GLint location, GLsizei count, const GLuint *value )

    void glUniform1uiv(int location, int count, java.nio.IntBuffer value);

// // C function void glUniform2uiv ( GLint location, GLsizei count, const GLuint *value )
//
// public void glUniform2uiv(
// int location,
// int count,
// int[] value,
// int offset
// );
//
// // C function void glUniform2uiv ( GLint location, GLsizei count, const GLuint *value )
//
// public void glUniform2uiv(
// int location,
// int count,
// java.nio.IntBuffer value
// );
//
// // C function void glUniform3uiv ( GLint location, GLsizei count, const GLuint *value )
//
// public void glUniform3uiv(
// int location,
// int count,
// int[] value,
// int offset
// );

    // C function void glUniform3uiv ( GLint location, GLsizei count, const GLuint *value )

    void glUniform3uiv(int location, int count, java.nio.IntBuffer value);

// // C function void glUniform4uiv ( GLint location, GLsizei count, const GLuint *value )
//
// public void glUniform4uiv(
// int location,
// int count,
// int[] value,
// int offset
// );

    // C function void glUniform4uiv ( GLint location, GLsizei count, const GLuint *value )

    void glUniform4uiv(int location, int count, java.nio.IntBuffer value);

// // C function void glClearBufferiv ( GLenum buffer, GLint drawbuffer, const GLint *value )
//
// public void glClearBufferiv(
// int buffer,
// int drawbuffer,
// int[] value,
// int offset
// );

    // C function void glClearBufferiv ( GLenum buffer, GLint drawbuffer, const GLint *value )

    void glClearBufferiv(int buffer, int drawbuffer, java.nio.IntBuffer value);

// // C function void glClearBufferuiv ( GLenum buffer, GLint drawbuffer, const GLuint *value )
//
// public void glClearBufferuiv(
// int buffer,
// int drawbuffer,
// int[] value,
// int offset
// );

    // C function void glClearBufferuiv ( GLenum buffer, GLint drawbuffer, const GLuint *value )

    void glClearBufferuiv(int buffer, int drawbuffer, java.nio.IntBuffer value);

// // C function void glClearBufferfv ( GLenum buffer, GLint drawbuffer, const GLfloat *value )
//
// public void glClearBufferfv(
// int buffer,
// int drawbuffer,
// float[] value,
// int offset
// );

    // C function void glClearBufferfv ( GLenum buffer, GLint drawbuffer, const GLfloat *value )

    void glClearBufferfv(int buffer, int drawbuffer, java.nio.FloatBuffer value);

    // C function void glClearBufferfi ( GLenum buffer, GLint drawbuffer, GLfloat depth, GLint stencil )

    void glClearBufferfi(int buffer, int drawbuffer, float depth, int stencil);

    // C function const GLubyte * glGetStringi ( GLenum name, GLuint index )

    String glGetStringi(int name, int index);

    // C function void glCopyBufferSubData ( GLenum readTarget, GLenum writeTarget, GLintptr readOffset, GLintptr writeOffset,
// GLsizeiptr size )

    void glCopyBufferSubData(int readTarget, int writeTarget, int readOffset, int writeOffset, int size);

// // C function void glGetUniformIndices ( GLuint program, GLsizei uniformCount, const GLchar *const *uniformNames, GLuint
// *uniformIndices )
//
// public void glGetUniformIndices(
// int program,
// String[] uniformNames,
// int[] uniformIndices,
// int uniformIndicesOffset
// );

    // C function void glGetUniformIndices ( GLuint program, GLsizei uniformCount, const GLchar *const *uniformNames, GLuint
// *uniformIndices )

    void glGetUniformIndices(int program, String[] uniformNames, java.nio.IntBuffer uniformIndices);

// // C function void glGetActiveUniformsiv ( GLuint program, GLsizei uniformCount, const GLuint *uniformIndices, GLenum pname,
// GLint *params )
//
// public void glGetActiveUniformsiv(
// int program,
// int uniformCount,
// int[] uniformIndices,
// int uniformIndicesOffset,
// int pname,
// int[] params,
// int paramsOffset
// );

    // C function void glGetActiveUniformsiv ( GLuint program, GLsizei uniformCount, const GLuint *uniformIndices, GLenum pname,
// GLint *params )

    void glGetActiveUniformsiv(int program, int uniformCount, java.nio.IntBuffer uniformIndices, int pname,
                               java.nio.IntBuffer params);

    // C function GLuint glGetUniformBlockIndex ( GLuint program, const GLchar *uniformBlockName )

    int glGetUniformBlockIndex(int program, String uniformBlockName);

// // C function void glGetActiveUniformBlockiv ( GLuint program, GLuint uniformBlockIndex, GLenum pname, GLint *params )
//
// public void glGetActiveUniformBlockiv(
// int program,
// int uniformBlockIndex,
// int pname,
// int[] params,
// int offset
// );

    // C function void glGetActiveUniformBlockiv ( GLuint program, GLuint uniformBlockIndex, GLenum pname, GLint *params )

    void glGetActiveUniformBlockiv(int program, int uniformBlockIndex, int pname, java.nio.IntBuffer params);

// // C function void glGetActiveUniformBlockName ( GLuint program, GLuint uniformBlockIndex, GLsizei bufSize, GLsizei *length,
// GLchar *uniformBlockName )
//
// public void glGetActiveUniformBlockName(
// int program,
// int uniformBlockIndex,
// int bufSize,
// int[] length,
// int lengthOffset,
// byte[] uniformBlockName,
// int uniformBlockNameOffset
// );

    // C function void glGetActiveUniformBlockName ( GLuint program, GLuint uniformBlockIndex, GLsizei bufSize, GLsizei *length,
// GLchar *uniformBlockName )

    void glGetActiveUniformBlockName(int program, int uniformBlockIndex, java.nio.Buffer length,
                                     java.nio.Buffer uniformBlockName);

    // C function void glGetActiveUniformBlockName ( GLuint program, GLuint uniformBlockIndex, GLsizei bufSize, GLsizei *length,
// GLchar *uniformBlockName )

    String glGetActiveUniformBlockName(int program, int uniformBlockIndex);

    // C function void glUniformBlockBinding ( GLuint program, GLuint uniformBlockIndex, GLuint uniformBlockBinding )

    void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding);

    // C function void glDrawArraysInstanced ( GLenum mode, GLint first, GLsizei count, GLsizei instanceCount )

    void glDrawArraysInstanced(int mode, int first, int count, int instanceCount);

// // C function void glDrawElementsInstanced ( GLenum mode, GLsizei count, GLenum type, const GLvoid *indices, GLsizei
// instanceCount )
//
// public void glDrawElementsInstanced(
// int mode,
// int count,
// int type,
// java.nio.Buffer indices,
// int instanceCount
// );

    // C function void glDrawElementsInstanced ( GLenum mode, GLsizei count, GLenum type, const GLvoid *indices, GLsizei
// instanceCount )

    void glDrawElementsInstanced(int mode, int count, int type, int indicesOffset, int instanceCount);

// // C function GLsync glFenceSync ( GLenum condition, GLbitfield flags )
//
// public long glFenceSync(
// int condition,
// int flags
// );
//
// // C function GLboolean glIsSync ( GLsync sync )
//
// public boolean glIsSync(
// long sync
// );
//
// // C function void glDeleteSync ( GLsync sync )
//
// public void glDeleteSync(
// long sync
// );
//
// // C function GLenum glClientWaitSync ( GLsync sync, GLbitfield flags, GLuint64 timeout )
//
// public int glClientWaitSync(
// long sync,
// int flags,
// long timeout
// );
//
// // C function void glWaitSync ( GLsync sync, GLbitfield flags, GLuint64 timeout )
//
// public void glWaitSync(
// long sync,
// int flags,
// long timeout
// );

// // C function void glGetInteger64v ( GLenum pname, GLint64 *params )
//
// public void glGetInteger64v(
// int pname,
// long[] params,
// int offset
// );

    // C function void glGetInteger64v ( GLenum pname, GLint64 *params )

    void glGetInteger64v(int pname, java.nio.LongBuffer params);

// // C function void glGetSynciv ( GLsync sync, GLenum pname, GLsizei bufSize, GLsizei *length, GLint *values )
//
// public void glGetSynciv(
// long sync,
// int pname,
// int bufSize,
// int[] length,
// int lengthOffset,
// int[] values,
// int valuesOffset
// );
//
// // C function void glGetSynciv ( GLsync sync, GLenum pname, GLsizei bufSize, GLsizei *length, GLint *values )
//
// public void glGetSynciv(
// long sync,
// int pname,
// int bufSize,
// java.nio.IntBuffer length,
// java.nio.IntBuffer values
// );
//
// // C function void glGetInteger64i_v ( GLenum target, GLuint index, GLint64 *data )
//
// public void glGetInteger64i_v(
// int target,
// int index,
// long[] data,
// int offset
// );
//
// // C function void glGetInteger64i_v ( GLenum target, GLuint index, GLint64 *data )
//
// public void glGetInteger64i_v(
// int target,
// int index,
// java.nio.LongBuffer data
// );
//
// // C function void glGetBufferParameteri64v ( GLenum target, GLenum pname, GLint64 *params )
//
// public void glGetBufferParameteri64v(
// int target,
// int pname,
// long[] params,
// int offset
// );

    // C function void glGetBufferParameteri64v ( GLenum target, GLenum pname, GLint64 *params )

    void glGetBufferParameteri64v(int target, int pname, java.nio.LongBuffer params);

    // C function void glGenSamplers ( GLsizei count, GLuint *samplers )

    void glGenSamplers(int count, int[] samplers, int offset);

    // C function void glGenSamplers ( GLsizei count, GLuint *samplers )

    void glGenSamplers(int count, java.nio.IntBuffer samplers);

    // C function void glDeleteSamplers ( GLsizei count, const GLuint *samplers )

    void glDeleteSamplers(int count, int[] samplers, int offset);

    // C function void glDeleteSamplers ( GLsizei count, const GLuint *samplers )

    void glDeleteSamplers(int count, java.nio.IntBuffer samplers);

    // C function GLboolean glIsSampler ( GLuint sampler )

    boolean glIsSampler(int sampler);

    // C function void glBindSampler ( GLuint unit, GLuint sampler )

    void glBindSampler(int unit, int sampler);

    // C function void glSamplerParameteri ( GLuint sampler, GLenum pname, GLint param )

    void glSamplerParameteri(int sampler, int pname, int param);

// // C function void glSamplerParameteriv ( GLuint sampler, GLenum pname, const GLint *param )
//
// public void glSamplerParameteriv(
// int sampler,
// int pname,
// int[] param,
// int offset
// );

    // C function void glSamplerParameteriv ( GLuint sampler, GLenum pname, const GLint *param )

    void glSamplerParameteriv(int sampler, int pname, java.nio.IntBuffer param);

    // C function void glSamplerParameterf ( GLuint sampler, GLenum pname, GLfloat param )

    void glSamplerParameterf(int sampler, int pname, float param);

// // C function void glSamplerParameterfv ( GLuint sampler, GLenum pname, const GLfloat *param )
//
// public void glSamplerParameterfv(
// int sampler,
// int pname,
// float[] param,
// int offset
// );

    // C function void glSamplerParameterfv ( GLuint sampler, GLenum pname, const GLfloat *param )

    void glSamplerParameterfv(int sampler, int pname, java.nio.FloatBuffer param);

// // C function void glGetSamplerParameteriv ( GLuint sampler, GLenum pname, GLint *params )
//
// public void glGetSamplerParameteriv(
// int sampler,
// int pname,
// int[] params,
// int offset
// );

    // C function void glGetSamplerParameteriv ( GLuint sampler, GLenum pname, GLint *params )

    void glGetSamplerParameteriv(int sampler, int pname, java.nio.IntBuffer params);

// // C function void glGetSamplerParameterfv ( GLuint sampler, GLenum pname, GLfloat *params )
//
// public void glGetSamplerParameterfv(
// int sampler,
// int pname,
// float[] params,
// int offset
// );

    // C function void glGetSamplerParameterfv ( GLuint sampler, GLenum pname, GLfloat *params )

    void glGetSamplerParameterfv(int sampler, int pname, java.nio.FloatBuffer params);

    // C function void glVertexAttribDivisor ( GLuint index, GLuint divisor )

    void glVertexAttribDivisor(int index, int divisor);

    // C function void glBindTransformFeedback ( GLenum target, GLuint id )

    void glBindTransformFeedback(int target, int id);

    // C function void glDeleteTransformFeedbacks ( GLsizei n, const GLuint *ids )

    void glDeleteTransformFeedbacks(int n, int[] ids, int offset);

    // C function void glDeleteTransformFeedbacks ( GLsizei n, const GLuint *ids )

    void glDeleteTransformFeedbacks(int n, java.nio.IntBuffer ids);

    // C function void glGenTransformFeedbacks ( GLsizei n, GLuint *ids )

    void glGenTransformFeedbacks(int n, int[] ids, int offset);

    // C function void glGenTransformFeedbacks ( GLsizei n, GLuint *ids )

    void glGenTransformFeedbacks(int n, java.nio.IntBuffer ids);

    // C function GLboolean glIsTransformFeedback ( GLuint id )

    boolean glIsTransformFeedback(int id);

    // C function void glPauseTransformFeedback ( void )

    void glPauseTransformFeedback();

    // C function void glResumeTransformFeedback ( void )

    void glResumeTransformFeedback();

// // C function void glGetProgramBinary ( GLuint program, GLsizei bufSize, GLsizei *length, GLenum *binaryFormat, GLvoid *binary
// )
//
// public void glGetProgramBinary(
// int program,
// int bufSize,
// int[] length,
// int lengthOffset,
// int[] binaryFormat,
// int binaryFormatOffset,
// java.nio.Buffer binary
// );
//
// // C function void glGetProgramBinary ( GLuint program, GLsizei bufSize, GLsizei *length, GLenum *binaryFormat, GLvoid *binary
// )
//
// public void glGetProgramBinary(
// int program,
// int bufSize,
// java.nio.IntBuffer length,
// java.nio.IntBuffer binaryFormat,
// java.nio.Buffer binary
// );
//
// // C function void glProgramBinary ( GLuint program, GLenum binaryFormat, const GLvoid *binary, GLsizei length )
//
// public void glProgramBinary(
// int program,
// int binaryFormat,
// java.nio.Buffer binary,
// int length
// );

    // C function void glProgramParameteri ( GLuint program, GLenum pname, GLint value )

    void glProgramParameteri(int program, int pname, int value);

// // C function void glInvalidateFramebuffer ( GLenum target, GLsizei numAttachments, const GLenum *attachments )
//
// public void glInvalidateFramebuffer(
// int target,
// int numAttachments,
// int[] attachments,
// int offset
// );

    // C function void glInvalidateFramebuffer ( GLenum target, GLsizei numAttachments, const GLenum *attachments )

    void glInvalidateFramebuffer(int target, int numAttachments, java.nio.IntBuffer attachments);

// // C function void glInvalidateSubFramebuffer ( GLenum target, GLsizei numAttachments, const GLenum *attachments, GLint x,
// GLint y, GLsizei width, GLsizei height )
//
// public void glInvalidateSubFramebuffer(
// int target,
// int numAttachments,
// int[] attachments,
// int offset,
// int x,
// int y,
// int width,
// int height
// );

    // C function void glInvalidateSubFramebuffer ( GLenum target, GLsizei numAttachments, const GLenum *attachments, GLint x,
// GLint y, GLsizei width, GLsizei height )

    void glInvalidateSubFramebuffer(int target, int numAttachments, java.nio.IntBuffer attachments, int x, int y,
                                    int width, int height);

// // C function void glTexStorage2D ( GLenum target, GLsizei levels, GLenum internalformat, GLsizei width, GLsizei height )
//
// public void glTexStorage2D(
// int target,
// int levels,
// int internalformat,
// int width,
// int height
// );
//
// // C function void glTexStorage3D ( GLenum target, GLsizei levels, GLenum internalformat, GLsizei width, GLsizei height,
// GLsizei depth )
//
// public void glTexStorage3D(
// int target,
// int levels,
// int internalformat,
// int width,
// int height,
// int depth
// );
//
// // C function void glGetInternalformativ ( GLenum target, GLenum internalformat, GLenum pname, GLsizei bufSize, GLint *params )
//
// public void glGetInternalformativ(
// int target,
// int internalformat,
// int pname,
// int bufSize,
// int[] params,
// int offset
// );
//
// // C function void glGetInternalformativ ( GLenum target, GLenum internalformat, GLenum pname, GLsizei bufSize, GLint *params )
//
// public void glGetInternalformativ(
// int target,
// int internalformat,
// int pname,
// int bufSize,
// java.nio.IntBuffer params
// );
}

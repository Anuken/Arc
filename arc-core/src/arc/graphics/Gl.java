package arc.graphics;

import arc.*;
import arc.struct.Bits;
import arc.util.*;

import java.nio.*;
import java.util.*;

public class Gl{
    private static final boolean optimize = true;

    public static final int
    esVersion20 = 1,
    depthBufferBit = 0x00000100,
    stencilBufferBit = 0x00000400,
    colorBufferBit = 0x00004000,
    falseV = 0,
    trueV = 1,
    points = 0x0000,
    lines = 0x0001,
    lineLoop = 0x0002,
    lineStrip = 0x0003,
    triangles = 0x0004,
    triangleStrip = 0x0005,
    triangleFan = 0x0006,
    zero = 0,
    one = 1,
    srcColor = 0x0300,
    oneMinusSrcColor = 0x0301,
    srcAlpha = 0x0302,
    oneMinusSrcAlpha = 0x0303,
    dstAlpha = 0x0304,
    oneMinusDstAlpha = 0x0305,
    dstColor = 0x0306,
    oneMinusDstColor = 0x0307,
    srcAlphaSaturate = 0x0308,
    funcAdd = 0x8006,
    blendEquation = 0x8009,
    blendEquationRgb = 0x8009,
    blendEquationAlpha = 0x883D,
    funcSubtract = 0x800A,
    funcReverseSubtract = 0x800B,
    min = 0x8007,
    max = 0x8008,
    blendDstRgb = 0x80C8,
    blendSrcRgb = 0x80C9,
    blendDstAlpha = 0x80CA,
    blendSrcAlpha = 0x80CB,
    constantColor = 0x8001,
    oneMinusConstantColor = 0x8002,
    constantAlpha = 0x8003,
    oneMinusConstantAlpha = 0x8004,
    blendColor = 0x8005,
    arrayBuffer = 0x8892,
    elementArrayBuffer = 0x8893,
    arrayBufferBinding = 0x8894,
    elementArrayBufferBinding = 0x8895,
    streamDraw = 0x88E0,
    staticDraw = 0x88E4,
    dynamicDraw = 0x88E8,
    bufferSize = 0x8764,
    bufferUsage = 0x8765,
    currentVertexAttrib = 0x8626,
    front = 0x0404,
    back = 0x0405,
    frontAndBack = 0x0408,
    texture2d = 0x0DE1,
    cullFace = 0x0B44,
    blend = 0x0BE2,
    dither = 0x0BD0,
    stencilTest = 0x0B90,
    depthTest = 0x0B71,
    scissorTest = 0x0C11,
    polygonOffsetFill = 0x8037,
    sampleAlphaToCoverage = 0x809E,
    sampleCoverage = 0x80A0,
    noError = 0,
    invalidEnum = 0x0500,
    invalidValue = 0x0501,
    invalidOperation = 0x0502,
    outOfMemory = 0x0505,
    cw = 0x0900,
    ccw = 0x0901,
    lineWidth = 0x0B21,
    aliasedPointSizeRange = 0x846D,
    aliasedLineWidthRange = 0x846E,
    cullFaceMode = 0x0B45,
    frontFace = 0x0B46,
    depthRange = 0x0B70,
    depthWritemask = 0x0B72,
    depthClearValue = 0x0B73,
    depthFunc = 0x0B74,
    stencilClearValue = 0x0B91,
    stencilFunc = 0x0B92,
    stencilFail = 0x0B94,
    stencilPassDepthFail = 0x0B95,
    stencilPassDepthPass = 0x0B96,
    stencilRef = 0x0B97,
    stencilValueMask = 0x0B93,
    stencilWritemask = 0x0B98,
    stencilBackFunc = 0x8800,
    stencilBackFail = 0x8801,
    stencilBackPassDepthFail = 0x8802,
    stencilBackPassDepthPass = 0x8803,
    stencilBackRef = 0x8CA3,
    stencilBackValueMask = 0x8CA4,
    stencilBackWritemask = 0x8CA5,
    viewport = 0x0BA2,
    scissorBox = 0x0C10,
    colorClearValue = 0x0C22,
    colorWritemask = 0x0C23,
    unpackAlignment = 0x0CF5,
    packAlignment = 0x0D05,
    maxTextureSize = 0x0D33,
    maxTextureUnits = 0x84E2,
    maxViewportDims = 0x0D3A,
    subpixelBits = 0x0D50,
    redBits = 0x0D52,
    greenBits = 0x0D53,
    blueBits = 0x0D54,
    alphaBits = 0x0D55,
    depthBits = 0x0D56,
    stencilBits = 0x0D57,
    polygonOffsetUnits = 0x2A00,
    polygonOffsetFactor = 0x8038,
    textureBinding2d = 0x8069,
    sampleBuffers = 0x80A8,
    samples = 0x80A9,
    sampleCoverageValue = 0x80AA,
    sampleCoverageInvert = 0x80AB,
    numCompressedTextureFormats = 0x86A2,
    compressedTextureFormats = 0x86A3,
    dontCare = 0x1100,
    fastest = 0x1101,
    nicest = 0x1102,
    generateMipmap = 0x8191,
    generateMipmapHint = 0x8192,
    byteV = 0x1400,
    unsignedByte = 0x1401,
    shortV = 0x1402,
    unsignedShort = 0x1403,
    intV = 0x1404,
    unsignedInt = 0x1405,
    floatV = 0x1406,
    fixed = 0x140C,
    depthComponent = 0x1902,
    alpha = 0x1906,
    rgb = 0x1907,
    rgba = 0x1908,
    luminance = 0x1909,
    luminanceAlpha = 0x190A,
    unsignedShort4444 = 0x8033,
    unsignedShort5551 = 0x8034,
    unsignedShort565 = 0x8363,
    fragmentShader = 0x8B30,
    vertexShader = 0x8B31,
    maxVertexAttribs = 0x8869,
    maxVertexUniformVectors = 0x8DFB,
    maxVaryingVectors = 0x8DFC,
    maxCombinedTextureImageUnits = 0x8B4D,
    maxVertexTextureImageUnits = 0x8B4C,
    maxTextureImageUnits = 0x8872,
    maxFragmentUniformVectors = 0x8DFD,
    shaderType = 0x8B4F,
    deleteStatus = 0x8B80,
    linkStatus = 0x8B82,
    validateStatus = 0x8B83,
    attachedShaders = 0x8B85,
    activeUniforms = 0x8B86,
    activeUniformMaxLength = 0x8B87,
    activeAttributes = 0x8B89,
    activeAttributeMaxLength = 0x8B8A,
    shadingLanguageVersion = 0x8B8C,
    currentProgram = 0x8B8D,
    never = 0x0200,
    less = 0x0201,
    equal = 0x0202,
    lequal = 0x0203,
    greater = 0x0204,
    notequal = 0x0205,
    gequal = 0x0206,
    always = 0x0207,
    keep = 0x1E00,
    replace = 0x1E01,
    incr = 0x1E02,
    decr = 0x1E03,
    invert = 0x150A,
    incrWrap = 0x8507,
    decrWrap = 0x8508,
    vendor = 0x1F00,
    renderer = 0x1F01,
    version = 0x1F02,
    extensions = 0x1F03,
    nearest = 0x2600,
    linear = 0x2601,
    nearestMipmapNearest = 0x2700,
    linearMipmapNearest = 0x2701,
    nearestMipmapLinear = 0x2702,
    linearMipmapLinear = 0x2703,
    textureMagFilter = 0x2800,
    textureMinFilter = 0x2801,
    textureWrapS = 0x2802,
    textureWrapT = 0x2803,
    texture = 0x1702,
    textureCubeMap = 0x8513,
    textureBindingCubeMap = 0x8514,
    textureCubeMapPositiveX = 0x8515,
    textureCubeMapNegativeX = 0x8516,
    textureCubeMapPositiveY = 0x8517,
    textureCubeMapNegativeY = 0x8518,
    textureCubeMapPositiveZ = 0x8519,
    textureCubeMapNegativeZ = 0x851A,
    maxCubeMapTextureSize = 0x851C,
    texture0 = 0x84C0,
    texture1 = 0x84C1,
    texture2 = 0x84C2,
    texture3 = 0x84C3,
    texture4 = 0x84C4,
    texture5 = 0x84C5,
    texture6 = 0x84C6,
    texture7 = 0x84C7,
    texture8 = 0x84C8,
    texture9 = 0x84C9,
    texture10 = 0x84CA,
    texture11 = 0x84CB,
    texture12 = 0x84CC,
    texture13 = 0x84CD,
    texture14 = 0x84CE,
    texture15 = 0x84CF,
    texture16 = 0x84D0,
    texture17 = 0x84D1,
    texture18 = 0x84D2,
    texture19 = 0x84D3,
    texture20 = 0x84D4,
    texture21 = 0x84D5,
    texture22 = 0x84D6,
    texture23 = 0x84D7,
    texture24 = 0x84D8,
    texture25 = 0x84D9,
    texture26 = 0x84DA,
    texture27 = 0x84DB,
    texture28 = 0x84DC,
    texture29 = 0x84DD,
    texture30 = 0x84DE,
    texture31 = 0x84DF,
    activeTexture = 0x84E0,
    repeat = 0x2901,
    clampToEdge = 0x812F,
    mirroredRepeat = 0x8370,
    floatVec2 = 0x8B50,
    floatVec3 = 0x8B51,
    floatVec4 = 0x8B52,
    intVec2 = 0x8B53,
    intVec3 = 0x8B54,
    intVec4 = 0x8B55,
    bool = 0x8B56,
    boolVec2 = 0x8B57,
    boolVec3 = 0x8B58,
    boolVec4 = 0x8B59,
    floatMat2 = 0x8B5A,
    floatMat3 = 0x8B5B,
    floatMat4 = 0x8B5C,
    sampler2d = 0x8B5E,
    samplerCube = 0x8B60,
    vertexAttribArrayEnabled = 0x8622,
    vertexAttribArraySize = 0x8623,
    vertexAttribArrayStride = 0x8624,
    vertexAttribArrayType = 0x8625,
    vertexAttribArrayNormalized = 0x886A,
    vertexAttribArrayPointer = 0x8645,
    vertexAttribArrayBufferBinding = 0x889F,
    implementationColorReadType = 0x8B9A,
    implementationColorReadFormat = 0x8B9B,
    compileStatus = 0x8B81,
    infoLogLength = 0x8B84,
    shaderSourceLength = 0x8B88,
    shaderCompiler = 0x8DFA,
    shaderBinaryFormats = 0x8DF8,
    numShaderBinaryFormats = 0x8DF9,
    lowFloat = 0x8DF0,
    mediumFloat = 0x8DF1,
    highFloat = 0x8DF2,
    lowInt = 0x8DF3,
    mediumInt = 0x8DF4,
    highInt = 0x8DF5,
    framebuffer = 0x8D40,
    renderbuffer = 0x8D41,
    rgba4 = 0x8056,
    rgb5A1 = 0x8057,
    rgb565 = 0x8D62,
    depthComponent16 = 0x81A5,
    stencilIndex = 0x1901,
    stencilIndex8 = 0x8D48,
    renderbufferWidth = 0x8D42,
    renderbufferHeight = 0x8D43,
    renderbufferInternalFormat = 0x8D44,
    renderbufferRedSize = 0x8D50,
    renderbufferGreenSize = 0x8D51,
    renderbufferBlueSize = 0x8D52,
    renderbufferAlphaSize = 0x8D53,
    renderbufferDepthSize = 0x8D54,
    renderbufferStencilSize = 0x8D55,
    framebufferAttachmentObjectType = 0x8CD0,
    framebufferAttachmentObjectName = 0x8CD1,
    framebufferAttachmentTextureLevel = 0x8CD2,
    framebufferAttachmentTextureCubeMapFace = 0x8CD3,
    colorAttachment0 = 0x8CE0,
    depthAttachment = 0x8D00,
    stencilAttachment = 0x8D20,
    none = 0,
    framebufferComplete = 0x8CD5,
    framebufferIncompleteAttachment = 0x8CD6,
    framebufferIncompleteMissingAttachment = 0x8CD7,
    framebufferIncompleteDimensions = 0x8CD9,
    framebufferUnsupported = 0x8CDD,
    framebufferBinding = 0x8CA6,
    renderbufferBinding = 0x8CA7,
    maxRenderbufferSize = 0x84E8,
    invalidFramebufferOperation = 0x0506,
    vertexProgramPointSize = 0x8642;

    //STATE - optimizes GL calls

    private static IntBuffer ibuf = Buffers.newIntBuffer(1), ibuf2 = Buffers.newIntBuffer(2);
    //last active texture unit
    private static int lastActiveTexture = -1;
    //last bound texture2ds, mapping from texture unit to texture handle
    private static int[] lastBoundTextures = new int[32];
    //last useProgram call
    private static int lastUsedProgram = 0;
    /** enabled bits, from glEnable/disable */
    private static Bits enabled = new Bits();
    private static boolean wasDepthMask = true;
    //blending state
    private static int lastSfactor = -1, lastDfactor = -1;

    static{
        reset();
    }

    /** Reset optimization cache. */
    public static void reset(){
        lastActiveTexture = -1;
        Arrays.fill(lastBoundTextures, -1);
        lastUsedProgram = 0;
        enabled.clear();
        wasDepthMask = true;
        lastSfactor = -1;
        lastDfactor = -1;
    }

    public static void activeTexture(int texture){
        if(optimize && lastActiveTexture == texture) return;

        Core.gl.glActiveTexture(texture);
        lastActiveTexture = texture;
    }

    public static void bindTexture(int target, int texture){
        if(optimize && target == texture2d){
            //current bound texture unit
            int index = lastActiveTexture - texture0;
            //make sure it's valid
            if(index >= 0 && index < lastBoundTextures.length){
                if(lastBoundTextures[index] == texture){
                    //skip double bindings
                    return;
                }
                lastBoundTextures[index] = texture;
            }
        }

        Core.gl.glBindTexture(target, texture);
    }

    public static void blendFunc(int sfactor, int dfactor){
        if(lastSfactor == sfactor && lastDfactor == dfactor) return;
        Core.gl.glBlendFunc(lastSfactor = sfactor, lastDfactor = dfactor);

        lastSfactor = sfactor;
        lastDfactor = dfactor;
    }

    public static void clear(int mask){
        Core.gl.glClear(mask);
    }

    public static void clearColor(float red, float green, float blue, float alpha){
        Core.gl.glClearColor(red, green, blue, alpha);
    }

    public static void clearDepthf(float depth){
        Core.gl.glClearDepthf(depth);
    }

    public static void clearStencil(int s){
        Core.gl.glClearStencil(s);
    }

    public static void colorMask(boolean red, boolean green, boolean blue, boolean alpha){
        Core.gl.glColorMask(red, green, blue, alpha);
    }

    public static void compressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, int imageSize, Buffer data){
        Core.gl.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
    }

    public static void compressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int imageSize, Buffer data){
        Core.gl.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
    }

    public static void copyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height, int border){
        Core.gl.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
    }

    public static void copyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width, int height){
        Core.gl.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
    }

    public static void cullFace(int mode){
        Core.gl.glCullFace(mode);
    }

    public static void deleteTexture(int texture){
        //clear deleted texture, as it may be reused later
        for(int i = 0; i < lastBoundTextures.length; i++){
            if(lastBoundTextures[i] == texture){
                lastBoundTextures[i] = -1;
            }
        }
        Core.gl.glDeleteTexture(texture);
    }

    public static void depthFunc(int func){
        Core.gl.glDepthFunc(func);
    }

    public static void depthMask(boolean flag){
        Core.gl.glDepthMask(flag);
    }

    public static void depthRangef(float zNear, float zFar){
        Core.gl.glDepthRangef(zNear, zFar);
    }

    public static void disable(int cap){
        if(optimize && !enabled.get(cap)){
            return;
        }
        Core.gl.glDisable(cap);
        enabled.clear(cap);
    }

    public static void drawArrays(int mode, int first, int count){
        Core.gl.glDrawArrays(mode, first, count);
    }

    public static void drawElements(int mode, int count, int type, Buffer indices){
        Core.gl.glDrawElements(mode, count, type, indices);
    }

    public static void enable(int cap){
        if(optimize && enabled.get(cap)){
            return;
        }
        Core.gl.glEnable(cap);
        enabled.set(cap);
    }

    public static void finish(){
        Core.gl.glFinish();
    }

    public static void flush(){
        Core.gl.glFlush();
    }

    public static void frontFace(int mode){
        Core.gl.glFrontFace(mode);
    }

    public static int genTexture(){
        return Core.gl.glGenTexture();
    }

    public static int getError(){
        return Core.gl.glGetError();
    }

    public static void getIntegerv(int pname, IntBuffer params){
        Core.gl.glGetIntegerv(pname, params);
    }

    public static int getInt(int name){
        ibuf.position(0);
        getIntegerv(name, ibuf);
        return ibuf.get(0);
    }

    public static String getString(int name){
        return Core.gl.glGetString(name);
    }

    public static void hint(int target, int mode){
        Core.gl.glHint(target, mode);
    }

    public static void pixelStorei(int pname, int param){
        Core.gl.glPixelStorei(pname, param);
    }

    public static void polygonOffset(float factor, float units){
        Core.gl.glPolygonOffset(factor, units);
    }

    public static void readPixels(int x, int y, int width, int height, int format, int type, Buffer pixels){
        Core.gl.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public static void scissor(int x, int y, int width, int height){
        Core.gl.glScissor(x, y, width, height);
    }

    public static void stencilFunc(int func, int ref, int mask){
        Core.gl.glStencilFunc(func, ref, mask);
    }

    public static void stencilMask(int mask){
        Core.gl.glStencilMask(mask);
    }

    public static void stencilOp(int fail, int zfail, int zpass){
        Core.gl.glStencilOp(fail, zfail, zpass);
    }

    public static void texImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels){
        Core.gl.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public static void texParameterf(int target, int pname, float param){
        Core.gl.glTexParameterf(target, pname, param);
    }

    public static void texSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels){
        Core.gl.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void viewport(int x, int y, int width, int height){
        Core.gl.glViewport(x, y, width, height);
    }

    public static void attachShader(int program, int shader){
        Core.gl.glAttachShader(program, shader);
    }

    public static void bindAttribLocation(int program, int index, String name){
        Core.gl.glBindAttribLocation(program, index, name);
    }

    public static void bindBuffer(int target, int buffer){
        Core.gl.glBindBuffer(target, buffer);
    }

    public static void bindFramebuffer(int target, int framebuffer){
        Core.gl.glBindFramebuffer(target, framebuffer);
    }

    public static void bindRenderbuffer(int target, int renderbuffer){
        Core.gl.glBindRenderbuffer(target, renderbuffer);
    }

    public static void blendColor(float red, float green, float blue, float alpha){
        Core.gl.glBlendColor(red, green, blue, alpha);
    }

    public static void blendEquation(int mode){
        Core.gl.glBlendEquation(mode);
    }

    public static void blendEquationSeparate(int modeRGB, int modeAlpha){
        Core.gl.glBlendEquationSeparate(modeRGB, modeAlpha);
    }

    public static void blendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha){
        Core.gl.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
    }

    public static void bufferData(int target, int size, Buffer data, int usage){
        Core.gl.glBufferData(target, size, data, usage);
    }

    public static void bufferSubData(int target, int offset, int size, Buffer data){
        Core.gl.glBufferSubData(target, offset, size, data);
    }

    public static int checkFramebufferStatus(int target){
        return Core.gl.glCheckFramebufferStatus(target);
    }

    public static void compileShader(int shader){
        Core.gl.glCompileShader(shader);
    }

    public static int createProgram(){
        return Core.gl.glCreateProgram();
    }

    public static int createShader(int type){
        return Core.gl.glCreateShader(type);
    }

    public static void deleteBuffer(int buffer){
        Core.gl.glDeleteBuffer(buffer);
    }

    public static void deleteFramebuffer(int framebuffer){
        Core.gl.glDeleteFramebuffer(framebuffer);
    }

    public static void deleteProgram(int program){
        Core.gl.glDeleteProgram(program);
    }

    public static void deleteRenderbuffer(int renderbuffer){
        Core.gl.glDeleteRenderbuffer(renderbuffer);
    }

    public static void deleteShader(int shader){
        Core.gl.glDeleteShader(shader);
    }

    public static void detachShader(int program, int shader){
        Core.gl.glDetachShader(program, shader);
    }

    public static void disableVertexAttribArray(int index){
        Core.gl.glDisableVertexAttribArray(index);
    }

    public static void drawElements(int mode, int count, int type, int indices){
        Core.gl.glDrawElements(mode, count, type, indices);
    }

    public static void enableVertexAttribArray(int index){
        Core.gl.glEnableVertexAttribArray(index);
    }

    public static void framebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer){
        Core.gl.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
    }

    public static void framebufferTexture2D(int target, int attachment, int textarget, int texture, int level){
        Core.gl.glFramebufferTexture2D(target, attachment, textarget, texture, level);
    }

    public static int genBuffer(){
        return Core.gl.glGenBuffer();
    }

    public static void generateMipmap(int target){
        Core.gl.glGenerateMipmap(target);
    }

    public static int genFramebuffer(){
        return Core.gl.glGenFramebuffer();
    }

    public static int genRenderbuffer(){
        return Core.gl.glGenRenderbuffer();
    }

    public static String getActiveAttrib(int program, int index, IntBuffer size, IntBuffer type){
        return Core.gl.glGetActiveAttrib(program, index, size, type);
    }

    public static String getActiveUniform(int program, int index, IntBuffer size, IntBuffer type){
        return Core.gl.glGetActiveUniform(program, index, size, type);
    }

    public static int getAttribLocation(int program, String name){
        return Core.gl.glGetAttribLocation(program, name);
    }

    public static void getBooleanv(int pname, Buffer params){
        Core.gl.glGetBooleanv(pname, params);
    }

    public static void getBufferParameteriv(int target, int pname, IntBuffer params){
        Core.gl.glGetBufferParameteriv(target, pname, params);
    }

    public static void getFloatv(int pname, FloatBuffer params){
        Core.gl.glGetFloatv(pname, params);
    }

    public static void getFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params){
        Core.gl.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
    }

    public static void getProgramiv(int program, int pname, IntBuffer params){
        Core.gl.glGetProgramiv(program, pname, params);
    }

    public static String getProgramInfoLog(int program){
        return Core.gl.glGetProgramInfoLog(program);
    }

    public static void getRenderbufferParameteriv(int target, int pname, IntBuffer params){
        Core.gl.glGetRenderbufferParameteriv(target, pname, params);
    }

    public static void getShaderiv(int shader, int pname, IntBuffer params){
        Core.gl.glGetShaderiv(shader, pname, params);
    }

    public static String getShaderInfoLog(int shader){
        return Core.gl.glGetShaderInfoLog(shader);
    }

    public static void getShaderPrecisionFormat(int shadertype, int precisiontype, IntBuffer range, IntBuffer precision){
        Core.gl.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);
    }

    public static void getTexParameterfv(int target, int pname, FloatBuffer params){
        Core.gl.glGetTexParameterfv(target, pname, params);
    }

    public static void getTexParameteriv(int target, int pname, IntBuffer params){
        Core.gl.glGetTexParameteriv(target, pname, params);
    }

    public static void getUniformfv(int program, int location, FloatBuffer params){
        Core.gl.glGetUniformfv(program, location, params);
    }

    public static void getUniformiv(int program, int location, IntBuffer params){
        Core.gl.glGetUniformiv(program, location, params);
    }

    public static int getUniformLocation(int program, String name){
        return Core.gl.glGetUniformLocation(program, name);
    }

    public static void getVertexAttribfv(int index, int pname, FloatBuffer params){
        Core.gl.glGetVertexAttribfv(index, pname, params);
    }

    public static void getVertexAttribiv(int index, int pname, IntBuffer params){
        Core.gl.glGetVertexAttribiv(index, pname, params);
    }

    public static boolean isBuffer(int buffer){
        return Core.gl.glIsBuffer(buffer);
    }

    public static boolean isEnabled(int cap){
        return Core.gl.glIsEnabled(cap);
    }

    public static boolean isFramebuffer(int framebuffer){
        return Core.gl.glIsFramebuffer(framebuffer);
    }

    public static boolean isProgram(int program){
        return Core.gl.glIsProgram(program);
    }

    public static boolean isRenderbuffer(int renderbuffer){
        return Core.gl.glIsRenderbuffer(renderbuffer);
    }

    public static boolean isShader(int shader){
        return Core.gl.glIsShader(shader);
    }

    public static boolean isTexture(int texture){
        return Core.gl.glIsTexture(texture);
    }

    public static void linkProgram(int program){
        Core.gl.glLinkProgram(program);
    }

    public static void releaseShaderCompiler(){
        Core.gl.glReleaseShaderCompiler();
    }

    public static void renderbufferStorage(int target, int internalformat, int width, int height){
        Core.gl.glRenderbufferStorage(target, internalformat, width, height);
    }

    public static void sampleCoverage(float value, boolean invert){
        Core.gl.glSampleCoverage(value, invert);
    }

    public static void shaderSource(int shader, String string){
        Core.gl.glShaderSource(shader, string);
    }

    public static void stencilFuncSeparate(int face, int func, int ref, int mask){
        Core.gl.glStencilFuncSeparate(face, func, ref, mask);
    }

    public static void stencilMaskSeparate(int face, int mask){
        Core.gl.glStencilMaskSeparate(face, mask);
    }

    public static void stencilOpSeparate(int face, int fail, int zfail, int zpass){
        Core.gl.glStencilOpSeparate(face, fail, zfail, zpass);
    }

    public static void texParameterfv(int target, int pname, FloatBuffer params){
        Core.gl.glTexParameterfv(target, pname, params);
    }

    public static void texParameteri(int target, int pname, int param){
        Core.gl.glTexParameteri(target, pname, param);
    }

    public static void texParameteriv(int target, int pname, IntBuffer params){
        Core.gl.glTexParameteriv(target, pname, params);
    }

    public static void uniform1f(int location, float x){
        Core.gl.glUniform1f(location, x);
    }

    public static void uniform1fv(int location, int count, FloatBuffer v){
        Core.gl.glUniform1fv(location, count, v);
    }

    public static void uniform1fv(int location, int count, float[] v, int offset){
        Core.gl.glUniform1fv(location, count, v, offset);
    }

    public static void uniform1i(int location, int x){
        Core.gl.glUniform1i(location, x);
    }

    public static void uniform1iv(int location, int count, IntBuffer v){
        Core.gl.glUniform1iv(location, count, v);
    }

    public static void uniform1iv(int location, int count, int[] v, int offset){
        Core.gl.glUniform1iv(location, count, v, offset);
    }

    public static void uniform2f(int location, float x, float y){
        Core.gl.glUniform2f(location, x, y);
    }

    public static void uniform2fv(int location, int count, FloatBuffer v){
        Core.gl.glUniform2fv(location, count, v);
    }

    public static void uniform2fv(int location, int count, float[] v, int offset){
        Core.gl.glUniform2fv(location, count, v, offset);
    }

    public static void uniform2i(int location, int x, int y){
        Core.gl.glUniform2i(location, x, y);
    }

    public static void uniform2iv(int location, int count, IntBuffer v){
        Core.gl.glUniform2iv(location, count, v);
    }

    public static void uniform2iv(int location, int count, int[] v, int offset){
        Core.gl.glUniform2iv(location, count, v, offset);
    }

    public static void uniform3f(int location, float x, float y, float z){
        Core.gl.glUniform3f(location, x, y, z);
    }

    public static void uniform3fv(int location, int count, FloatBuffer v){
        Core.gl.glUniform3fv(location, count, v);
    }

    public static void uniform3fv(int location, int count, float[] v, int offset){
        Core.gl.glUniform3fv(location, count, v, offset);
    }

    public static void uniform3i(int location, int x, int y, int z){
        Core.gl.glUniform3i(location, x, y, z);
    }

    public static void uniform3iv(int location, int count, IntBuffer v){
        Core.gl.glUniform3iv(location, count, v);
    }

    public static void uniform3iv(int location, int count, int[] v, int offset){
        Core.gl.glUniform3iv(location, count, v, offset);
    }

    public static void uniform4f(int location, float x, float y, float z, float w){
        Core.gl.glUniform4f(location, x, y, z, w);
    }

    public static void uniform4fv(int location, int count, FloatBuffer v){
        Core.gl.glUniform4fv(location, count, v);
    }

    public static void uniform4fv(int location, int count, float[] v, int offset){
        Core.gl.glUniform4fv(location, count, v, offset);
    }

    public static void uniform4i(int location, int x, int y, int z, int w){
        Core.gl.glUniform4i(location, x, y, z, w);
    }

    public static void uniform4iv(int location, int count, IntBuffer v){
        Core.gl.glUniform4iv(location, count, v);
    }

    public static void uniform4iv(int location, int count, int[] v, int offset){
        Core.gl.glUniform4iv(location, count, v, offset);
    }

    public static void uniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer value){
        Core.gl.glUniformMatrix2fv(location, count, transpose, value);
    }

    public static void uniformMatrix2fv(int location, int count, boolean transpose, float[] value, int offset){
        Core.gl.glUniformMatrix2fv(location, count, transpose, value, offset);
    }

    public static void uniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer value){
        Core.gl.glUniformMatrix3fv(location, count, transpose, value);
    }

    public static void uniformMatrix3fv(int location, int count, boolean transpose, float[] value, int offset){
        Core.gl.glUniformMatrix3fv(location, count, transpose, value, offset);
    }

    public static void uniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer value){
        Core.gl.glUniformMatrix4fv(location, count, transpose, value);
    }

    public static void uniformMatrix4fv(int location, int count, boolean transpose, float[] value, int offset){
        Core.gl.glUniformMatrix4fv(location, count, transpose, value, offset);
    }

    public static void useProgram(int program){
        if(optimize && lastUsedProgram == program){
            return;
        }
        Core.gl.glUseProgram(program);
        lastUsedProgram = program;
    }

    public static void validateProgram(int program){
        Core.gl.glValidateProgram(program);
    }

    public static void vertexAttrib1f(int indx, float x){
        Core.gl.glVertexAttrib1f(indx, x);
    }

    public static void vertexAttrib1fv(int indx, FloatBuffer values){
        Core.gl.glVertexAttrib1fv(indx, values);
    }

    public static void vertexAttrib2f(int indx, float x, float y){
        Core.gl.glVertexAttrib2f(indx, x, y);
    }

    public static void vertexAttrib2fv(int indx, FloatBuffer values){
        Core.gl.glVertexAttrib2fv(indx, values);
    }

    public static void vertexAttrib3f(int indx, float x, float y, float z){
        Core.gl.glVertexAttrib3f(indx, x, y, z);
    }

    public static void vertexAttrib3fv(int indx, FloatBuffer values){
        Core.gl.glVertexAttrib3fv(indx, values);
    }

    public static void vertexAttrib4f(int indx, float x, float y, float z, float w){
        Core.gl.glVertexAttrib4f(indx, x, y, z, w);
    }

    public static void vertexAttrib4fv(int indx, FloatBuffer values){
        Core.gl.glVertexAttrib4fv(indx, values);
    }

    public static void vertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, Buffer ptr){
        Core.gl.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
    }

    public static void vertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, int ptr){
        Core.gl.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
    }
}

package arc.graphics.vector;

import arc.*;
import arc.graphics.*;
import arc.util.*;
import arc.util.pooling.Pool.*;

import java.nio.*;

public class CanvasShader implements Poolable{
    private static final String gl20Header = "#version 100\n"
    + "#define NANOVG_GL2 1\n"
    + "#define UNIFORMARRAY_SIZE " + GlUniforms.UNIFORMARRAY_SIZE + "\n";

    private static final String gl30Header = "#version 300 es\n"
    + "#define NANOVG_GL3 1\n"
    + "#define UNIFORMARRAY_SIZE " + GlUniforms.UNIFORMARRAY_SIZE + "\n";

    private static final String vertexShader =
    "#ifdef NANOVG_GL3\n"
    + "	uniform vec2 viewSize;\n"
    + "	in vec2 vertex;\n"
    + "	in vec2 tcoord;\n"
    + "	out vec2 ftcoord;\n"
    + "	out vec2 fpos;\n"
    + "#else\n"
    + "	uniform vec2 viewSize;\n"
    + "	attribute vec2 vertex;\n"
    + "	attribute vec2 tcoord;\n"
    + "	varying vec2 ftcoord;\n"
    + "	varying vec2 fpos;\n"
    + "#endif\n"
    + "void main(void) {\n"
    + "	   ftcoord = tcoord;\n"
    + "	   fpos = vertex;\n"
    + "	   gl_Position = vec4(2.0 * vertex.x/viewSize.x - 1.0, 1.0 - 2.0 * vertex.y/viewSize.y, 0, 1);\n"
    + "}\n";

    private static final String fragmentShader =
    "#if defined(GL_FRAGMENT_PRECISION_HIGH) || defined(NANOVG_GL3)\n"
    + " precision highp float;\n"
    + "#else\n"
    + " precision mediump float;\n"
    + "#endif\n"
    + "#ifdef NANOVG_GL3\n"
    + "#ifdef USE_UNIFORMBUFFER\n"
    + "	layout(std140) uniform frag {\n"
    + "		mat3 scissorMatrix;\n"
    + "		mat3 imageMatrix;\n"
    + "		mat3 gradientMatrix;\n"
    + "		vec4 solidColor;\n"
    + "		vec2 scissorExtent;\n"
    + "		vec2 scissorScale;\n"
    + "		vec2 imageExtent;\n"
    + "		vec2 gradientPoint1;\n"
    + "		vec2 gradientPoint2;\n"
    + "		int imageType;\n"
    + "		int useVertexForImageSample;\n"
    + "		int gradientStops;\n"
    + "		float gradientRadius;\n"
    + "		int gradientType;\n"
    + "		int gradientSpread;\n"
    + "		float strokeMult;\n"
    + "		float strokeThr;\n"
    + "                     \n"
    + "		int fragmentType;\n"
    + "	};\n"
    + "#else\n" // NANOVG_GL3 && !USE_UNIFORMBUFFER
    + "	uniform vec4 frag[UNIFORMARRAY_SIZE];\n"
    + "#endif\n"
    + "	uniform sampler2D image;\n"
    + "	uniform sampler2D gradientImage;\n"
    + "	in vec2 ftcoord;\n"
    + "	in vec2 fpos;\n"
    + "	out vec4 outColor;\n"
    + "#else\n" // !NANOVG_GL3
    + "	uniform vec4 frag[UNIFORMARRAY_SIZE];\n"
    + "	uniform sampler2D image;\n"
    + "	uniform sampler2D gradientImage;\n"
    + "	varying vec2 ftcoord;\n"
    + "	varying vec2 fpos;\n"
    + "#endif\n"
    + "#ifndef USE_UNIFORMBUFFER\n"
    + "	#define scissorMatrix mat3(frag[0].xyz, frag[1].xyz, frag[2].xyz)\n"
    + "	#define imageMatrix mat3(frag[3].xyz, frag[4].xyz, frag[5].xyz)\n"
    + "	#define gradientMatrix mat3(frag[6].xyz, frag[7].xyz, frag[8].xyz)\n"
    + "	#define solidColor frag[9]\n"
    + "	#define scissorExtent frag[10].xy \n"
    + "	#define scissorScale frag[10].zw \n"
    + "	#define imageExtent frag[11].xy \n"
    + "	#define gradientPoint1 frag[11].zw \n"
    + "	#define gradientPoint2 frag[12].xy \n"
    + "	#define imageType int(frag[12].z)\n"
    + "	#define useVertexForImageSample int(frag[12].w)\n"
    + "	#define gradientStops int(frag[13].x)\n"
    + "	#define gradientRadius frag[13].y\n"
    + "	#define gradientType int(frag[13].z)\n"
    + "	#define gradientSpread int(frag[13].w)\n"
    + "	#define strokeMult frag[14].x\n"
    + "	#define strokeThr frag[14].y\n"
    + "                     \n"
    + "	#define fragmentType int(frag[14].z)\n"
    + "#endif\n"
    + "                     \n"
    + "#define M_PI  3.14159265358979323846\n"
    + "                     \n"
    + "// Scissoring\n"
    + "float scissorMask(vec2 p) {\n"
    + "	   vec2 sc = (abs((scissorMatrix * vec3(p, 1.0)).xy) - scissorExtent);\n"
    + "	   sc = vec2(0.5,0.5) - sc * scissorScale;\n"
    + "	   return clamp(sc.x,0.0,1.0) * clamp(sc.y,0.0,1.0);\n"
    + "}\n"
    + "                     \n"
    + "#ifdef EDGE_AA\n"
    + "// Stroke - from [0..1] to clipped pyramid, where the slope is 1px.\n"
    + "float strokeMask() {\n"
    + "	   if(useVertexForImageSample == 1) {\n"
    + "	       return 1.0; \n"
    + "	   } else {\n"
    + "	       return min(1.0, (1.0 - abs(ftcoord.x * 2.0 - 1.0)) * strokeMult) * min(1.0, ftcoord.y);\n"
    + "	   }\n"
    + "}\n"
    + "#endif\n"
    + "\n"
    + "                     "
    + "vec4 interpolateColor( vec4 C1, vec4 C2, float start, float mid, float end ) { \n"
    + "    float at = (mid - start) / (end - start + 1E-16); \n"
    + "	   return (1.0 - at) * C1 + at * C2; \n"
    + "} \n"
    + "                     \n"
    + "vec4 getTexColor(sampler2D samp, vec2 point) { \n"
    + "#ifdef NANOVG_GL3\n"
    + "	    return texture(samp, point); \n"
    + "#else\n"
    + "	    return texture2D(samp, point); \n"
    + "#endif\n"
    + "} \n"
    + "                     \n"
    + "float getGradientSamplerStop( int i ) { \n"
    + "	    float gradientIndex = clamp(float(i), 0.0, float(gradientStops - 1)); \n"
    + "	    gradientIndex = (gradientIndex * 2.0 + 0.5) / float(gradientStops * 2); \n"
    + "	    return getTexColor(gradientImage, vec2(gradientIndex, 0.0))[0]; \n"
    + "} \n"
    + "                     \n"
    + "vec4 getGradientSamplerColor( int i ) { \n"
    + "	    float gradientIndex = clamp(float(i), 0.0, float(gradientStops - 1)); \n"
    + "	    gradientIndex = (gradientIndex * 2.0 + 1.5) / float(gradientStops * 2); \n"
    + "	    return getTexColor(gradientImage, vec2(gradientIndex, 0.0)); \n"
    + "} \n"
    + "                     \n"
    + "float clampGradientPoint(float at) { \n"
    + "	    if( gradientSpread == 0 ) {           //PAD \n"
    + "	        return clamp(at, 0.0, 1.0); \n"
    + "	    } else if( gradientSpread == 1 ) {    //REFLECT \n"
    + "	        if(at >= 0.0 && at <= 1.0) { \n"
    + "	            return at; \n"
    + "	        } \n"
    + "	        float floorAt = floor(at); \n"
    + "	        float fraction = at - floorAt; \n"
    + "	        if(mod(floorAt, 2.0) == 0.0) { \n"
    + "	            return fraction; \n"
    + "	        } \n"
    + "	        if(fraction < 0.0) { \n"
    + "	            return 1.0 + fraction; \n"
    + "	        } else { \n"
    + "	            return 1.0 - fraction; \n"
    + "	        } \n"
    + "	    } else if( gradientSpread == 2 ) {    //REPEAT\n"
    + "	        if(at >= 0.0 && at <= 1.0) { \n"
    + "	            return at; \n"
    + "	        } \n"
    + "	        float fraction = at - floor(at); \n"
    + "	        if(fraction > 0.0) { \n"
    + "	            return fraction; \n"
    + "	        } else { \n"
    + "	            return 1.0 - fraction; \n"
    + "	        } \n"
    + "	    } else { \n"
    + "	       return at; \n"
    + "	    } \n"
    + "} \n"
    + "                     \n"
    + "vec4 sampleGradientColor(float at) { \n"
    + "	    if(gradientStops == 0) return solidColor; \n"
    + "	    if(gradientStops == 1) return getGradientSamplerColor(0); \n"
    + "	    at = clampGradientPoint(at); \n"
    + "	    if( at < 0.0 ) at = 0.0; \n"
    + "	    if( at > 1.0 ) at = 1.0; \n"
    + "	    if( at < getGradientSamplerStop(0) || at > getGradientSamplerStop(gradientStops - 1) ){ \n"
    + "	    	    vec4 C1 = getGradientSamplerColor(gradientStops - 1); \n"
    + "	    	    vec4 C2 = getGradientSamplerColor(0); \n"
    + "	    	    float start = getGradientSamplerStop(gradientStops - 1); \n"
    + "	    	    float end = getGradientSamplerStop(0) + 1.0; \n"
    + "	    	    if( at < start ) at += 1.0; \n"
    + "	    	    return interpolateColor( C1, C2, start, at, end ); \n"
    + "	    } \n"
    + "	    for(int i = 1; i < 16; ++i){ \n" //TODO make sure this is truly max gradient stops (it's currently 16)
    + "				if(i >= gradientStops){ break; }\n"
    + "	    	    float start = getGradientSamplerStop(i-1); \n"
    + "	    	    float end = getGradientSamplerStop(i); \n"
    + "	    	    if( at >= start && at <= end ) { \n"
    + "	    	        vec4 C1 = getGradientSamplerColor(i-1); \n"
    + "	    	        vec4 C2 = getGradientSamplerColor(i); \n"
    + "	    	        return interpolateColor( C1, C2, start, at, end ); \n"
    + "	    	    } \n"
    + "	    } \n"
    + "	    return solidColor; \n"
    + "}\n"
    + "                     \n"
    + "vec4 computeLinearGradient( vec2 point ) { \n"
    + "    vec2 A = gradientPoint1; \n"
    + "    vec2 B = gradientPoint2; \n"
    + "    vec2 C = point; \n"
    + "    float BxAx = B.x - A.x; \n"
    + "    float ByAy = B.y - A.y; \n"
    + "    float CxAx = C.x - A.x; \n"
    + "    float CyAy = C.y - A.y; \n"
    + "    float t = (CxAx * BxAx + CyAy * ByAy) / (BxAx * BxAx + ByAy * ByAy); \n"
    + "    return sampleGradientColor( t ); \n"
    + "}\n"
    + "                     \n"
    + "vec4 computeRadialGradient(vec2 point) {\n"
    + "    vec2 C = gradientPoint1; \n"
    + "    vec2 F = gradientPoint2; \n"
    + "    vec2 G = point; \n"
    + "    float R = gradientRadius; \n"
    + "    float GxFx = G.x - F.x; \n"
    + "    float GyFy = G.y - F.y; \n"
    + "    float FxCx = F.x - C.x; \n"
    + "    float FyCy = F.y - C.y; \n"
    + "    float a = GxFx * GxFx + GyFy * GyFy; \n"
    + "    float b = 2.0 * (GxFx * FxCx + GyFy * FyCy); \n"
    + "    float c = FxCx * FxCx + FyCy * FyCy - R * R; \n"
    + "    float t = (- b + sqrt(b * b - 4.0 * a * c)) / (2.0 * a); \n"
    + "    return sampleGradientColor(1.0 / t); \n"
    + "}\n"
    + "                     \n"
    + "vec4 computeBoxGradient(vec2 point) {\n"
    + "	   vec2 min = gradientPoint1;\n"
    + "	   vec2 dimensions = gradientPoint2;\n"
    + "	   vec2 center = min + (dimensions / 2.0);\n"
    + "	   vec2 max = min + dimensions;\n"
    + "	   vec2 delta = abs(center - point);\n"
    + "	   vec2 extent = delta / dimensions;\n"
    + "	   bool ab = (point.y - min.y) * dimensions.x > dimensions.y * (point.x - min.x); \n"
    + "	   bool ad = (point.y - min.y) * dimensions.x > dimensions.y * (max.x - point.x); \n"
    + "	   if(ab && ad || !ab && !ad) { \n"
    + "        return sampleGradientColor(extent.y); \n"
    + "	   } else { \n"
    + "        return sampleGradientColor(extent.x); \n"
    + "	   }\n"
    + "}\n"
    + "                     \n"
    + "vec4 computeConicalGradient(vec2 point) {\n"
    + "	   vec2 A = point - gradientPoint1;\n"
    + "	   if (abs(A.y) == abs(A.x)) A.y += 0.002;\n"
    + "	   float t = atan(-A.y, A.x) / (2.0 * M_PI);\n"
    + "	   float at = t - floor(t);\n"
    + "    return sampleGradientColor(at); \n"
    + "}\n"
    + "                     \n"
    + "vec4 computeGradient(vec2 point) { \n"
    + "    if( gradientType == 1 ) return computeLinearGradient( point ); \n"
    + "    else if( gradientType == 2 ) return computeRadialGradient( point ); \n"
    + "    else if( gradientType == 3 ) return computeBoxGradient( point ); \n"
    + "    else if( gradientType == 4 ) return computeConicalGradient( point ); \n"
    + "    return solidColor; \n"
    + "}\n"
    + "                     \n"
    + "vec4 getTexColorByTexType(sampler2D samp, vec2 point) { \n"
    + "    vec4 color = getTexColor(samp, point);\n"
    + "    if (imageType == 2) color = vec4(color.xyz * color.w, color.w);"
    + "    if (imageType == 3) color = vec4(color.x);"
    + "    return color;"
    + "} \n"
    + "                     \n"
    + "void main(void) {\n"
    + "    vec4 color;\n"
    + "	   float scissor = scissorMask(fpos);\n"
    + "#ifdef EDGE_AA\n"
    + "	   float strokeAlpha = strokeMask();\n"
    + "	   //if (strokeAlpha < strokeThr) discard;\n"//TODO remove at bottom
    + "#else\n"
    + "	   float strokeAlpha = 1.0;\n"
    + "#endif\n"
    + "	   if (fragmentType == 0) {			// Stencil fill \n"
    + "		    color = vec4(1.0, 1.0, 1.0, 1.0); \n"
    + "	   } else if (fragmentType == 1) {		// Fill\n"
    + "		    // Calculate gradient color using box gradient\n"
    + "		    color = solidColor;\n"
    + "		    \n"
    + "		    if(gradientType != 0) { \n"
    + "		        // Calculate color from gradient\n"
    + "		        vec2 pt = (gradientMatrix * vec3(fpos, 1.0)).xy;\n"
    + "		        color *= computeGradient(pt); \n"
    + "		    } \n"
    + "		    \n"
    + "		    if(imageType != 0) { \n"
    + "		        if(useVertexForImageSample == 1) { \n"
    + "		            color *= getTexColorByTexType(image, ftcoord); \n"
    + "		        } else { \n"
    + "		            // Calculate color from texture\n"
    + "		            vec2 pt = (imageMatrix * vec3(fpos, 1.0)).xy / imageExtent;\n"
    + "		            color *= getTexColorByTexType(image, pt);\n"
    + "		        } \n"
    + "		    } \n"
    + "		    \n"
    + "		    // Combine alpha\n"
    + "		    color *= strokeAlpha * scissor;\n"
    + "	   } \n"
    + "                     \n"
    + "#ifdef EDGE_AA\n"
    + "	   if (strokeAlpha < strokeThr) discard;\n"
    + "#endif\n"
    + "#ifdef NANOVG_GL3\n"
    + "	   outColor = color;\n"
    + "#else\n"
    + "	   gl_FragColor = color;\n"
    + "#endif\n               "
    + "}\n                    ";
    private final IntBuffer tmpHandle = Buffers.newIntBuffer(1);
    int programHandle;
    int fragmentShaderHandle;
    int vertexShaderHandle;
    int viewSizeUniformLocation;
    int imageUniformLocation;
    int gradientImageUniformLocation;
    int fragUniformLocation;

    public void init(boolean antiAlias, boolean debug){
        String opts = antiAlias ? "#define EDGE_AA 1\n" : "";
        String header = Core.graphics.isGL30Available() ? gl30Header : gl20Header;

        programHandle = Gl.createProgram();
        vertexShaderHandle = Gl.createShader(GL20.GL_VERTEX_SHADER);
        fragmentShaderHandle = Gl.createShader(GL20.GL_FRAGMENT_SHADER);

        Gl.shaderSource(vertexShaderHandle, header + opts + vertexShader);
        Gl.shaderSource(fragmentShaderHandle, header + opts + fragmentShader);

        if(!compileShader(vertexShaderHandle, "vertex")){
            throw new RuntimeException("Error compiling vertex shader: " + Gl.getShaderInfoLog(vertexShaderHandle));
        }

        if(!compileShader(fragmentShaderHandle, "fragment")){
            throw new RuntimeException("Error compiling fragment shader: " + Gl.getShaderInfoLog(fragmentShaderHandle));
        }

        Gl.attachShader(programHandle, vertexShaderHandle);
        Gl.attachShader(programHandle, fragmentShaderHandle);

        Gl.bindAttribLocation(programHandle, 0, "vertex");
        Gl.bindAttribLocation(programHandle, 1, "tcoord");

        Gl.linkProgram(programHandle);
        tmpHandle.clear();
        Gl.getProgramiv(programHandle, GL20.GL_LINK_STATUS, tmpHandle);
        if(tmpHandle.get(0) != GL20.GL_TRUE){
            dumpProgramError(programHandle);
            throw new RuntimeException("Shader error: " + Gl.getProgramInfoLog(programHandle));
        }

        checkError(debug, "init");
        initUniformLocations(debug);
    }

    private boolean compileShader(int shaderHandle, String shaderType){
        Gl.compileShader(shaderHandle);
        tmpHandle.clear();
        Gl.getShaderiv(shaderHandle, GL20.GL_COMPILE_STATUS, tmpHandle);
        if(tmpHandle.get(0) == GL20.GL_TRUE){
            return true;
        }else{
            dumpShaderError(shaderHandle, shaderType);
            return false;
        }
    }

    private void initUniformLocations(boolean debug){
        viewSizeUniformLocation = Gl.getUniformLocation(programHandle, "viewSize");
        imageUniformLocation = Gl.getUniformLocation(programHandle, "image");
        gradientImageUniformLocation = Gl.getUniformLocation(programHandle, "gradientImage");
        fragUniformLocation = Gl.getUniformLocation(programHandle, "frag");
        checkError(debug, "uniform locations");
    }

    private void checkError(boolean debug, String str){
        if(debug){
            int err = Gl.getError();
            if(err != GL20.GL_NO_ERROR){
                Log.err("Error " + err + " after " + str);
            }
        }
    }

    private void dumpShaderError(int shader, String type){
        String str = Gl.getShaderInfoLog(shader);
        Log.err("Shader " + type + " error: " + str);
    }

    private void dumpProgramError(int prog){
        String str = Gl.getProgramInfoLog(prog);
        Log.err("Program error: " + str);
    }

    private void delete(){
        if(programHandle != 0){
            Gl.deleteProgram(programHandle);
        }
        if(vertexShaderHandle != 0){
            Gl.deleteShader(vertexShaderHandle);
        }
        if(fragmentShaderHandle != 0){
            Gl.deleteShader(fragmentShaderHandle);
        }
    }

    @Override
    public void reset(){
        delete();
        programHandle = 0;
        fragmentShaderHandle = 0;
        vertexShaderHandle = 0;
        viewSizeUniformLocation = -1;
        imageUniformLocation = -1;
        fragUniformLocation = -1;
    }
}

package arc.graphics.gl;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;

import java.nio.*;

/**
 * <p>
 * A shader program encapsulates a vertex and fragment shader pair linked to form a shader program.
 * </p>
 *
 * <p>
 * After construction a Shader can be used to draw {@link Mesh}. To make the GPU use a specific Shader the programs
 * {@link Shader#bind()} method must be used which effectively binds the program.
 * </p>
 *
 * <p>
 * When a Shader is bound one can set uniforms, vertex attributes and attributes as needed via the respective methods.
 * </p>
 *
 *
 * <p>
 * A Shader must be disposed via a call to {@link Shader#dispose()} when it is no longer needed
 * </p>
 *
 * <p>
 * ShaderPrograms are managed. In case the OpenGL context is lost all shaders get invalidated and have to be reloaded. This
 * happens on Android when a user switches to another application or receives an incoming call. Managed ShaderPrograms are
 * automatically reloaded when the OpenGL context is recreated so you don't have to do this manually.
 * </p>
 * @author mzechner
 */
public class Shader implements Disposable{
    /** default name for position attributes **/
    public static final String positionAttribute = "a_position";
    /** default name for normal attributes **/
    public static final String normalAttribute = "a_normal";
    /** default name for color attributes **/
    public static final String colorAttribute = "a_color";
    /** default name for mix color attributes **/
    public static final String mixColorAttribute = "a_mix_color";
    /** default name for texcoords attributes, append texture unit number **/
    public static final String texcoordAttribute = "a_texCoord";
    /** default name for tangent attribute **/
    public static final String tangentAttribute = "a_tangent";
    /** default name for binormal attribute **/
    public static final String binormalAttribute = "a_binormal";
    /** default name for boneweight attribute **/
    public static final String boneweightAttribute = "a_boneWeight";
    /** the list of currently available shaders **/
    private final static ObjectMap<Application, Array<Shader>> shaders = new ObjectMap<>();
    /** flag indicating whether attributes & uniforms must be present at all times **/
    public static boolean pedantic = false;
    /**
     * code that is always added to the vertex shader code, typically used to inject a #version line. Note that this is added
     * as-is, you should include a newline (`\n`) if needed.
     */
    public static String prependVertexCode = "";
    /**
     * code that is always added to every fragment shader code, typically used to inject a #version line. Note that this is added
     * as-is, you should include a newline (`\n`) if needed.
     */
    public static String prependFragmentCode = "";
    /** uniform lookup **/
    private final ObjectIntMap<String> uniforms = new ObjectIntMap<>();
    /** uniform types **/
    private final ObjectIntMap<String> uniformTypes = new ObjectIntMap<>();
    /** uniform sizes **/
    private final ObjectIntMap<String> uniformSizes = new ObjectIntMap<>();
    /** attribute lookup **/
    private final ObjectIntMap<String> attributes = new ObjectIntMap<>();
    /** attribute types **/
    private final ObjectIntMap<String> attributeTypes = new ObjectIntMap<>();
    /** attribute sizes **/
    private final ObjectIntMap<String> attributeSizes = new ObjectIntMap<>();
    /** vertex shader source **/
    private final String vertexShaderSource;
    /** fragment shader source **/
    private final String fragmentShaderSource;
    IntBuffer params = Buffers.newIntBuffer(1);
    IntBuffer type = Buffers.newIntBuffer(1);
    /** the log **/
    private String log = "";
    /** whether this program compiled successfully **/
    private boolean isCompiled;
    /** uniform names **/
    private String[] uniformNames;
    /** attribute names **/
    private String[] attributeNames;
    /** program handle **/
    private int program;
    /** vertex shader handle **/
    private int vertexShaderHandle;
    /** fragment shader handle **/
    private int fragmentShaderHandle;
    /** whether this shader was invalidated **/
    private boolean invalidated, disposed;

    /**
     * Constructs a new Shader and immediately compiles it.
     * @param vertexShader the vertex shader
     * @param fragmentShader the fragment shader
     */
    public Shader(String vertexShader, String fragmentShader){
        if(vertexShader == null) throw new IllegalArgumentException("vertex shader must not be null");
        if(fragmentShader == null) throw new IllegalArgumentException("fragment shader must not be null");

        if(prependVertexCode != null && prependVertexCode.length() > 0)
            vertexShader = prependVertexCode + vertexShader;
        if(prependFragmentCode != null && prependFragmentCode.length() > 0)
            fragmentShader = prependFragmentCode + fragmentShader;

        this.vertexShaderSource = vertexShader;
        this.fragmentShaderSource = fragmentShader;

        compileShaders(vertexShader, fragmentShader);
        if(isCompiled()){
            fetchAttributes();
            fetchUniforms();
            addManagedShader(Core.app, this);
        }else{
            throw new IllegalArgumentException("Failed to compile shader: " + log);
        }
    }

    public Shader(Fi vertexShader, Fi fragmentShader){
        this(vertexShader.readString(), fragmentShader.readString());
    }

    /**Applies all relevant uniforms, if applicable. Should be overriden.*/
    public void apply(){}

    /**Invalidates all shaders so the next time they are used new handles are generated*/
    public static void invalidateAllShaderPrograms(Application app){
        if(Core.gl20 == null) return;

        Array<Shader> shaderArray = shaders.get(app);
        if(shaderArray == null) return;

        for(int i = 0; i < shaderArray.size; i++){
            shaderArray.get(i).invalidated = true;
            shaderArray.get(i).checkManaged();
        }
    }

    public static void clearAllShaderPrograms(Application app){
        shaders.remove(app);
    }

    public static String getManagedStatus(){
        StringBuilder builder = new StringBuilder();
        builder.append("Managed shaders/app: { ");
        for(Application app : shaders.keys()){
            builder.append(shaders.get(app).size);
            builder.append(" ");
        }
        builder.append("}");
        return builder.toString();
    }

    /** @return the number of managed shader programs currently loaded */
    public static int getNumManagedShaderPrograms(){
        return shaders.get(Core.app).size;
    }

    /**
     * Loads and compiles the shaders, creates a new program and links the shaders.
     */
    private void compileShaders(String vertexShader, String fragmentShader){
        vertexShaderHandle = loadShader(GL20.GL_VERTEX_SHADER, vertexShader);
        fragmentShaderHandle = loadShader(GL20.GL_FRAGMENT_SHADER, fragmentShader);

        if(vertexShaderHandle == -1 || fragmentShaderHandle == -1){
            isCompiled = false;
            return;
        }

        program = linkProgram(createProgram());
        if(program == -1){
            isCompiled = false;
            return;
        }

        isCompiled = true;
    }

    private int loadShader(int type, String source){
        IntBuffer intbuf = Buffers.newIntBuffer(1);

        int shader = Gl.createShader(type);
        if(shader == 0) return -1;

        Gl.shaderSource(shader, source);
        Gl.compileShader(shader);
        Gl.getShaderiv(shader, GL20.GL_COMPILE_STATUS, intbuf);

        int compiled = intbuf.get(0);
        if(compiled == 0){
            String infoLog = Gl.getShaderInfoLog(shader);
            log += type == GL20.GL_VERTEX_SHADER ? "Vertex shader\n" : "Fragment shader:\n";
            log += infoLog;
            return -1;
        }

        return shader;
    }

    protected int createProgram(){
        int program = Gl.createProgram();
        return program != 0 ? program : -1;
    }

    private int linkProgram(int program){
        if(program == -1) return -1;

        Gl.attachShader(program, vertexShaderHandle);
        Gl.attachShader(program, fragmentShaderHandle);
        Gl.linkProgram(program);

        ByteBuffer tmp = ByteBuffer.allocateDirect(4);
        tmp.order(ByteOrder.nativeOrder());
        IntBuffer intbuf = tmp.asIntBuffer();

        Gl.getProgramiv(program, GL20.GL_LINK_STATUS, intbuf);
        int linked = intbuf.get(0);
        if(linked == 0){
            log = Gl.getProgramInfoLog(program);
            return -1;
        }

        return program;
    }

    /**
     * @return the log info for the shader compilation and program linking stage. The shader needs to be bound for this method to
     * have an effect.
     */
    public String getLog(){
        if(isCompiled){
            log = Gl.getProgramInfoLog(program);
            return log;
        }else{
            return log;
        }
    }

    /** @return whether this Shader compiled successfully. */
    public boolean isCompiled(){
        return isCompiled;
    }

    private int fetchAttributeLocation(String name){
        // -2 == not yet cached
        // -1 == cached but not found
        int location;
        if((location = attributes.get(name, -2)) == -2){
            location = Gl.getAttribLocation(program, name);
            attributes.put(name, location);
        }
        return location;
    }

    private int fetchUniformLocation(String name){
        return fetchUniformLocation(name, pedantic);
    }

    public int fetchUniformLocation(String name, boolean pedantic){
        // -2 == not yet cached
        // -1 == cached but not found
        int location;
        if((location = uniforms.get(name, -2)) == -2){
            location = Gl.getUniformLocation(program, name);
            if(location == -1 && pedantic)
                throw new IllegalArgumentException("no uniform with name '" + name + "' in shader");
            uniforms.put(name, location);
        }
        return location;
    }

    /**
     * Sets the uniform with the given name. The {@link Shader} must be bound for this to work.
     * @param name the name of the uniform
     * @param value the value
     */
    public void setUniformi(String name, int value){
        checkManaged();
        int location = fetchUniformLocation(name);
        Gl.uniform1i(location, value);
    }

    public void setUniformi(int location, int value){
        checkManaged();
        Gl.uniform1i(location, value);
    }

    /**
     * Sets the uniform with the given name. The {@link Shader} must be bound for this to work.
     * @param name the name of the uniform
     * @param value1 the first value
     * @param value2 the second value
     */
    public void setUniformi(String name, int value1, int value2){
        checkManaged();
        int location = fetchUniformLocation(name);
        Gl.uniform2i(location, value1, value2);
    }

    public void setUniformi(int location, int value1, int value2){
        checkManaged();
        Gl.uniform2i(location, value1, value2);
    }

    /**
     * Sets the uniform with the given name. The {@link Shader} must be bound for this to work.
     * @param name the name of the uniform
     * @param value1 the first value
     * @param value2 the second value
     * @param value3 the third value
     */
    public void setUniformi(String name, int value1, int value2, int value3){
        checkManaged();
        int location = fetchUniformLocation(name);
        Gl.uniform3i(location, value1, value2, value3);
    }

    public void setUniformi(int location, int value1, int value2, int value3){
        checkManaged();
        Gl.uniform3i(location, value1, value2, value3);
    }

    /**
     * Sets the uniform with the given name. The {@link Shader} must be bound for this to work.
     * @param name the name of the uniform
     * @param value1 the first value
     * @param value2 the second value
     * @param value3 the third value
     * @param value4 the fourth value
     */
    public void setUniformi(String name, int value1, int value2, int value3, int value4){
        checkManaged();
        int location = fetchUniformLocation(name);
        Gl.uniform4i(location, value1, value2, value3, value4);
    }

    public void setUniformi(int location, int value1, int value2, int value3, int value4){
        checkManaged();
        Gl.uniform4i(location, value1, value2, value3, value4);
    }

    /**
     * Sets the uniform with the given name. The {@link Shader} must be bound for this to work.
     * @param name the name of the uniform
     * @param value the value
     */
    public void setUniformf(String name, float value){
        checkManaged();
        int location = fetchUniformLocation(name);
        Gl.uniform1f(location, value);
    }

    public void setUniformf(int location, float value){
        checkManaged();
        Gl.uniform1f(location, value);
    }

    /**
     * Sets the uniform with the given name. The {@link Shader} must be bound for this to work.
     * @param name the name of the uniform
     * @param value1 the first value
     * @param value2 the second value
     */
    public void setUniformf(String name, float value1, float value2){
        checkManaged();
        int location = fetchUniformLocation(name);
        Gl.uniform2f(location, value1, value2);
    }

    public void setUniformf(int location, float value1, float value2){
        checkManaged();
        Gl.uniform2f(location, value1, value2);
    }

    /**
     * Sets the uniform with the given name. The {@link Shader} must be bound for this to work.
     * @param name the name of the uniform
     * @param value1 the first value
     * @param value2 the second value
     * @param value3 the third value
     */
    public void setUniformf(String name, float value1, float value2, float value3){
        checkManaged();
        int location = fetchUniformLocation(name);
        Gl.uniform3f(location, value1, value2, value3);
    }

    public void setUniformf(int location, float value1, float value2, float value3){
        checkManaged();
        Gl.uniform3f(location, value1, value2, value3);
    }

    /**
     * Sets the uniform with the given name. The {@link Shader} must be bound for this to work.
     * @param name the name of the uniform
     * @param value1 the first value
     * @param value2 the second value
     * @param value3 the third value
     * @param value4 the fourth value
     */
    public void setUniformf(String name, float value1, float value2, float value3, float value4){
        checkManaged();
        int location = fetchUniformLocation(name);
        Gl.uniform4f(location, value1, value2, value3, value4);
    }

    public void setUniformf(int location, float value1, float value2, float value3, float value4){
        checkManaged();
        Gl.uniform4f(location, value1, value2, value3, value4);
    }

    public void setUniform1fv(String name, float[] values, int offset, int length){
        checkManaged();
        int location = fetchUniformLocation(name);
        Gl.uniform1fv(location, length, values, offset);
    }

    public void setUniform1fv(int location, float[] values, int offset, int length){
        checkManaged();
        Gl.uniform1fv(location, length, values, offset);
    }

    public void setUniform2fv(String name, float[] values, int offset, int length){
        checkManaged();
        int location = fetchUniformLocation(name);
        Gl.uniform2fv(location, length / 2, values, offset);
    }

    public void setUniform2fv(int location, float[] values, int offset, int length){
        checkManaged();
        Gl.uniform2fv(location, length / 2, values, offset);
    }

    public void setUniform3fv(String name, float[] values, int offset, int length){
        checkManaged();
        int location = fetchUniformLocation(name);
        Gl.uniform3fv(location, length / 3, values, offset);
    }

    public void setUniform3fv(int location, float[] values, int offset, int length){
        checkManaged();
        Gl.uniform3fv(location, length / 3, values, offset);
    }

    public void setUniform4fv(String name, float[] values, int offset, int length){
        checkManaged();
        int location = fetchUniformLocation(name);
        Gl.uniform4fv(location, length / 4, values, offset);
    }

    public void setUniform4fv(int location, float[] values, int offset, int length){
        checkManaged();
        Gl.uniform4fv(location, length / 4, values, offset);
    }

    /**
     * Sets the uniform matrix with the given name. The {@link Shader} must be bound for this to work.
     * @param name the name of the uniform
     * @param matrix the matrix
     */
    public void setUniformMatrix(String name, Mat matrix){
        setUniformMatrix(name, matrix, false);
    }

    /**
     * Sets the uniform matrix with the given name. The {@link Shader} must be bound for this to work.
     * @param name the name of the uniform
     * @param matrix the matrix
     * @param transpose whether the uniform matrix should be transposed
     */
    public void setUniformMatrix(String name, Mat matrix, boolean transpose){
        setUniformMatrix(fetchUniformLocation(name), matrix, transpose);
    }

    public void setUniformMatrix(int location, Mat matrix){
        setUniformMatrix(location, matrix, false);
    }

    public void setUniformMatrix(int location, Mat matrix, boolean transpose){
        checkManaged();
        Gl.uniformMatrix3fv(location, 1, transpose, matrix.val, 0);
    }

    public void setUniformMatrix4(String name, float[] val){
        checkManaged();
        Gl.uniformMatrix4fv(fetchUniformLocation(name), 1, false, val, 0);
    }

    /**
     * Sets an array of uniform matrices with the given name. The {@link Shader} must be bound for this to work.
     * @param name the name of the uniform
     * @param buffer buffer containing the matrix data
     * @param transpose whether the uniform matrix should be transposed
     */
    public void setUniformMatrix3fv(String name, FloatBuffer buffer, int count, boolean transpose){
        checkManaged();
        buffer.position(0);
        int location = fetchUniformLocation(name);
        Gl.uniformMatrix3fv(location, count, transpose, buffer);
    }

    /**
     * Sets an array of uniform matrices with the given name. The {@link Shader} must be bound for this to work.
     * @param name the name of the uniform
     * @param buffer buffer containing the matrix data
     * @param transpose whether the uniform matrix should be transposed
     */
    public void setUniformMatrix4fv(String name, FloatBuffer buffer, int count, boolean transpose){
        checkManaged();
        buffer.position(0);
        int location = fetchUniformLocation(name);
        Gl.uniformMatrix4fv(location, count, transpose, buffer);
    }

    public void setUniformMatrix4fv(int location, float[] values, int offset, int length){
        checkManaged();
        Gl.uniformMatrix4fv(location, length / 16, false, values, offset);
    }

    public void setUniformMatrix4fv(String name, float[] values, int offset, int length){
        setUniformMatrix4fv(fetchUniformLocation(name), values, offset, length);
    }

    /**
     * Sets the uniform with the given name. The {@link Shader} must be bound for this to work.
     * @param name the name of the uniform
     * @param values x and y as the first and second values respectively
     */
    public void setUniformf(String name, Vec2 values){
        setUniformf(name, values.x, values.y);
    }

    public void setUniformf(int location, Vec2 values){
        setUniformf(location, values.x, values.y);
    }

    /**
     * Sets the uniform with the given name. The {@link Shader} must be bound for this to work.
     * @param name the name of the uniform
     * @param values x, y and z as the first, second and third values respectively
     */
    public void setUniformf(String name, Vec3 values){
        setUniformf(name, values.x, values.y, values.z);
    }

    public void setUniformf(int location, Vec3 values){
        setUniformf(location, values.x, values.y, values.z);
    }

    /**
     * Sets the uniform with the given name. The {@link Shader} must be bound for this to work.
     * @param name the name of the uniform
     * @param values r, g, b and a as the first through fourth values respectively
     */
    public void setUniformf(String name, Color values){
        setUniformf(name, values.r, values.g, values.b, values.a);
    }

    public void setUniformf(int location, Color values){
        setUniformf(location, values.r, values.g, values.b, values.a);
    }

    /**
     * Sets the vertex attribute with the given name. The {@link Shader} must be bound for this to work.
     * @param name the attribute name
     * @param size the number of components, must be >= 1 and <= 4
     * @param type the type, must be one of GL20.GL_BYTE, GL20.GL_UNSIGNED_BYTE, GL20.GL_SHORT,
     * GL20.GL_UNSIGNED_SHORT,GL20.GL_FIXED, or GL20.GL_FLOAT. GL_FIXED will not work on the desktop
     * @param normalize whether fixed point data should be normalized. Will not work on the desktop
     * @param stride the stride in bytes between successive attributes
     * @param buffer the buffer containing the vertex attributes.
     */
    public void setVertexAttribute(String name, int size, int type, boolean normalize, int stride, Buffer buffer){
        checkManaged();
        int location = fetchAttributeLocation(name);
        if(location == -1) return;
        Gl.vertexAttribPointer(location, size, type, normalize, stride, buffer);
    }

    public void setVertexAttribute(int location, int size, int type, boolean normalize, int stride, Buffer buffer){
        checkManaged();
        Gl.vertexAttribPointer(location, size, type, normalize, stride, buffer);
    }

    /**
     * Sets the vertex attribute with the given name. The {@link Shader} must be bound for this to work.
     * @param name the attribute name
     * @param size the number of components, must be >= 1 and <= 4
     * @param type the type, must be one of GL20.GL_BYTE, GL20.GL_UNSIGNED_BYTE, GL20.GL_SHORT,
     * GL20.GL_UNSIGNED_SHORT,GL20.GL_FIXED, or GL20.GL_FLOAT. GL_FIXED will not work on the desktop
     * @param normalize whether fixed point data should be normalized. Will not work on the desktop
     * @param stride the stride in bytes between successive attributes
     * @param offset byte offset into the vertex buffer object bound to GL20.GL_ARRAY_BUFFER.
     */
    public void setVertexAttribute(String name, int size, int type, boolean normalize, int stride, int offset){
        checkManaged();
        int location = fetchAttributeLocation(name);
        if(location == -1) return;
        Gl.vertexAttribPointer(location, size, type, normalize, stride, offset);
    }

    public void setVertexAttribute(int location, int size, int type, boolean normalize, int stride, int offset){
        checkManaged();
        Gl.vertexAttribPointer(location, size, type, normalize, stride, offset);
    }

    /**
     * Makes OpenGL ES 2.0 use this vertex and fragment shader pair.
     */
    public void bind(){
        checkManaged();
        Gl.useProgram(program);
    }

    /** Disposes all resources associated with this shader. Must be called when the shader is no longer used. */
    @Override
    public void dispose(){
        if(disposed) return;

        Gl.useProgram(0);
        Gl.deleteShader(vertexShaderHandle);
        Gl.deleteShader(fragmentShaderHandle);
        Gl.deleteProgram(program);
        if(shaders.get(Core.app) != null) shaders.get(Core.app).remove(this, true);
        disposed = true;
    }

    @Override
    public boolean isDisposed(){
        return disposed;
    }

    /**
     * Disables the vertex attribute with the given name
     * @param name the vertex attribute name
     */
    public void disableVertexAttribute(String name){
        checkManaged();
        int location = fetchAttributeLocation(name);
        if(location == -1) return;
        Gl.disableVertexAttribArray(location);
    }

    public void disableVertexAttribute(int location){
        checkManaged();
        Gl.disableVertexAttribArray(location);
    }

    /**
     * Enables the vertex attribute with the given name
     * @param name the vertex attribute name
     */
    public void enableVertexAttribute(String name){
        checkManaged();
        int location = fetchAttributeLocation(name);
        if(location == -1) return;
        Gl.enableVertexAttribArray(location);
    }

    public void enableVertexAttribute(int location){
        checkManaged();
        Gl.enableVertexAttribArray(location);
    }

    private void checkManaged(){
        if(invalidated){
            compileShaders(vertexShaderSource, fragmentShaderSource);
            invalidated = false;
        }
    }

    private void addManagedShader(Application app, Shader shader){
        Array<Shader> managedResources = shaders.get(app);
        if(managedResources == null) managedResources = new Array<>();
        managedResources.add(shader);
        shaders.put(app, managedResources);
    }

    /**
     * Sets the given attribute
     * @param name the name of the attribute
     * @param value1 the first value
     * @param value2 the second value
     * @param value3 the third value
     * @param value4 the fourth value
     */
    public void setAttributef(String name, float value1, float value2, float value3, float value4){
        int location = fetchAttributeLocation(name);
        Gl.vertexAttrib4f(location, value1, value2, value3, value4);
    }

    private void fetchUniforms(){
        params.clear();
        Gl.getProgramiv(program, GL20.GL_ACTIVE_UNIFORMS, params);
        int numUniforms = params.get(0);

        uniformNames = new String[numUniforms];

        for(int i = 0; i < numUniforms; i++){
            params.clear();
            params.put(0, 1);
            type.clear();
            String name = Gl.getActiveUniform(program, i, params, type);
            int location = Gl.getUniformLocation(program, name);
            uniforms.put(name, location);
            uniformTypes.put(name, type.get(0));
            uniformSizes.put(name, params.get(0));
            uniformNames[i] = name;
        }
    }

    private void fetchAttributes(){
        params.clear();
        Gl.getProgramiv(program, GL20.GL_ACTIVE_ATTRIBUTES, params);
        int numAttributes = params.get(0);

        attributeNames = new String[numAttributes];

        for(int i = 0; i < numAttributes; i++){
            params.clear();
            params.put(0, 1);
            type.clear();
            String name = Gl.getActiveAttrib(program, i, params, type);
            int location = Gl.getAttribLocation(program, name);
            attributes.put(name, location);
            attributeTypes.put(name, type.get(0));
            attributeSizes.put(name, params.get(0));
            attributeNames[i] = name;
        }
    }

    /**
     * @param name the name of the attribute
     * @return whether the attribute is available in the shader
     */
    public boolean hasAttribute(String name){
        return attributes.containsKey(name);
    }

    /**
     * @param name the name of the attribute
     * @return the type of the attribute, one of {@link GL20#GL_FLOAT}, {@link GL20#GL_FLOAT_VEC2} etc.
     */
    public int getAttributeType(String name){
        return attributeTypes.get(name, 0);
    }

    /**
     * @param name the name of the attribute
     * @return the location of the attribute or -1.
     */
    public int getAttributeLocation(String name){
        return attributes.get(name, -1);
    }

    /**
     * @param name the name of the attribute
     * @return the size of the attribute or 0.
     */
    public int getAttributeSize(String name){
        return attributeSizes.get(name, 0);
    }

    /**
     * @param name the name of the uniform
     * @return whether the uniform is available in the shader
     */
    public boolean hasUniform(String name){
        return uniforms.containsKey(name);
    }

    /**
     * @param name the name of the uniform
     * @return the type of the uniform, one of {@link GL20#GL_FLOAT}, {@link GL20#GL_FLOAT_VEC2} etc.
     */
    public int getUniformType(String name){
        return uniformTypes.get(name, 0);
    }

    /**
     * @param name the name of the uniform
     * @return the location of the uniform or -1.
     */
    public int getUniformLocation(String name){
        return uniforms.get(name, -1);
    }

    /**
     * @param name the name of the uniform
     * @return the size of the uniform or 0.
     */
    public int getUniformSize(String name){
        return uniformSizes.get(name, 0);
    }

    /** @return the attributes */
    public String[] getAttributes(){
        return attributeNames;
    }

    /** @return the uniforms */
    public String[] getUniforms(){
        return uniformNames;
    }

    /** @return the source of the vertex shader */
    public String getVertexShaderSource(){
        return vertexShaderSource;
    }

    /** @return the source of the fragment shader */
    public String getFragmentShaderSource(){
        return fragmentShaderSource;
    }
}

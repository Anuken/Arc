package arc.g3d;

import arc.geom.*;
import arc.math.geom.*;
import arc.util.*;

public class Camera3D{
    /** field of view. */
    public float fov = 67;
    /** the near clipping plane distance, has to be positive **/
    public float near = 1;
    /** the far clipping plane distance, has to be positive **/
    public float far = 100;
    /** the viewport width and height **/
    public float width, height;
    /** the position of the camera **/
    public final Vec3 position = new Vec3();
    /** the unit length direction vector of the camera **/
    public final Vec3 direction = new Vec3(0, 0, -1);
    /** the unit length up vector of the camera **/
    public final Vec3 up = new Vec3(0, 1, 0);

    /** the combined projection and view matrix **/
    private final Matrix4 combined = new Matrix4();
    /** the projection matrix **/
    private final Matrix4 projection = new Matrix4();
    /** the view matrix **/
    private final Matrix4 view = new Matrix4();
    /** the inverse combined projection and view matrix **/
    private final Matrix4 invProjectionView = new Matrix4();

    public void update(){
        float aspect = width / height;
        projection.setToProjection(Math.abs(near), Math.abs(far), fov, aspect);
        view.setToLookAt(position, Tmp.v31.set(position).add(direction), up);
        combined.set(projection);
        Matrix4.mul(combined.val, view.val);
    }
}

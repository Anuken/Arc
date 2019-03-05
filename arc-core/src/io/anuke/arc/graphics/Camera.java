package io.anuke.arc.graphics;

import io.anuke.arc.Core;
import io.anuke.arc.Graphics;
import io.anuke.arc.Input;
import io.anuke.arc.math.Matrix3;
import io.anuke.arc.math.geom.Rectangle;
import io.anuke.arc.math.geom.Vector2;
import io.anuke.arc.math.geom.Vector3;

public class Camera{
    /** temporary vector which is returned. */
    private static final Vector2 tmpVector = new Vector2();
    /** the position of the camera **/
    public final Vector2 position = new Vector2();
    /** the combined projection and view matrix **/
    private final Matrix3 combined = new Matrix3();
    /** the inverse combined projection and view matrix **/
    private final Matrix3 invProjectionView = new Matrix3();
    /** the viewport width and height **/
    public float width, height;

    /**
     * Recalculates the projection and view matrix of this camera. Use this after you've manipulated
     * any of the attributes of the camera.
     */
    public void update(){
        combined.setOrtho(position.x - width / 2f, position.y - height / 2f, width, height);
        invProjectionView.set(combined).inv();
    }

    public void resize(float viewportWidth, float viewportHeight){
        this.width = viewportWidth;
        this.height = viewportHeight;
        update();
    }

    public Matrix3 projection(){
        return combined;
    }

    /**
     * Function to translate a point given in screen coordinates to world space. It's the same as GLU gluUnProject, but does not
     * rely on OpenGL. The x- and y-coordinate of vec are assumed to be in screen coordinates (origin is the top left corner, y
     * pointing down, x pointing to the right) as reported by the touch methods in {@link Input}. A z-coordinate of 0 will return a
     * point on the near plane, a z-coordinate of 1 will return a point on the far plane. This method allows you to specify the
     * viewport position and dimensions in the coordinate system expected by {@link GL20#glViewport(int, int, int, int)}, with the
     * origin in the bottom left corner of the screen.
     * @param screenCoords the point in screen coordinates (origin top left)
     * @param viewportX the coordinate of the bottom left corner of the viewport in glViewport coordinates.
     * @param viewportY the coordinate of the bottom left corner of the viewport in glViewport coordinates.
     * @param viewportWidth the width of the viewport in pixels
     * @param viewportHeight the height of the viewport in pixels
     * @return the mutated and unprojected screenCoords {@link Vector3}
     */
    public Vector2 unproject(Vector2 screenCoords, float viewportX, float viewportY, float viewportWidth, float viewportHeight){
        float x = screenCoords.x, y = screenCoords.y;
        x = x - viewportX;
        y = y - viewportY;
        screenCoords.x = (2 * x) / viewportWidth - 1;
        screenCoords.y = (2 * y) / viewportHeight - 1;
        screenCoords.mul(invProjectionView);
        return screenCoords;
    }

    /**
     * Function to translate a point given in screen coordinates to world space. It's the same as GLU gluUnProject but does not
     * rely on OpenGL. The viewport is assumed to span the whole screen and is fetched from {@link Graphics#getWidth()} and
     * {@link Graphics#getHeight()}. The x- and y-coordinate of vec are assumed to be in screen coordinates (origin is the top left
     * corner, y pointing down, x pointing to the right) as reported by the touch methods in {@link Input}. A z-coordinate of 0
     * will return a point on the near plane, a z-coordinate of 1 will return a point on the far plane.
     * @param screenCoords the point in screen coordinates
     * @return the mutated and unprojected screenCoords {@link Vector3}
     */
    public Vector2 unproject(Vector2 screenCoords){
        unproject(screenCoords, 0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());
        return screenCoords;
    }

    /** See {@link #unproject(Vector2)}. Returns the same Vector2 each time. */
    public Vector2 unproject(float screenX, float screenY){
        unproject(tmpVector.set(screenX, screenY), 0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());
        return tmpVector;
    }

    /**
     * Projects the {@link Vector3} given in world space to screen coordinates. It's the same as GLU gluProject with one small
     * deviation: The viewport is assumed to span the whole screen. The screen coordinate system has its origin in the
     * <b>bottom</b> left, with the y-axis pointing <b>upwards</b> and the x-axis pointing to the right.
     * @return the mutated and projected worldCoords {@link Vector3}
     */
    public Vector2 project(Vector2 worldCoords){
        project(worldCoords, 0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());
        return worldCoords;
    }

    /** See {@link #project(Vector2)}. Returns the same Vector2 each time. */
    public Vector2 project(float screenX, float screenY){
        project(tmpVector.set(screenX, screenY), 0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());
        return tmpVector;
    }

    /**
     * Projects the {@link Vector3} given in world space to screen coordinates. It's the same as GLU gluProject with one small
     * deviation: The viewport is assumed to span the whole screen. The screen coordinate system has its origin in the
     * <b>bottom</b> left, with the y-axis pointing <b>upwards</b> and the x-axis pointing to the right.
     * This method allows you to specify the viewport position and
     * dimensions in the coordinate system expected by {@link GL20#glViewport(int, int, int, int)}, with the origin in the bottom
     * left corner of the screen.
     * @param viewportX the coordinate of the bottom left corner of the viewport in glViewport coordinates.
     * @param viewportY the coordinate of the bottom left corner of the viewport in glViewport coordinates.
     * @param viewportWidth the width of the viewport in pixels
     * @param viewportHeight the height of the viewport in pixels
     * @return the mutated and projected worldCoords {@link Vector3}
     */
    public Vector2 project(Vector2 worldCoords, float viewportX, float viewportY, float viewportWidth, float viewportHeight){
        worldCoords.mul(combined);
        worldCoords.x = viewportWidth * (worldCoords.x + 1) / 2 + viewportX;
        worldCoords.y = viewportHeight * (worldCoords.y + 1) / 2 + viewportY;
        return worldCoords;
    }

    /**Sets the specified rectangle to this camera's bounds.*/
    public Rectangle bounds(Rectangle out){
        return out.setSize(width, height).setCenter(position);
    }
}

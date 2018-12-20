package io.anuke.arc.maps;

import io.anuke.arc.graphics.Camera;
import io.anuke.arc.math.Matrix3;

/** Models a common way of rendering {@link Map} objects */
public interface MapRenderer{
    /**
     * Sets the projection matrix and viewbounds from the given camera. If the camera changes, you have to call this method again.
     * The viewbounds are taken from the camera's position and viewport size as well as the scale. This method will only work if
     * the camera's direction vector is (0,0,-1) and its up vector is (0, 1, 0), which are the defaults.
     * @param camera the {@link Camera}
     */
    void setView(Camera camera);

    /**
     * Sets the projection matrix for rendering, as well as the bounds of the map which should be rendered. Make sure that the
     * frustum spanned by the projection matrix coincides with the viewbounds.
     */
    void setView(Matrix3 projectionMatrix, float viewboundsX, float viewboundsY, float viewboundsWidth,
                 float viewboundsHeight);

    /** Renders all the layers of a map. */
    void render();

    /**
     * Renders the given layers of a map.
     * @param layers the layers to render.
     */
    void render(int[] layers);
}

package arc.maps;

import arc.struct.Array;
import arc.util.ArcRuntimeException;

/** Map layer containing a set of objects and properties */
public class MapLayer{
    public String name = "";
    public float opacity = 1.0f;
    public boolean visible = true;

    private float offsetX;
    private float offsetY;
    private float renderOffsetX;
    private float renderOffsetY;
    private boolean renderOffsetDirty = true;
    private MapLayer parent;

    public final Array<MapObject> objects = new Array<>();
    public final MapProperties properties = new MapProperties();

    /** @return layer's x offset */
    public float getOffsetX(){
        return offsetX;
    }

    /** @param offsetX new x offset for the layer */
    public void setOffsetX(float offsetX){
        this.offsetX = offsetX;
        invalidateRenderOffset();
    }

    /** @return layer's y offset */
    public float getOffsetY(){
        return offsetY;
    }

    /** @param offsetY new y offset for the layer */
    public void setOffsetY(float offsetY){
        this.offsetY = offsetY;
        invalidateRenderOffset();
    }

    /** @return the layer's x render offset, this takes into consideration all parent layers' offsets **/
    public float getRenderOffsetX(){
        if(renderOffsetDirty) calculateRenderOffsets();
        return renderOffsetX;
    }

    /** @return the layer's y render offset, this takes into consideration all parent layers' offsets **/
    public float getRenderOffsetY(){
        if(renderOffsetDirty) calculateRenderOffsets();
        return renderOffsetY;
    }

    /** set the renderOffsetDirty state to true, when this layer or any parents' offset has changed **/
    public void invalidateRenderOffset(){
        renderOffsetDirty = true;
    }

    /** @return the layer's parent {@link MapLayer}, or null if the layer does not have a parent **/
    public MapLayer getParent(){
        return parent;
    }

    /** @param parent the layer's new parent MapLayer, internal use only **/
    public void setParent(MapLayer parent){
        if(parent == this) throw new ArcRuntimeException("Can't set self as the parent");
        this.parent = parent;
    }

    protected void calculateRenderOffsets(){
        if(parent != null){
            parent.calculateRenderOffsets();
            renderOffsetX = parent.getRenderOffsetX() + offsetX;
            renderOffsetY = parent.getRenderOffsetY() + offsetY;
        }else{
            renderOffsetX = offsetX;
            renderOffsetY = offsetY;
        }
        renderOffsetDirty = false;
    }
}

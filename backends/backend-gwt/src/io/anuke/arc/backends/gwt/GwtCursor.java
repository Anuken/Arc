package io.anuke.arc.backends.gwt;

import io.anuke.arc.graphics.Cursor;
import io.anuke.arc.graphics.Pixmap;
import io.anuke.arc.util.ArcRuntimeException;

public class GwtCursor implements Cursor{
    String cssCursorProperty;

    public GwtCursor(Pixmap pixmap, int xHotspot, int yHotspot){
        if(pixmap == null){
            this.cssCursorProperty = "auto";
            return;
        }

        if(pixmap.getFormat() != Pixmap.Format.RGBA8888){
            throw new ArcRuntimeException("Cursor image pixmap is not in RGBA8888 format.");
        }

        if((pixmap.getWidth() & (pixmap.getWidth() - 1)) != 0){
            throw new ArcRuntimeException("Cursor image pixmap width of " + pixmap.getWidth()
            + " is not a power-of-two greater than zero.");
        }

        if((pixmap.getHeight() & (pixmap.getHeight() - 1)) != 0){
            throw new ArcRuntimeException("Cursor image pixmap height of " + pixmap.getHeight()
            + " is not a power-of-two greater than zero.");
        }

        if(xHotspot < 0 || xHotspot >= pixmap.getWidth()){
            throw new ArcRuntimeException("xHotspot coordinate of " + xHotspot + " is not within image width bounds: [0, "
            + pixmap.getWidth() + ").");
        }

        if(yHotspot < 0 || yHotspot >= pixmap.getHeight()){
            throw new ArcRuntimeException("yHotspot coordinate of " + yHotspot + " is not within image height bounds: [0, "
            + pixmap.getHeight() + ").");
        }
        cssCursorProperty = "url('";
        cssCursorProperty += pixmap.getCanvasElement().toDataUrl("image/png");
        cssCursorProperty += "')";
        cssCursorProperty += xHotspot;
        cssCursorProperty += " ";
        cssCursorProperty += yHotspot;
        cssCursorProperty += ",auto";
    }

    static String getNameForSystemCursor(SystemCursor systemCursor){
        if(systemCursor == SystemCursor.Arrow){
            return "default";
        }else if(systemCursor == SystemCursor.Crosshair){
            return "crosshair";
        }else if(systemCursor == SystemCursor.Hand){
            return "pointer"; // Don't change to 'hand', 'hand' doesn't work in the newer IEs
        }else if(systemCursor == SystemCursor.HorizontalResize){
            return "ew-resize";
        }else if(systemCursor == SystemCursor.VerticalResize){
            return "ns-resize";
        }else if(systemCursor == SystemCursor.Ibeam){
            return "text";
        }else{
            throw new ArcRuntimeException("Unknown system cursor " + systemCursor);
        }
    }

    @Override
    public void dispose(){
    }
}

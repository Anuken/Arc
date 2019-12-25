package arc.graphics.g2d;

import arc.graphics.*;
import arc.util.ArcAnnotate.*;

/** Defines a region of a pixmap, like a TextureRegion. */
public class PixmapRegion{
    public @NonNull Pixmap pixmap;
    public int x, y, width, height;

    public PixmapRegion(Pixmap pixmap, int x, int y, int width, int height){
        set(pixmap, x, y, width, height);
    }

    public PixmapRegion(Pixmap pixmap){
        set(pixmap);
    }

    public int getPixel(int x, int y){
        return pixmap.getPixel(this.x + x, this.y +y);
    }

    public int getPixel(int x, int y, Color color){
        int c = getPixel(x, y);
        color.set(c);
        return c;
    }

    public PixmapRegion set(Pixmap pixmap){
        return set(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight());
    }

    public PixmapRegion set(Pixmap pixmap, int x, int y, int width, int height){
        this.pixmap = pixmap;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }

    public Pixmap crop(){
        return Pixmaps.crop(pixmap, x, y, width, height);
    }
}

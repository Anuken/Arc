package arc.graphics.g2d;

import arc.graphics.*;

/** Defines a region of a pixmap, like a TextureRegion. */
public class PixmapRegion{
    public Pixmap pixmap;
    public int x, y, width, height;

    public PixmapRegion(Pixmap pixmap, int x, int y, int width, int height){
        set(pixmap, x, y, width, height);
    }

    public PixmapRegion(Pixmap pixmap){
        set(pixmap);
    }

    public int getPixel(int x, int y){
        return pixmap.get(this.x + x, this.y +y);
    }

    public int getPixel(int x, int y, Color color){
        int c = getPixel(x, y);
        color.set(c);
        return c;
    }

    public PixmapRegion set(Pixmap pixmap){
        return set(pixmap, 0, 0, pixmap.width, pixmap.height);
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

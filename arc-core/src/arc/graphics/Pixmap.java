package arc.graphics;

import arc.*;
import arc.files.*;
import arc.func.*;
import arc.graphics.PixmapIO.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;

import java.io.*;
import java.nio.*;

/**
 * <p>
 * A Pixmap represents an image in memory. It has a width and height expressed in pixels as well as a {@link Format} specifying
 * the number and order of color components per pixel. Coordinates of pixels are specified with respect to the top left corner of
 * the image, with the x-axis pointing to the right and the y-axis pointing downwards.
 * </p>
 *
 * <p>
 * The {@link Pixmap#draw(Pixmap, int, int, int, int, int, int, int, int)} method will scale and stretch the source image to a
 * target image. There either nearest neighbour or bilinear filtering can be used.
 * </p>
 *
 * <p>
 * A Pixmap stores its data in native heap memory. It is mandatory to call {@link Pixmap#dispose()} when the pixmap is no longer
 * needed, otherwise memory leaks will result.
 * </p>
 * @author badlogicgames@gmail.com
 */
public class Pixmap implements Disposable{

    /** Size of the pixmap. Do not modify unless you know what you are doing. */
    public int width, height;

    /** Internal data, arranged as RGBA with 1 byte per component. This buffer must be direct or natively-allocated. */
    public ByteBuffer pixels;

    /**
     * When natives are present, this handle is the address of the memory region.
     * When this pixmap is disposed/uninitialized, this value is 0.
     * When natives are not present, this value is -1.
     */
    long handle;

    /** Creates a new Pixmap instance with the given width and height. */
    public Pixmap(int width, int height){
        load(width, height);
    }

    /** @see #Pixmap(byte[], int, int) */
    public Pixmap(byte[] encodedData){
        this(encodedData, 0, encodedData.length);
    }

    /**
     * Creates a new Pixmap instance from the given encoded image data. The image can be encoded as JPEG, PNG or BMP.
     * @param encodedData the image data to load, typically read from a PNG file
     */
    public Pixmap(byte[] encodedData, int offset, int len){
        load(encodedData, offset, len, null);
    }

    public Pixmap(String file){
        this(Core.files == null ? Fi.get(file) : Core.files.internal(file));
    }

    /**
     * Creates a new Pixmap instance from the given file. The file must be a Png, Jpeg or Bitmap. Paletted formats are not
     * supported.
     * @param file the {@link Fi}
     */
    public Pixmap(Fi file){
        byte[] bytes = file.readBytes();
        load(bytes, 0, bytes.length, file.toString());
    }

    /** Creates a pixmap from a direct ByteBuffer. */
    public Pixmap(ByteBuffer buffer, int width, int height){
        if(!buffer.isDirect()) throw new ArcRuntimeException("Pixmaps may only use direct/native ByteBuffers!");

        this.width = width;
        this.height = height;
        this.pixels = buffer;
        this.handle = -1;

        buffer.position(0).limit(buffer.capacity());
    }

    //region deprecated code kept for compatibility

    /** @deprecated the pixmap format parameter is redundant, don't use it. */
    @Deprecated
    public Pixmap(int width, int height, Format format){
        this(width, height);
    }

    /** @deprecated always returns rgba8888 */
    @Deprecated
    public Format getFormat(){
        return Format.rgba8888;
    }

    @Deprecated
    public enum Blending{
        none, sourceOver
    }

    /** @deprecated does nothing */
    @Deprecated
    public void setBlending(Blending blend){

    }

    /** @deprecated use get instead */
    @Deprecated
    public int getPixel(int x, int y){
        return get(x, y);
    }

    /** @deprecated use set instead */
    @Deprecated
    public void draw(int x, int y, int color){
        set(x, y, color);
    }

    /** @deprecated use set instead */
    @Deprecated
    public void draw(int x, int y, Color color){
        set(x, y, color);
    }

    //endregion

    /** @return a newly allocated copy with the same pixels. */
    public Pixmap copy(){
        Pixmap out = new Pixmap(width, height);
        pixels.position(0);
        out.pixels.position(0);
        out.pixels.put(pixels);
        out.pixels.position(0);
        return out;
    }

    /** Iterates through every position in this Pixmap. */
    public void each(Intc2 cons){
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                cons.get(x, y);
            }
        }
    }

    /** Fills the complete bitmap with the specified color. */
    public void fill(int color){
        int len = width * height * 4;
        for(int i = 0; i < len; i += 4){
            pixels.putInt(i, color);
        }
    }

    /** Fills the complete bitmap with the specified color. */
    public void fill(Color color){
        fill(color.rgba());
    }

    /** @return whether this point is in the pixmap. */
    public boolean in(int x, int y){
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public Pixmap crop(int x, int y, int width, int height){
        if(isDisposed()) throw new IllegalStateException("input is disposed.");
        Pixmap pixmap = new Pixmap(width, height);
        pixmap.draw(this, 0, 0, x, y, width, height);
        return pixmap;
    }

    /** @return a newly allocated pixmap, flipped vertically. */
    public Pixmap flipY(){
        Pixmap copy = new Pixmap(width, height);

        //TODO this can be optimized significantly by putting each line
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                copy.setRaw(x, height - 1 - y, getRaw(x, y));
            }
        }

        return copy;
    }

    /** @return a newly allocated pixmap, flipped horizontally. */
    public Pixmap flipX(){
        Pixmap copy = new Pixmap(width, height);

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                copy.set(width - 1 - x, y, getRaw(x, y));
            }
        }

        return copy;
    }

    /** @return a newly allocated pixmap with the specified outline. */
    public Pixmap outline(Color color, int radius){
        return outline(color.rgba(), radius);
    }

    /** @return a newly allocated pixmap with the specified outline. */
    public Pixmap outline(int color, int radius){
        Pixmap pixmap = copy();

        //TODO this messes with antialiasing?
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                if(getA(x, y) == 0){
                    boolean found = false;
                    outer:
                    for(int dx = -radius; dx <= radius; dx++){
                        for(int dy = -radius; dy <= radius; dy++){
                            if((dx*dx + dy*dy <= radius*radius) && !empty(get(x + dx, y + dy))){
                                found = true;
                                break outer;
                            }
                        }
                    }
                    if(found){
                        pixmap.setRaw(x, y, color);
                    }
                }
            }
        }
        return pixmap;
    }

    /** Draws a line between the given coordinates using the provided RGBA color. */
    public void drawLine(int x, int y, int x2, int y2, int color){
        int dy = y - y2;
        int dx = x - x2;
        int fraction, stepx, stepy;

        if(dy < 0){
            dy = -dy;
            stepy = -1;
        }else{
            stepy = 1;
        }
        if(dx < 0){
            dx = -dx;
            stepx = -1;
        }else{
            stepx = 1;
        }
        dy <<= 1;
        dx <<= 1;

        set(x, y, color);

        if(dx > dy){
            fraction = dy - (dx >> 1);
            while(x != x2){
                if(fraction >= 0){
                    y += stepy;
                    fraction -= dx;
                }
                x += stepx;
                fraction += dy;
                set(x, y, color);
            }
        }else{
            fraction = dx - (dy >> 1);
            while(y != y2){
                if(fraction >= 0){
                    x += stepx;
                    fraction -= dy;
                }
                y += stepy;
                fraction += dx;
                set(x, y, color);
            }
        }
    }

    /** Draws a rectangle outline starting at x, y extending by width to the right and by height downwards (y-axis points downwards) using the provided color. */
    public void drawRect(int x, int y, int width, int height, int color){
        hline(x, x + width - 1, y, color);
        hline(x, x + width - 1, y + height - 1, color);
        vline(y, y + height - 1, x, color);
        vline(y, y + height - 1, x + width - 1, color);
    }

    void circlePoints(int cx, int cy, int x, int y, int col){
        if(x == 0){
            set(cx, cy + y, col);
            set(cx, cy - y, col);
            set(cx + y, cy, col);
            set(cx - y, cy, col);
        }else if(x == y){
            set(cx + x, cy + y, col);
            set(cx - x, cy + y, col);
            set(cx + x, cy - y, col);
            set(cx - x, cy - y, col);
        }else if(x < y){
            set(cx + x, cy + y, col);
            set(cx - x, cy + y, col);
            set(cx + x, cy - y, col);
            set(cx - x, cy - y, col);
            set(cx + y, cy + x, col);
            set(cx - y, cy + x, col);
            set(cx + y, cy - x, col);
            set(cx - y, cy - x, col);
        }
    }

    public void drawCircle(int x, int y, int radius, int col){
        int px = 0;
        int py = radius;
        int p = (5 - radius * 4) / 4;

        circlePoints(x, y, px, py, col);
        while(px < py){
            px++;
            if(p < 0){
                p += 2 * px + 1;
            }else{
                py--;
                p += 2 * (px - py) + 1;
            }
            circlePoints(x, y, px, py, col);
        }
    }

    public void draw(PixmapRegion region){
        draw(region.pixmap, region.x, region.y, region.width, region.height, 0, 0, region.width, region.height);
    }

    public void draw(PixmapRegion region, boolean blend){
        draw(region.pixmap, region.x, region.y, region.width, region.height, 0, 0, region.width, region.height, false, blend);
    }

    public void draw(PixmapRegion region, int x, int y){
        draw(region.pixmap, region.x, region.y, region.width, region.height, x, y, region.width, region.height);
    }

    public void draw(PixmapRegion region, int x, int y, int width, int height){
        draw(region.pixmap, region.x, region.y, region.width, region.height, x, y, width, height);
    }

    public void draw(PixmapRegion region, int x, int y, int srcx, int srcy, int srcWidth, int srcHeight){
        draw(region.pixmap, x, y, region.x + srcx, region.y + srcy, srcWidth, srcHeight);
    }

    public void draw(PixmapRegion region, int srcx, int srcy, int srcWidth, int srcHeight, int dstx, int dsty, int dstWidth, int dstHeight){
        draw(region.pixmap, region.x + srcx, region.y + srcy, srcWidth, srcHeight, dstx, dsty, dstWidth, dstHeight);
    }

    public void draw(Pixmap pixmap, boolean blend){
        draw(pixmap, 0, 0, pixmap.width, pixmap.height, 0, 0, pixmap.width, pixmap.height, false, blend);
    }

    public void draw(Pixmap pixmap){
        draw(pixmap, 0, 0);
    }

    /**
     * Draws an area from another Pixmap to this Pixmap.
     * @param pixmap The other Pixmap
     * @param x The target x-coordinate (top left corner)
     * @param y The target y-coordinate (top left corner)
     */
    public void draw(Pixmap pixmap, int x, int y){
        draw(pixmap, x, y, 0, 0, pixmap.width, pixmap.height);
    }

    /**
     * Draws an area from another Pixmap to this Pixmap.
     * @param pixmap The other Pixmap
     * @param x The target x-coordinate (top left corner)
     * @param y The target y-coordinate (top left corner)
     */
    public void draw(Pixmap pixmap, int x, int y, boolean blending){
        draw(pixmap, 0, 0, pixmap.width, pixmap.height, x, y, pixmap.width, pixmap.height, false, blending);
    }


    /** Draws an area from another Pixmap to this Pixmap. */
    public void draw(Pixmap pixmap, int x, int y, int width, int height){
        draw(pixmap, x, y, width, height, false);
    }

    /** Draws an area from another Pixmap to this Pixmap. */
    public void draw(Pixmap pixmap, int x, int y, int width, int height, boolean filter){
        draw(pixmap, 0, 0, pixmap.width, pixmap.height, x, y, width, height, filter);
    }

    /** Draws an area from another Pixmap to this Pixmap. */
    public void draw(Pixmap pixmap, int x, int y, int srcx, int srcy, int srcWidth, int srcHeight){
        draw(pixmap, srcx, srcy, srcWidth, srcHeight, x, y, srcWidth, srcHeight);
    }

    /**
     * Draws an area from another Pixmap to this Pixmap. This will automatically scale and stretch the source image to the
     * specified target rectangle.
     */
    public void draw(Pixmap pixmap, int srcx, int srcy, int srcWidth, int srcHeight, int dstx, int dsty, int dstWidth, int dstHeight){
        draw(pixmap, srcx, srcy, srcWidth, srcHeight, dstx, dsty, dstWidth, dstHeight, false);
    }

    /**
     * Draws an area from another Pixmap to this Pixmap. This will automatically scale and stretch the source image to the
     * specified target rectangle.
     */
    public void draw(Pixmap pixmap, int srcx, int srcy, int srcWidth, int srcHeight, int dstx, int dsty, int dstWidth, int dstHeight, boolean filtering){
        draw(pixmap, srcx, srcy, srcWidth, srcHeight, dstx, dsty, dstWidth, dstHeight, filtering, false);
    }

    /**
     * Draws an area from another Pixmap to this Pixmap. This will automatically scale and stretch the source image to the
     * specified target rectangle. Blending is currently unsupported for stretched/scaled pixmaps.
     * @param pixmap The other Pixmap
     * @param srcx The source x-coordinate (top left corner)
     * @param srcy The source y-coordinate (top left corner);
     * @param srcWidth The width of the area from the other Pixmap in pixels
     * @param srcHeight The height of the area from the other Pixmap in pixels
     * @param dstx The target x-coordinate (top left corner)
     * @param dsty The target y-coordinate (top left corner)
     * @param dstWidth The target width
     * @param dstHeight the target height
     */
    public void draw(Pixmap pixmap, int srcx, int srcy, int srcWidth, int srcHeight, int dstx, int dsty, int dstWidth, int dstHeight, boolean filtering, boolean blending){
        int width = this.width, height = this.height, owidth = pixmap.width, oheight = pixmap.height;

        //don't bother drawing invalid regions
        if(srcWidth == 0 || srcHeight == 0 || dstWidth == 0 || dstHeight == 0){
            return;
        }

        if(srcWidth == dstWidth && srcHeight == dstHeight){

            //same-size blit, no filtering
            int sx, dx;
            int sy = srcy, dy = dsty;

            if(blending){
                for(; sy < srcy + srcHeight; sy++, dy++){
                    if(sy < 0 || dy < 0) continue;
                    if(sy >= oheight || dy >= height) break;

                    for(sx = srcx, dx = dstx; sx < srcx + srcWidth; sx++, dx++){
                        if(sx < 0 || dx < 0) continue;
                        if(sx >= owidth || dx >= width) break;
                        setRaw(dx, dy, blend(pixmap.getRaw(sx, sy), getRaw(dx, dy)));
                    }
                }
            }else{
                //TODO this can be optimized with scanlines, potentially
                for(; sy < srcy + srcHeight; sy++, dy++){
                    if(sy < 0 || dy < 0) continue;
                    if(sy >= oheight || dy >= height) break;

                    for(sx = srcx, dx = dstx; sx < srcx + srcWidth; sx++, dx++){
                        if(sx < 0 || dx < 0) continue;
                        if(sx >= owidth || dx >= width) break;
                        setRaw(dx, dy, pixmap.getRaw(sx, sy));
                    }
                }
            }
        }else{
            if(filtering){
                //blit with bilinear filtering
                float x_ratio = ((float)srcWidth - 1) / dstWidth;
                float y_ratio = ((float)srcHeight - 1) / dstHeight;
                int rX = Math.max(Mathf.round(x_ratio), 1), rY = Math.max(Mathf.round(y_ratio), 1);
                float xdiff, ydiff;
                int spitch = 4 * owidth;
                int dx, dy, sx, sy, i = 0, j;
                ByteBuffer spixels = pixmap.pixels;

                for(; i < dstHeight; i++){
                    sy = (int)(i * y_ratio) + srcy;
                    dy = i + dsty;
                    ydiff = (y_ratio * i + srcy) - sy;
                    if(sy < 0 || dy < 0) continue;
                    if(sy >= oheight || dy >= height) break;

                    for(j = 0; j < dstWidth; j++){
                        sx = (int)(j * x_ratio) + srcx;
                        dx = j + dstx;
                        xdiff = (x_ratio * j + srcx) - sx;
                        if(sx < 0 || dx < 0) continue;
                        if(sx >= owidth || dx >= width) break;

                        int
                        srcp = (sx + sy * owidth) * 4,
                        c1 = spixels.getInt(srcp),
                        c2 = sx + rX < srcWidth ? spixels.getInt(srcp + 4 *rX) : c1,
                        c3 = sy + rY < srcHeight ? spixels.getInt(srcp + spitch * rY) : c1,
                        c4 = sx + rX < srcWidth && sy + rY < srcHeight ? spixels.getInt(srcp + 4 * rX + spitch * rY) : c1;

                        float ta = (1 - xdiff) * (1 - ydiff);
                        float tb = (xdiff) * (1 - ydiff);
                        float tc = (1 - xdiff) * (ydiff);
                        float td = (xdiff) * (ydiff);

                        int r = (int)(((c1 & 0xff000000) >>> 24) * ta + ((c2 & 0xff000000) >>> 24) * tb + ((c3 & 0xff000000) >>> 24) * tc + ((c4 & 0xff000000) >>> 24) * td) & 0xff;
                        int g = (int)(((c1 & 0xff0000) >>> 16) * ta + ((c2 & 0xff0000) >>> 16) * tb + ((c3 & 0xff0000) >>> 16) * tc + ((c4 & 0xff0000) >>> 16) * td) & 0xff;
                        int b = (int)(((c1 & 0xff00) >>> 8) * ta + ((c2 & 0xff00) >>> 8) * tb + ((c3 & 0xff00) >>> 8) * tc + ((c4 & 0xff00) >>> 8) * td) & 0xff;
                        int a = (int)((c1 & 0xff) * ta + (c2 & 0xff) * tb + (c3 & 0xff) * tc + (c4 & 0xff) * td) & 0xff;
                        int srccol = (r << 24) | (g << 16) | (b << 8) | a;

                        setRaw(dx, dy, srccol);
                    }
                }
            }else{
                //blit with nearest neighbor filtering
                int xratio = (srcWidth << 16) / dstWidth + 1;
                int yratio = (srcHeight << 16) / dstHeight + 1;
                int dx, dy, sx, sy;

                for(int i = 0; i < dstHeight; i++){
                    sy = ((i * yratio) >> 16) + srcy;
                    dy = i + dsty;
                    if(sy < 0 || dy < 0) continue;
                    if(sy >= oheight || dy >= height) break;

                    for(int j = 0; j < dstWidth; j++){
                        sx = ((j * xratio) >> 16) + srcx;
                        dx = j + dstx;
                        if(sx < 0 || dx < 0) continue;
                        if(sx >= owidth || dx >= width) break;

                        setRaw(dx, dy, pixmap.getRaw(sx, sy));
                    }
                }
            }
        }
    }

    /**
     * Fills a rectangle starting at x, y extending by width to the right and by height downwards (y-axis points downwards) using
     * the current color.
     * @param x The x coordinate
     * @param y The y coordinate
     * @param width The width in pixels
     * @param height The height in pixels
     */
    public void fillRect(int x, int y, int width, int height, int color){
        int x2 = x + width - 1;
        int y2 = y + height - 1;

        if(x >= this.width) return;
        if(y >= this.height) return;
        if(x2 < 0) return;
        if(y2 < 0) return;
        if(x < 0) x = 0;
        if(y < 0) y = 0;
        if(x2 >= this.width) x2 = this.width - 1;
        if(y2 >= this.height) y2 = this.height - 1;

        y2++;
        while(y != y2){
            hline(x, x2, y, color);
            y++;
        }
    }

    /**
     * Fills a circle with the center at x,y and a radius using the current color.
     * @param radius The radius in pixels
     */
    public void fillCircle(int x, int y, int radius, int color){
        int f = 1 - radius;
        int ddF_x = 1;
        int ddF_y = -2 * radius;
        int px = 0;
        int py = radius;

        hline(x, x, y + radius, color);
        hline(x, x, y - radius, color);
        hline(x - radius, x + radius, y, color);

        while(px < py){
            if(f >= 0){
                py--;
                ddF_y += 2;
                f += ddF_y;
            }
            px++;
            ddF_x += 2;
            f += ddF_x;
            hline(x - px, x + px, y + py, color);
            hline(x - px, x + px, y - py, color);
            hline(x - py, x + py, y + px, color);
            hline(x - py, x + py, y - px, color);
        }
    }

    /** @return The pixel color in RGBA8888 format, or 0 if out of bounds. */
    public int get(int x, int y){
        return in(x, y) ? pixels.getInt((x + y * width) * 4) : 0;
    }

    /** @return The pixel color in RGBA8888 format. No bounds checks are done! */
    public int getRaw(int x, int y){
        return pixels.getInt((x + y * width) * 4);
    }

    /** @return The pixel alpha as a byte, 0-255. No bounds checks are done! */
    public int getA(int x, int y){
        return pixels.get((x + y * width) * 4 + 3) & 0xff;
    }

    /** @return whether the alpha at a position is 0. No bounds checks are done. */
    public boolean empty(int x, int y){
        return pixels.get((x + y * width) * 4 + 3) == 0;
    }

    /** @return The width of the Pixmap in pixels. */
    public int getWidth(){
        return width;
    }

    /** @return The height of the Pixmap in pixels. */
    public int getHeight(){
        return height;
    }

    /** Releases all resources associated with this Pixmap. */
    @Override
    public void dispose(){
        if(handle <= 0) return;
        free(handle);
        handle = 0;
    }

    @Override
    public boolean isDisposed(){
        return handle == 0;
    }

    public void set(int x, int y, Color color){
        set(x, y, color.rgba());
    }

    /**
     * Sets a pixel at the given location with the given color.
     * @param color the color in RGBA8888 format.
     */
    public void set(int x, int y, int color){
        if(in(x, y)){
            pixels.putInt((x + y * width) * 4, color);
        }
    }

    /**
     * Sets a pixel at the given location with the given color. No bounds checks are done!
     * @param color the color in RGBA8888 format.
     */
    public void setRaw(int x, int y, int color){
        pixels.putInt((x + y * width) * 4, color);
    }

    /**
     * Returns the OpenGL ES format of this Pixmap. Used as the seventh parameter to
     * {@link GL20#glTexImage2D(int, int, int, int, int, int, int, int, java.nio.Buffer)}.
     * @return one of GL_ALPHA, GL_RGB, GL_RGBA, GL_LUMINANCE, or GL_LUMINANCE_ALPHA.
     */
    public int getGLFormat(){
        return Gl.rgba;
    }

    /**
     * Returns the OpenGL ES format of this Pixmap. Used as the third parameter to
     * {@link GL20#glTexImage2D(int, int, int, int, int, int, int, int, java.nio.Buffer)}.
     * @return one of GL_ALPHA, GL_RGB, GL_RGBA, GL_LUMINANCE, or GL_LUMINANCE_ALPHA.
     */
    public int getGLInternalFormat(){
        return Gl.rgba;
    }

    /**
     * Returns the OpenGL ES type of this Pixmap. Used as the eighth parameter to
     * {@link GL20#glTexImage2D(int, int, int, int, int, int, int, int, java.nio.Buffer)}.
     * @return one of GL_UNSIGNED_BYTE, GL_UNSIGNED_SHORT_5_6_5, GL_UNSIGNED_SHORT_4_4_4_4
     */
    public int getGLType(){
        return Gl.unsignedByte;
    }

    /** @return the direct {@link ByteBuffer} holding the pixel data. */
    public ByteBuffer getPixels(){
        if(handle == 0) throw new ArcRuntimeException("Pixmap already disposed");
        return pixels;
    }

    void hline(int x1, int x2, int y, int color){
        if(y < 0 || y >= height) return;
        int tmp;

        if(x1 > x2){
            tmp = x1;
            x1 = x2;
            x2 = tmp;
        }

        if(x1 >= width) return;
        if(x2 < 0) return;

        if(x1 < 0) x1 = 0;
        if(x2 >= width) x2 = width - 1;
        x2++;

        while(x1 != x2){
            setRaw(x1++, y, color);
        }
    }

    void vline(int y1, int y2, int x, int color){
        if(x < 0 || x >= width) return;
        int tmp;

        if(y1 > y2){
            tmp = y1;
            y1 = y2;
            y2 = tmp;
        }

        if(y1 >= height) return;
        if(y2 < 0) return;

        if(y1 < 0) y1 = 0;
        if(y2 >= height) y2 = height - 1;
        y2++;

        while(y1 != y2){
            setRaw(x, y1++, color);
        }
    }

    /** @return the blended result of the input colors */
    public static int blend(int src, int dst){
        int src_a = src & 0xff;
        if(src_a == 0) return dst;

        int dst_a = dst & 0xff;
        if(dst_a == 0) return src;
        int dst_b = (dst >>> 8) & 0xff;
        int dst_g = (dst >>> 16) & 0xff;
        int dst_r = (dst >>> 24) & 0xff;

        dst_a -= (dst_a * src_a) / 255;
        int a = dst_a + src_a;
        dst_r = (dst_r * dst_a + ((src >>> 24) & 0xff) * src_a) / a;
        dst_g = (dst_g * dst_a + ((src >>> 16) & 0xff) * src_a) / a;
        dst_b = (dst_b * dst_a + ((src >>> 8) & 0xff) * src_a) / a;
        return ((dst_r << 24) | (dst_g << 16) | (dst_b << 8) | a);
    }

    public static boolean empty(int i){
        return (i & 0x000000ff) == 0;
    }

    private void load(byte[] encodedData, int offset, int len, String file){
        //use native implementation when possible
        if(ArcNativesLoader.loaded){
            try{
                //read with stb_image, which is slightly faster for large images and supports more formats
                long[] nativeData = new long[3];
                pixels = loadJni(nativeData, encodedData, offset, len);
                if(pixels == null) throw new ArcRuntimeException("Error loading pixmap from image data: " + getFailureReason() + (file == null ? "" : " (" + file + ")"));

                handle = nativeData[0];
                width = (int)nativeData[1];
                height = (int)nativeData[2];
                pixels.position(0).limit(pixels.capacity());
            }catch(ArcRuntimeException e){
                //stb_image bug? some PNGs fail with "corrupt JPEG" as the error, try the Java implementation if so
                if(e.getMessage() != null && e.getMessage().contains("Corrupt JPEG")){
                    try{
                        loadJava(encodedData, offset, len, file);
                        return;
                    }catch(Exception ignored){
                        //I did my best, fall through and throw the original exception
                    }
                }
                throw e;
            }
        }else{
            loadJava(encodedData, offset, len, file);
        }
    }

    private void loadJava(byte[] encodedData, int offset, int len, String file){
        //read with the pure java implementation
        try{
            PngReader reader = new PngReader();
            pixels = reader.read(new ByteArrayInputStream(encodedData, offset, len));
            width = reader.width;
            height = reader.height;
            handle = -1;
            pixels.position(0).limit(pixels.capacity());
        }catch(Exception e){
            throw new ArcRuntimeException("Failed to load PNG data" + (file == null ? "" : " (" + file + ")"), e);
        }
    }

    private void load(int width, int height){
        //use native implementation when possible
        if(ArcNativesLoader.loaded){
            long[] nativeData = new long[3];
            pixels = createJni(nativeData, width, height);
            if(pixels == null) throw new ArcRuntimeException("Error loading pixmap.");
            pixels.limit(pixels.capacity());

            this.handle = nativeData[0];
            this.width = (int)nativeData[1];
            this.height = (int)nativeData[2];
        }else{
            //use DirectByteBuffer instead
            this.handle = -1; //handle -1 means non-native buffer
            this.width = width;
            this.height = height;
            this.pixels = ByteBuffer.allocateDirect(width * height * 4);
        }
    }

    /**
     * Different pixel formats.
     * @author mzechner
     */
    public enum Format{
        alpha(Gl.unsignedByte, Gl.alpha),
        intensity(Gl.unsignedByte, Gl.alpha),
        luminanceAlpha(Gl.unsignedByte, Gl.luminanceAlpha),
        rgb565(Gl.unsignedShort565, Gl.rgb),
        rgba4444(Gl.unsignedShort4444, Gl.rgba),
        rgb888(Gl.unsignedByte, Gl.rgb),
        rgba8888(Gl.unsignedByte, Gl.rgba);

        public static final Format[] all = values();
        public final int glFormat, glType;

        Format(int glType, int glFormat){
            this.glFormat = glFormat;
            this.glType = glType;
        }
    }

    /*JNI

    #include <stdlib.h>
    #include <stdint.h>

    #include <stdlib.h>
    #define STB_IMAGE_IMPLEMENTATION
    #define STBI_FAILURE_USERMSG
    #define STBI_NO_STDIO
    #ifdef __APPLE__
    #define STBI_NO_THREAD_LOCALS
    #endif
    #include "stb_image.h"

    */

    /** Loads a pixmap from bytes and returns [address, width, height] in nativeData. */
    static native ByteBuffer loadJni(long[] nativeData, byte[] buffer, int offset, int len); /*MANUAL
        const unsigned char* p_buffer = (const unsigned char*)env->GetPrimitiveArrayCritical(buffer, 0);

        int32_t width, height, format;

        //always use STBI_rgb_alpha (4) as the format, since that's the only thing pixmaps support
        //RGB images are generally uncommon and the memory savings don't really matter; formats have to be converted to RGBA for drawing anyway
        const unsigned char* pixels = stbi_load_from_memory(p_buffer + offset, len, &width, &height, &format, STBI_rgb_alpha);

        if(pixels == NULL) return NULL;

        env->ReleasePrimitiveArrayCritical(buffer, (char*)p_buffer, 0);

        jobject pixel_buffer = env->NewDirectByteBuffer((void*)pixels, width * height * 4);
        jlong* p_native_data = (jlong*)env->GetPrimitiveArrayCritical(nativeData, 0);
        p_native_data[0] = (jlong)pixels;
        p_native_data[1] = width;
        p_native_data[2] = height;
        env->ReleasePrimitiveArrayCritical(nativeData, p_native_data, 0);

        return pixel_buffer;
     */

    /** Creates a new pixmap and returns [address, width, height] in nativeData. */
    static native ByteBuffer createJni(long[] nativeData, int width, int height); /*MANUAL
        const unsigned char* pixels = (unsigned char*)malloc(width * height * 4);

        if(!pixels) return 0;

        //fill pixel array with 0s
        //TODO use calloc insted?
        memset((void*)pixels, 0, width * height * 4);

        jobject pixel_buffer = env->NewDirectByteBuffer((void*)pixels, width * height * 4);
        jlong* p_native_data = (jlong*)env->GetPrimitiveArrayCritical(nativeData, 0);
        p_native_data[0] = (jlong)pixels;
        p_native_data[1] = width;
        p_native_data[2] = height;
        env->ReleasePrimitiveArrayCritical(nativeData, p_native_data, 0);

        return pixel_buffer;
     */

    static native void free(long buffer); /*
        free((void*)buffer);
     */

    static native String getFailureReason(); /*
        return env->NewStringUTF(stbi_failure_reason());
     */
}

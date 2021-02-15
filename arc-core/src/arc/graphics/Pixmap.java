package arc.graphics;

import arc.*;
import arc.files.*;
import arc.func.*;
import arc.graphics.g2d.*;
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
 * By default all methods use blending. You can disable blending with {@link Pixmap#setBlending(Blending)}. The
 * {@link Pixmap#drawPixmap(Pixmap, int, int, int, int, int, int, int, int)} method will scale and stretch the source image to a
 * target image. There either nearest neighbour or bilinear filtering can be used.
 * </p>
 *
 * <p>
 * A Pixmap stores its data in native heap memory. It is mandatory to call {@link Pixmap#dispose()} when the pixmap is no longer
 * needed, otherwise memory leaks will result
 * </p>
 * @author badlogicgames@gmail.com
 */
public class Pixmap implements Disposable{
    static final int pixmapFormatAlpha = 1, pixmapFormatLuminanceAlpha = 2, pixmapFormatRGB888 = 3, pixmapFormatRGBA8888 = 4, pixmapFormatRGB565 = 5, pixmapFormatRGBA4444 = 6;
    static final int pixmapScaleNearest = 0, pixmapScaleLinear = 1;
    
    final NativePixmap pixmap;
    int color = 0;
    private Blending blending = Blending.sourceOver;
    private PixmapFilter filter = PixmapFilter.bilinear;
    private boolean disposed;

    /** Uses RGBA8888.*/
    public Pixmap(int width, int height){
        this(width, height, Format.rgba8888);
    }

    /**
     * Creates a new Pixmap instance with the given width, height and format.
     * @param width the width in pixels
     * @param height the height in pixels
     * @param format the {@link Format}
     */
    public Pixmap(int width, int height, Format format){
        pixmap = new NativePixmap(width, height, format.toPixmapFormat());
        setColor(0, 0, 0, 0);
        fill();
    }

    /**
     * @see #Pixmap(byte[], int, int)
     */
    public Pixmap(byte[] encodedData){
        this(encodedData, 0, encodedData.length);
    }

    /**
     * Creates a new Pixmap instance from the given encoded image data. The image can be encoded as JPEG, PNG or BMP.
     * @param encodedData the encoded image data
     * @param offset the offset
     * @param len the length
     */
    public Pixmap(byte[] encodedData, int offset, int len){
        try{
            pixmap = new NativePixmap(encodedData, offset, len, 0);
        }catch(IOException e){
            throw new ArcRuntimeException("Couldn't load pixmap from image data", e);
        }
    }

    public Pixmap(String file){
        this(Core.files.internal(file));
    }

    /**
     * Creates a new Pixmap instance from the given file. The file must be a Png, Jpeg or Bitmap. Paletted formats are not
     * supported.
     * @param file the {@link Fi}
     */
    public Pixmap(Fi file){
        try{
            byte[] bytes = file.readBytes();
            pixmap = new NativePixmap(bytes, 0, bytes.length, 0);
        }catch(Exception e){
            throw new ArcRuntimeException("Couldn't load file: " + file, e);
        }
    }

    /**
     * Constructs a new Pixmap from a {@link NativePixmap}.
     */
    public Pixmap(NativePixmap pixmap){
        this.pixmap = pixmap;
    }

    public void each(Intc2 cons){
        for(int x = 0; x < getWidth(); x++){
            for(int y = 0; y < getHeight(); y++){
                cons.get(x, y);
            }
        }
    }

    /**
     * Sets the color for the following drawing operations
     * @param color the color, encoded as RGBA8888
     */
    public void setColor(int color){
        this.color = color;
    }

    /**
     * Sets the color for the following drawing operations.
     * @param r The red component.
     * @param g The green component.
     * @param b The blue component.
     * @param a The alpha component.
     */
    public void setColor(float r, float g, float b, float a){
        color = Color.rgba8888(r, g, b, a);
    }

    /**
     * Sets the color for the following drawing operations.
     * @param color The color.
     */
    public void setColor(Color color){
        this.color = Color.rgba8888(color.r, color.g, color.b, color.a);
    }

    /** Fills the complete bitmap with the currently set color. */
    public void fill(){
        clear(pixmap.basePtr, color);
    }

    /**
     * Draws a line between the given coordinates using the currently set color.
     * @param x The x-coodinate of the first point
     * @param y The y-coordinate of the first point
     * @param x2 The x-coordinate of the first point
     * @param y2 The y-coordinate of the first point
     */
    public void drawLine(int x, int y, int x2, int y2){
        drawLine(pixmap.basePtr, x, y, x2, y2, color);
    }

    /**
     * Draws a rectangle outline starting at x, y extending by width to the right and by height downwards (y-axis points downwards)
     * using the current color.
     * @param x The x coordinate
     * @param y The y coordinate
     * @param width The width in pixels
     * @param height The height in pixels
     */
    public void drawRectangle(int x, int y, int width, int height){
        drawRect(pixmap.basePtr, x, y, width, height, color);
    }

    public void draw(PixmapRegion region){
        drawPixmap(region.pixmap, region.x, region.y, region.width, region.height, 0, 0, region.width, region.height);
    }

    public void draw(PixmapRegion region, int x, int y){
        drawPixmap(region.pixmap, region.x, region.y, region.width, region.height, x, y, region.width, region.height);
    }

    public void draw(PixmapRegion region, int x, int y, int width, int height){
        drawPixmap(region.pixmap, region.x, region.y, region.width, region.height, x, y, width, height);
    }

    public void draw(PixmapRegion region, int x, int y, int srcx, int srcy, int srcWidth, int srcHeight){
        drawPixmap(region.pixmap, x, y, region.x + srcx, region.y + srcy, srcWidth, srcHeight);
    }

    public void draw(PixmapRegion region, int srcx, int srcy, int srcWidth, int srcHeight, int dstx, int dsty, int dstWidth, int dstHeight){
        drawPixmap(region.pixmap, region.x + srcx, region.y + srcy, srcWidth, srcHeight, dstx, dsty, dstWidth, dstHeight);
    }

    public void drawPixmap(Pixmap pixmap){
        drawPixmap(pixmap, 0, 0);
    }

    /**
     * Draws an area from another Pixmap to this Pixmap.
     * @param pixmap The other Pixmap
     * @param x The target x-coordinate (top left corner)
     * @param y The target y-coordinate (top left corner)
     */
    public void drawPixmap(Pixmap pixmap, int x, int y){
        drawPixmap(pixmap, x, y, 0, 0, pixmap.getWidth(), pixmap.getHeight());
    }

    /**
     * Draws an area from another Pixmap to this Pixmap.
     * @param pixmap The other Pixmap
     * @param x The target x-coordinate (top left corner)
     * @param y The target y-coordinate (top left corner)
     * @param srcx The source x-coordinate (top left corner)
     * @param srcy The source y-coordinate (top left corner);
     * @param srcWidth The width of the area from the other Pixmap in pixels
     * @param srcHeight The height of the area from the other Pixmap in pixels
     */
    public void drawPixmap(Pixmap pixmap, int x, int y, int srcx, int srcy, int srcWidth, int srcHeight){
        drawPixmap(pixmap.pixmap.basePtr, this.pixmap.basePtr, srcx, srcy, srcWidth, srcHeight, x, y, srcWidth, srcHeight);
    }

    /**
     * Draws an area from another Pixmap to this Pixmap. This will automatically scale and stretch the source image to the
     * specified target rectangle. Use {@link Pixmap#setFilter(PixmapFilter)} to specify the type of filtering to be used (nearest
     * neighbour or bilinear).
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
    public void drawPixmap(Pixmap pixmap, int srcx, int srcy, int srcWidth, int srcHeight, int dstx, int dsty, int dstWidth, int dstHeight){
        drawPixmap(pixmap.pixmap.basePtr, this.pixmap.basePtr, srcx, srcy, srcWidth, srcHeight, dstx, dsty, dstWidth, dstHeight);
    }

    /**
     * Fills a rectangle starting at x, y extending by width to the right and by height downwards (y-axis points downwards) using
     * the current color.
     * @param x The x coordinate
     * @param y The y coordinate
     * @param width The width in pixels
     * @param height The height in pixels
     */
    public void fillRectangle(int x, int y, int width, int height){
        fillRect(pixmap.basePtr, x, y, width, height, color);
    }

    /**
     * Draws a circle outline with the center at x,y and a radius using the current color and stroke width.
     * @param x The x-coordinate of the center
     * @param y The y-coordinate of the center
     * @param radius The radius in pixels
     */
    public void drawCircle(int x, int y, int radius){
        drawCircle(pixmap.basePtr, x, y, radius, color);
    }

    /**
     * Fills a circle with the center at x,y and a radius using the current color.
     * @param x The x-coordinate of the center
     * @param y The y-coordinate of the center
     * @param radius The radius in pixels
     */
    public void fillCircle(int x, int y, int radius){
        fillCircle(pixmap.basePtr, x, y, radius, color);
    }

    /**
     * Fills a triangle with vertices at x1,y1 and x2,y2 and x3,y3 using the current color.
     * @param x1 The x-coordinate of vertex 1
     * @param y1 The y-coordinate of vertex 1
     * @param x2 The x-coordinate of vertex 2
     * @param y2 The y-coordinate of vertex 2
     * @param x3 The x-coordinate of vertex 3
     * @param y3 The y-coordinate of vertex 3
     */
    public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3){
        fillTriangle(pixmap.basePtr, x1, y1, x2, y2, x3, y3, color);
    }

    /**
     * Returns the 32-bit RGBA8888 value of the pixel at x, y. For Alpha formats the RGB components will be one.
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @return The pixel color in RGBA8888 format.
     */
    public int getPixel(int x, int y){
        return getPixel(pixmap.basePtr, x, y);
    }

    /** @return The width of the Pixmap in pixels. */
    public int getWidth(){
        return pixmap.width;
    }

    /** @return The height of the Pixmap in pixels. */
    public int getHeight(){
        return pixmap.height;
    }

    /** Releases all resources associated with this Pixmap. */
    public void dispose(){
        if(disposed) throw new ArcRuntimeException("Pixmap already disposed!");
        free(pixmap.basePtr);
        disposed = true;
    }

    @Override
    public boolean isDisposed(){
        return disposed;
    }

    public void draw(int x, int y, Color color){
        draw(x, y, color.rgba());
    }

    /**
     * Draws a pixel at the given location with the current color.
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public void draw(int x, int y){
        setPixel(pixmap.basePtr, x, y, color);
    }

    /**
     * Draws a pixel at the given location with the given color.
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param color the color in RGBA8888 format.
     */
    public void draw(int x, int y, int color){
        setPixel(pixmap.basePtr, x, y, color);
    }

    /**
     * Returns the OpenGL ES format of this Pixmap. Used as the seventh parameter to
     * {@link GL20#glTexImage2D(int, int, int, int, int, int, int, int, java.nio.Buffer)}.
     * @return one of GL_ALPHA, GL_RGB, GL_RGBA, GL_LUMINANCE, or GL_LUMINANCE_ALPHA.
     */
    public int getGLFormat(){
        return toGlFormat(pixmap.format);
    }

    /**
     * Returns the OpenGL ES format of this Pixmap. Used as the third parameter to
     * {@link GL20#glTexImage2D(int, int, int, int, int, int, int, int, java.nio.Buffer)}.
     * @return one of GL_ALPHA, GL_RGB, GL_RGBA, GL_LUMINANCE, or GL_LUMINANCE_ALPHA.
     */
    public int getGLInternalFormat(){
        return toGlFormat(pixmap.format);
    }

    /**
     * Returns the OpenGL ES type of this Pixmap. Used as the eighth parameter to
     * {@link GL20#glTexImage2D(int, int, int, int, int, int, int, int, java.nio.Buffer)}.
     * @return one of GL_UNSIGNED_BYTE, GL_UNSIGNED_SHORT_5_6_5, GL_UNSIGNED_SHORT_4_4_4_4
     */
    public int getGLType(){
        return toGlType(pixmap.format);
    }

    /**
     * Returns the direct ByteBuffer holding the pixel data. For the format Alpha each value is encoded as a byte. For the format
     * LuminanceAlpha the luminance is the first byte and the alpha is the second byte of the pixel. For the formats RGB888 and
     * RGBA8888 the color components are stored in a single byte each in the order red, green, blue (alpha). For the formats RGB565
     * and RGBA4444 the pixel colors are stored in shorts in machine dependent order.
     * @return the direct {@link ByteBuffer} holding the pixel data.
     */
    public ByteBuffer getPixels(){
        if(disposed) throw new ArcRuntimeException("Pixmap already disposed");
        return pixmap.pixelPtr;
    }

    /** @return the {@link Format} of this Pixmap. */
    public Format getFormat(){
        return Format.fromPixmapFormat(pixmap.format);
    }

    /** @return the currently set {@link Blending} */
    public Blending getBlending(){
        return blending;
    }

    /**
     * Sets the type of {@link Blending} to be used for all operations. Default is {@link Blending#sourceOver}.
     * @param blending the blending type
     */
    public void setBlending(Blending blending){
        this.blending = blending;
        int blend = blending == Blending.none ? 0 : 1;
        setBlend(pixmap.basePtr, blend);
    }

    /** @return the currently set {@link PixmapFilter} */
    public PixmapFilter getFilter(){
        return filter;
    }

    /**
     * Sets the type of interpolation {@link PixmapFilter} to be used in conjunction with
     * {@link Pixmap#drawPixmap(Pixmap, int, int, int, int, int, int, int, int)}.
     * @param filter the filter.
     */
    public void setFilter(PixmapFilter filter){
        this.filter = filter;
        int scale = filter == PixmapFilter.nearestNeighbour ? pixmapScaleNearest : pixmapScaleLinear;
        setScale(pixmap.basePtr, scale);
    }

    /**
     * Different pixel formats.
     * @author mzechner
     */
    public enum Format{
        alpha, intensity, luminanceAlpha, rgb565, rgba4444, rgb888, rgba8888;

        public int toPixmapFormat(){
            switch(this){
                case alpha:
                case intensity: return pixmapFormatAlpha;
                case luminanceAlpha: return pixmapFormatLuminanceAlpha;
                case rgb565: return pixmapFormatRGB565;
                case rgba4444: return pixmapFormatRGBA4444;
                case rgb888: return pixmapFormatRGB888;
                case rgba8888: return pixmapFormatRGBA8888;
                default: throw new ArcRuntimeException("Unknown Format: " + this);
            }
        }

        public static Format fromPixmapFormat(int format){
            switch(format){
                case pixmapFormatAlpha: return alpha;
                case pixmapFormatLuminanceAlpha: return luminanceAlpha;
                case pixmapFormatRGB565: return rgb565;
                case pixmapFormatRGBA4444: return rgba4444;
                case pixmapFormatRGB888: return rgb888;
                case pixmapFormatRGBA8888: return rgba8888;
                default: throw new ArcRuntimeException("Unknown Pixmap Format: " + format);
            }
        }

        public int toGlFormat(){
            return Pixmap.toGlFormat(toPixmapFormat());
        }

        public int toGlType(){
            return Pixmap.toGlType(toPixmapFormat());
        }
    }

    public static int toGlFormat(int format){
        switch(format){
            case pixmapFormatAlpha: return GL20.GL_ALPHA;
            case pixmapFormatLuminanceAlpha: return GL20.GL_LUMINANCE_ALPHA;
            case pixmapFormatRGB888:
            case pixmapFormatRGB565: return GL20.GL_RGB;
            case pixmapFormatRGBA8888:
            case pixmapFormatRGBA4444: return GL20.GL_RGBA;
            default: throw new ArcRuntimeException("unknown format: " + format);
        }
    }

    public static int toGlType(int format){
        switch(format){
            case pixmapFormatAlpha:
            case pixmapFormatLuminanceAlpha:
            case pixmapFormatRGB888:
            case pixmapFormatRGBA8888: return GL20.GL_UNSIGNED_BYTE;
            case pixmapFormatRGB565: return GL20.GL_UNSIGNED_SHORT_5_6_5;
            case pixmapFormatRGBA4444: return GL20.GL_UNSIGNED_SHORT_4_4_4_4;
            default: throw new ArcRuntimeException("unknown format: " + format);
        }
    }

    /**
     * Blending functions to be set with {@link Pixmap#setBlending}.
     * @author mzechner
     */
    public enum Blending{
        none, sourceOver
    }

    /**
     * Filters to be used with {@link Pixmap#drawPixmap(Pixmap, int, int, int, int, int, int, int, int)}.
     * @author mzechner
     */
    public enum PixmapFilter{
        nearestNeighbour, bilinear
    }

    /*JNI
    #include <pix.h>
    #include <stdlib.h>
     */

    static native ByteBuffer load(long[] nativeData, byte[] buffer, int offset, int len); /*MANUAL
        const unsigned char* p_buffer = (const unsigned char*)env->GetPrimitiveArrayCritical(buffer, 0);
        pix_handle* pixmap = pix_load(p_buffer + offset, len);
        env->ReleasePrimitiveArrayCritical(buffer, (char*)p_buffer, 0);

        if(pixmap==0)
            return 0;

        jobject pixel_buffer = env->NewDirectByteBuffer((void*)pixmap->pixels, pixmap->width * pixmap->height * pix_bytes_per_pixel(pixmap->format));
        jlong* p_native_data = (jlong*)env->GetPrimitiveArrayCritical(nativeData, 0);
        p_native_data[0] = (jlong)pixmap;
        p_native_data[1] = pixmap->width;
        p_native_data[2] = pixmap->height;
        p_native_data[3] = pixmap->format;
        env->ReleasePrimitiveArrayCritical(nativeData, p_native_data, 0);

        return pixel_buffer;
     */

    static native ByteBuffer newPixmap(long[] nativeData, int width, int height, int format); /*MANUAL
        pix_handle* pixmap = pix_new(width, height, format);
        if(pixmap==0)
            return 0;

        jobject pixel_buffer = env->NewDirectByteBuffer((void*)pixmap->pixels, pixmap->width * pixmap->height * pix_bytes_per_pixel(pixmap->format));
        jlong* p_native_data = (jlong*)env->GetPrimitiveArrayCritical(nativeData, 0);
        p_native_data[0] = (jlong)pixmap;
        p_native_data[1] = pixmap->width;
        p_native_data[2] = pixmap->height;
        p_native_data[3] = pixmap->format;
        env->ReleasePrimitiveArrayCritical(nativeData, p_native_data, 0);

        return pixel_buffer;
     */

    static native void free(long pixmap); /*
        pix_free((pix_handle*)pixmap);
     */

    private static native void clear(long pixmap, int color); /*
        pix_clear((pix_handle*)pixmap, color);
     */

    private static native void setPixel(long pixmap, int x, int y, int color); /*
        pix_set_pixel((pix_handle*)pixmap, x, y, color);
     */

    private static native int getPixel(long pixmap, int x, int y); /*
        return pix_get_pixel((pix_handle*)pixmap, x, y);
     */

    private static native void drawLine(long pixmap, int x, int y, int x2, int y2, int color); /*
        pix_draw_line((pix_handle*)pixmap, x, y, x2, y2, color);
     */

    private static native void drawRect(long pixmap, int x, int y, int width, int height, int color); /*
        pix_draw_rect((pix_handle*)pixmap, x, y, width, height, color);
     */

    private static native void drawCircle(long pixmap, int x, int y, int radius, int color); /*
        pix_draw_circle((pix_handle*)pixmap, x, y, radius, color);
     */

    private static native void fillRect(long pixmap, int x, int y, int width, int height, int color); /*
        pix_fill_rect((pix_handle*)pixmap, x, y, width, height, color);
     */

    private static native void fillCircle(long pixmap, int x, int y, int radius, int color); /*
        pix_fill_circle((pix_handle*)pixmap, x, y, radius, color);
     */

    private static native void fillTriangle(long pixmap, int x1, int y1, int x2, int y2, int x3, int y3, int color); /*
        pix_fill_triangle((pix_handle*)pixmap, x1, y1, x2, y2, x3, y3, color);
     */

    static native void drawPixmap(long src, long dst, int srcX, int srcY, int srcWidth, int srcHeight, int dstX,
                                  int dstY, int dstWidth, int dstHeight); /*
        pix_draw_pixmap((pix_handle*)src, (pix_handle*)dst, srcX, srcY, srcWidth, srcHeight, dstX, dstY, dstWidth, dstHeight);
     */

    private static native void setBlend(long src, int blend); /*
        pix_set_blend((pix_handle*)src, blend);
     */

    private static native void setScale(long src, int scale); /*
        pix_set_scale((pix_handle*)src, scale);
     */

    public static native String getFailureReason(); /*
        return env->NewStringUTF(pix_get_failure_reason());
     */

    private static class NativePixmap{
        long basePtr;
        int width;
        int height;
        int format;
        ByteBuffer pixelPtr;
        long[] nativeData = new long[4];

        public NativePixmap(byte[] encodedData, int offset, int len, int requestedFormat) throws IOException{
            pixelPtr = load(nativeData, encodedData, offset, len);
            if(pixelPtr == null) throw new IOException("Error loading pixmap: " + getFailureReason());

            basePtr = nativeData[0];
            width = (int)nativeData[1];
            height = (int)nativeData[2];
            format = (int)nativeData[3];

            if(requestedFormat != 0 && requestedFormat != format){
                convert(requestedFormat);
            }
        }

        /** @throws ArcRuntimeException if allocation failed. */
        public NativePixmap(int width, int height, int format) throws ArcRuntimeException{
            pixelPtr = newPixmap(nativeData, width, height, format);
            if(pixelPtr == null) throw new ArcRuntimeException("Error loading pixmap.");

            this.basePtr = nativeData[0];
            this.width = (int)nativeData[1];
            this.height = (int)nativeData[2];
            this.format = (int)nativeData[3];
        }

        private void convert(int requestedFormat){
            NativePixmap pixmap = new NativePixmap(width, height, requestedFormat);
            drawPixmap(basePtr, pixmap.basePtr, 0, 0, width, height, 0, 0, width, height);
            free(basePtr);
            this.basePtr = pixmap.basePtr;
            this.format = pixmap.format;
            this.height = pixmap.height;
            this.nativeData = pixmap.nativeData;
            this.pixelPtr = pixmap.pixelPtr;
            this.width = pixmap.width;
        }

    }
}

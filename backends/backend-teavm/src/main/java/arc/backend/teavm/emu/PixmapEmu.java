package arc.backend.teavm.emu;

import arc.backend.teavm.*;
import arc.backend.teavm.plugin.Annotations.*;
import arc.struct.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.Pixmap.Blending;
import arc.graphics.Pixmap.*;
import arc.util.*;
import org.teavm.backend.javascript.spi.*;
import org.teavm.jso.browser.*;
import org.teavm.jso.canvas.*;
import org.teavm.jso.dom.html.*;
import org.teavm.jso.typedarrays.*;

import java.nio.*;


@Replace(Pixmap.class)
public class PixmapEmu implements Disposable{
    private static final Window window = Window.current();
    private static final HTMLDocument document = window.getDocument();
    private static int nextId = 0;
    private static String clearColor = make(255, 255, 255, 1.0f);
    public static IntMap<PixmapEmu> pixmaps = new IntMap<>();

    int width;
    int height;
    Format format;
    HTMLCanvasElement canvas;
    CanvasRenderingContext2D context;
    int id;
    int r = 255, g = 255, b = 255;
    float a;
    String color = make(r, g, b, a);
    private Blending blending;
    Uint8ClampedArray pixels;

    public PixmapEmu(Fi file){
        TeaFi teavmFile = (TeaFi)file;
        TeaFi.FSEntry entry = teavmFile.entry();
        HTMLImageElement img = entry.imageElem;
        if(img == null){
            throw new ArcRuntimeException("Couldn't load image '" + file.path() + "', file does not exist");
        }
        create(img.getWidth(), img.getHeight(), Format.RGBA8888);
        context.setGlobalCompositeOperation("copy");
        context.drawImage(img, 0, 0);
        context.setGlobalCompositeOperation("source-over");
    }

    public PixmapEmu(HTMLImageElement img){
        create(img.getWidth(), img.getHeight(), Format.RGBA8888);
        context.drawImage(img, 0, 0);
    }

    public PixmapEmu(int width, int height, Pixmap.Format format){
        create(width, height, format);
    }

    private void create(int width, int height, @SuppressWarnings("unused") Pixmap.Format format2){
        this.width = width;
        this.height = height;
        this.format = Format.RGBA8888;
        canvas = (HTMLCanvasElement)document.createElement("canvas");
        canvas.getStyle().setProperty("display", "none");
        document.getBody().appendChild(canvas);
        canvas.setWidth(width);
        canvas.setHeight(height);
        context = (CanvasRenderingContext2D)canvas.getContext("2d");
        context.setGlobalCompositeOperation("source-over");
        id = nextId++;
        pixmaps.put(id, this);
    }

    public static String make(int r2, int g2, int b2, float a2){
        return "rgba(" + r2 + "," + g2 + "," + b2 + "," + a2 + ")";
    }

    public void setBlending(Blending blending){
        this.blending = blending;
        for(PixmapEmu pixmap : pixmaps.values()){
            pixmap.context.setGlobalCompositeOperation("source-over");
        }
    }

    public Blending getBlending(){
        return blending;
    }

    public void setFilter(@SuppressWarnings("unused") Filter filter){
    }

    public Format getFormat(){
        return format;
    }

    public int getGLInternalFormat(){
        return GL20.GL_RGBA;
    }

    public int getGLFormat(){
        return GL20.GL_RGBA;
    }

    public int getGLType(){
        return GL20.GL_UNSIGNED_BYTE;
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    @Override
    public void dispose(){
        PixmapEmu pixmap = pixmaps.remove(id);
        if(pixmap.canvas != null){
            pixmap.canvas.getParentNode().removeChild(pixmap.canvas);
        }
    }

    public void setColor(int color){
        r = (color >>> 24) & 0xff;
        g = (color >>> 16) & 0xff;
        b = (color >>> 8) & 0xff;
        a = (color & 0xff) / 255f;
        this.color = make(r, g, b, a);
        context.setFillStyle(this.color);
        context.setStrokeStyle(this.color);
    }

    public void setColor(float r, float g, float b, float a){
        this.r = (int)(r * 255);
        this.g = (int)(g * 255);
        this.b = (int)(b * 255);
        this.a = a;
        color = make(this.r, this.g, this.b, this.a);
        context.setFillStyle(color);
        context.setStrokeStyle(this.color);
    }

    public void setColor(Color color){
        setColor(color.r, color.g, color.b, color.a);
    }

    public void fill(){
        rectangle(0, 0, getWidth(), getHeight(), DrawType.FILL);
    }

    public void drawLine(int x, int y, int x2, int y2){
        line(x, y, x2, y2, DrawType.STROKE);
    }

    public void drawRectangle(int x, int y, int width, int height){
        rectangle(x, y, width, height, DrawType.STROKE);
    }

    public void drawPixmap(PixmapEmu pixmap, int x, int y){
        HTMLCanvasElement image = pixmap.canvas;
        image(image, 0, 0, image.getWidth(), image.getHeight(), x, y, image.getWidth(), image.getHeight());
    }

    public void drawPixmap(PixmapEmu pixmap, int x, int y, int srcx, int srcy, int srcWidth, int srcHeight){
        HTMLCanvasElement image = pixmap.canvas;
        image(image, srcx, srcy, srcWidth, srcHeight, x, y, srcWidth, srcHeight);
    }

    public void drawPixmap(PixmapEmu pixmap, int srcx, int srcy, int srcWidth, int srcHeight, int dstx, int dsty,
                           int dstWidth, int dstHeight){
        image(pixmap.canvas, srcx, srcy, srcWidth, srcHeight, dstx, dsty, dstWidth, dstHeight);
    }

    public void fillRectangle(int x, int y, int width, int height){
        rectangle(x, y, width, height, DrawType.FILL);
    }

    public void drawCircle(int x, int y, int radius){
        circle(x, y, radius, DrawType.STROKE);
    }

    public void fillCircle(int x, int y, int radius){
        circle(x, y, radius, DrawType.FILL);
    }

    public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3){
        triangle(x1, y1, x2, y2, x3, y3, DrawType.FILL);
    }

    public int getPixel(int x, int y){
        if(pixels == null){
            pixels = context.getImageData(0, 0, width, height).getData();
        }
        int i = x * 4 + y * width * 4;
        int r = pixels.get(i + 0) & 0xff;
        int g = pixels.get(i + 1) & 0xff;
        int b = pixels.get(i + 2) & 0xff;
        int a = pixels.get(i + 3) & 0xff;
        return (r << 24) | (g << 16) | (b << 8) | (a);
    }

    public ByteBuffer getPixels(){
        if(pixels == null){
            pixels = context.getImageData(0, 0, width, height).getData();
        }
        return ByteBuffer.wrap(bufferAsArray(pixels.getBuffer()));
    }

    @GeneratedBy(PixmapNativeGenerator.class)
    private native byte[] bufferAsArray(ArrayBuffer array);

    public void drawPixel(int x, int y){
        rectangle(x, y, 1, 1, DrawType.FILL);
    }

    public void drawPixel(int x, int y, int color){
        setColor(color);
        drawPixel(x, y);
    }

    private void circle(int x, int y, int radius, DrawType drawType){
        if(blending == Blending.None){
            context.setFillStyle(clearColor);
            context.setStrokeStyle(clearColor);
            context.setGlobalCompositeOperation("clear");
            context.beginPath();
            context.arc(x, y, radius, 0, 2 * Math.PI, false);
            fillOrStrokePath(drawType);
            context.closePath();
            context.setFillStyle(color);
            context.setStrokeStyle(color);
            context.setGlobalCompositeOperation("source-over");
        }
        context.beginPath();
        context.arc(x, y, radius, 0, 2 * Math.PI, false);
        fillOrStrokePath(drawType);
        context.closePath();
        pixels = null;
    }

    private void line(int x, int y, int x2, int y2, DrawType drawType){
        if(blending == Blending.None){
            context.setFillStyle(clearColor);
            context.setStrokeStyle(clearColor);
            context.setGlobalCompositeOperation("clear");
            context.beginPath();
            context.moveTo(x, y);
            context.lineTo(x2, y2);
            fillOrStrokePath(drawType);
            context.closePath();
            context.setFillStyle(color);
            context.setStrokeStyle(color);
            context.setGlobalCompositeOperation("source-over");
        }
        context.beginPath();
        context.moveTo(x, y);
        context.lineTo(x2, y2);
        fillOrStrokePath(drawType);
        context.closePath();
    }

    private void rectangle(int x, int y, int width, int height, DrawType drawType){
        if(blending == Blending.None){
            context.setFillStyle(clearColor);
            context.setStrokeStyle(clearColor);
            context.setGlobalCompositeOperation("clear");
            context.beginPath();
            context.rect(x, y, width, height);
            fillOrStrokePath(drawType);
            context.closePath();
            context.setFillStyle(color);
            context.setStrokeStyle(color);
            context.setGlobalCompositeOperation("source-over");
        }
        context.beginPath();
        context.rect(x, y, width, height);
        fillOrStrokePath(drawType);
        context.closePath();
        pixels = null;
    }

    private void triangle(int x1, int y1, int x2, int y2, int x3, int y3, DrawType drawType){
        if(blending == Blending.None){
            context.setFillStyle(clearColor);
            context.setStrokeStyle(clearColor);
            context.setGlobalCompositeOperation("clear");
            context.beginPath();
            context.moveTo(x1, y1);
            context.lineTo(x2, y2);
            context.lineTo(x3, y3);
            context.lineTo(x1, y1);
            fillOrStrokePath(drawType);
            context.closePath();
            context.setFillStyle(color);
            context.setStrokeStyle(color);
            context.setGlobalCompositeOperation("source-over");
        }
        context.beginPath();
        context.moveTo(x1, y1);
        context.lineTo(x2, y2);
        context.lineTo(x3, y3);
        context.lineTo(x1, y1);
        fillOrStrokePath(drawType);
        context.closePath();
        pixels = null;
    }

    private void image(HTMLCanvasElement image, int srcX, int srcY, int srcWidth, int srcHeight, int dstX, int dstY,
                       int dstWidth, int dstHeight){
        if(blending == Blending.None){
            context.setFillStyle(clearColor);
            context.setStrokeStyle(clearColor);
            context.setGlobalCompositeOperation("clear");
            context.beginPath();
            context.rect(dstX, dstY, dstWidth, dstHeight);
            fillOrStrokePath(DrawType.FILL);
            context.closePath();
            context.setFillStyle(color);
            context.setStrokeStyle(color);
            context.setGlobalCompositeOperation("source-over");
        }
        context.drawImage(image, srcX, srcY, srcWidth, srcHeight, dstX, dstY, dstWidth, dstHeight);
        pixels = null;
    }

    private void fillOrStrokePath(DrawType drawType){
        switch(drawType){
            case FILL:
                context.fill();
                break;
            case STROKE:
                context.stroke();
                break;
        }
    }

    private enum DrawType{
        FILL, STROKE
    }
}

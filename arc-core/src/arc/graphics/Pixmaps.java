package arc.graphics;

import arc.func.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;

import java.nio.*;

/** Various pixmap utilities. */
public class Pixmaps{
    private static Pixmap drawPixmap;
    private static IntSeq tmpArray = new IntSeq();

    public static Pixmap noise(int w, int h){
        Pixmap out = new Pixmap(w, h);
        for(int x = 0; x < w; x++){
            for(int y = 0; y < h; y++){
                out.draw(x, y, Tmp.c1.rand());
            }
        }
        return out;
    }

    public static Texture noiseTex(int w, int h){
        Pixmap p = noise(w, h);
        Texture tex = new Texture(p);
        tex.setWrap(TextureWrap.repeat);
        p.dispose();
        return tex;
    }

    public static void flip(Pixmap pixmap){
        ByteBuffer pixels = pixmap.getPixels();
        int numBytes = pixmap.width * pixmap.height * 4;
        byte[] lines = new byte[numBytes];
        int numBytesPerLine = pixmap.width * 4;
        for(int i = 0; i < pixmap.height; i++){
            pixels.position((pixmap.height - i - 1) * numBytesPerLine);
            pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
        }
        pixels.clear();
        pixels.put(lines);
    }

    public static Pixmap median(Pixmap input, int radius, double percentile){
        return median(input, radius, percentile, tmpArray);
    }

    public static Pixmap median(Pixmap input, int radius, double percentile, IntSeq tmp){
        Pixmap pixmap = new Pixmap(input.width, input.height);
        input.each((x, y) -> {
            tmp.clear();
            Geometry.circle(x, y, pixmap.width, pixmap.height, radius, (cx, cy) -> tmp.add(input.getPixel(cx, cy)));
            tmp.sort();
            pixmap.draw(x, y, tmp.get(Mathf.clamp((int)(tmp.size * percentile), 0, tmp.size - 1)));
        });
        return pixmap;
    }

    public static Pixmap scale(Pixmap pixmap, int width, int height, boolean filter){
        Pixmap dest = new Pixmap(width, height);
        dest.drawPixmap(pixmap, 0, 0, pixmap.width, pixmap.height, 0, 0, width, height, filter);
        return dest;
    }

    public static Pixmap scale(Pixmap input, float scale){
        return scale(input, scale, scale);
    }

    public static Pixmap scale(Pixmap input, float scalex, float scaley){
        Pixmap pixmap = new Pixmap((int)(input.width * scalex), (int)(input.height * scaley));
        for(int x = 0; x < pixmap.width; x++){
            for(int y = 0; y < pixmap.height; y++){
                pixmap.draw(x, y, input.getPixel((int)(x / scalex), (int)(y / scaley)));
            }
        }
        return pixmap;
    }

    public static Pixmap outline(Pixmap input, Color color, int thickness){
        if(thickness == 1){
            return outline(input, color);
        }else{
            Pixmap pixmap = input.copy();
            int col = color.rgba();

            for(int x = 0; x < pixmap.width; x++){
                for(int y = 0; y < pixmap.height; y++){
                    if(empty(input.getPixel(x, y))){
                        boolean found = false;
                        outer:
                        for(int dx = -thickness; dx <= thickness; dx++){
                            for(int dy = -thickness; dy <= thickness; dy++){
                                if(Mathf.within(dx, dy, thickness) && !empty(input.getPixel(x + dx, y + dy))){
                                    found = true;
                                    break outer;
                                }
                            }
                        }
                        if(found){
                            pixmap.draw(x, y, col);
                        }
                    }


                }
            }
            return pixmap;
        }
    }

    public static Pixmap outline(PixmapRegion region, Color color, int radius){
        int outlineColor = color.rgba8888();
        Pixmap out = region.crop();
        for(int x = 0; x < region.width; x++){
            for(int y = 0; y < region.height; y++){

                if((region.getPixel(x, y) & 0x000000ff) < 255){
                    boolean found = false;
                    outer:
                    for(int rx = -radius; rx <= radius; rx++){
                        for(int ry = -radius; ry <= radius; ry++){
                            if(Structs.inBounds(rx + x, ry + y, region.width, region.height) && Mathf.within(rx, ry, radius) && !empty(region.getPixel(rx + x, ry + y))){
                                found = true;
                                break outer;
                            }
                        }
                    }
                    if(found){
                        out.draw(x, y, outlineColor);
                    }
                }
            }
        }
        return out;
    }

    public static Pixmap outline(Pixmap input, Color color){
        Pixmap pixmap = input.copy();
        int col = color.rgba();

        for(int x = 0; x < pixmap.width; x++){
            for(int y = 0; y < pixmap.height; y++){
                if(empty(input.getPixel(x, y)) &&
                (!empty(input.getPixel(x, y + 1)) || !empty(input.getPixel(x, y - 1)) || !empty(input.getPixel(x - 1, y)) || !empty(input.getPixel(x + 1, y))))
                    pixmap.draw(x, y, col);
            }
        }
        return pixmap;
    }

    public static Pixmap zoom(Pixmap input, int scale){
        Pixmap pixmap = new Pixmap(input.width, input.height);
        for(int x = 0; x < pixmap.width; x++){
            for(int y = 0; y < pixmap.height; y++){
                pixmap.draw(x, y, input.getPixel(x / scale + pixmap.width / 2 / scale, y / scale + pixmap.height / 2 / scale));
            }
        }
        return pixmap;
    }

    public static Pixmap resize(Pixmap input, int width, int height){
        Pixmap pixmap = new Pixmap(width, height);
        pixmap.drawPixmap(input, width / 2 - input.width / 2, height / 2 - input.height / 2);

        return pixmap;
    }

    public static Pixmap resize(Pixmap input, int width, int height, int backgroundColor){
        Pixmap pixmap = new Pixmap(width, height);
        pixmap.fill(backgroundColor);
        pixmap.drawPixmap(input, width / 2 - input.width / 2, height / 2 - input.height / 2);

        return pixmap;
    }

    public static Pixmap crop(Pixmap input, int x, int y, int width, int height){
        if(input.isDisposed()) throw new IllegalStateException("input is disposed.");
        Pixmap pixmap = new Pixmap(width, height);
        pixmap.drawPixmap(input, 0, 0, x, y, width, height);
        return pixmap;
    }

    public static Pixmap rotate(Pixmap input, float angle){
        Vec2 vector = new Vec2();
        Pixmap pixmap = new Pixmap(input.height, input.width);

        for(int x = 0; x < input.width; x++){
            for(int y = 0; y < input.height; y++){
                vector.set(x - input.width / 2f + 0.5f, y - input.height / 2f);
                vector.rotate(-angle);
                int px = (int)(vector.x + input.width / 2f + 0.01f);
                int py = (int)(vector.y + input.height / 2f + 0.01f);
                pixmap.draw(px - input.width / 2 + pixmap.width / 2, py - input.height / 2 + pixmap.height / 2, input.getPixel(x, y));
            }
        }

        return pixmap;
    }

    public static boolean empty(int i){
        return (i & 0x000000ff) == 0;
    }

    public static void traverse(Pixmap input, Intc2 t){
        for(int x = 0; x < input.width; x++){
            for(int y = 0; y < input.height; y++){
                t.get(x, y);
            }
        }
    }

    public static Pixmap huePixmap(int width, int height){
        Pixmap pixmap = new Pixmap(width, height);
        Color color = new Color(1, 1, 1, 1);

        for(int x = 0; x < width; x++){
            color.fromHsv(x / (float)width, 1f, 1);
            int rgba = color.rgba();
            for(int y = 0; y < height; y++){
                pixmap.draw(x, y, rgba);
            }
        }
        return pixmap;
    }

    public static Texture hueTexture(int width, int height){
        return new Texture(huePixmap(width, height));
    }

    public static Pixmap blankPixmap(){
        Pixmap pixmap = new Pixmap(1, 1);
        pixmap.fill(Color.whiteRgba);
        return pixmap;
    }

    public static Texture blankTexture(){
        Texture texture = new Texture(blankPixmap());
        texture.setWrap(TextureWrap.repeat, TextureWrap.repeat);
        return texture;
    }

    public static TextureRegion blankTextureRegion(){
        return new TextureRegion(blankTexture());
    }

    public static void drawPixel(Texture texture, int x, int y, int color){
        if(drawPixmap == null){
            drawPixmap = new Pixmap(1, 1);
        }

        drawPixmap.fill(color);
        texture.draw(drawPixmap, x, y);
    }
}

package arc.graphics;

import arc.func.Intc2;
import arc.graphics.Pixmap.*;
import arc.graphics.Texture.TextureWrap;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.*;
import arc.struct.*;

import java.nio.ByteBuffer;

/** Various pixmap utilities. */
public class Pixmaps{
    private static Pixmap drawPixmap;
    private static IntArray tmpArray = new IntArray();

    public static void flip(Pixmap pixmap){
        ByteBuffer pixels = pixmap.getPixels();
        int numBytes = pixmap.getWidth() * pixmap.getHeight() * 4;
        byte[] lines = new byte[numBytes];
        int numBytesPerLine = pixmap.getWidth() * 4;
        for(int i = 0; i < pixmap.getHeight(); i++){
            pixels.position((pixmap.getHeight() - i - 1) * numBytesPerLine);
            pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
        }
        pixels.clear();
        pixels.put(lines);
    }

    public static Pixmap median(Pixmap input, int radius, double percentile){
        return median(input, radius, percentile, tmpArray);
    }

    public static Pixmap median(Pixmap input, int radius, double percentile, IntArray tmp){
        Pixmap pixmap = new Pixmap(input.getWidth(), input.getHeight());
        input.each((x, y) -> {
            tmp.clear();
            Geometry.circle(x, y, pixmap.getWidth(), pixmap.getHeight(), radius, (cx, cy) -> tmp.add(input.getPixel(cx, cy)));
            tmp.sort();
            pixmap.draw(x, y, tmp.get(Mathf.clamp((int)(tmp.size * percentile), 0, tmp.size - 1)));
        });
        return pixmap;
    }

    public static Pixmap copy(Pixmap input){
        Pixmap pixmap = new Pixmap(input.getWidth(), input.getHeight(), Format.RGBA8888);
        pixmap.drawPixmap(input, 0, 0);
        return pixmap;
    }

    public static Pixmap scale(Pixmap pixmap, int width, int height, Filter filter){
        Pixmap dest = new Pixmap(width, height);
        dest.setFilter(filter);
        dest.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), 0, 0, width, height);
        return dest;
    }

    public static Pixmap scale(Pixmap input, float scale){
        return scale(input, scale, scale);
    }

    public static Pixmap scale(Pixmap input, float scalex, float scaley){
        Pixmap pixmap = new Pixmap((int)(input.getWidth() * scalex), (int)(input.getHeight() * scaley), Format.RGBA8888);
        for(int x = 0; x < pixmap.getWidth(); x++){
            for(int y = 0; y < pixmap.getHeight(); y++){
                pixmap.draw(x, y, input.getPixel((int)(x / scalex), (int)(y / scaley)));
            }
        }
        return pixmap;
    }

    public static Pixmap outline(Pixmap input, Color color, int thickness){
        if(thickness == 1){
            return outline(input, color);
        }else{
            Pixmap pixmap = copy(input);
            pixmap.setColor(color);

            for(int x = 0; x < pixmap.getWidth(); x++){
                for(int y = 0; y < pixmap.getHeight(); y++){
                    if(empty(input.getPixel(x, y))){
                        boolean found = false;
                        outer:
                        for(int dx = -thickness; dx <= thickness; dx++){
                            for(int dy = -thickness; dy <= thickness; dy++){
                                if(Mathf.dst2(dx, dy) <= thickness * thickness && !empty(input.getPixel(x + dx, y + dy))){
                                    found = true;
                                    break outer;
                                }
                            }
                        }
                        if(found){
                            pixmap.draw(x, y);
                        }
                    }


                }
            }
            return pixmap;
        }
    }

    public static Pixmap outline(Pixmap input, Color color){
        Pixmap pixmap = copy(input);
        pixmap.setColor(color);

        for(int x = 0; x < pixmap.getWidth(); x++){
            for(int y = 0; y < pixmap.getHeight(); y++){
                if(empty(input.getPixel(x, y)) &&
                (!empty(input.getPixel(x, y + 1)) || !empty(input.getPixel(x, y - 1)) || !empty(input.getPixel(x - 1, y)) || !empty(input.getPixel(x + 1, y))))
                    pixmap.draw(x, y);
            }
        }
        return pixmap;
    }

    public static Pixmap zoom(Pixmap input, int scale){
        Pixmap pixmap = new Pixmap(input.getWidth(), input.getHeight(), Format.RGBA8888);
        for(int x = 0; x < pixmap.getWidth(); x++){
            for(int y = 0; y < pixmap.getHeight(); y++){
                pixmap.draw(x, y, input.getPixel(x / scale + pixmap.getWidth() / 2 / scale, y / scale + pixmap.getHeight() / 2 / scale));
            }
        }
        return pixmap;
    }

    public static Pixmap resize(Pixmap input, int width, int height){
        Pixmap pixmap = new Pixmap(width, height, Format.RGBA8888);
        pixmap.drawPixmap(input, width / 2 - input.getWidth() / 2, height / 2 - input.getHeight() / 2);

        return pixmap;
    }

    public static Pixmap resize(Pixmap input, int width, int height, int backgroundColor){
        Pixmap pixmap = new Pixmap(width, height, Format.RGBA8888);
        pixmap.setColor(backgroundColor);
        pixmap.fill();
        pixmap.drawPixmap(input, width / 2 - input.getWidth() / 2, height / 2 - input.getHeight() / 2);

        return pixmap;
    }

    public static Pixmap crop(Pixmap input, int x, int y, int width, int height){
        Pixmap pixmap = new Pixmap(width, height, Format.RGBA8888);
        pixmap.drawPixmap(input, 0, 0, x, y, width, height);
        return pixmap;
    }

    public static Pixmap rotate(Pixmap input, float angle){
        Vec2 vector = new Vec2();
        Pixmap pixmap = new Pixmap(input.getHeight(), input.getWidth(), Format.RGBA8888);

        for(int x = 0; x < input.getWidth(); x++){
            for(int y = 0; y < input.getHeight(); y++){
                vector.set(x - input.getWidth() / 2f + 0.5f, y - input.getHeight() / 2f);
                vector.rotate(-angle);
                int px = (int)(vector.x + input.getWidth() / 2f + 0.01f);
                int py = (int)(vector.y + input.getHeight() / 2f + 0.01f);
                pixmap.draw(px - input.getWidth() / 2 + pixmap.getWidth() / 2, py - input.getHeight() / 2 + pixmap.getHeight() / 2, input.getPixel(x, y));
            }
        }

        return pixmap;
    }

    private static boolean empty(int i){
        return (i & 0x000000ff) == 0;
    }

    public static void traverse(Pixmap input, Intc2 t){
        for(int x = 0; x < input.getWidth(); x++){
            for(int y = 0; y < input.getHeight(); y++){
                t.get(x, y);
            }
        }
    }

    public static Pixmap huePixmap(int width, int height){
        Pixmap pixmap = new Pixmap(width, height, Format.RGBA8888);
        Color color = new Color(1, 1, 1, 1);

        for(int x = 0; x < width; x++){
            color.fromHsv(x / (float)width, 1f, 1);
            pixmap.setColor(color);
            for(int y = 0; y < height; y++){
                pixmap.draw(x, y);
            }
        }
        return pixmap;
    }

    public static Texture hueTexture(int width, int height){
        return new Texture(huePixmap(width, height));
    }

    public static Pixmap blankPixmap(){
        Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
        pixmap.setColor(Color.white);
        pixmap.fill();
        return pixmap;
    }

    public static Texture blankTexture(){
        Texture texture = new Texture(blankPixmap());
        texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        return texture;
    }

    public static TextureRegion blankTextureRegion(){
        return new TextureRegion(blankTexture());
    }

    public static void drawPixel(Texture texture, int x, int y, int color){
        if(drawPixmap == null){
            drawPixmap = new Pixmap(1, 1, Format.RGBA8888);
        }

        drawPixmap.setColor(color);
        drawPixmap.fill();
        texture.draw(drawPixmap, x, y);
    }
}

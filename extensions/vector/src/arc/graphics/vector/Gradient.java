package arc.graphics.vector;

import arc.graphics.*;
import arc.graphics.Pixmap.*;
import arc.struct.*;
import arc.util.*;
import arc.util.pooling.Pool.*;
import arc.util.pooling.*;

public class Gradient implements Poolable, Disposable{
    final FloatSeq stops = new FloatSeq();
    int id;
    Texture texture;

    private Gradient(){
    }

    public static Gradient obtain(){
        return Pools.obtain(Gradient.class, Gradient::new);
    }

    public static Gradient obtain(FloatSeq stops){
        Gradient gradient = obtain();
        gradient.stops.addAll(stops);
        return gradient;
    }

    static Gradient obtain(int id, FloatSeq stops){
        Gradient gradient = obtain();
        gradient.id = id;
        gradient.stops.addAll(stops);
        return gradient;
    }

    public float getOffset(int index){
        return stops.get(index * 5);
    }

    public float getRed(int index){
        return stops.get(index * 5 + 1);
    }

    public float getGreen(int index){
        return stops.get(index * 5 + 2);
    }

    public float getBlue(int index){
        return stops.get(index * 5 + 3);
    }

    public float getAlpha(int index){
        return stops.get(index * 5 + 4);
    }

    public int getLength(){
        return stops.size / 5;
    }

    public Color getColor(int index, Color out){
        return out.set(getRed(index), getGreen(index), getBlue(index), getAlpha(index));
    }

    public Gradient add(float offset, int rgba){
        stops.add(offset);
        stops.add(((rgba & 0xff000000) >>> 24) / 255f);
        stops.add(((rgba & 0x00ff0000) >>> 16) / 255f);
        stops.add(((rgba & 0x0000ff00) >>> 8) / 255f);
        stops.add(((rgba & 0x000000ff)) / 255f);
        return this;
    }

    public Gradient add(float offset, Color color){
        stops.add(offset);
        stops.add(color.r);
        stops.add(color.g);
        stops.add(color.b);
        stops.add(color.a);
        return this;
    }

    public Gradient add(float offset, int r, int g, int b, int a){
        stops.add(offset);
        stops.add(r / 255);
        stops.add(g / 255);
        stops.add(b / 255);
        stops.add(a / 255);
        return this;
    }

    public Gradient add(float offset, float r, float g, float b, float a){
        stops.add(offset);
        stops.add(r);
        stops.add(g);
        stops.add(b);
        stops.add(a);
        return this;
    }

    public Gradient set(int index, float offset, int rgba){
        int stopStart = index * 5;
        stops.set(stopStart, offset);
        stops.set(stopStart + 1, ((rgba & 0xff000000) >>> 24) / 255f);
        stops.set(stopStart + 2, ((rgba & 0x00ff0000) >>> 16) / 255f);
        stops.set(stopStart + 3, ((rgba & 0x0000ff00) >>> 8) / 255f);
        stops.set(stopStart + 4, ((rgba & 0x000000ff)) / 255f);
        return this;
    }

    public Gradient set(int index, float offset, Color color){
        int stopStart = index * 5;
        stops.set(stopStart, offset);
        stops.set(stopStart + 1, color.r);
        stops.set(stopStart + 2, color.g);
        stops.set(stopStart + 3, color.b);
        stops.set(stopStart + 4, color.a);
        return this;
    }

    public Gradient set(int index, float offset, int r, int g, int b, int a){
        int stopStart = index * 5;
        stops.set(stopStart, offset);
        stops.set(stopStart + 1, r / 255);
        stops.set(stopStart + 2, g / 255);
        stops.set(stopStart + 3, b / 255);
        stops.set(stopStart + 4, a / 255);
        return this;
    }

    public Gradient set(int index, float offset, float r, float g, float b, float a){
        int stopStart = index * 5;
        stops.set(stopStart, offset);
        stops.set(stopStart + 1, r);
        stops.set(stopStart + 2, g);
        stops.set(stopStart + 3, b);
        stops.set(stopStart + 4, a);
        return this;
    }

    public Gradient set(Gradient other){
        stops.clear();
        stops.addAll(other.stops);
        return this;
    }

    int getTexturehandle(){
        if(texture == null){
            texture = createTexture();
        }
        return texture.getTextureObjectHandle();
    }

    private Texture createTexture(){
        int gradientStops = getLength();
        int width = gradientStops * 2;
        Pixmap data = new Pixmap(width, 1, Format.rgba8888);

        for(int i = 0; i < gradientStops; i++){
            data.draw(i * 2, 0, Color.rgba8888(getOffset(i), 0, 0, 1));
            data.draw(i * 2 + 1, 0, Color.rgba8888(getRed(i), getGreen(i), getBlue(i), getAlpha(i)));
        }

        return new Texture(data);
    }

    @Override
    public void reset(){
        id = 0;
        stops.clear();
        disposeTexture();
    }

    private void disposeTexture(){
        if(texture != null){
            texture.dispose();
            texture = null;
        }
    }

    @Override
    public void dispose(){
        disposeTexture();
    }

    public void free(){
        Pools.free(this);
    }

    @Override
    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(obj.getClass() != getClass()){
            return false;
        }

        Gradient other = (Gradient)obj;
        return stops.equals(other.stops);
    }

    @Override
    public int hashCode(){
        int result = 0;
        for(int i = 0; i < stops.size; i++){
            float stop = stops.get(i);
            result = 31 * result + (stop != +0.0f ? Float.floatToIntBits(stop) : 0);
        }
        return result;
    }
}

import arc.files.*;
import arc.graphics.*;
import arc.util.*;
import arc.util.noise.*;
import org.junit.*;

import static arc.math.Mathf.*;
import static org.junit.Assert.*;

public class PixmapTest{

    @Test
    public void pixmapCreate(){

        //test with no natives
        Pixmap pix = new Pixmap(100, 100);
        pix.fillCircle(50, 50, 30, Color.red.rgba());

        assertEquals(Color.red.rgba(), pix.get(50, 50));
        assertEquals(Color.red.rgba(), pix.get(54, 54));
        assertEquals(0, pix.get(0, 0));

        ArcNativesLoader.load();

        pix = new Pixmap(100, 100);
        pix.fillCircle(50, 50, 30, Color.red.rgba());

        assertEquals(Color.red.rgba(), pix.get(50, 50));
        assertEquals(Color.red.rgba(), pix.get(54, 54));
        assertEquals(0, pix.get(0, 0));
    }

    @Test
    public void noise(){
        int w = 100, h = 100;
        Pixmap pix = new Pixmap(w * 4, h * 4);
        pix.each((x, y) -> {
            double n = (tiledNoise(x % w, y % h, w, h, 20) + tiledNoise(x % w, y % h, w, h, 10) * 0.5) / 1.5;
            pix.set(x, y, Tmp.c1.set(1f, 1f, 1f, 1f).mul((float)n));
        });
        Fi.get("/home/anuke/out.png").writePng(pix);
    }

    static double tiledNoise(float x, float y, float w, float h, float scl){
        x /= scl;
        y /= scl;
        w /= scl;
        h /= scl;

        float
        x1 = 0f, x2 = w - 1f,
        y1 = 0f, y2 = h - 1f;

        float s = x / w, t = y / h;
        float dx = x2 - x1, dy = y2 - y1;

        double nx = x1 + Math.cos(s*2*PI)*dx/PI2;
        double ny = y1 + Math.cos(t*2*PI)*dy/PI2;
        double nz = x1 + Math.sin(s*2*PI)*dx/PI2;
        double nw = y1 + Math.sin(t*2*PI)*dy/PI2;

        return Simplex.raw4d(nx, ny, nz, nw);
    }

    /*
    @Test
    public void normals(){
        Vec3 light = new Vec3(1, 1, 1).nor();
        Pixmap normals = new Pixmap(new Fi("/home/anuke/Projects/Mindustry/core/assets/sprites/cloud_normal.png"));
        Pixmap clouds = new Pixmap(new Fi("/home/anuke/Projects/Mindustry/core/assets/sprites/clouds_basic.png"));
        normals.each((x, y) -> {
            Tmp.c1.set(normals.get(x, y));
            Tmp.v31.set(Tmp.c1.r * 2f - 1f, Tmp.c1.g * 2f - 1f, (Tmp.c1.b - 0.5f) * 2f - 1f).nor();
            float dot = Tmp.v31.dot(light);
            float alpha = Mathf.clamp(dot + 1);
            clouds.set(x, y, Tmp.c1.set(clouds.get(x, y)).mul(Mathf.lerp(alpha, 1f, 0.85f)));
            //float l = Mathf.lerp(alpha, 1f, 0.5f);
            //normals.set(x, y, Color.rgba8888(l, l, l, Mathf.lerp(alpha, 1f, 0f)));
        });
        new Fi("/home/anuke/out.png").writePng(normals);
        new Fi("/home/anuke/clouds.png").writePng(clouds);
    }*/

    void bench(Runnable a, Runnable b, int amount){
        for(int i = 0; i < amount/3; i++){
            a.run();
            b.run();
        }

        Time.mark();

        for(int i = 0; i < amount; i++){
            a.run();
        }

        Log.info("Time for A: " + Time.elapsed());

        Time.mark();

        for(int i = 0; i < amount; i++){
            b.run();
        }

        Log.info("Time for B: " + Time.elapsed());
    }

}
import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import org.junit.*;

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

    static  Rect rect = new Rect();
    static Vec2 v1 = new Vec2(), v2 = new Vec2();

    static void randomize(){
        rect.set(Mathf.random(30f), Mathf.random(30f), 8f, 8f);
        v1.set(Mathf.random(30f), Mathf.random(30f));
        v2.set(Mathf.random(30f), Mathf.random(30f));
    }

    @Test
    public void raycasts(){

        Vec2 hole = new Vec2();
        int[] results = {0};

        bench(() -> {
            randomize();

            results[0] += Intersector.intersectSegmentRectangle(v1, v2, rect) ? 1 : 0;
        }, () -> {
            randomize();

            results[0] +=  Intersector.intersectSegmentRectangleFast(v1.x, v1.y, v2.x, v2.y, rect.x, rect.y, rect.width, rect.height) ? 1 : 0;
        }, 100_000_000);

        Log.info(results[0]);
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
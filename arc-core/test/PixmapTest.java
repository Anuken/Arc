import arc.files.*;
import arc.graphics.*;
import arc.util.*;
import org.junit.*;

import static org.junit.Assert.*;

public class PixmapTest{

    @Test
    public void pixmapCreate(){
        ArcNativesLoader.load();

        Pixmap pix = new Pixmap(100, 100);
        pix.fillCircle(50, 50, 30, Color.red.rgba());

        assertEquals(Color.red.rgba(), pix.get(50, 50));
        assertEquals(Color.red.rgba(), pix.get(54, 54));
        assertEquals(0, pix.get(0, 0));
    }

    //@Test
    public void testScaling(){
        Pixmap base = new Pixmap(new Fi("/home/anuke/soup.png"));
        Pixmap result = new Pixmap(50, 50);
        result.draw(base, 0, 0, base.width, base.height, 0, 0, result.width, result.height, true, false);
        Fi.get("/home/anuke/soup_scaled.png").writePng(result);
    }

    void bench(Runnable a, Runnable b, int amount){
        for(int i = 0; i < amount/2; i++){
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
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

        assertEquals(Color.red.rgba(), pix.getPixel(50, 50));
        assertEquals(Color.red.rgba(), pix.getPixel(54, 54));
        assertEquals(0, pix.getPixel(0, 0));
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
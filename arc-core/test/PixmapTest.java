import arc.files.*;
import arc.graphics.*;
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

    //@Test
    public void convert(){
        Fi.get("/home/anuke/Projects/Mindustry/core/assets-raw/spritescopy").walk(f -> {
            if(f.extEquals("png")){
                f.writePng(new Pixmap(f));
            }
        });
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
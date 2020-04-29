import arc.graphics.*;
import arc.util.*;
import org.junit.*;

import static org.junit.Assert.assertEquals;

public class PixmapTest{

    @Test
    public void pixmapCreate(){
        ArcNativesLoader.load();

        Pixmap pix = new Pixmap(100, 100);
        pix.setColor(Color.red);
        pix.fillCircle(50, 50, 30);

        assertEquals(Color.red.rgba(), pix.getPixel(50, 50));
        assertEquals(Color.red.rgba(), pix.getPixel(54, 54));
        assertEquals(0, pix.getPixel(0, 0));
    }
}

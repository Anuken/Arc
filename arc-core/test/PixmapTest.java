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
        pix.drawCircle(50, 50, 30);

        assertEquals(pix.getPixel(50, 50), Color.red.rgba());
        assertEquals(pix.getPixel(54, 54), Color.red.rgba());
        assertEquals(pix.getPixel(0, 0), 0);
    }
}

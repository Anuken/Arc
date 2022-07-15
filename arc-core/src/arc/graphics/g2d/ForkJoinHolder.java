package arc.graphics.g2d;

import java.util.concurrent.*;

public class ForkJoinHolder{
    public final ForkJoinPool pool = ForkJoinPool.commonPool();
}

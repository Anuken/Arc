package math;

import arc.func.*;
import arc.struct.*;
import arc.util.*;
import arc.util.pooling.*;
import org.junit.*;

import static org.junit.Assert.assertEquals;

public class PoolTest{

    @Test
    public void allocation(){
        long start = 0;

        int objects = 100000;
        Array<Object> list = new Array<>(objects);
        Pools.get(Object.class, Object::new, objects);

        for(int i = 0; i < objects; i++){
            list.add(Pools.obtain(Object.class, Object::new));
        }

        Pools.freeAll(list, true);
        list.clear();

        long pre = memory();

        for(int i = 0; i < objects; i++){
            list.add(Pools.get(Object.class, Object::new).obtain());
        }

        Pools.freeAll(list, true);
        list.clear();

        for(int i = 0; i < objects; i++){
            list.add(Pools.get(Object.class, Object::new).obtain());
        }

        long post = memory();

        Prov a = Object::new;
        Prov b = Object::new;

        Log.info("a == b: {0}; codes: {1} {2}; equality: {3}", a == b, a.hashCode(), b.hashCode(), a.equals(b));
        Log.info("Memory delta: {0} b", (post - pre));
        Log.info("Total memory allocated: {0} mb", Strings.fixed((post - start)/1024f/1024f, 1));

        assertEquals("Memory usage of pools must be 0.", pre - post, 0);
    }

    long memory(){
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
}

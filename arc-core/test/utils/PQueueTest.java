package utils;

import arc.math.*;
import arc.struct.*;
import arc.util.*;
import org.junit.*;

public class PQueueTest{

    @Test
    public void test(){
        PQueue<Float> queue1 = new PQueue<>(10, Float::compare);
        java.util.PriorityQueue<Float> queue2 = new java.util.PriorityQueue<>(10, Float::compare);

        int amount = 1000;

        for(int j = 0; j < 10; j++){
            queue1.clear();
            queue2.clear();

            Time.mark();
            for(int i = 0; i < amount; i++){
                queue1.add(Mathf.random());
            }
            Log.info(Time.elapsed());

            Time.mark();
            for(int i = 0; i < amount; i++){
                queue2.add(Mathf.random());
            }
            Log.info(Time.elapsed());

            Log.info("");
        }

        float result = 0;
        float sum = 0;

        Log.info("POLLING");

        for(int j = 0; j < 10; j++){
            queue1.clear();
            queue2.clear();

            Time.mark();
            for(int i = 0; i < amount; i++){
                queue1.add(Mathf.random());
            }

            for(int i = 0; i < amount; i++){
                result = queue1.poll();
                sum += result;
            }
            Log.info(Time.elapsed());

            Time.mark();
            for(int i = 0; i < amount; i++){
                queue2.add(Mathf.random());
            }

            for(int i = 0; i < amount; i++){
                result = queue2.poll();
                sum += result;
            }
            Log.info(Time.elapsed());

            Log.info("");
        }

        Log.info("ignore " + sum);


    }
}

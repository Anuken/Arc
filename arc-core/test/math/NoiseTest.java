package math;

import arc.util.*;
import arc.util.noise.*;
import org.junit.*;

public class NoiseTest{

    @Test
    public void perfcomp(){
        Simplex sim = new Simplex(1);

        int iterations = 1000000;

        for(int i = 0; i < iterations; i++){
            sim.raw_noise_2d(Math.random(), Math.random());
            Noise.rawNoise(Math.random(), Math.random());
        }

        Time.mark();

        for(int i = 0; i < iterations; i++){
            Noise.rawNoise(Math.random(), Math.random());
        }

        Log.info("Perlin: {0}", Time.elapsed());

        Time.mark();

        for(int i = 0; i < iterations; i++){
            sim.raw_noise_2d(Math.random(), Math.random());
        }

        Log.info("Simplex: {0}", Time.elapsed());
    }
}

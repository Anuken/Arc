package arc.util.noise;

//horribly butchered from libnoiseforjava, please ignore
//TODO this is a mess - uses doubles instead of floats, but only sometimes
public class Ridged{
    static final int X_NOISE_GEN = 1619;
    static final int Y_NOISE_GEN = 31337;
    static final int Z_NOISE_GEN = 6971;
    static final int SEED_NOISE_GEN = 1013;
    static final int SHIFT_NOISE_GEN = 8;
    static final int quality = 2;

    private Ridged(){}

    public static float noise2d(int seed, double x, double y, double frequency){
        return noise2d(seed, x, y, 1, frequency);
    }

    public static float noise2d(int seed, double x, double y, int octaves, double frequency){
        return noise2d(seed, x, y, octaves, 0.5, frequency);
    }

    public static float noise2d(int seed, double x, double y, int octaves, double persistence, double frequency){
        double x1 = x * frequency;
        double y1 = y * frequency;

        double signal;
        double value = 0.0;
        double weight = 1.0;

        double offset = 1.0;
        double gain = 2.0;
        double sweight = 1.0;

        for(int curOctave = 0; curOctave < octaves; curOctave++){

            double nx = range(x1);
            double ny = range(y1);

            // Get the coherent-noise value.
            signal = gradientCoherentNoise(nx, ny, (seed + curOctave) & 0x7fffffff);

            // Make the ridges.
            signal = Math.abs(signal);
            signal = offset - signal;

            // Square the signal to increase the sharpness of the ridges.
            signal *= signal;

            // The weighting from the previous octave is applied to the signal.
            // Larger values have higher weights, producing sharp points along the ridges.
            signal *= weight;

            // Weight successive contributions by the previous signal.
            weight = signal * gain;
            if(weight > 1.0){
                weight = 1.0;
            }
            if(weight < 0.0){
                weight = 0.0;
            }

            // Add the signal to the output value.
            value += (signal * sweight);

            sweight *= persistence;

            // Go to the next octave.
            x1 *= 2;
            y1 *= 2;
        }

        return (float)((value * 1.25) - 1.0);
    }

    public static float noise3d(int seed, double x, double y, double z, float frequency){
        return noise3d(seed, x, y, z, 1, frequency);
    }

    public static float noise3d(int seed, double x, double y, double z, int octaves, float frequency){
        double x1 = x * frequency;
        double y1 = y * frequency;
        double z1 = z * frequency;

        double signal;
        double value = 0.0;
        double weight = 1.0;

        double offset = 1.0;
        double gain = 2.0;
        double scaling = 0.5;
        double sweight = 1.0;

        for(int curOctave = 0; curOctave < octaves; curOctave++){

            double nx, ny, nz;
            nx = range(x1);
            ny = range(y1);
            nz = range(z1);

            // Get the coherent-noise value.
            signal = gradientCoherentNoise3D(nx, ny, nz, (seed + curOctave) & 0x7fffffff);

            // Make the ridges.
            signal = Math.abs(signal);
            signal = offset - signal;

            // Square the signal to increase the sharpness of the ridges.
            signal *= signal;

            // The weighting from the previous octave is applied to the signal.
            // Larger values have higher weights, producing sharp points along the ridges.
            signal *= weight;

            // Weight successive contributions by the previous signal.
            weight = signal * gain;
            if(weight > 1.0){
                weight = 1.0;
            }
            if(weight < 0.0){
                weight = 0.0;
            }

            // Add the signal to the output value.
            value += (signal * sweight);

            sweight *= scaling;

            // Go to the next octave.
            x1 *= 2;
            y1 *= 2;
            z1 *= 2;
        }

        return (float)((value * 1.25) - 1.0);
    }

    static double range(double n){
        if(n >= 1073741824.0)
            return (2.0 * (n % 1073741824.0)) - 1073741824.0;
        else if(n <= -1073741824.0)
            return (2.0 * (n % 1073741824.0)) + 1073741824.0;
        else
            return n;
    }

    static double gradientCoherentNoise3D(double x, double y, double z, int seed){
        // Create a unit-length cube aligned along an integer boundary. This cube surrounds the input point.
        int x0 = (x > 0.0 ? (int)x : (int)x - 1);
        int x1 = x0 + 1;
        int y0 = (y > 0.0 ? (int)y : (int)y - 1);
        int y1 = y0 + 1;
        int z0 = (z > 0.0 ? (int)z : (int)z - 1);
        int z1 = z0 + 1;

        // Map the difference between the coordinates of the input value and the
        // coordinates of the cube's outer-lower-left vertex onto an S-curve.
        double xs = 0, ys = 0, zs = 0;
        switch(quality){
            case 0: // fast
                xs = (x - (double)x0);
                ys = (y - (double)y0);
                zs = (z - (double)z0);
                break;
            case 1: // STD
                xs = scurve3(x - (double)x0);
                ys = scurve3(y - (double)y0);
                zs = scurve3(z - (double)z0);
                break;
            case 2: // best
                xs = scurve5(x - (double)x0);
                ys = scurve5(y - (double)y0);
                zs = scurve5(z - (double)z0);
                break;
        }

        // Now calculate the noise values at each vertex of the cube. To
        // generate
        // the coherent-noise value at the input point, interpolate these eight
        // noise values using the S-curve value as the interpolant (trilinear
        // interpolation.)
        double n0, n1, ix0, ix1, iy0, iy1;
        n0 = gradientNoise3D(x, y, z, x0, y0, z0, seed);
        n1 = gradientNoise3D(x, y, z, x1, y0, z0, seed);
        ix0 = linearInterp(n0, n1, xs);
        n0 = gradientNoise3D(x, y, z, x0, y1, z0, seed);
        n1 = gradientNoise3D(x, y, z, x1, y1, z0, seed);
        ix1 = linearInterp(n0, n1, xs);
        iy0 = linearInterp(ix0, ix1, ys);
        n0 = gradientNoise3D(x, y, z, x0, y0, z1, seed);
        n1 = gradientNoise3D(x, y, z, x1, y0, z1, seed);
        ix0 = linearInterp(n0, n1, xs);
        n0 = gradientNoise3D(x, y, z, x0, y1, z1, seed);
        n1 = gradientNoise3D(x, y, z, x1, y1, z1, seed);
        ix1 = linearInterp(n0, n1, xs);
        iy1 = linearInterp(ix0, ix1, ys);

        return linearInterp(iy0, iy1, zs);
    }

    static double gradientCoherentNoise(double x, double y, int seed){
        // Create a unit-length cube aligned along an integer boundary. This cube surrounds the input point.
        int x0 = (x > 0.0 ? (int)x : (int)x - 1);
        int x1 = x0 + 1;
        int y0 = (y > 0.0 ? (int)y : (int)y - 1);
        int y1 = y0 + 1;

        // Map the difference between the coordinates of the input value and the
        // coordinates of the cube's outer-lower-left vertex onto an S-curve.
        double xs = 0, ys = 0;
        switch(quality){
            case 0: // fast
                xs = (x - (double)x0);
                ys = (y - (double)y0);
                break;
            case 1: // STD
                xs = scurve3(x - (double)x0);
                ys = scurve3(y - (double)y0);
                break;
            case 2: // best
                xs = scurve5(x - (double)x0);
                ys = scurve5(y - (double)y0);
                break;
        }

        // Now calculate the noise values at each vertex of the cube. To
        // generate
        // the coherent-noise value at the input point, interpolate these eight
        // noise values using the S-curve value as the interpolant (trilinear
        // interpolation.)
        double n0, n1, ix0, ix1, iy0;
        n0 = gradientNoise(x, y, x0, y0,seed);
        n1 = gradientNoise(x, y, x1, y0, seed);
        ix0 = linearInterp(n0, n1, xs);
        n0 = gradientNoise(x, y, x0, y1, seed);
        n1 = gradientNoise(x, y, x1, y1, seed);
        ix1 = linearInterp(n0, n1, xs);
        iy0 = linearInterp(ix0, ix1, ys);

        return iy0;
    }

    static double gradientNoise3D(double fx, double fy, double fz, int ix, int iy, int iz, int seed){
        int vectorIndex = (X_NOISE_GEN * ix + Y_NOISE_GEN * iy + Z_NOISE_GEN * iz + SEED_NOISE_GEN * seed);

        vectorIndex ^= (vectorIndex >> SHIFT_NOISE_GEN);
        vectorIndex &= 0xff;

        vectorIndex *= 3;

        double xvGradient = VectorTable.randomVectors[vectorIndex];
        double yvGradient = VectorTable.randomVectors[vectorIndex + 1];
        double zvGradient = VectorTable.randomVectors[vectorIndex + 2];


        // Set up us another vector equal to the distance between the two
        // vectors
        // passed to this function.
        double xvPoint = (fx - (double)ix);
        double yvPoint = (fy - (double)iy);
        double zvPoint = (fz - (double)iz);

        // Now compute the dot product of the gradient vector with the distance
        // vector. The resulting value is gradient noise. Apply a scaling value
        // so that this noise value ranges from -1.0 to 1.0.
        return ((xvGradient * xvPoint) + (yvGradient * yvPoint) + (zvGradient * zvPoint)) * 2.12;
    }

    static double gradientNoise(double fx, double fy, int ix, int iy, int seed){
        int vectorIndex = (X_NOISE_GEN * ix + Y_NOISE_GEN * iy + SEED_NOISE_GEN * seed);

        vectorIndex ^= (vectorIndex >> SHIFT_NOISE_GEN);
        vectorIndex &= 0xff;

        vectorIndex *= 3;

        double xvGradient = VectorTable.randomVectors[vectorIndex];
        double yvGradient = VectorTable.randomVectors[vectorIndex + 1];

        // Set up us another vector equal to the distance between the two
        // vectors
        // passed to this function.
        double xvPoint = (fx - (double)ix);
        double yvPoint = (fy - (double)iy);

        // Now compute the dot product of the gradient vector with the distance
        // vector. The resulting value is gradient noise. Apply a scaling value
        // so that this noise value ranges from -1.0 to 1.0.
        return ((xvGradient * xvPoint) + (yvGradient * yvPoint)) * 2.12;
    }

    static double cubicInterp(double n0, double n1, double n2, double n3, double a){
        double p = (n3 - n2) - (n0 - n1);
        double q = (n0 - n1) - p;
        double r = n2 - n0;
        return p * a * a * a + q * a * a + r * a + n1;
    }

    static double linearInterp(double n0, double n1, double a){
        return ((1.0 - a) * n0) + (a * n1);
    }

    static double scurve3(double a){
        return (a * a * (3.0 - 2.0 * a));
    }

    static double scurve5(double a){
        double a3 = a * a * a;
        double a4 = a3 * a;
        double a5 = a4 * a;
        return (6.0 * a5) - (15.0 * a4) + (10.0 * a3);
    }

}

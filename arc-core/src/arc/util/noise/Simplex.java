package arc.util.noise;

import java.util.Random;
//TODoneish this class is less of a disaster:
//- bad parameter inputs, have to use 1/scale
//- may need a replacement with a completely different class


public class Simplex {
    static final int[][] grad3 = new int[][]{{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}, {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}, {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}};
    static final int[][] grad4 = new int[][]{{0, 1, 1, 1}, {0, 1, 1, -1}, {0, 1, -1, 1}, {0, 1, -1, -1}, {0, -1, 1, 1}, {0, -1, 1, -1}, {0, -1, -1, 1}, {0, -1, -1, -1}, {1, 0, 1, 1}, {1, 0, 1, -1}, {1, 0, -1, 1}, {1, 0, -1, -1}, {-1, 0, 1, 1}, {-1, 0, 1, -1}, {-1, 0, -1, 1}, {-1, 0, -1, -1}, {1, 1, 0, 1}, {1, 1, 0, -1}, {1, -1, 0, 1}, {1, -1, 0, -1}, {-1, 1, 0, 1}, {-1, 1, 0, -1}, {-1, -1, 0, 1}, {-1, -1, 0, -1}, {1, 1, 1, 0}, {1, 1, -1, 0}, {1, -1, 1, 0}, {1, -1, -1, 0}, {-1, 1, 1, 0}, {-1, 1, -1, 0}, {-1, -1, 1, 0}, {-1, -1, -1, 0}};
    static int[][] simplex = new int[][]{{0, 1, 2, 3}, {0, 1, 3, 2}, {0, 0, 0, 0}, {0, 2, 3, 1}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {1, 2, 3, 0}, {0, 2, 1, 3}, {0, 0, 0, 0}, {0, 3, 1, 2}, {0, 3, 2, 1}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {1, 3, 2, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {1, 2, 0, 3}, {0, 0, 0, 0}, {1, 3, 0, 2}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {2, 3, 0, 1}, {2, 3, 1, 0}, {1, 0, 2, 3}, {1, 0, 3, 2}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {2, 0, 3, 1}, {0, 0, 0, 0}, {2, 1, 3, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {2, 0, 1, 3}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {3, 0, 1, 2}, {3, 0, 2, 1}, {0, 0, 0, 0}, {3, 1, 2, 0}, {2, 1, 0, 3}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {3, 1, 0, 2}, {0, 0, 0, 0}, {3, 2, 0, 1}, {3, 2, 1, 0}};
    final int[] perm = new int[]{151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142, 8, 99, 37, 240, 21, 10, 23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117, 35, 11, 32, 57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71, 134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105, 92, 41, 55, 46, 245, 40, 244, 102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169, 200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226, 250, 124, 123, 5, 202, 38, 147, 118, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42, 223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43, 172, 9, 129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104, 218, 246, 97, 228, 251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14, 239, 107, 49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254, 138, 236, 205, 93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180, 151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142, 8, 99, 37, 240, 21, 10, 23, 190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117, 35, 11, 32, 57, 177, 33, 88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71, 134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105, 92, 41, 55, 46, 245, 40, 244, 102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169, 200, 196, 135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226, 250, 124, 123, 5, 202, 38, 147, 118, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42, 223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43, 172, 9, 129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104, 218, 246, 97, 228, 251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14, 239, 107, 49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254, 138, 236, 205, 93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180};

    public Simplex() {
    }

    public Simplex(long seed) {
        this.setSeed(seed);
    }

    public void setSeed(long seed) {
        Random random = new Random(seed);

        for(int i = 0; i < this.perm.length; ++i) {
            this.perm[i] = random.nextInt(256);
        }

    }
    /*returns a value between 0 and 1
     * octaves - amount of layers,
     *       - Higher value -> finer details.
     * persistence - 'opacity' amount of each consecutive applied layer, 1 being no fade, 0 would remove all noise after the first layer. 0.5 is typical.
     *       - High values would lead to more chaotic and 'messy' noise. Keep between 0 and 1
     * scale - Not the size of the grid cell, but the 'sample density' scale, a higher value will lead to more noise cells per unit area.
     *       - Otherwise can be considered as the inverse of zoom scale.
     * */
    public float octaveNoise2D(float octaves, float persistence, float scale, float x, float y) {
        float total = 0.0f;
        float frequency = scale;
        float amplitude = 1.0f;
        float maxAmplitude = 0.0f;

        for(int i = 0; (float)i < octaves; ++i) {
            total += (this.rawNoise2D(x * frequency, y * frequency) + 1.0F) / 2.0F * amplitude;
            frequency *= 2.0F;
            maxAmplitude += amplitude;
            amplitude *= persistence;
        }

        return total / maxAmplitude;
    }

    public float octaveNoise3F(float octaves, float persistence, float scale, float x, float y, float z) {
        float total = 0.0F;
        float frequency = scale;
        float amplitude = 1.0F;
        float maxAmplitude = 0.0F;

        for(int i = 0; (float)i < octaves; ++i) {
            total += (this.rawNoise3F(x * frequency, y * frequency, z * frequency) + 1.0F) / 2.0F * amplitude;
            frequency *= 2.0F;
            maxAmplitude += amplitude;
            amplitude *= persistence;
        }

        return total / maxAmplitude;
    }

    public float octaveNoise4D(float octaves, float persistence, float scale, float x, float y, float z, float w) {
        float total = 0.0F;
        float frequency = scale;
        float amplitude = 1.0F;
        float maxAmplitude = 0.0F;

        for(int i = 0; (float)i < octaves; ++i) {
            total += this.rawNoise4D(x * frequency, y * frequency, z * frequency, w * frequency) * amplitude;
            frequency *= 2.0F;
            maxAmplitude += amplitude;
            amplitude *= persistence;
        }

        return total / maxAmplitude;
    }
    /*
     * returns a octaved simplex noise sample value between loBound and hiBound at the specified coords
     * This used to be broken, as whoever wrote this forgot octaveNoise returned between 0 and 1, not -1 and 1
     * */
    public float scaledOctaveNoise2d(float octaves, float persistence, float scale, float loBound, float hiBound, float x, float y) {
        return this.octaveNoise2D(octaves, persistence, scale, x, y) * (hiBound - loBound) + (loBound);
    }

    public float scaledOctaveNoise3F(float octaves, float persistence, float scale, float loBound, float hiBound, float x, float y, float z) {
        return this.octaveNoise3F(octaves, persistence, scale, x, y, z) * (hiBound - loBound) + (loBound);
    }

    public float scaledOctaveNoise4D(float octaves, float persistence, float scale, float loBound, float hiBound, float x, float y, float z, float w) {
        return this.octaveNoise4D(octaves, persistence, scale, x, y, z, w) * (hiBound - loBound) + (loBound);
    }
    /*
     * returns a unoctaved simplex noise sample value between loBound and hiBound at the specified coords
     */
    public float scaledRawNoise2D(float loBound, float hiBound, float x, float y) {
        return (this.rawNoise2D(x, y)+1F)*0.5F * (hiBound - loBound) + (loBound);
    }

    public float scaledRawNoise3F(float loBound, float hiBound, float x, float y, float z) {
        return (this.rawNoise3F(x, y, z)+1F)*0.5F * (hiBound - loBound) + (loBound);
    }

    public float scaledRawNoise4D(float loBound, float hiBound, float x, float y, float z, float w) {
        return (this.rawNoise4D(x, y, z, w)+1F)*0.5F * (hiBound - loBound) + (loBound);
    }
    /*
     * returns a unoctaved simplex noise sample value between -1 and 1 at the specified coords
     * Simplex cell size of the noise is 1 unit. (aka you can expect to find local minima and maxima spaced by roughly 1 unit or so)
     * */

    public float rawNoise2D(float x, float y) {
        float F2 = 0.5F * (Mathf.sqrt(3.0F) - 1.0F);
        float s = (x + y) * F2;
        int i = this.fastfloor(x + s);
        int j = this.fastfloor(y + s);
        float G2 = (3.0F - Mathf.sqrt(3.0F)) / 6.0F;
        float t = (float)(i + j) * G2;
        float X0 = (float)i - t;
        float Y0 = (float)j - t;
        float x0 = x - X0;
        float y0 = y - Y0;
        byte i1;
        byte j1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }

        float x1 = x0 - (float)i1 + G2;
        float y1 = y0 - (float)j1 + G2;
        float x2 = x0 - 1.0F + 2.0F * G2;
        float y2 = y0 - 1.0F + 2.0F * G2;
        int ii = i & 255;
        int jj = j & 255;
        int gi0 = this.perm[ii + this.perm[jj]] % 12;
        int gi1 = this.perm[ii + i1 + this.perm[jj + j1]] % 12;
        int gi2 = this.perm[ii + 1 + this.perm[jj + 1]] % 12;
        float t0 = 0.5F - x0 * x0 - y0 * y0;
        float n0;
        if (t0 < 0.0F) {
            n0 = 0.0F;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * this.dot(grad3[gi0], x0, y0);
        }

        float t1 = 0.5F - x1 * x1 - y1 * y1;
        float n1;
        if (t1 < 0.0F) {
            n1 = 0.0F;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * this.dot(grad3[gi1], x1, y1);
        }

        float t2 = 0.5F - x2 * x2 - y2 * y2;
        float n2;
        if (t2 < 0.0F) {
            n2 = 0.0F;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * this.dot(grad3[gi2], x2, y2);
        }

        return 70.0F * (n0 + n1 + n2);
    }

    public float rawNoise3F(float x, float y, float z) {
        float F3 = 0.3333333333333333F;
        float s = (x + y + z) * F3;
        int i = this.fastfloor(x + s);
        int j = this.fastfloor(y + s);
        int k = this.fastfloor(z + s);
        float G3 = 0.16666666666666666F;
        float t = (float)(i + j + k) * G3;
        float X0 = (float)i - t;
        float Y0 = (float)j - t;
        float Z0 = (float)k - t;
        float x0 = x - X0;
        float y0 = y - Y0;
        float z0 = z - Z0;
        byte i1;
        byte j1;
        byte k1;
        byte i2;
        byte j2;
        byte k2;
        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 1;
                k2 = 0;
            } else if (x0 >= z0) {
                i1 = 1;
                j1 = 0;
                k1 = 0;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            } else {
                i1 = 0;
                j1 = 0;
                k1 = 1;
                i2 = 1;
                j2 = 0;
                k2 = 1;
            }
        } else if (y0 < z0) {
            i1 = 0;
            j1 = 0;
            k1 = 1;
            i2 = 0;
            j2 = 1;
            k2 = 1;
        } else if (x0 < z0) {
            i1 = 0;
            j1 = 1;
            k1 = 0;
            i2 = 0;
            j2 = 1;
            k2 = 1;
        } else {
            i1 = 0;
            j1 = 1;
            k1 = 0;
            i2 = 1;
            j2 = 1;
            k2 = 0;
        }

        float x1 = x0 - (float)i1 + G3;
        float y1 = y0 - (float)j1 + G3;
        float z1 = z0 - (float)k1 + G3;
        float x2 = x0 - (float)i2 + 2.0F * G3;
        float y2 = y0 - (float)j2 + 2.0F * G3;
        float z2 = z0 - (float)k2 + 2.0F * G3;
        float x3 = x0 - 1.0F + 3.0F * G3;
        float y3 = y0 - 1.0F + 3.0F * G3;
        float z3 = z0 - 1.0F + 3.0F * G3;
        int ii = i & 255;
        int jj = j & 255;
        int kk = k & 255;
        int gi0 = this.perm[ii + this.perm[jj + this.perm[kk]]] % 12;
        int gi1 = this.perm[ii + i1 + this.perm[jj + j1 + this.perm[kk + k1]]] % 12;
        int gi2 = this.perm[ii + i2 + this.perm[jj + j2 + this.perm[kk + k2]]] % 12;
        int gi3 = this.perm[ii + 1 + this.perm[jj + 1 + this.perm[kk + 1]]] % 12;
        float t0 = 0.6F - x0 * x0 - y0 * y0 - z0 * z0;
        float n0;
        if (t0 < 0.0F) {
            n0 = 0.0F;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * this.dot(grad3[gi0], x0, y0, z0);
        }

        float t1 = 0.6F - x1 * x1 - y1 * y1 - z1 * z1;
        float n1;
        if (t1 < 0.0F) {
            n1 = 0.0F;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * this.dot(grad3[gi1], x1, y1, z1);
        }

        float t2 = 0.6F - x2 * x2 - y2 * y2 - z2 * z2;
        float n2;
        if (t2 < 0.0F) {
            n2 = 0.0F;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * this.dot(grad3[gi2], x2, y2, z2);
        }

        float t3 = 0.6F - x3 * x3 - y3 * y3 - z3 * z3;
        float n3;
        if (t3 < 0.0F) {
            n3 = 0.0F;
        } else {
            t3 *= t3;
            n3 = t3 * t3 * this.dot(grad3[gi3], x3, y3, z3);
        }

        return 32.0F * (n0 + n1 + n2 + n3);
    }

    public float rawNoise4D(float x, float y, float z, float w) {
        float F4 = (Mathf.sqrt(5.0F) - 1.0F) / 4.0F;
        float G4 = (5.0F - Mathf.sqrt(5.0F)) / 20.0F;
        float s = (x + y + z + w) * F4;
        int i = this.fastfloor(x + s);
        int j = this.fastfloor(y + s);
        int k = this.fastfloor(z + s);
        int l = this.fastfloor(w + s);
        float t = (float)(i + j + k + l) * G4;
        float X0 = (float)i - t;
        float Y0 = (float)j - t;
        float Z0 = (float)k - t;
        float W0 = (float)l - t;
        float x0 = x - X0;
        float y0 = y - Y0;
        float z0 = z - Z0;
        float w0 = w - W0;
        int c1 = x0 > y0 ? 32 : 0;
        int c2 = x0 > z0 ? 16 : 0;
        int c3 = y0 > z0 ? 8 : 0;
        int c4 = x0 > w0 ? 4 : 0;
        int c5 = y0 > w0 ? 2 : 0;
        int c6 = z0 > w0 ? 1 : 0;
        int c = c1 + c2 + c3 + c4 + c5 + c6;
        int i1 = simplex[c][0] >= 3 ? 1 : 0;
        int j1 = simplex[c][1] >= 3 ? 1 : 0;
        int k1 = simplex[c][2] >= 3 ? 1 : 0;
        int l1 = simplex[c][3] >= 3 ? 1 : 0;
        int i2 = simplex[c][0] >= 2 ? 1 : 0;
        int j2 = simplex[c][1] >= 2 ? 1 : 0;
        int k2 = simplex[c][2] >= 2 ? 1 : 0;
        int l2 = simplex[c][3] >= 2 ? 1 : 0;
        int i3 = simplex[c][0] >= 1 ? 1 : 0;
        int j3 = simplex[c][1] >= 1 ? 1 : 0;
        int k3 = simplex[c][2] >= 1 ? 1 : 0;
        int l3 = simplex[c][3] >= 1 ? 1 : 0;
        float x1 = x0 - (float)i1 + G4;
        float y1 = y0 - (float)j1 + G4;
        float z1 = z0 - (float)k1 + G4;
        float w1 = w0 - (float)l1 + G4;
        float x2 = x0 - (float)i2 + 2.0F * G4;
        float y2 = y0 - (float)j2 + 2.0F * G4;
        float z2 = z0 - (float)k2 + 2.0F * G4;
        float w2 = w0 - (float)l2 + 2.0F * G4;
        float x3 = x0 - (float)i3 + 3.0F * G4;
        float y3 = y0 - (float)j3 + 3.0F * G4;
        float z3 = z0 - (float)k3 + 3.0F * G4;
        float w3 = w0 - (float)l3 + 3.0F * G4;
        float x4 = x0 - 1.0F + 4.0F * G4;
        float y4 = y0 - 1.0F + 4.0F * G4;
        float z4 = z0 - 1.0F + 4.0F * G4;
        float w4 = w0 - 1.0F + 4.0F * G4;
        int ii = i & 255;
        int jj = j & 255;
        int kk = k & 255;
        int ll = l & 255;
        int gi0 = this.perm[ii + this.perm[jj + this.perm[kk + this.perm[ll]]]] % 32;
        int gi1 = this.perm[ii + i1 + this.perm[jj + j1 + this.perm[kk + k1 + this.perm[ll + l1]]]] % 32;
        int gi2 = this.perm[ii + i2 + this.perm[jj + j2 + this.perm[kk + k2 + this.perm[ll + l2]]]] % 32;
        int gi3 = this.perm[ii + i3 + this.perm[jj + j3 + this.perm[kk + k3 + this.perm[ll + l3]]]] % 32;
        int gi4 = this.perm[ii + 1 + this.perm[jj + 1 + this.perm[kk + 1 + this.perm[ll + 1]]]] % 32;
        float t0 = 0.6F - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0;
        float n0;
        if (t0 < 0.0F) {
            n0 = 0.0F;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * this.dot(grad4[gi0], x0, y0, z0, w0);
        }

        float t1 = 0.6F - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1;
        float n1;
        if (t1 < 0.0F) {
            n1 = 0.0F;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * this.dot(grad4[gi1], x1, y1, z1, w1);
        }

        float t2 = 0.6F - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2;
        float n2;
        if (t2 < 0.0F) {
            n2 = 0.0F;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * this.dot(grad4[gi2], x2, y2, z2, w2);
        }

        float t3 = 0.6F - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3;
        float n3;
        if (t3 < 0.0F) {
            n3 = 0.0F;
        } else {
            t3 *= t3;
            n3 = t3 * t3 * this.dot(grad4[gi3], x3, y3, z3, w3);
        }

        float t4 = 0.6F - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4;
        float n4;
        if (t4 < 0.0F) {
            n4 = 0.0F;
        } else {
            t4 *= t4;
            n4 = t4 * t4 * this.dot(grad4[gi4], x4, y4, z4, w4);
        }

        return 27.0F * (n0 + n1 + n2 + n3 + n4);
    }

    int fastfloor(float x) {
        return x > 0.0F ? (int)x : (int)x - 1;
    }

    float dot(int[] g, float x, float y) {
        return (float)g[0] * x + (float)g[1] * y;
    }

    float dot(int[] g, float x, float y, float z) {
        return (float)g[0] * x + (float)g[1] * y + (float)g[2] * z;
    }

    float dot(int[] g, float x, float y, float z, float w) {
        return (float)g[0] * x + (float)g[1] * y + (float)g[2] * z + (float)g[3] * w;
    }
}

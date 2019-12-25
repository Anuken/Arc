package com.badlogic.gdx.math;

public class Matrix4{
    public static native void mul (float[] mata, float[] matb);
    public static native void mulVec (float[] mat, float[] vec);
    public static native void mulVec (float[] mat, float[] vecs, int offset, int numVecs, int stride);
    public static native void prj (float[] mat, float[] vec);
    public static native void prj (float[] mat, float[] vecs, int offset, int numVecs, int stride);
    public static native void rot (float[] mat, float[] vec);
    public static native void rot (float[] mat, float[] vecs, int offset, int numVecs, int stride);
    public static native boolean inv (float[] values);
    public static native float det (float[] values);
}

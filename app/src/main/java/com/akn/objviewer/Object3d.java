package com.akn.objviewer;

import android.opengl.Matrix;

public class Object3d {
    private boolean isEnabled;
    private final float[] matrix = new float[16];

    public Object3d() {
        isEnabled = true;
        Matrix.setIdentityM(matrix, 0);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public float[] getMatrix() {
        return matrix;
    }
}

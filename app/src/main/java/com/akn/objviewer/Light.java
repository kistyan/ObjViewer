package com.akn.objviewer;

import android.opengl.Matrix;

public abstract class Light {
    private final float[] matrix = new float[16];
    private float colorR, colorG, colorB;

    public Light() {
        Matrix.setIdentityM(matrix, 0);
        colorR = colorG = colorB = 1;
    }

    public float[] getMatrix() {
        return matrix;
    }

    public float getColorR() {
        return colorR;
    }

    public float getColorG() {
        return colorG;
    }

    public float getColorB() {
        return colorB;
    }

    public void setColor(float colorR, float colorB, float colorG) {
        this.colorR = colorR;
        this.colorB = colorB;
        this.colorG = colorG;
    }
}

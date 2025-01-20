package com.akn.objviewer;

public class PointLight extends Light {
    private float intensity;

    public PointLight() {
        super();
        intensity = 1;
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }
}

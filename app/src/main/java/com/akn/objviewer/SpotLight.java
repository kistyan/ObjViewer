package com.akn.objviewer;

public class SpotLight extends PointLight {
    private float angle;

    public SpotLight() {
        super();
        angle = 10;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }
}

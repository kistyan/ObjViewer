package com.akn.objviewer;

public class HitSphere {
    public interface OnHitListener {
        void onHit();
    }

    private final float[] centerPosition;
    private final float radius;
    private final OnHitListener onHitListener;

    public HitSphere(float[] centerPosition, float radius, OnHitListener onHitListener) {
        this.centerPosition = centerPosition;
        this.radius = radius;
        this.onHitListener = onHitListener;
    }

    private float dot(float[] vec1, float[] vec2) {
        return vec1[0] * vec2[0] + vec1[1] * vec2[1] + vec1[2] * vec2[2];
    }

    private float[] normalize(float[] vector) {
        float length = (float) Math.sqrt(dot(vector, vector));
        if (length > 0) {
            return new float[]{vector[0] / length, vector[1] / length, vector[2] / length};
        }
        return vector;
    }

    public void onRayCasted(float[] rayStartPosition, float[] rayDirection) {
        if (onHitListener == null)
            return;

        rayDirection = normalize(rayDirection);
        float[] sphereCenterDirection = normalize(new float[] {
                rayStartPosition[0] - centerPosition[0],
                rayStartPosition[1] - centerPosition[1],
                rayStartPosition[2] - centerPosition[2]
        });

        float a = dot(rayDirection, rayDirection),
                b = 2 * dot(rayDirection, sphereCenterDirection),
                c = dot(sphereCenterDirection, sphereCenterDirection) - radius * radius,
                discriminant = b * b - 4 * a * c;
        if (discriminant >= 0)
            onHitListener.onHit();
    }
}

package com.akn.objviewer;

public class Surface {
    private final int[] faceIndices;
    private final String material;

    public Surface(int[] faceIndices, String material) {
        this.faceIndices = faceIndices;
        this.material = material;
    }

    public int[] getFaceIndices() {
        return faceIndices;
    }

    public String getMaterial() {
        return material;
    }
}

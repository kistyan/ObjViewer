package com.akn.objviewer;

public class Model {
    private final float[] vertices, vertexNormals, textureCoordinates;
    private final Surface[] surfaces;
    private final String[] materialLibraries;

    public Model(
            float[] vertices,
            float[] vertexNormals,
            float[] textureCoordinates,
            Surface[] surfaces,
            String[] materialLibraries
    ) {
        this.vertices = vertices;
        this.vertexNormals = vertexNormals;
        this.textureCoordinates = textureCoordinates;
        this.surfaces = surfaces;
        this.materialLibraries = materialLibraries;
    }

    public float[] getVertices() {
        return vertices;
    }

    public float[] getVertexNormals() {
        return vertexNormals;
    }

    public float[] getTextureCoordinates() {
        return textureCoordinates;
    }

    public Surface[] getSurfaces() {
        return surfaces;
    }

    public String[] getMaterialLibraries() {
        return materialLibraries;
    }
}

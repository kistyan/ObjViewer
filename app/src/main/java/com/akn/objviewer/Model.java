package com.akn.objviewer;

public class Model {
    private final float[] vertices, vertexNormals, textureCoordinates, faceTangents;
    private final Surface[] surfaces;
    private final String[] materialLibraries;

    public Model(
            float[] vertices,
            float[] vertexNormals,
            float[] textureCoordinates,
            float[] faceTangents,
            Surface[] surfaces,
            String[] materialLibraries
    ) {
        this.vertices = vertices;
        this.vertexNormals = vertexNormals;
        this.textureCoordinates = textureCoordinates;
        this.faceTangents = faceTangents;
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

    public float[] getFaceTangents() {
        return faceTangents;
    }
}

package com.akn.objviewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ObjParser {
    public static Model parse(String source) {
        ArrayList<Float[]> vertices = new ArrayList<>(),
                vertexNormals = new ArrayList<>(),
                textureCoordinates = new ArrayList<>();
        HashMap<String, ArrayList<Integer[]>> surfaces = new HashMap<>();
        HashMap<FloatArrayKey, Integer> points = new HashMap<>();
        ArrayList<String> materialLibraries = new ArrayList<>();
        String material = null;
        String[] lines = source.split("\n");
        for (String line : lines) {
            String[] tokens = line.split("\\s+");
            if (tokens.length == 0)
                continue;
            switch (tokens[0]) {
                case "v":
                    vertices.add(new Float[]{
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    });
                    break;
                case "vn":
                    vertexNormals.add(new Float[]{
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    });
                    break;
                case "vt":
                    textureCoordinates.add(new Float[]{
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2])
                    });
                    break;
                case "f":
                    for (int tokenIndex = 1; tokenIndex < tokens.length; tokenIndex++) {
                        String[] indexStrings = tokens[tokenIndex].split("/");
                        Integer[] indices = new Integer[]{
                                Integer.parseInt(indexStrings[0]) - 1,
                                indexStrings[1].isEmpty() ? null :
                                        Integer.parseInt(indexStrings[1]) - 1,
                                indexStrings[2].isEmpty() ? null :
                                        Integer.parseInt(indexStrings[2]) - 1
                        };
                        ArrayList<Integer[]> indicesList
                                = surfaces.computeIfAbsent(material, key -> new ArrayList<>());
                        indicesList.add(indices);
                    }
                    break;
                case "mtllib":
                    materialLibraries.add(tokens[1]);
                    break;
                case "usemtl":
                    material = tokens[1];
                    break;
            }
        }
        Surface[] surfaceArray = new Surface[surfaces.size()];
        int surfaceIndex = 0;
        for (Map.Entry<String, ArrayList<Integer[]>> surface : surfaces.entrySet()) {
            ArrayList<Integer[]> faceIndices = surface.getValue();
            int[] faceIndexArray = new int[faceIndices.size()];
            for (int faceIndexIndex = 0; faceIndexIndex < faceIndexArray.length; faceIndexIndex++) {
                Integer[] faceIndex = faceIndices.get(faceIndexIndex);
                FloatArrayKey indexKey = new FloatArrayKey(new Float[][]{
                        vertices.get(faceIndex[0]),
                        faceIndex[1] == null ? null : textureCoordinates.get(faceIndex[1]),
                        faceIndex[2] == null ? null : vertexNormals.get(faceIndex[2])
                });
                faceIndexArray[faceIndexIndex]
                        = points.computeIfAbsent(indexKey, key -> points.size());
            }
            surfaceArray[surfaceIndex++] = new Surface(faceIndexArray, surface.getKey());
        }
        float[] vertexArray = new float[points.size() * 3],
                vertexNormalArray = new float[points.size() * 3],
                textureCoordinateArray = new float[points.size() * 2];
        for (Map.Entry<FloatArrayKey, Integer> point : points.entrySet()) {
            Float[][] values = point.getKey().getArray();
            int offset = point.getValue();
            for (int vertexIndex = 0; vertexIndex < 3; vertexIndex++)
                vertexArray[offset * 3 + vertexIndex] = values[0][vertexIndex];
            if (values[2] != null)
                for (int vertexNormalIndex = 0; vertexNormalIndex < 3; vertexNormalIndex++)
                    vertexNormalArray[offset * 3 + vertexNormalIndex] = values[2][vertexNormalIndex];
            if (values[1] != null) {
                textureCoordinateArray[offset * 2] = values[1][0];
                textureCoordinateArray[offset * 2 + 1] = values[1][1];
            }
        }
        return new Model(
                vertexArray,
                vertexNormalArray,
                textureCoordinateArray,
                surfaceArray,
                materialLibraries.toArray(new String[0])
        );
    }
}

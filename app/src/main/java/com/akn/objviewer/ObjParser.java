package com.akn.objviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ObjParser {
    private static void normalizeVector(Float[] vector) {
        if (vector.length == 0)
            return;
        float maxAbsoluteValue = vector[0];
        for (int valueIndex = 1; valueIndex < vector.length; valueIndex++) {
            float absoluteValue = Math.abs(vector[valueIndex]);
            if (absoluteValue > maxAbsoluteValue)
                maxAbsoluteValue = absoluteValue;
        }
        for (int valueIndex = 0; valueIndex < vector.length; valueIndex++)
            vector[valueIndex] /= maxAbsoluteValue;
    }

    public static Model parse(String source) {
        ArrayList<Float[]> vertices = new ArrayList<>(),
                vertexNormals = new ArrayList<>(),
                textureCoordinates = new ArrayList<>();
        HashMap<String, ArrayList<Integer[][]>> surfaces = new HashMap<>();
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
                    Integer[][] triangle = new Integer[3][3];
                    for (int pointIndex = 0; pointIndex < 3; pointIndex++) {
                        String[] indexStrings = tokens[pointIndex + 1].split("/");
                        triangle[pointIndex] = new Integer[]{
                                Integer.parseInt(indexStrings[0]) - 1,
                                indexStrings.length < 2 || indexStrings[1].isEmpty() ? null :
                                        Integer.parseInt(indexStrings[1]) - 1,
                                indexStrings.length < 3 || indexStrings[2].isEmpty() ? null :
                                        Integer.parseInt(indexStrings[2]) - 1
                        };
                    }
                    ArrayList<Integer[][]> triangleList
                            = surfaces.computeIfAbsent(material, key -> new ArrayList<>());
                    triangleList.add(triangle);
                    break;
                case "mtllib":
                    materialLibraries.add(tokens[1]);
                    break;
                case "usemtl":
                    material = tokens[1];
                    break;
            }
        }
        HashMap<FloatArrayKey, Integer> points = new HashMap<>();
        Surface[] surfaceArray = new Surface[surfaces.size()];
        int surfaceIndex = 0;
        for (Map.Entry<String, ArrayList<Integer[][]>> surface : surfaces.entrySet()) {
            ArrayList<Integer[][]> triangles = surface.getValue();
            int[] faceIndexArray = new int[triangles.size() * 3];
            for (int triangleIndex = 0; triangleIndex < triangles.size(); triangleIndex++) {
                Integer[][] triangle = triangles.get(triangleIndex);

                Float[] faceTangent;
                float[] edge1 = new float[]{
                        vertices.get(triangle[1][0])[0] - vertices.get(triangle[0][0])[0],
                        vertices.get(triangle[1][0])[1] - vertices.get(triangle[0][0])[1],
                        vertices.get(triangle[1][0])[2] - vertices.get(triangle[0][0])[2]
                }, edge2 = new float[]{
                        vertices.get(triangle[2][0])[0] - vertices.get(triangle[0][0])[0],
                        vertices.get(triangle[2][0])[1] - vertices.get(triangle[0][0])[1],
                        vertices.get(triangle[2][0])[2] - vertices.get(triangle[0][0])[2]
                };
                if (triangle[0][1] != null && triangle[1][1] != null && triangle[1][2] != null) {
                    float[] deltaUV1 = new float[]{
                            textureCoordinates.get(triangle[1][1])[0]
                                    - textureCoordinates.get(triangle[0][0])[0],
                            textureCoordinates.get(triangle[1][1])[1]
                                    - textureCoordinates.get(triangle[0][0])[1]
                    }, deltaUV2 = new float[]{
                            textureCoordinates.get(triangle[2][1])[0]
                                    - textureCoordinates.get(triangle[0][0])[0],
                            textureCoordinates.get(triangle[2][1])[1]
                                    - textureCoordinates.get(triangle[0][0])[1]
                    };
                    float f = 1 / (deltaUV1[0] * deltaUV2[1] - deltaUV2[0] * deltaUV1[1]);
                    faceTangent = new Float[]{
                            f * (deltaUV2[1] * edge1[0] - deltaUV1[1] * edge2[0]),
                            f * (deltaUV2[1] * edge1[1] - deltaUV1[1] * edge2[1]),
                            f * (deltaUV2[1] * edge1[2] - deltaUV1[1] * edge2[2])
                    };
                    normalizeVector(faceTangent);
                }
                else
                    faceTangent = new Float[]{
                            edge1[0] - edge2[0],
                            edge1[1] - edge2[1],
                            edge1[2] - edge2[2],
                    };
                for (int pointIndex = 0; pointIndex < 3; pointIndex++) {
                    Integer[] point = triangle[pointIndex];
                    FloatArrayKey indexKey = new FloatArrayKey(new Float[][]{
                            vertices.get(point[0]),
                            point[1] == null ? null : textureCoordinates.get(point[1]),
                            point[2] == null ? null : vertexNormals.get(point[2]),
                            faceTangent
                    });
                    faceIndexArray[triangleIndex * 3 + pointIndex]
                            = points.computeIfAbsent(indexKey, key -> points.size());
                }
            }
            surfaceArray[surfaceIndex++] = new Surface(faceIndexArray, surface.getKey());
        }
        float[] vertexArray = new float[points.size() * 3],
                vertexNormalArray = new float[points.size() * 3],
                textureCoordinateArray = new float[points.size() * 2],
                faceTangentArray = new float[points.size() * 3];
        for (Map.Entry<FloatArrayKey, Integer> point : points.entrySet()) {
            Float[][] values = point.getKey().getArray();
            int offset = point.getValue();
            for (int vertexIndex = 0; vertexIndex < 3; vertexIndex++)
                vertexArray[offset * 3 + vertexIndex] = values[0][vertexIndex];
            if (values[1] != null) {
                textureCoordinateArray[offset * 2] = values[1][0];
                textureCoordinateArray[offset * 2 + 1] = values[1][1];
            }
            if (values[2] != null)
                for (int vertexNormalIndex = 0; vertexNormalIndex < 3; vertexNormalIndex++)
                    vertexNormalArray[offset * 3 + vertexNormalIndex] = values[2][vertexNormalIndex];
            if (values[3] != null)
                for (int faceTangentIndex = 0; faceTangentIndex < 3; faceTangentIndex++)
                    faceTangentArray[offset * 3 + faceTangentIndex] = values[3][faceTangentIndex];
        }
        return new Model(
                vertexArray,
                vertexNormalArray,
                textureCoordinateArray,
                faceTangentArray,
                surfaceArray,
                materialLibraries.toArray(new String[0])
        );
    }
}

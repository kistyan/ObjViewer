package com.akn.objviewer;

import java.util.HashMap;
import java.util.Objects;

public class MtlParser {
    public static HashMap<String, Material> parse(String source) {
        HashMap<String, Material> materials = new HashMap<>();
        String name = null;
        Material material = null;
        String[] lines = source.split("\n");
        for (String line : lines) {
            String[] tokens = line.split("\\s+");
            if (tokens.length == 0 || material == null && !Objects.equals(tokens[0], "newmtl"))
                continue;
            switch (tokens[0]) {
                case "newmtl":
                    if (material != null)
                        materials.put(name, material);
                    name = tokens[1];
                    material = new Material();
                    break;
                case "Kd":
                    material.setDiffuseColor(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    );
                    break;
                case "Ka":
                    material.setAmbientColor(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    );
                    break;
                case "Ks":
                    material.setSpecularColor(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    );
                    break;
                case "d":
                    material.setDissolve(Float.parseFloat(tokens[1]));
                    break;
                case "Tr":
                    material.setDissolve(1 - Float.parseFloat(tokens[1]));
                    break;
                case "Ns":
                    material.setSpecularHighlights(Float.parseFloat(tokens[1]));
                    break;
                case "Ni":
                    material.setOpticalDensity(Float.parseFloat(tokens[1]));
                    break;
                case "map_Kd":
                    material.setDiffuseTexture(tokens[1]);
                    break;
                case "map_Ka":
                    material.setAmbientTexture(tokens[1]);
                    break;
                case "map_Ks":
                    material.setSpecularTexture(tokens[1]);
                    break;
                case "map_d":
                    material.setDissolveTexture(tokens[1]);
                    break;
            }
        }
        if (material != null)
            materials.put(name, material);
        return materials;
    }
}

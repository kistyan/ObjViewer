package com.akn.objviewer;

public class Material {
    private float diffuseColorR, diffuseColorG, diffuseColorB,
            ambientColorR, ambientColorG, ambientColorB,
            specularColorR, specularColorG, specularColorB;
    private float dissolve, specularHighlights, opticalDensity;
    private String diffuseTexture, ambientTexture, specularTexture, dissolveTexture;

    public Material() {
        diffuseColorR = diffuseColorG = diffuseColorB = 1;
        specularColorR = specularColorG = specularColorB = 1;
        dissolve = 1;
    }

    public float getDiffuseColorR() {
        return diffuseColorR;
    }

    public float getDiffuseColorG() {
        return diffuseColorG;
    }

    public float getDiffuseColorB() {
        return diffuseColorB;
    }

    public void setDiffuseColor(float diffuseColorR, float diffuseColorG, float diffuseColorB) {
        this.diffuseColorR = diffuseColorR;
        this.diffuseColorG = diffuseColorG;
        this.diffuseColorB = diffuseColorB;
    }

    public float getAmbientColorR() {
        return ambientColorR;
    }

    public float getAmbientColorG() {
        return ambientColorG;
    }

    public float getAmbientColorB() {
        return ambientColorB;
    }

    public void setAmbientColor(float ambientColorR, float ambientColorG, float ambientColorB) {
        this.ambientColorR = ambientColorR;
        this.ambientColorG = ambientColorG;
        this.ambientColorB = ambientColorB;
    }

    public float getSpecularColorR() {
        return specularColorR;
    }

    public float getSpecularColorG() {
        return specularColorG;
    }

    public float getSpecularColorB() {
        return specularColorB;
    }

    public void setSpecularColor(float specularColorR, float specularColorG, float specularColorB) {
        this.specularColorR = specularColorR;
        this.specularColorG = specularColorG;
        this.specularColorB = specularColorB;
    }

    public float getDissolve() {
        return dissolve;
    }

    public void setDissolve(float dissolve) {
        this.dissolve = dissolve;
    }

    public float getSpecularHighlights() {
        return specularHighlights;
    }

    public void setSpecularHighlights(float specularHighlights) {
        this.specularHighlights = specularHighlights;
    }

    public float getOpticalDensity() {
        return opticalDensity;
    }

    public void setOpticalDensity(float opticalDensity) {
        this.opticalDensity = opticalDensity;
    }

    public String getDiffuseTexture() {
        return diffuseTexture;
    }

    public void setDiffuseTexture(String diffuseTexture) {
        this.diffuseTexture = diffuseTexture;
    }

    public String getAmbientTexture() {
        return ambientTexture;
    }

    public void setAmbientTexture(String ambientTexture) {
        this.ambientTexture = ambientTexture;
    }

    public String getSpecularTexture() {
        return specularTexture;
    }

    public void setSpecularTexture(String specularTexture) {
        this.specularTexture = specularTexture;
    }

    public String getDissolveTexture() {
        return dissolveTexture;
    }

    public void setDissolveTexture(String dissolveTexture) {
        this.dissolveTexture = dissolveTexture;
    }
}

package com.akn.objviewer;

import static android.opengl.GLES30.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Renderer implements GLSurfaceView.Renderer {
    private static final float DEFAULT_SCALE = 1,
            MIN_SCALE = 0.5f,
            MAX_SCALE = 2,
            DEFAULT_X_ROTATION = -45,
            DEFAULT_Y_ROTATION = -45,
            DEFAULT_TRANSLATION_Z = 4;
    private static final int POINT_LIGHT_COUNT = 1,
            SPOT_LIGHT_COUNT = 0,
            DIRECTIONAL_LIGHT_COUNT = 0;

    private final Context context;
    private final Model model;
    private final HashMap<String, Material> materials;
    private final HashMap<String, Bitmap> textures;
    private final HashMap<String, Integer> textureIds;

    private Material defaultMaterial;
    private int defaultTextureId, defaultNormalTextureId;

    private float scale, rotationX, rotationY, translationZ;
    private float viewXFactor, viewYFactor;

    private int modelShaders, aPositionLocation, aVertexNormalLocation, aTextureCoordinateLocation,
            aFaceTangentLocation, uModelMatrixLocation, uModelViewProjectionMatrixLocation,
            uNormalMatrixLocation, uCameraPositionLocation;
    private final PointLightLocation[] uPointLightLocations
            = new PointLightLocation[POINT_LIGHT_COUNT];
    private final SpotLightLocation[] uSpotLightLocations = new SpotLightLocation[SPOT_LIGHT_COUNT];
    private final DirectionalLightLocation[] uDirectionalLightLocations
            = new DirectionalLightLocation[DIRECTIONAL_LIGHT_COUNT];
    private MaterialLocation uMaterialLocation;
    private final float[] projectionMatrix = new float[16],
            viewMatrix = new float[16],
            modelMatrix = new float[16];

    private final PointLight[] pointLighting = new PointLight[POINT_LIGHT_COUNT];
    private final SpotLight[] spotLighting = new SpotLight[SPOT_LIGHT_COUNT];
    private final DirectionalLight[] directionalLighting
            = new DirectionalLight[DIRECTIONAL_LIGHT_COUNT];

    public Renderer(
            Context context,
            Model model,
            HashMap<String, Material> materials,
            HashMap<String, Bitmap> textures
    ) {
        this.context = context;
        this.model = model;
        this.materials = materials;
        this.textures = textures;
        textureIds = new HashMap<>();
    }

    private int bindShader(int type, String source) {
        int shaderId = glCreateShader(type);
        glShaderSource(shaderId, source);
        glCompileShader(shaderId);
        return shaderId;
    }

    private void bindModelLocations(int programId) {
        aPositionLocation = glGetAttribLocation(programId, "a_Position");
        aVertexNormalLocation = glGetAttribLocation(programId, "a_VertexNormal");
        aTextureCoordinateLocation = glGetAttribLocation(programId, "a_TextureCoordinate");
        aFaceTangentLocation = glGetAttribLocation(programId, "a_FaceTangent");
    }

    private void bindMatrixLocations(int programId) {
        uModelViewProjectionMatrixLocation
                = glGetUniformLocation(programId, "u_ModelViewProjectionMatrix");
        uModelMatrixLocation = glGetUniformLocation(programId, "u_ModelMatrix");
        uNormalMatrixLocation = glGetUniformLocation(programId, "u_NormalMatrix");
    }

    private void bindCameraLocations(int programId) {
        uCameraPositionLocation = glGetUniformLocation(programId, "u_CameraPosition");
    }

    private void bindMaterialLocations(int programId) {
        uMaterialLocation = new MaterialLocation();
        uMaterialLocation.ambientColor = glGetUniformLocation(programId, "u_Material.ambientColor");
        uMaterialLocation.diffuseColor = glGetUniformLocation(programId, "u_Material.diffuseColor");
        uMaterialLocation.specularColor
                = glGetUniformLocation(programId, "u_Material.specularColor");
        uMaterialLocation.dissolve = glGetUniformLocation(programId, "u_Material.dissolve");
        uMaterialLocation.specularHighlights
                = glGetUniformLocation(programId, "u_Material.specularHighlights");
        uMaterialLocation.ambientTexture
                = glGetUniformLocation(programId, "u_Material.ambientTexture");
        uMaterialLocation.diffuseTexture
                = glGetUniformLocation(programId, "u_Material.diffuseTexture");
        uMaterialLocation.specularTexture
                = glGetUniformLocation(programId, "u_Material.specularTexture");
        uMaterialLocation.dissolveTexture
                = glGetUniformLocation(programId, "u_Material.dissolveTexture");
        uMaterialLocation.normalTexture
                = glGetUniformLocation(programId, "u_Material.normalTexture");
    }

    private void bindPointLightLocations(int programId, int pointLightIndex) {
        PointLightLocation pointLightLocation = new PointLightLocation();
        pointLightLocation.color = glGetUniformLocation(programId, String.format(
                Locale.US,
                "u_PointLighting[%d].color",
                pointLightIndex
        ));
        pointLightLocation.position = glGetUniformLocation(programId, String.format(
                Locale.US,
                "u_PointLighting[%d].position",
                pointLightIndex
        ));
        pointLightLocation.intensity = glGetUniformLocation(programId, String.format(
                Locale.US,
                "u_PointLighting[%d].intensity",
                pointLightIndex
        ));
        uPointLightLocations[pointLightIndex] = pointLightLocation;
    }

    private void bindSpotLightLocations(int programId, int spotLightIndex) {
        SpotLightLocation spotLightLocation = new SpotLightLocation();
        spotLightLocation.color = glGetUniformLocation(programId, String.format(
                Locale.US,
                "u_SpotLighting[%d].color",
                spotLightIndex
        ));
        spotLightLocation.position = glGetUniformLocation(programId, String.format(
                Locale.US,
                "u_SpotLighting[%d].position",
                spotLightIndex
        ));
        spotLightLocation.direction = glGetUniformLocation(programId, String.format(
                Locale.US,
                "u_SpotLighting[%d].direction",
                spotLightIndex
        ));
        spotLightLocation.intensity = glGetUniformLocation(programId, String.format(
                Locale.US,
                "u_SpotLighting[%d].intensity",
                spotLightIndex
        ));
        spotLightLocation.angle = glGetUniformLocation(programId, String.format(
                Locale.US,
                "u_SpotLighting[%d].angle",
                spotLightIndex
        ));
        uSpotLightLocations[spotLightIndex] = spotLightLocation;
    }

    private void bindDirectionalLightLocations(int programId, int directionalLightIndex) {
        DirectionalLightLocation directionalLightLocation = new DirectionalLightLocation();
        directionalLightLocation.color = glGetUniformLocation(programId, String.format(
                Locale.US,
                "u_DirectionalLighting[%d].color",
                directionalLightIndex
        ));
        directionalLightLocation.direction = glGetUniformLocation(programId, String.format(
                Locale.US,
                "u_DirectionalLighting[%d].direction",
                directionalLightIndex
        ));
        uDirectionalLightLocations[directionalLightIndex] = directionalLightLocation;
    }

    private void bindLightLocations(int programId) {
        for (int pointLightIndex = 0; pointLightIndex < POINT_LIGHT_COUNT; pointLightIndex++)
            bindPointLightLocations(programId, pointLightIndex);
        for (int spotLightIndex = 0; spotLightIndex < SPOT_LIGHT_COUNT; spotLightIndex++)
            bindSpotLightLocations(programId, spotLightIndex);
        for (
                int directionalLightIndex = 0;
                directionalLightIndex < DIRECTIONAL_LIGHT_COUNT;
                directionalLightIndex++
        )
            bindDirectionalLightLocations(programId, directionalLightIndex);
    }

    private int loadShaders() {
        try {
            int vertexShaderId = bindShader(GL_VERTEX_SHADER, IOUtils.toString(
                    context.getResources().openRawResource(R.raw.vertex_shader),
                    StandardCharsets.UTF_8
            )), fragmentShaderId = bindShader(GL_FRAGMENT_SHADER, String.format(
                    Locale.US,
                    IOUtils.toString(
                            context.getResources().openRawResource(R.raw.fragment_shader),
                            StandardCharsets.UTF_8
                    ),
                    POINT_LIGHT_COUNT,
                    SPOT_LIGHT_COUNT,
                    DIRECTIONAL_LIGHT_COUNT
            )), programId = glCreateProgram();
            glAttachShader(programId, vertexShaderId);
            glAttachShader(programId, fragmentShaderId);
            glLinkProgram(programId);
            return programId;
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public int loadTexture(Bitmap texture) {
        int[] textureIdPointer = new int[1];
        glGenTextures(1, textureIdPointer, 0);
        glBindTexture(GL_TEXTURE_2D, textureIdPointer[0]);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        GLUtils.texImage2D(GL_TEXTURE_2D, 0, texture, 0);
        glGenerateMipmap(GL_TEXTURE_2D);

        return textureIdPointer[0];
    }

    private void loadTextures() {
        Bitmap defaultTexture = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
        defaultTexture.setPixel(0, 0, Color.WHITE);
        defaultTextureId = loadTexture(defaultTexture);
        defaultTexture.setPixel(0, 0, 0x80_80_ff);
        defaultNormalTextureId = loadTexture(defaultTexture);
        defaultTexture.recycle();
        for (String textureName : textures.keySet())
            textureIds.put(
                    textureName,
                    loadTexture(textures.get(textureName))
            );
    }

    private void calculateModelMatrix() {
        float[] center = new float[3], vertices = model.getVertices();
        for (int vertexStartIndex = 0; vertexStartIndex < vertices.length; vertexStartIndex += 3) {
            center[0] += vertices[vertexStartIndex];
            center[1] += vertices[vertexStartIndex + 1];
            center[2] += vertices[vertexStartIndex + 2];
        }
        int vertexCount = vertices.length / 3;
        for (int asixIndex = 0; asixIndex < 3; asixIndex++)
            center[asixIndex] /= vertexCount;
        float maxRadius = 0;
        for (int vertexStartIndex = 0; vertexStartIndex < vertices.length; vertexStartIndex += 3) {
            float deltaX = vertices[vertexStartIndex] - center[0],
                    deltaY = vertices[vertexStartIndex + 1] - center[1],
                    deltaZ = vertices[vertexStartIndex + 2] - center[2],
                    radius = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
            if (radius > maxRadius)
                maxRadius = radius;
        }
        float scale = 1 / maxRadius;
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.scaleM(modelMatrix, 0, scale, scale, scale);
        Matrix.translateM(modelMatrix, 0, -center[0], -center[1], -center[2]);
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        scale = Math.max(MIN_SCALE, Math.min(scale, MAX_SCALE));
        this.scale = scale;
    }

    public float getRotationX() {
        return rotationX;
    }

    public float getRotationY() {
        return rotationY;
    }

    public float getViewXFactor() {
        return viewXFactor;
    }

    public float getViewYFactor() {
        return viewYFactor;
    }

    public float getTranslationZ() {
        return translationZ;
    }

    public void setRotation(float rotationX, float rotationY) {
        rotationX = (rotationX + 180) % 360 - 180;
        rotationY = (rotationY + 180) % 360 - 180;
        rotationX = Math.max(-90, Math.min(rotationX, 90));
        this.rotationX = rotationX;
        this.rotationY = rotationY;
    }

    public void setTranslationZ(float translationZ) {
        this.translationZ = translationZ;
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, translationZ, 0, 0, 0, 0, 1, 0);
    }

    private FloatBuffer getBuffer(float[] array) {
        FloatBuffer buffer = ByteBuffer
                .allocateDirect(array.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buffer.put(array);
        buffer.position(0);
        return buffer;
    }

    private IntBuffer getBuffer(int[] array) {
        IntBuffer buffer = ByteBuffer
                .allocateDirect(array.length * 4)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer()
                .put(array);
        buffer.position(0);
        return buffer;
    }

    private void loadVertices() {
        glEnableVertexAttribArray(aPositionLocation);
        glVertexAttribPointer(
                aPositionLocation,
                3,
                GL_FLOAT,
                false,
                0,
                getBuffer(model.getVertices())
        );
    }

    private void loadVertexNormals() {
        glEnableVertexAttribArray(aVertexNormalLocation);
        glVertexAttribPointer(
                aVertexNormalLocation,
                3,
                GL_FLOAT,
                false,
                0,
                getBuffer(model.getVertexNormals())
        );
    }

    private void loadTextureCoordinates() {
        glEnableVertexAttribArray(aTextureCoordinateLocation);
        glVertexAttribPointer(
                aTextureCoordinateLocation,
                2,
                GL_FLOAT,
                false,
                0,
                getBuffer(model.getTextureCoordinates())
        );
    }

    private void loadFaceTangents() {
        glEnableVertexAttribArray(aFaceTangentLocation);
        glVertexAttribPointer(
                aFaceTangentLocation,
                3,
                GL_FLOAT,
                false,
                0,
                getBuffer(model.getFaceTangents())
        );
    }

    private void placeLighting() {
        PointLight pointLight = new PointLight();
        pointLight.setIntensity(10);
        pointLighting[0] = pointLight;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        glEnable(GL10.GL_MULTISAMPLE);
        glClearColor(0, 0, 0, 1);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        modelShaders = loadShaders();
        bindLightLocations(modelShaders);
        bindCameraLocations(modelShaders);
        bindModelLocations(modelShaders);
        bindMatrixLocations(modelShaders);
        bindMaterialLocations(modelShaders);

        defaultMaterial = new Material();
        loadTextures();

        setScale(DEFAULT_SCALE);
        setTranslationZ(DEFAULT_TRANSLATION_Z);
        calculateModelMatrix();
        setRotation(DEFAULT_X_ROTATION, DEFAULT_Y_ROTATION);

        placeLighting();
    }

    private int getTextureId(String textureName, int defaultTextureId) {
        Integer textureId = textureIds.get(textureName);
        if (textureId == null)
            textureId = defaultTextureId;
        return textureId;
    }

    private void loadMaterial(Material material) {
        glUniform3f(
                uMaterialLocation.ambientColor,
                material.getAmbientColorR(),
                material.getAmbientColorG(),
                material.getAmbientColorB()
        );
        glUniform3f(
                uMaterialLocation.diffuseColor,
                material.getDiffuseColorR(),
                material.getDiffuseColorG(),
                material.getDiffuseColorB()
        );
        glUniform3f(
                uMaterialLocation.specularColor,
                material.getSpecularColorR(),
                material.getSpecularColorG(),
                material.getSpecularColorB()
        );
        glUniform1f(uMaterialLocation.dissolve, material.getDissolve());
        glUniform1f(uMaterialLocation.specularHighlights, material.getSpecularHighlights());
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, getTextureId(material.getAmbientTexture(), defaultTextureId));
        glUniform1i(uMaterialLocation.ambientTexture, 0);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, getTextureId(material.getDiffuseTexture(), defaultTextureId));
        glUniform1i(uMaterialLocation.diffuseTexture, 1);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, getTextureId(material.getSpecularTexture(), defaultTextureId));
        glUniform1i(uMaterialLocation.specularTexture, 2);
        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D, getTextureId(material.getDissolveTexture(), defaultTextureId));
        glUniform1i(uMaterialLocation.dissolveTexture, 3);
        glActiveTexture(GL_TEXTURE4);
        glBindTexture(
                GL_TEXTURE_2D,
                getTextureId(material.getNormalTexture(), defaultNormalTextureId)
        );
        glUniform1i(uMaterialLocation.normalTexture, 4);
    }

    private void loadMaterial(String materialName) {
        Material material = materials.get(materialName);
        if (material == null)
            material = defaultMaterial;
        loadMaterial(material);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        glViewport(0, 0, width, height);
        Matrix.perspectiveM(projectionMatrix, 0, 90, (float) width / height, 0.1f, 100f);
        float[] invertedProjectionMatrix = new float[16],
                screenCoordinates = new float[]{2f / width, 2f / height, 0, 1},
                viewCoordinateFactor = new float[4];
        Matrix.invertM(invertedProjectionMatrix, 0, projectionMatrix, 0);
        Matrix.multiplyMV(
                viewCoordinateFactor, 0,
                invertedProjectionMatrix, 0,
                screenCoordinates, 0
        );
        viewXFactor = viewCoordinateFactor[0];
        viewYFactor = viewCoordinateFactor[1];
    }

    private void updateCameraMatrix(float[] cameraMatrix) {
        Matrix.setIdentityM(cameraMatrix, 0);
        Matrix.rotateM(cameraMatrix, 0, -rotationY, 0, 1, 0);
        Matrix.rotateM(cameraMatrix, 0, rotationX, 1, 0, 0);
        Matrix.translateM(cameraMatrix, 0, 0, 0, translationZ);
    }

    private void loadCamera() {
        float[] cameraPosition = new float[]{0, 0, 0, 1},
                cameraMatrix = new float[16];
        updateCameraMatrix(cameraMatrix);
        Matrix.multiplyMV(cameraPosition, 0, cameraMatrix, 0, cameraPosition, 0);
        glUniform3f(
                uCameraPositionLocation,
                cameraPosition[0],
                cameraPosition[1],
                cameraPosition[2]
        );
    }

    private void loadLight(LightLocation lightLocation, Light light) {
        glUniform3f(
                lightLocation.color,
                light.getColorR(),
                light.getColorG(),
                light.getColorB()
        );
    }

    private void loadPointLight(PointLightLocation pointLightLocation, PointLight pointLight) {
        loadLight(pointLightLocation, pointLight);
        float[] lightPosition = new float[]{0, 0, 0, 1};
        Matrix.multiplyMV(lightPosition, 0, pointLight.getMatrix(), 0, lightPosition, 0);
        glUniform3f(
                pointLightLocation.position,
                lightPosition[0],
                lightPosition[1],
                lightPosition[2]
        );
        glUniform1f(pointLightLocation.intensity, pointLight.getIntensity());
    }

    private void loadSpotLight(SpotLightLocation spotLightLocation, SpotLight spotLight) {
        loadPointLight(spotLightLocation, spotLight);
        float[] lightDirection = new float[]{0, 0, 1, 0};
        Matrix.multiplyMV(lightDirection, 0, spotLight.getMatrix(), 0, lightDirection, 0);
        glUniform3f(
                spotLightLocation.direction,
                lightDirection[0],
                lightDirection[1],
                lightDirection[2]
        );
        glUniform1f(spotLightLocation.angle, (float) Math.toRadians(spotLight.getAngle()));
    }

    private void loadDirectionalLight(
            DirectionalLightLocation directionalLightLocation,
            DirectionalLight directionalLight
    ) {
        loadLight(directionalLightLocation, directionalLight);
        float[] lightDirection = new float[]{0, 0, 1, 0};
        Matrix.multiplyMV(lightDirection, 0, directionalLight.getMatrix(), 0, lightDirection, 0);
        glUniform3f(
                directionalLightLocation.direction,
                lightDirection[0],
                lightDirection[1],
                lightDirection[2]
        );
    }

    private void loadLighting() {
        for (int pointLightIndex = 0; pointLightIndex < POINT_LIGHT_COUNT; pointLightIndex++)
            loadPointLight(
                    uPointLightLocations[pointLightIndex],
                    pointLighting[pointLightIndex]
            );
        for (int spotLightIndex = 0; spotLightIndex < SPOT_LIGHT_COUNT; spotLightIndex++)
            loadSpotLight(
                    uSpotLightLocations[spotLightIndex],
                    spotLighting[spotLightIndex]
            );
        for (
                int directionalLightIndex = 0;
                directionalLightIndex < DIRECTIONAL_LIGHT_COUNT;
                directionalLightIndex++
        )
            loadDirectionalLight(
                    uDirectionalLightLocations[directionalLightIndex],
                    directionalLighting[directionalLightIndex]
            );
    }

    private void loadMatrices() {
        glUniformMatrix4fv(uModelMatrixLocation, 1, false, modelMatrix, 0);
        float[] normalMatrix = new float[9];
        Matrix3x3.from4x4(normalMatrix, modelMatrix);
        Matrix3x3.inverse(normalMatrix);
        Matrix3x3.transpose(normalMatrix);
        glUniformMatrix3fv(uNormalMatrixLocation, 1, false, normalMatrix, 0);

        float[] modelViewProjectionMatrix = new float[16];
        Matrix.setIdentityM(modelViewProjectionMatrix, 0);
        Matrix.scaleM(modelViewProjectionMatrix, 0, scale, scale, 1);
        Matrix.multiplyMM(
                modelViewProjectionMatrix, 0,
                modelViewProjectionMatrix, 0,
                viewMatrix, 0
        );
        Matrix.rotateM(modelViewProjectionMatrix, 0, -rotationX, 1, 0, 0);
        Matrix.rotateM(modelViewProjectionMatrix, 0, rotationY, 0, 1, 0);
        Matrix.multiplyMM(
                modelViewProjectionMatrix, 0,
                modelViewProjectionMatrix, 0,
                modelMatrix, 0
        );
        Matrix.multiplyMM(
                modelViewProjectionMatrix, 0,
                projectionMatrix, 0,
                modelViewProjectionMatrix, 0
        );
        glUniformMatrix4fv(
                uModelViewProjectionMatrixLocation,
                1,
                false,
                modelViewProjectionMatrix,
                0
        );
    }

    private void drawModel() {
        loadVertices();
        loadVertexNormals();
        loadTextureCoordinates();
        loadFaceTangents();

        for (Surface surface : model.getSurfaces()) {
            loadMaterial(surface.getMaterial());
            glDrawElements(
                    GL_TRIANGLES,
                    surface.getFaceIndices().length,
                    GL_UNSIGNED_INT,
                    getBuffer(surface.getFaceIndices())
            );
        }
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUseProgram(modelShaders);

        loadCamera();
        updateCameraMatrix(pointLighting[0].getMatrix());
        loadLighting();
        loadMatrices();
        drawModel();

        System.out.println(glGetError());
    }
}

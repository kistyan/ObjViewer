package com.akn.objviewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;

public class ViewActivity extends AppCompatActivity {
    private HashMap<String, Material> loadMaterials(DocumentFile directory, Model model)
            throws IOException {
        HashMap<String, Material> materials = new HashMap<>();
        for (String materialLibrary : model.getMaterialLibraries())
            materials.putAll(MtlParser.parse(IOUtils.toString(getContentResolver()
                            .openInputStream(directory.findFile(materialLibrary).getUri()),
                    StandardCharsets.UTF_8)));
        return materials;
    }

    private Bitmap loadTexture(DocumentFile directory, String name) throws FileNotFoundException {
        return BitmapFactory.decodeStream(getContentResolver()
                .openInputStream(directory.findFile(name).getUri()));
    }

    private HashMap<String, Bitmap> loadTextures(
            DocumentFile directory,
            HashMap<String, Material> materials
    ) throws FileNotFoundException {
        HashMap<String, Bitmap> textures = new HashMap<>();
        for (Material material : materials.values()) {
            String[] names = new String[]{
                    material.getDiffuseTexture(),
                    material.getAmbientTexture(),
                    material.getSpecularTexture(),
                    material.getDissolveTexture(),
                    material.getNormalTexture()
            };
            for (String name : names)
                if (name != null && !textures.containsKey(name))
                    textures.put(name, loadTexture(directory, name));
        }
        return textures;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        Uri directoryUri = Uri.parse(getIntent().getStringExtra("directory_uri")),
                modelUri = Uri.parse(getIntent().getStringExtra("model_uri"));
        DocumentFile directory = DocumentFile.fromTreeUri(this, directoryUri);

        ((TextView) findViewById(R.id.name)).setText(
                DocumentFile.fromSingleUri(this, modelUri).getName()
        );

        try {
            View view = findViewById(R.id.surface);
            Model model = ObjParser.parse(IOUtils.toString(
                    getContentResolver().openInputStream(modelUri),
                    StandardCharsets.UTF_8
            ));
            HashMap<String, Material> materials = loadMaterials(directory, model);
            HashMap<String, Bitmap> textures = loadTextures(directory, materials);
            view.setEGLContextClientVersion(3);
            view.setEGLConfigChooser((egl10, eglDisplay) -> {
                int[] attributes = {
                        EGL10.EGL_LEVEL, 0,
                        EGL10.EGL_RENDERABLE_TYPE, 4,
                        EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
                        EGL10.EGL_RED_SIZE, 8,
                        EGL10.EGL_GREEN_SIZE, 8,
                        EGL10.EGL_BLUE_SIZE, 8,
                        EGL10.EGL_DEPTH_SIZE, 16,
                        EGL10.EGL_SAMPLE_BUFFERS, 1,
                        EGL10.EGL_SAMPLES, 4,
                        EGL10.EGL_NONE
                };
                EGLConfig[] configs = new EGLConfig[1];
                int[] configCounts = new int[1];
                egl10.eglChooseConfig(eglDisplay, attributes, configs, 1, configCounts);
                if (configCounts[0] == 0)
                    return null;
                else
                    return configs[0];
            });
            view.setRenderer(new Renderer(this, model, materials, textures));
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
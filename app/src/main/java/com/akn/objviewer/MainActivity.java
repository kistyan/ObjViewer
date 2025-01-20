package com.akn.objviewer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_DIRECTORY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, PICK_DIRECTORY);
    }

    private void openModel(Uri directoryUri, Uri modelUri) {
        Intent intent = new Intent(this, ViewActivity.class);
        intent.putExtra("directory_uri", directoryUri.toString());
        intent.putExtra("model_uri", modelUri.toString());
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            DocumentFile directory = DocumentFile.fromTreeUri(this, data.getData());
            ArrayList<DocumentFile> models = new ArrayList<>();
            for (DocumentFile file : directory.listFiles())
                if (file.isFile() && Objects.equals(file.getType(), "model/obj"))
                    models.add(file);
            if (!models.isEmpty()) {
                String[] modelNames = new String[models.size()];
                for (int modelIndex = 0; modelIndex < modelNames.length; modelIndex++)
                    modelNames[modelIndex] = models.get(modelIndex).getName();
                ListView list = findViewById(R.id.models);
                list.setAdapter(new ArrayAdapter<>(
                        list.getContext(),
                        R.layout.list_item,
                        R.id.name,
                        modelNames
                ));
                list.setOnItemClickListener((parent, view, position, id)
                        -> openModel(data.getData(), models.get(position).getUri()));
                if (models.size() == 1)
                    openModel(data.getData(), models.get(0).getUri());
                return;
            }
        }
        finish();
    }
}
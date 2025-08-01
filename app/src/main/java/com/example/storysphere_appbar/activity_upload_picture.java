package com.example.storysphere_appbar;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.content.Intent;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;

public class activity_upload_picture extends AppCompatActivity {

    static final int IMAGE_PICK_CODE = 1000;

    ImageView imageView;
    EditText editTitle;
    Button btnPick, btnSave;
    Uri selectedImageUri;
    DBHelper dbHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_picture);

        imageView = findViewById(R.id.imageView);
        editTitle = findViewById(R.id.editTitle);
        btnPick = findViewById(R.id.btnPickImage);
        btnSave = findViewById(R.id.btnSave);
        dbHelper = new DBHelper(this);

        btnPick.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, IMAGE_PICK_CODE);
        });

        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString();
            if (selectedImageUri != null && !title.isEmpty()) {
                dbHelper.insertBook(title, selectedImageUri.toString());
                Toast.makeText(this, "Book saved", Toast.LENGTH_SHORT).show();
                imageView.setImageURI(null);
                editTitle.setText("");
            } else {
                Toast.makeText(this, "Title or Image missing", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);
        }
    }
}

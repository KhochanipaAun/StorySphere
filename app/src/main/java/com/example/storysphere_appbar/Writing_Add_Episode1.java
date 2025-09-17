package com.example.storysphere_appbar;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Arrays;
import java.util.List;


public class Writing_Add_Episode1 extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int REQUEST_CODE_PERMISSION = 101;

    private EditText edtTitle, edtTagline, edtTag;
    private Spinner spinnerCategory;
    private Button bttCreate;
    private ImageView imageViewUpload;

    private DBHelper dbHelper;
    private String imagePath = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing_add_episode1);


        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(Writing_Add_Episode1.this, activity_writing.class);
            startActivity(intent);
            finish();
        });

        edtTitle = findViewById(R.id.edtAddTitle);
        edtTagline = findViewById(R.id.edtAddTagline);
        edtTag = findViewById(R.id.edtAddTag);
        spinnerCategory = findViewById(R.id.spinner_custom);
        bttCreate = findViewById(R.id.bttCreate);
        imageViewUpload = findViewById(R.id.imageView4);

        dbHelper = new DBHelper(this);

        // เติมข้อมูล Spinner
        List<String> categories = Arrays.asList("Fantasy", "Romance", "Sci-fi", "Drama","Comedy","Mystery");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        imageViewUpload.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
            } else {
                openGallery();
            }
        });

        bttCreate.setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            String tagline = edtTagline.getText().toString().trim();
            String tag = edtTag.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();
            String content = ""; // ✅ ยังไม่ใช้เนื้อหายาว ให้เก็บค่าว่างไว้ก่อน

            if (title.isEmpty()) {
                edtTitle.setError("กรุณากรอกหัวข้อ");
                edtTitle.requestFocus();
                return;
            }



            long id = dbHelper.insertWriting(title, tagline, tag, category, imagePath, content);

            if (id > 0) {
                Toast.makeText(this, "สร้างงานเขียนใหม่สำเร็จ", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, activity_writing.class));
                finish();
            } else {
                Toast.makeText(this, "เกิดข้อผิดพลาดในการบันทึกข้อมูล", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            imageViewUpload.setImageURI(imageUri);
            // เก็บ path ของรูปภาพ (URI string)
            imagePath = imageUri.toString();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "อนุญาตสิทธิ์เพื่อเลือกภาพ", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

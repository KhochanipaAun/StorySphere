package com.example.storysphere_appbar;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class activity_writnig_edit_writting extends AppCompatActivity {

    EditText edtEditTitle, edtEditTagline;
    Button bttEdit;
    DBHelper dbHelper;
    int writingId = -1; // รับค่าที่ส่งมาจากหน้าก่อน

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writnig_edit_writting);

        edtEditTitle = findViewById(R.id.edtEditTitle);
        edtEditTagline = findViewById(R.id.edtEditTagline);
        bttEdit = findViewById(R.id.bttEdit);
        dbHelper = new DBHelper(this);

        // รับ writing_id จาก Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("writing_id")) {
            writingId = intent.getIntExtra("writing_id", -1);
            loadWritingData(writingId);
        }

        // บันทึกข้อมูลที่แก้ไข
        bttEdit.setOnClickListener(v -> {
            String newTitle = edtEditTitle.getText().toString().trim();
            String newTagline = edtEditTagline.getText().toString().trim();

            if (newTitle.isEmpty() || newTagline.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบ", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean updated = dbHelper.updateWriting(writingId, newTitle, newTagline);
            if (updated) {
                Toast.makeText(this, "บันทึกสำเร็จ", Toast.LENGTH_SHORT).show();
                finish(); // กลับหน้าก่อน
            } else {
                Toast.makeText(this, "เกิดข้อผิดพลาด", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadWritingData(int id) {
        Cursor cursor = dbHelper.getWritingById(id);
        if (cursor != null && cursor.moveToFirst()) {
            edtEditTitle.setText(cursor.getString(cursor.getColumnIndexOrThrow("title")));
            edtEditTagline.setText(cursor.getString(cursor.getColumnIndexOrThrow("tagline")));
            cursor.close();
        }
    }
}
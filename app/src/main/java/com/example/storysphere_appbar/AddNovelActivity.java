package com.example.storysphere_appbar;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import jp.wasabeef.richeditor.RichEditor;

public class AddNovelActivity extends AppCompatActivity {

    private EditText editTitle;
    private RichEditor editor;
    private TextView btnSave;
    private ImageButton btnBack;
    private DBHelper dbHelper;

    // ปุ่มจัดรูปแบบ
    private ImageButton btnBold, btnItalic, btnUnderline;
    private ImageButton btnAlignLeft, btnAlignCenter, btnAlignRight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_novel);

        // ตั้งค่า Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ผูก View กับ ID
        editTitle = findViewById(R.id.editTitle);
        editor = findViewById(R.id.editor);
        btnSave = findViewById(R.id.saveButton);
        btnBack = findViewById(R.id.btnBack);

        // ปุ่มจัดรูปแบบ
        btnBold = findViewById(R.id.action_bold);
        btnItalic = findViewById(R.id.action_italic);
        btnUnderline = findViewById(R.id.action_underline);
        btnAlignLeft = findViewById(R.id.action_align_left);
        btnAlignCenter = findViewById(R.id.action_align_center);
        btnAlignRight = findViewById(R.id.action_align_right);

        // ตั้งค่า Editor
        editor.setEditorHeight(300);
        editor.setEditorFontSize(16);
        editor.setPlaceholder("เขียนเนื้อหานิยายที่นี่...");

        // ฟังก์ชันจัดรูปแบบข้อความ
        btnBold.setOnClickListener(v -> editor.setBold());
        btnItalic.setOnClickListener(v -> editor.setItalic());
        btnUnderline.setOnClickListener(v -> editor.setUnderline());
        btnAlignLeft.setOnClickListener(v -> editor.setAlignLeft());
        btnAlignCenter.setOnClickListener(v -> editor.setAlignCenter());
        btnAlignRight.setOnClickListener(v -> editor.setAlignRight());

        // ปุ่มย้อนกลับ
        btnBack.setOnClickListener(v -> finish());

        // ปุ่มบันทึก
        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String content = editor.getHtml();

            if (title.isEmpty() || content == null || content.trim().isEmpty()) {
                Toast.makeText(this, "กรุณากรอกหัวเรื่องและเนื้อหา", Toast.LENGTH_SHORT).show();
            } else {
                // ✅ บันทึกข้อมูลลง SQLite
                long result = dbHelper.insertWriting(
                        title, "", "", "", "", content // tagline, tag, category, imagePath = เว้นไว้
                );

                if (result != -1) {
                    Toast.makeText(this, "บันทึกเรียบร้อย", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "เกิดข้อผิดพลาดในการบันทึก", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
package com.example.storysphere_appbar;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class AdminBannerActivity extends AppCompatActivity {

    private ImageView preview;
    private EditText edtTitle, edtDeeplink;
    private Button btnSave, btnPick;
    private Uri picked;
    private DBHelper db;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    picked = uri;
                    if (preview != null) preview.setImageURI(uri);
                    if (btnSave != null) btnSave.setEnabled(true);
                }
            });

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_admin_banner);

        db = new DBHelper(this);

        // Toolbar back
        MaterialToolbar tb = findViewById(R.id.toolbar);
        if (tb != null) tb.setNavigationOnClickListener(v -> finish());

        preview     = findViewById(R.id.preview);
        edtTitle    = findViewById(R.id.edtTitle);
        edtDeeplink = findViewById(R.id.edtDeeplink);
        btnPick     = findViewById(R.id.btnPick);
        btnSave     = findViewById(R.id.btnSave);

        // ตรวจ id layout ให้ครบ
        if (preview == null || edtTitle == null || edtDeeplink == null || btnPick == null || btnSave == null) {
            Toast.makeText(this, "Layout ids not found. Check activity_admin_banner.xml", Toast.LENGTH_LONG).show();
            return;
        }

        // ยังไม่เลือกรูป → ปุ่ม Save เป็น disabled
        btnSave.setEnabled(false);

        // Pick image
        btnPick.setOnClickListener(v -> pickImage.launch("image/*"));

        // Save banner
        btnSave.setOnClickListener(v -> {
            if (picked == null) {
                Toast.makeText(this, "Please pick an image", Toast.LENGTH_SHORT).show();
                return;
            }

            String title = edtTitle.getText() != null ? edtTitle.getText().toString().trim() : "";
            String link  = edtDeeplink.getText() != null ? edtDeeplink.getText().toString().trim() : "";

            // ตัวอย่าง validate ง่าย ๆ
            if (!TextUtils.isEmpty(link) && !(link.startsWith("http://") || link.startsWith("https://"))) {
                Toast.makeText(this, "Deeplink/URL ควรขึ้นต้นด้วย http:// หรือ https://", Toast.LENGTH_SHORT).show();
                return;
            }

            long id = db.insertBanner(
                    picked.toString(),
                    title,
                    link,
                    true
            );

            Toast.makeText(this, id != -1 ? "Saved!" : "Failed", Toast.LENGTH_SHORT).show();
            if (id != -1) finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

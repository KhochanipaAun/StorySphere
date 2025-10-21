package com.example.storysphere_appbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class AdminBannerActivity extends AppCompatActivity {

    private ImageView preview;
    private EditText edtTitle, edtDeeplink;
    private Button btnSave, btnPick;
    @Nullable private Uri picked;
    private DBHelper db;

    // ✅ ใช้ OpenDocument แทน GetContent เพื่อให้ขอ persistable URI permission ได้
    private final ActivityResultLauncher<String[]> openImage =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    // ✅ ขอสิทธิ์อ่านแบบถาวร (persistable)
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (Exception ignored) { /* บาง provider ไม่รองรับ ก็ยังใช้ได้ระหว่าง session */ }

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

        if (preview == null || edtTitle == null || edtDeeplink == null || btnPick == null || btnSave == null) {
            Toast.makeText(this, "Layout ids not found. Check activity_admin_banner.xml", Toast.LENGTH_LONG).show();
            return;
        }

        btnSave.setEnabled(false);

        // เลือกรูป
        btnPick.setOnClickListener(v -> {
            // จำกัดให้เป็นรูปภาพเท่านั้น
            openImage.launch(new String[]{"image/*"});
        });

        // บันทึกแบนเนอร์
        btnSave.setOnClickListener(v -> {
            if (picked == null) {
                Toast.makeText(this, "Please pick an image", Toast.LENGTH_SHORT).show();
                return;
            }

            String title = edtTitle.getText() != null ? edtTitle.getText().toString().trim() : "";
            String link  = edtDeeplink.getText() != null ? edtDeeplink.getText().toString().trim() : "";

            // ✅ ยอมรับได้ทั้ง http/https และ deeplink ภายในแอป เช่น app://writing/123
            if (!TextUtils.isEmpty(link)) {
                boolean okScheme =
                        link.startsWith("http://") ||
                                link.startsWith("https://") ||
                                link.startsWith("app://");
                if (!okScheme) {
                    Toast.makeText(this, "ลิงก์ควรขึ้นต้นด้วย http://, https:// หรือ app://", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            long id = db.insertBanner(
                    picked.toString(),                 // ✅ เก็บเป็น URI string (content://…)
                    TextUtils.isEmpty(title) ? null : title,
                    TextUtils.isEmpty(link)  ? null : link,
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

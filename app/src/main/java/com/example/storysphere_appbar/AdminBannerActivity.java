package com.example.storysphere_appbar;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class AdminBannerActivity extends AppCompatActivity {
    private ImageView preview;
    private EditText edtTitle, edtDeeplink;
    private Uri picked;
    private DBHelper db;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    picked = uri;
                    if (preview != null) preview.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_admin_banner); // <- ต้องเป็นไฟล์นี้จริง

        db = new DBHelper(this);

        preview     = findViewById(R.id.preview);
        edtTitle    = findViewById(R.id.edtTitle);
        edtDeeplink = findViewById(R.id.edtDeeplink);
        Button btnPick = findViewById(R.id.btnPick);
        Button btnSave = findViewById(R.id.btnSave);

        // กันกรณี id ไม่ตรง/หาไม่เจอ
        if (preview == null || edtTitle == null || edtDeeplink == null || btnPick == null || btnSave == null) {
            Toast.makeText(this, "Layout ids not found. Check activity_admin_banner.xml", Toast.LENGTH_LONG).show();
            return;
        }

        btnPick.setOnClickListener(v -> pickImage.launch("image/*"));

        btnSave.setOnClickListener(v -> {
            if (picked == null) {
                Toast.makeText(this, "Please pick an image", Toast.LENGTH_SHORT).show();
                return;
            }
            String title = edtTitle.getText() != null ? edtTitle.getText().toString() : "";
            String link  = edtDeeplink.getText() != null ? edtDeeplink.getText().toString() : "";

            long id = db.insertBanner(picked.toString(), title, link, true);
            Toast.makeText(this, id != -1 ? "Saved!" : "Failed", Toast.LENGTH_SHORT).show();
            if (id != -1) finish();
        });
    }
}

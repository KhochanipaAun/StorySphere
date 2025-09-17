package com.example.storysphere_appbar;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class activity_follow_writer extends AppCompatActivity {

    public static final String EXTRA_USERNAME = "extra_username"; // เผื่อใช้ในอนาคต
    public static final String EXTRA_EMAIL = "extra_email";       // ← ใช้ตัวนี้

    private DBHelper db;
    private String email;    // อีเมลที่ล็อกอิน
    private String username; // ดึงมาจากฐานข้อมูลด้วยอีเมล

    private ImageView ivAvatar;
    private TextView tvName;
    private RecyclerView rvWritings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_writer);

        db = new DBHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> finish());

        ivAvatar   = findViewById(R.id.ivAvatar);
        tvName     = findViewById(R.id.tvName);
        rvWritings = findViewById(R.id.rvWritings);
        rvWritings.setLayoutManager(new LinearLayoutManager(this));

        // 1) เอาอีเมลจาก Intent หรือจาก SharedPreferences (session)
        email = getIntent().getStringExtra(EXTRA_EMAIL);
        if (email == null || email.trim().isEmpty()) {
            email = getSharedPreferences("session", MODE_PRIVATE).getString("email", null);
        }

        // 2) ใช้อีเมลไปดึง user -> ได้ username + รูป
        if (email != null) {
            Cursor u = db.getUserByEmail(email);
            if (u != null && u.moveToFirst()) {
                int unIdx = u.getColumnIndex("username");
                if (unIdx >= 0) username = u.getString(unIdx);

                int imgIdx = u.getColumnIndex("image_uri");
                if (imgIdx >= 0) {
                    String imageUri = u.getString(imgIdx);
                    if (imageUri != null && !imageUri.trim().isEmpty()) {
                        try { ivAvatar.setImageURI(Uri.parse(imageUri)); } catch (Exception ignored) {}
                    }
                }
            }
            if (u != null) u.close();
        }

        // 3) ตั้งชื่อบนจอ → ใช้ username จาก DB; ถ้าไม่มี ใช้ส่วนหน้าอีเมล
        if (username != null && !username.trim().isEmpty()) {
            tvName.setText(username);
        } else if (email != null) {
            tvName.setText(emailLocalPart(email)); // fallback: "foo" จาก "foo@bar.com"
        } else {
            tvName.setText("");
        }

        // 4) โหลดงานเขียนของผู้ใช้ (ผูกด้วย username ที่ได้จากอีเมล; ถ้า schema ยังไม่ผูกจะ fallback ทั้งหมด)
        List<WritingItem> items = db.getWritingItemsByUsername(
                (username != null && !username.isEmpty()) ? username : emailLocalPart(email)
        );
        rvWritings.setAdapter(new FollowWriterAdapter(items, writingId -> {
            Intent it = new Intent(this, ReadingMainActivity.class);
            it.putExtra(ReadingMainActivity.EXTRA_WRITING_ID, writingId);
            startActivity(it);
        }));
    }

    private String emailLocalPart(String mail) {
        if (mail == null) return "";
        int at = mail.indexOf('@');
        return at > 0 ? mail.substring(0, at) : mail;
    }
}

package com.example.storysphere_appbar;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;

import androidx.appcompat.app.AppCompatActivity;

public class activity_edit_profile extends AppCompatActivity {

    private EditText etUsername, etOldPassword, etNewPassword;
    private TextView tvEmail;
    private View btnSave; // ปุ่มเซฟ (ถ้าใน layout ชื่ออื่นให้เปลี่ยน id ตรง findViewById)
    private DBHelper dbHelper;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        dbHelper      = new DBHelper(this);
        etUsername    = findViewById(R.id.et_username);
        etOldPassword = findViewById(R.id.et_old_password);
        etNewPassword = findViewById(R.id.et_new_password);
        tvEmail       = findViewById(R.id.tv_email);
        //btnSave       = findViewById(R.id.btn_save); // <-- ถ้า layout ไม่มี id นี้ ให้ใส่ id ให้ปุ่มเซฟ

        userEmail = getIntent().getStringExtra("email");

        // Toolbar back
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(activity_edit_profile.this, activity_profile.class);
            if (userEmail != null && !userEmail.isEmpty()) {
                intent.putExtra("email", userEmail);
            }
            startActivity(intent);
            finish();
        });

        // ----- TEST MODE: ไม่มี email -----
        if (userEmail == null || userEmail.isEmpty()) {
            tvEmail.setText("guest@example.com");
            etUsername.setText("Guest User");
            etOldPassword.setEnabled(false);
            etNewPassword.setEnabled(false);
            if (btnSave != null) { btnSave.setEnabled(false); btnSave.setAlpha(0.5f); }
            Toast.makeText(this, "Test mode (no email) — fields are mocked", Toast.LENGTH_SHORT).show();
            return; // ไม่ query DB
        }

        // ----- NORMAL MODE: มี email -----
        tvEmail.setText(userEmail);
        Cursor cursor = null;
        try {
            cursor = dbHelper.getUserByEmail(userEmail);
            if (cursor != null && cursor.moveToFirst()) {
                etUsername.setText(cursor.getString(cursor.getColumnIndexOrThrow("username")));
            } else {
                Toast.makeText(this, "User not found in database", Toast.LENGTH_SHORT).show();
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public void save_change(View view) {
        // กัน test mode
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Test mode: saving is disabled", Toast.LENGTH_SHORT).show();
            return;
        }

        String username   = etUsername.getText().toString().trim();
        String oldPassword= etOldPassword.getText().toString().trim();
        String newPassword= etNewPassword.getText().toString().trim();

        if (username.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = null;
        try {
            cursor = dbHelper.getUserByEmail(userEmail);
            if (cursor != null && cursor.moveToFirst()) {
                String currentPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"));
                if (!oldPassword.equals(currentPassword)) {
                    Toast.makeText(this, "Old password is incorrect", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                Toast.makeText(this, "User not found in database", Toast.LENGTH_SHORT).show();
                return;
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        boolean updated = dbHelper.updateUser(userEmail, username, newPassword);
        if (updated) {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, activity_profile.class);
            intent.putExtra("email", userEmail);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }

    public void onChangePictureClick(View view) {
        Toast.makeText(this, "Feature not implemented", Toast.LENGTH_SHORT).show();
    }
}

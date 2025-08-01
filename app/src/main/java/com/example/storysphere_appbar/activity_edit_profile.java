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
    private DBHelper dbHelper;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        userEmail = getIntent().getStringExtra("email");

        dbHelper = new DBHelper(this);

        etUsername = findViewById(R.id.et_username);
        etOldPassword = findViewById(R.id.et_old_password);
        etNewPassword = findViewById(R.id.et_new_password);
        tvEmail = findViewById(R.id.tv_email);

        tvEmail.setText(userEmail);

        Cursor cursor = dbHelper.getUserByEmail(userEmail);
        if (cursor != null && cursor.moveToFirst()) {
            etUsername.setText(cursor.getString(cursor.getColumnIndexOrThrow("username")));
            cursor.close();
        }

        // เพื่อให้ลูกศรย้อนกลับทำงาน
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(activity_edit_profile.this, activity_profile.class);
            intent.putExtra("email", userEmail);
            startActivity(intent);
            finish();
        });
    }

    public void save_change(View view) {
        String username = etUsername.getText().toString().trim();
        String oldPassword = etOldPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();

        if (username.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = dbHelper.getUserByEmail(userEmail);
        if (cursor != null && cursor.moveToFirst()) {
            String currentPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"));
            if (!oldPassword.equals(currentPassword)) {
                Toast.makeText(this, "Old password is incorrect", Toast.LENGTH_SHORT).show();
                cursor.close();
                return;
            }
        } else {
            Toast.makeText(this, "User not found in database", Toast.LENGTH_SHORT).show();
            return;
        }
        cursor.close();

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

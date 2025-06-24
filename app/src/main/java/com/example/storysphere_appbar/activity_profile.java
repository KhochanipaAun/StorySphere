package com.example.storysphere_appbar;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class activity_profile extends AppCompatActivity {

    private TextView tvUsername, tvEmail;
    private ImageView imgProfile;
    private DBHelper dbHelper;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userEmail = getIntent().getStringExtra("email");

        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Email not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new DBHelper(this);

        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);
        imgProfile = findViewById(R.id.img_profile);

        tvEmail.setText(userEmail);

        Cursor cursor = dbHelper.getUserByEmail(userEmail);
        if (cursor != null && cursor.moveToFirst()) {
            int index = cursor.getColumnIndex("username");
            if (index != -1) {
                String username = cursor.getString(index);
                tvUsername.setText(username);
            } else {
                tvUsername.setText("No username found");
            }
            cursor.close();
        } else {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
        }
    }

    public void edit_profile(View view) {
        Intent intent = new Intent(this, activity_edit_profile.class);
        intent.putExtra("email", userEmail);
        startActivity(intent);
    }

    public void logOut(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void delete_account(View view) {
        boolean deleted = dbHelper.deleteUser(userEmail);
        if (deleted) {
            Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, activity_sign_up.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show();
        }
    }
}
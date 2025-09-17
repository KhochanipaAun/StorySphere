package com.example.storysphere_appbar;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class LibraryHistoryActivity extends AppCompatActivity {   // ← ต้อง extends AppCompatActivity/Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_history);        // ← ชี้ไปที่ layout ของหน้านี้

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_library);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0,0);
                finish();
                return true;

            } else if (id == R.id.nav_library) {
                // ถ้าไฟล์นี้คือ LibraryActivity เอง ก็แค่ return true;
                // ถ้าไม่ใช่:
                startActivity(new Intent(this, LibraryHistoryActivity.class));
                overridePendingTransition(0,0);
                finish();
                return true;

            } else if (id == R.id.nav_writing) {
                startActivity(new Intent(this, activity_writing.class));
                overridePendingTransition(0,0);
                finish();
                return true;

            } else if (id == R.id.nav_activity) {
                startActivity(new Intent(this, UserActivity.class));
                overridePendingTransition(0,0);
                finish();
                return true;
            }

            return false;
        });
    }
}
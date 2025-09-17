package com.example.storysphere_appbar;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class activity_writing extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WritingAdapter adapter;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing);

        BottomNavigationView bottomNav = requireViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_writing);
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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ⬇️⬇️ เพิ่มบรรทัดนี้ เพื่อคัดลอกฐานจาก assets มาครั้งแรก (ถ้ายังไม่มี)
        DatabaseCopier.copyIfNeeded(this, DBHelper.DATABASE_NAME);


        recyclerView = findViewById(R.id.recyclerViewWriting);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DBHelper(this);
        loadData();
    }

    private void loadData() {
        List<WritingItem> list = dbHelper.getAllWritingItems();
        adapter = new WritingAdapter(this, list);
        recyclerView.setAdapter(adapter);
    }

    protected void onResume() {
        super.onResume();
        loadData();
    }
    public void AddNewWritng (View view){
        Intent intent;
        intent = new Intent(this,Writing_Add_Episode1.class);
        startActivity(intent);
    }
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(this, MainActivity.class); // เปลี่ยนชื่อคลาสหน้า Login ตามที่ใช้จริง
        startActivity(intent);
        finish(); // ปิดหน้านี้ไม่ให้ย้อนกลับมาได้
        return true;
    }
}

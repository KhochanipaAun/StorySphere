package com.example.storysphere_appbar;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.widget.Toolbar;

import java.util.List;

public class activity_writing extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WritingAdapter adapter;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


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
    public void AddNewWriting (View view){
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

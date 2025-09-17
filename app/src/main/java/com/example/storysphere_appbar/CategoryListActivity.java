package com.example.storysphere_appbar;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Collections;
import java.util.List;

public class CategoryListActivity extends AppCompatActivity {
    public static final String EXTRA_CATEGORY = "extra_category";

    private RecyclerView rv;
    private TextView emptyView; // ถ้ามีใน layout
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        // --- Toolbar ---
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            // ให้แน่ใจว่าโชว์ title (เผื่อมีที่อื่นเคย setDisplayShowTitleEnabled(false))
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        String category = getIntent().getStringExtra(EXTRA_CATEGORY);
        if (category == null) category = "Category";
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(cap(category));
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // --- RecyclerView ---
        rv = findViewById(R.id.rvCategory);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // (ถ้าใน activity_category_list มี TextView สำหรับกรณีไม่มีข้อมูล)
        emptyView = findViewById(R.id.emptyView);

        // --- Load data ---
        db = new DBHelper(this);
        List<WritingItem> items = db.getWritingItemsByTag(category, 200);
        if (items == null) items = Collections.emptyList();

        // --- Adapter ---
        rv.setAdapter(new StoryAdapter(this, items));

        // --- Empty state ---
        if (items.isEmpty() && emptyView != null) {
            emptyView.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
        }
    }

    private String cap(String s){
        if (s == null || s.isEmpty()) return "";
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }
}

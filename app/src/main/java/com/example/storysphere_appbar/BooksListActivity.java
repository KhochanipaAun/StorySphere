package com.example.storysphere_appbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BooksListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SearchView searchView;
    private Spinner spinnerBook;
    private final List<String> categoryOpts = new ArrayList<>();

    private BooksAdapter adapter;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_book);

        db = new DBHelper(this);

        // Toolbar back
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // โหลดหนังสือทั้งหมด
        List<WritingItem> all = db.getAllWritingItems();
        adapter = new BooksAdapter(all, new BooksAdapter.OnItemClick() {
            @Override
            public void onClick(WritingItem item) {
                Intent it = new Intent(BooksListActivity.this, BookDetailActivity.class);
                it.putExtra("book_id", item.getId());
                startActivity(it);
            }

            @Override
            public void onDelete(WritingItem item, int position) {
                new androidx.appcompat.app.AlertDialog.Builder(BooksListActivity.this)
                        .setTitle("ลบหนังสือ")
                        .setMessage("ต้องการลบเรื่อง: \"" + item.getTitle() + "\" ?")
                        .setPositiveButton("ลบ", (d, w) -> {
                            boolean ok = db.deleteWriting(item.getId());
                            if (ok) {
                                adapter.removeAt(position); // ตัดออกจาก UI ทันที
                                Toast.makeText(BooksListActivity.this, "ลบแล้ว", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(BooksListActivity.this, "ลบไม่สำเร็จ", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("ยกเลิก", null)
                        .show();
            }
        });
        recyclerView.setAdapter(adapter);

        // ===== Spinner (กรองหมวดหมู่) =====
        spinnerBook = findViewById(R.id.spinnerUser);
        categoryOpts.clear();
        categoryOpts.add("All books");
        categoryOpts.addAll(extractNonEmptyCategories(all));

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categoryOpts
        );
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBook.setAdapter(catAdapter);

        spinnerBook.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    adapter.resetFilter();
                } else {
                    adapter.filter(categoryOpts.get(position));
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        // ===== SearchView (กรองข้อความ) =====
        searchView = findViewById(R.id.searchViewBook);
        if (searchView != null) {
            searchView.setIconifiedByDefault(false);
            searchView.setQueryHint("Search...");
            searchView.clearFocus();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override public boolean onQueryTextSubmit(String q) { adapter.filter(q); return true; }
                @Override public boolean onQueryTextChange(String q) { adapter.filter(q == null ? "" : q); return true; }
            });
        }

        // ===== ปุ่ม Home =====
        ImageView homeBtn = findViewById(R.id.imageView12);
        if (homeBtn != null) {
            homeBtn.setOnClickListener(v -> {
                Intent i = new Intent(BooksListActivity.this, AdminPanelActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }
    }

    private List<String> extractNonEmptyCategories(List<WritingItem> items) {
        Set<String> set = new HashSet<>();
        if (items != null) {
            for (WritingItem w : items) {
                if (w != null && w.getCategory() != null) {
                    String c = w.getCategory().trim();
                    if (!c.isEmpty()) set.add(c);
                }
            }
        }
        return new ArrayList<>(set);
    }
}

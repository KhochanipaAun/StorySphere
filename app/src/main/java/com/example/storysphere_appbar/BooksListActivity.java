package com.example.storysphere_appbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
    private SearchView searchView;     // @+id/searchViewBook
    private Spinner spinnerBook;       // map ไปที่ @+id/spinnerUser ใน XML
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
        adapter = new BooksAdapter(all, item -> {
            Intent it = new Intent(BooksListActivity.this, BookDetailActivity.class);
            it.putExtra("book_id", item.getId());
            startActivity(it);
        });
        recyclerView.setAdapter(adapter);

        // ===== Spinner (spinnerBook): กรองตามหมวดหมู่ =====
        // XML ใช้ id "spinnerUser" อยู่แล้ว จับมาเป็นตัวแปร spinnerBook
        spinnerBook = findViewById(R.id.spinnerUser);

        categoryOpts.clear();
        categoryOpts.add("All books");
        categoryOpts.addAll(extractNonEmptyCategories(all)); // ดึงจากลิสต์ ไม่พึ่ง DBHelper เพิ่ม

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categoryOpts
        );
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBook.setAdapter(catAdapter);

        spinnerBook.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    tryResetOrClearFilter();  // โชว์ทั้งหมด
                } else {
                    String selectedCat = categoryOpts.get(position);
                    adapter.filter(selectedCat); // ใช้ filter เดิม ให้กรองด้วยข้อความ
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { /* no-op */ }
        });

        // ===== SearchView: กรองด้วยคำค้น =====
        searchView = findViewById(R.id.searchViewBook);
        if (searchView != null) {
            searchView.setIconifiedByDefault(false);
            searchView.setQueryHint("Search...");
            searchView.clearFocus();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override public boolean onQueryTextSubmit(String q) {
                    adapter.filter(q);
                    return true;
                }
                @Override public boolean onQueryTextChange(String q) {
                    adapter.filter(q == null ? "" : q);
                    return true;
                }
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

        // ===== ลบหนังสือเมื่อกดปุ่มถังขยะในแต่ละแถว (ไม่แก้ BooksAdapter) =====
        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override public void onChildViewAttachedToWindow(@NonNull View itemView) {
                ImageView del = itemView.findViewById(R.id.imgDelete);
                if (del != null) {
                    del.setOnClickListener(v -> {
                        TextView tvTitle = itemView.findViewById(R.id.txtTitleBook);
                        String title = tvTitle != null ? tvTitle.getText().toString().trim() : null;

                        if (title == null || title.isEmpty()) {
                            Toast.makeText(BooksListActivity.this, "ไม่พบชื่อเรื่องของแถวนั้น", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int writingId = findWritingIdByTitle(title);
                        if (writingId <= 0) {
                            Toast.makeText(BooksListActivity.this, "หา id ของ \"" + title + "\" ไม่เจอ", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        new androidx.appcompat.app.AlertDialog.Builder(BooksListActivity.this)
                                .setTitle("ลบหนังสือ")
                                .setMessage("ต้องการลบเรื่อง: \"" + title + "\" ?")
                                .setPositiveButton("ลบ", (d, w) -> {
                                    boolean ok = db.deleteWriting(writingId);
                                    if (ok) {
                                        List<WritingItem> refreshed = db.getAllWritingItems();
                                        try {
                                            adapter.updateData(refreshed); // ถ้ามีเมธอดนี้
                                        } catch (Throwable ignore) {
                                            // ถ้าอะแดปเตอร์ไม่มี updateData: สร้างใหม่
                                            adapter = new BooksAdapter(refreshed, item -> {
                                                Intent it = new Intent(BooksListActivity.this, BookDetailActivity.class);
                                                it.putExtra("book_id", item.getId());
                                                startActivity(it);
                                            });
                                            recyclerView.setAdapter(adapter);
                                        }
                                        Toast.makeText(BooksListActivity.this, "ลบแล้ว", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(BooksListActivity.this, "ลบไม่สำเร็จ", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("ยกเลิก", null)
                                .show();
                    });
                }
            }
            @Override public void onChildViewDetachedFromWindow(@NonNull View itemView) {
                ImageView del = itemView.findViewById(R.id.imgDelete);
                if (del != null) del.setOnClickListener(null);
            }
        });
    }

    /** ถ้า BooksAdapter มีเมธอด resetFilter() ก็เรียกใช้, ถ้าไม่มีก็ fallback เป็น filter("") */
    private void tryResetOrClearFilter() {
        try {
            BooksAdapter.class.getMethod("resetFilter").invoke(adapter);
        } catch (Exception ignore) {
            adapter.filter("");
        }
    }

    /** ดึงหมวดหมู่ที่ไม่ว่าง/ไม่ซ้ำ จากรายการหนังสือ */
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

    /** หา id จาก title (วิธีง่าย—ระวังกรณีชื่อซ้ำ) */
    private int findWritingIdByTitle(String title) {
        List<WritingItem> all = db.getAllWritingItems();
        for (WritingItem w : all) {
            if (w != null && w.getTitle() != null && w.getTitle().equals(title)) {
                return w.getId();
            }
        }
        return -1;
    }
}

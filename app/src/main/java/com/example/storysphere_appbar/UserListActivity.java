package com.example.storysphere_appbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserListActivity extends AppCompatActivity {

    // Views
    private RecyclerView recyclerView;
    private SearchView searchView;
    private Spinner spinnerUser;

    // Data / helpers
    private final List<User> fullList = new ArrayList<>();
    private final List<String> nameOptions = new ArrayList<>();
    private UserAdapter adapter;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        db = new DBHelper(this);

        // Toolbar (ปุ่ม back)
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // SearchView
        searchView = findViewById(R.id.searchView);

        // โหลด Users สำหรับลิสต์หลัก
        fullList.clear();
        fullList.addAll(db.getAllUsers());

        adapter = new UserAdapter(fullList, user -> {
            Intent it = new Intent(UserListActivity.this, UserDetailActivity.class);
            it.putExtra("user_id", user.id);
            startActivity(it);
        });
        recyclerView.setAdapter(adapter);

        // ========= Spinner: ดึงชื่อจาก fullList (ไม่ใช้เมธอด DBHelper เพิ่มเติม) =========
        spinnerUser = findViewById(R.id.spinnerUser);

        nameOptions.clear();
        nameOptions.add("All users");
        nameOptions.addAll(extractNonEmptyUsernames(fullList)); // จากลิสต์ปัจจุบัน

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, nameOptions
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUser.setAdapter(spinnerAdapter);

        spinnerUser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // แสดงทั้งหมด
                    adapter.filter(""); // เคลียร์ฟิลเตอร์
                } else {
                    String selected = nameOptions.get(position);
                    // ใช้ฟิลเตอร์เดิม (ค้นใน username/display/email/code แบบ contains)
                    adapter.filter(selected);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { /* no-op */ }
        });

        // ========= SearchView: กรองแบบพิมพ์คำค้น =========
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override public boolean onQueryTextSubmit(String query) { return false; }
                @Override public boolean onQueryTextChange(String newText) {
                    adapter.filter(newText == null ? "" : newText);
                    return true;
                }
            });
        }

        // ========= ปุ่ม Home =========
        ImageView homeBtn = findViewById(R.id.imageView12);
        if (homeBtn != null) {
            homeBtn.setOnClickListener(v -> {
                Intent i = new Intent(UserListActivity.this, AdminPanelActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }

        // ========= ลบผู้ใช้: ผูกคลิกให้ปุ่มถังขยะในแต่ละแถว =========
        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override public void onChildViewAttachedToWindow(@NonNull View itemView) {
                ImageView del = itemView.findViewById(R.id.imgDelete);
                if (del != null) {
                    del.setOnClickListener(v -> {
                        TextView tvEmail = itemView.findViewById(R.id.txtEmailUser);
                        String email = tvEmail != null ? tvEmail.getText().toString().trim() : null;

                        if (email == null || email.isEmpty()) {
                            Toast.makeText(UserListActivity.this, "ไม่พบอีเมลของผู้ใช้แถวนี้", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        new androidx.appcompat.app.AlertDialog.Builder(UserListActivity.this)
                                .setTitle("ลบผู้ใช้")
                                .setMessage("ต้องการลบผู้ใช้: " + email + " ?")
                                .setPositiveButton("ลบ", (d, w) -> {
                                    boolean ok = db.deleteUser(email);
                                    if (ok) {
                                        List<User> refreshed = db.getAllUsers();
                                        adapter.updateData(refreshed);
                                        Toast.makeText(UserListActivity.this, "ลบแล้ว", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(UserListActivity.this, "ลบไม่สำเร็จ", Toast.LENGTH_SHORT).show();
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

    /** ดึงรายการ username ที่ไม่ว่าง/ไม่ซ้ำ จากลิสต์ผู้ใช้ */
    private List<String> extractNonEmptyUsernames(List<User> users) {
        Set<String> set = new HashSet<>();
        for (User u : users) {
            if (u != null && u.username != null) {
                String name = u.username.trim();
                if (!name.isEmpty()) set.add(name);
            }
        }
        return new ArrayList<>(set);
    }
}

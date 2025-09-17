package com.example.storysphere_appbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView ivProfile;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // --- Search (กดปุ่มค้นหาหรือ Enter) ---
        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            boolean isEnter = event != null &&
                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                    event.getAction() == KeyEvent.ACTION_DOWN;

            if (actionId == EditorInfo.IME_ACTION_SEARCH || isEnter) {
                String q = v.getText().toString().trim();
                if (!q.isEmpty()) {
                    Intent i = new Intent(this, SearchResultsActivity.class);
                    i.putExtra("q", q);
                    startActivity(i);
                }
                return true;
            }
            return false;
        });

        // ===== กัน Toolbar โดนสถานะบาร์/รูมกล้องทับ (Edge-to-edge + padding) =====
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        final View appBar = findViewById(R.id.appbar);
        ViewCompat.setOnApplyWindowInsetsListener(appBar, (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(v.getPaddingLeft(), sb.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        // ===== Toolbar =====
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // ซ่อน "Home"
        }

        // ===== Profile icon =====
        ivProfile = findViewById(R.id.ivProfile);
        ivProfile.setOnClickListener(v -> {
            String email = getSharedPreferences("auth", MODE_PRIVATE)
                    .getString("email", null);

            Intent i = new Intent(this, activity_profile.class);
            if (email != null) {
                i.putExtra("email", email);
            }
            startActivity(i);
        });

        // ===== BottomNavigationView =====
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;

            } else if (id == R.id.nav_library) {
                startActivity(new Intent(this, LibraryHistoryActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;

            } else if (id == R.id.nav_writing) {
                startActivity(new Intent(this, activity_writing.class));
                overridePendingTransition(0, 0);
                finish();
                return true;

            } else if (id == R.id.nav_activity) {
                startActivity(new Intent(this, UserActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });

        // ===== Search pill =====
        findViewById(R.id.etSearch).setOnClickListener(v ->
                Toast.makeText(this, "Search clicked", Toast.LENGTH_SHORT).show()
        );

        // ===== ตั้งชื่อหัวข้อ =====
        setSectionText(R.id.rowYouMayLike, "You may also like");
        setSectionText(R.id.rowTopChart, "Top Chart");
        setSectionText(R.id.rowRomance, "Romance");
        setSectionText(R.id.rowDrama, "Drama");
        setSectionText(R.id.rowComedy, "Comedy");
        setSectionText(R.id.rowFantasy, "Fantasy");
        setSectionText(R.id.rowScifi, "Sci-fi");
        setSectionText(R.id.rowMystery, "Mystery");

        // ===== ตั้งแนว RecyclerView เป็นแนวนอนตามหมวด =====
        setHorizontalList(R.id.rvYouMayLike);
        setHorizontalList(R.id.rvRomance);
        setHorizontalList(R.id.rvDrama);
        setHorizontalList(R.id.rvComedy);
        setHorizontalList(R.id.rvFantasy);
        setHorizontalList(R.id.rvScifi);
        setHorizontalList(R.id.rvMystery);
        // setHorizontalList(R.id.rvTopChart);

        // === กดหัวข้อ/ปุ่ม '>' เพื่อไปหน้า Category ===
        wireSectionNav(R.id.rowRomance, "Romance");
        wireSectionNav(R.id.rowDrama,   "Drama");
        wireSectionNav(R.id.rowComedy,  "Comedy");
        wireSectionNav(R.id.rowFantasy, "Fantasy");
        wireSectionNav(R.id.rowScifi,   "Sci-fi");   // ให้สะกดตรงกับใน DB
        wireSectionNav(R.id.rowMystery, "Mystery");

        // === Chip ด้านบน ===
        Chip chipRomance = findViewById(R.id.chipRomance);
        Chip chipDrama   = findViewById(R.id.chipDrama);
        Chip chipComedy  = findViewById(R.id.chipComedy);
        Chip chipFantasy = findViewById(R.id.chipFantasy);
        Chip chipMystery = findViewById(R.id.chipMystery);
        Chip chipScifi   = findViewById(R.id.chipScifi);

        if (chipRomance != null) chipRomance.setOnClickListener(v -> openCategory("Romance"));
        if (chipDrama   != null) chipDrama.setOnClickListener(v   -> openCategory("Drama"));
        if (chipComedy  != null) chipComedy.setOnClickListener(v  -> openCategory("Comedy"));
        if (chipFantasy != null) chipFantasy.setOnClickListener(v -> openCategory("Fantasy"));
        if (chipMystery != null) chipMystery.setOnClickListener(v -> openCategory("Mystery"));
        if (chipScifi   != null) chipScifi.setOnClickListener(v   -> openCategory("Sci-fi")); // ตรง DB

        // ===== โหลดข้อมูลจาก DB แล้ว setAdapter =====
        DBHelper db = new DBHelper(this);

        RecyclerView rvYouMayLike = findViewById(R.id.rvYouMayLike);
        rvYouMayLike.setAdapter(new CoverSquareAdapter(
                db.getRecentWritings(12),
                item -> openWritingDetail(item)
        ));

        RecyclerView rvTopChart = findViewById(R.id.rvTopChart);
        rvTopChart.setAdapter(new CoverSquareAdapter(
                db.getRecentWritings(10),
                item -> openWritingDetail(item)
        ));

        RecyclerView rvRomance = findViewById(R.id.rvRomance);
        rvRomance.setAdapter(new CoverSquareAdapter(
                db.getWritingItemsByTag("romance", 12),
                item -> openWritingDetail(item)
        ));

        RecyclerView rvDrama = findViewById(R.id.rvDrama);
        rvDrama.setAdapter(new CoverSquareAdapter(
                db.getWritingItemsByTag("drama", 12),
                item -> openWritingDetail(item)
        ));

        RecyclerView rvComedy = findViewById(R.id.rvComedy);
        rvComedy.setAdapter(new CoverSquareAdapter(
                db.getWritingItemsByTag("comedy", 12),
                item -> openWritingDetail(item)
        ));

        RecyclerView rvFantasy = findViewById(R.id.rvFantasy);
        rvFantasy.setAdapter(new CoverSquareAdapter(
                db.getWritingItemsByTag("fantasy", 12),
                item -> openWritingDetail(item)
        ));

        RecyclerView rvScifi = findViewById(R.id.rvScifi);
        rvScifi.setAdapter(new CoverSquareAdapter(
                db.getWritingItemsByTag("sci-fi", 12),
                item -> openWritingDetail(item)
        ));

        RecyclerView rvMystery = findViewById(R.id.rvMystery);
        rvMystery.setAdapter(new CoverSquareAdapter(
                db.getWritingItemsByTag("mystery", 12),
                item -> openWritingDetail(item)
        ));
    }

    /** ใส่ข้อความลงใน TextView tvSectionTitle */
    private void setSectionText(int includeId, String title) {
        View row = findViewById(includeId);
        if (row == null) return;
        TextView tv = row.findViewById(R.id.tvSectionTitle);
        if (tv != null) tv.setText(title);
    }

    /** ตั้ง LayoutManager แนวนอนให้กับ RecyclerView */
    private void setHorizontalList(int recyclerId) {
        RecyclerView rv = findViewById(recyclerId);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rv.setHasFixedSize(true);
        }
    }

    private void openWritingDetail(WritingItem item) {
        if (item == null) return;
        Intent i = new Intent(this, ReadingMainActivity.class);
        i.putExtra(ReadingMainActivity.EXTRA_WRITING_ID, item.getId()); // แทน "writing_id"
        startActivity(i);
    }

    // เปิดหน้า CategoryListActivity พร้อมชื่อหมวด
    private void openCategory(String category) {
        if (category == null || category.trim().isEmpty()) return;
        Intent i = new Intent(this, CategoryListActivity.class);
        i.putExtra(CategoryListActivity.EXTRA_CATEGORY, category);
        startActivity(i);
    }

    // ผูกคลิกทั้งแถวให้พาไปหน้า Category
    private void wireSectionNav(int includeId, String category) {
        View row = findViewById(includeId);
        if (row == null) return;

        View arrow = row.findViewById(R.id.btnMore);
        if (arrow != null) {
            arrow.setOnClickListener(v -> openCategory(category));
        }
    }


}

package com.example.storysphere_appbar;


import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;


public class DeleteWritingActivity extends AppCompatActivity {

    private Spinner spinnerTitle, spinnerEpisode;
    private Button btnDelete;

    private DBHelper db;
    private final List<Integer> writingIds = new ArrayList<>();
    private final List<Integer> episodeIds = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_writing);

        // ✅ Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // เปิดปุ่ม back
        }

        spinnerTitle   = findViewById(R.id.spinnerTitle);
        spinnerEpisode = findViewById(R.id.spinnerEpisode);
        btnDelete      = findViewById(R.id.btnDelete);
        db             = new DBHelper(this);

        // ดึง writing_id ที่ส่งมาจากหน้าเดิม (ถ้ามี)
        int writingIdFromIntent = getIntent().getIntExtra("writing_id", -1);

        // 1) โหลดรายชื่อเรื่อง
        loadTitles();

        // 1.1) ถ้ามี writingId ที่ส่งมา ให้เลือกเรื่องนั้น
        if (writingIdFromIntent != -1) {
            int index = writingIds.indexOf(writingIdFromIntent);
            if (index >= 0) {
                spinnerTitle.setSelection(index);
            }
        }

        // 2) เปลี่ยนตอนตามเรื่องที่เลือก
        spinnerTitle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < writingIds.size()) {
                    int selectedWritingId = writingIds.get(position);
                    loadEpisodes(selectedWritingId);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnDelete.setOnClickListener(v -> {
            if (writingIds.isEmpty()) {
                Toast.makeText(this, "ยังไม่มีเรื่องให้ลบ", Toast.LENGTH_SHORT).show();
                return;
            }
            int writingIndex = spinnerTitle.getSelectedItemPosition();
            if (writingIndex < 0 || writingIndex >= writingIds.size()) return;

            int selectedWritingId = writingIds.get(writingIndex);
            int episodeIndex = spinnerEpisode.getSelectedItemPosition();

            final boolean deleteWholeWriting = (episodeIndex == 0);
            final String confirmMsg = deleteWholeWriting
                    ? "คุณแน่ใจหรือไม่ว่าจะลบงานเขียนนี้ทั้งหมด?"
                    : "คุณแน่ใจหรือไม่ว่าจะลบตอนนี้?";

            // root ที่ปลอดภัยสุด: content view ของ Activity นี้
            View root = findViewById(android.R.id.content);

            PopupUtil.showConfirm(root, confirmMsg, () -> {
                boolean ok;
                if (deleteWholeWriting) {
                    ok = db.deleteWriting(selectedWritingId);
                    Toast.makeText(this, ok ? "ลบทั้งเรื่องสำเร็จ" : "ลบเรื่องไม่สำเร็จ", Toast.LENGTH_SHORT).show();
                } else {
                    int listIndex = episodeIndex - 1;
                    if (listIndex < 0 || listIndex >= episodeIds.size()) return;
                    int episodeId = episodeIds.get(listIndex);

                    ok = db.deleteEpisode(episodeId);
                    Toast.makeText(this, ok ? "ลบตอนสำเร็จ" : "ลบตอนไม่สำเร็จ", Toast.LENGTH_SHORT).show();
                }
                // กลับหน้า add3 ให้มัน onResume() → loadEpisodes() เอง
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    private void loadTitles() {
        writingIds.clear();
        List<String> titles = new ArrayList<>();

        try (Cursor c = db.getAllWritings()) {
            if (c != null) {
                while (c.moveToNext()) {
                    int id = c.getInt(c.getColumnIndexOrThrow("id"));
                    String title = c.getString(c.getColumnIndexOrThrow("title"));
                    writingIds.add(id);
                    titles.add(title != null && !title.isEmpty() ? title : "(ไม่มีชื่อเรื่อง)");
                }
            }
        }

        if (titles.isEmpty()) {
            titles.add("— ไม่มีเรื่อง —");
        }

        ArrayAdapter<String> adapterTitle =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, titles);
        adapterTitle.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTitle.setAdapter(adapterTitle);

        if (!writingIds.isEmpty()) {
            loadEpisodes(writingIds.get(0));
        } else {
            ArrayAdapter<String> eAdapter =
                    new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                            new String[]{"— ลบทั้งเรื่อง —"});
            eAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerEpisode.setAdapter(eAdapter);
        }
    }

    private void loadEpisodes(int writingId) {
        episodeIds.clear();
        List<String> items = new ArrayList<>();
        items.add("— ลบทั้งเรื่อง —"); // position 0

        List<Episode> eps = db.getEpisodesByWritingId(writingId);
        for (Episode e : eps) {
            episodeIds.add(e.episodeId);
            items.add(e.title != null && !e.title.isEmpty() ? e.title : "(ตอนไม่มีชื่อ)");
        }

        ArrayAdapter<String> eAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        eAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEpisode.setAdapter(eAdapter);
    }
    public boolean onSupportNavigateUp() {
        onBackPressed();  // หรือ finish();
        return true;
    }
}
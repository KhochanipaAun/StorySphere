package com.example.storysphere_appbar;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;  // เพิ่ม import

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.storysphere_appbar.Episode;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;


public class Writing_Add_Episode3 extends AppCompatActivity {

    private static final int REQ_ADD_EDIT_EPISODE = 1001;

    private int writingId = -1;
    private TextView bookTitle, bookTagline;
    private DBHelper dbHelper;

    // ✅ เพิ่ม: สำหรับลิสต์ตอน
    private RecyclerView recyclerView;
    private EpisodeAdapter episodeAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_writing_add_episode3);

        new DBHelper(this).ensureEpisodesTable();

        // ✅ Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // เปิดปุ่ม back
        }

        // ✅ ต้องผูกกับ field ของคลาส
        bookTitle = findViewById(R.id.bookTitle);
        bookTagline = findViewById(R.id.bookTagline);
        dbHelper = new DBHelper(this);

        // เพิ่มบรรทัดนี้หลังจากสร้าง dbHelper
        dbHelper.ensureEpisodesTable();

        // ✅ เก็บค่า writing_id
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("writing_id")) {
            writingId = intent.getIntExtra("writing_id", -1);
            // Toast.makeText(this, "writingId=" + writingId, Toast.LENGTH_SHORT).show();
        }

// ✅ ปุ่ม Add new episode -> ไปหน้า AddNovelActivity (โหมด create)
        findViewById(R.id.btnAddEpisode).setOnClickListener(v -> {
            Intent add = new Intent(this, AddNovelActivity.class);
            add.putExtra("mode", "create");
            add.putExtra("writing_id", writingId);   // <- ต้องไม่เป็น -1
            startActivityForResult(add, REQ_ADD_EDIT_EPISODE);
        });

// ✅ ตั้งค่า RecyclerView (รายการตอน)
        recyclerView = findViewById(R.id.recyclerViewEpisodes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
// EpisodeAdapter ต้องมี constructor ที่รับ (List<Episode>, OnEditClick)
        episodeAdapter = new EpisodeAdapter(new ArrayList<>(), this::onEditEpisodeClick);
        recyclerView.setAdapter(episodeAdapter);


        // ✅ เรียกหน้า edit
        bookTitle.setOnClickListener(v -> {
            Intent editIntent = new Intent(Writing_Add_Episode3.this, activity_writnig_edit_writting.class);
            editIntent.putExtra("writing_id", writingId);
            startActivityForResult(editIntent, 1);
        });

        // ไปหน้า DeleteWritingActivity
        TextView deleteButton = findViewById(R.id.textView4);
        deleteButton.setOnClickListener(v -> {
            Intent it = new Intent(Writing_Add_Episode3.this, DeleteWritingActivity.class);
            it.putExtra("writing_id", writingId);   // ส่ง id ของเรื่องไปใช้โหลด/ลบ
            startActivity(it);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWritingData(); // โหลดข้อมูลล่าสุด
        dbHelper.backfillEpisodeNumbersIfNeeded();
        loadEpisodes();   // ✅ โหลดรายการตอนทุกครั้งที่กลับเข้าหน้านี้
    }

    // ✅ โหลดรายการตอนจาก DB มาใส่ RecyclerView
    private void loadEpisodes() {
        List<Episode> list = dbHelper.getEpisodesByWritingId(writingId);
        if (episodeAdapter != null) {
            episodeAdapter.submit(list); // ให้ adapter มีเมธอด submit(list) เพื่อ notifyDataSetChanged ภายใน
        }
    }

    // ✅ คลิกไอคอนดินสอ -> ไปแก้ตอนใน AddNovelActivity (โหมด edit)
    private void onEditEpisodeClick(Episode ep) {
        Intent edit = new Intent(this, AddNovelActivity.class);
        edit.putExtra("mode", "edit");
        edit.putExtra("episode_id", ep.episodeId);
        startActivityForResult(edit, REQ_ADD_EDIT_EPISODE);
    }

    // ✅ โหลดข้อมูล title, tagline จาก SQLite
    private void loadWritingData() {
        Cursor cursor = dbHelper.getWritingById(writingId);
        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String tagline = cursor.getString(cursor.getColumnIndexOrThrow("tagline"));
            String tagString = cursor.getString(cursor.getColumnIndexOrThrow("tag")); // ✅ โหลดแท็กจาก DB

            bookTitle.setText(title);
            bookTagline.setText(tagline);

            // ✅ แปลงเป็น List แล้วส่งไป showTags
            if (tagString != null && !tagString.trim().isEmpty()) {
                List<String> tagList = Arrays.asList(tagString.split(","));
                showTags(tagList);
            } else {
                showTags(new ArrayList<>()); // ถ้าไม่มีแท็ก เคลียร์คอนเทนเนอร์
            }

            cursor.close();
        }
    }

    // ✅ โหลดใหม่หลังจากกลับจากหน้าแก้ไข
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadWritingData();
        }
        if (requestCode == REQ_ADD_EDIT_EPISODE && resultCode == RESULT_OK) {
            loadEpisodes();   // ✅ เซฟ/แก้ไขตอนเสร็จ รีโหลดลิสต์
        }
    }

    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(this, activity_writing.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // ✅ กัน Stack ซ้อน
        startActivity(intent);
        finish(); // ปิดหน้านี้
        return true;
    }

    private void showTags(List<String> tags) {
        LinearLayout tagContainer = findViewById(R.id.tagContainer);
        tagContainer.removeAllViews(); // ล้างของเก่าออก

        LayoutInflater inflater = LayoutInflater.from(this);

        for (String tag : tags) {
            // Inflate layout ของแท็ก
            View tagView = inflater.inflate(R.layout.item_tag, tagContainer, false);

            // หา TextView และตั้งค่า
            TextView tvTagName = tagView.findViewById(R.id.tagName);
            tvTagName.setText(tag);

            // เพิ่มเข้าไปใน container
            tagContainer.addView(tagView);
        }
    }



}
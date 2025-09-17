package com.example.storysphere_appbar;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class ReadingMainActivity extends AppCompatActivity {

    public static final String EXTRA_WRITING_ID = "writing_id";
    public static final String EXTRA_EPISODE_ID = "episode_id";

    private DBHelper db;
    private int writingId;
    private RecyclerView rvEpisodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_main);

        db = new DBHelper(this);

        // ---- Toolbar: ซ่อน title ของแอปให้โล่ง ----
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
            toolbar.setTitle("");
            toolbar.setSubtitle("");
            toolbar.setNavigationIcon(R.drawable.round_arrow_back_ios_24);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // ---- รับ writingId (ถ้าไม่มีใช้เรื่องล่าสุด) ----
        // รองรับทั้ง key เดิม ("writing_id") และแบบใหม่ ("extra_writing_id")
        int fromExtra1 = getIntent().getIntExtra("writing_id", -1);
        int fromExtra2 = getIntent().getIntExtra(EXTRA_WRITING_ID, -1); // EXTRA_WRITING_ID = "extra_writing_id"

        writingId = (fromExtra2 > 0) ? fromExtra2 : fromExtra1;
        if (writingId <= 0) writingId = pickLatestWritingId();

        // ---- Header เรื่อง + แท็ก ----
        bindStoryHeader(writingId);

        // ---- ลิสต์ตอน ----
        rvEpisodes = findViewById(R.id.recyclerViewEpisodes);
        rvEpisodes.setLayoutManager(new LinearLayoutManager(this));
        rvEpisodes.setHasFixedSize(false);

        List<Episode> eps = db.getEpisodesByWritingId(writingId);
        EpisodeReadingAdapter adp = new EpisodeReadingAdapter(eps, ep -> {
            Intent it = new Intent(this, activity_episode.class);
            it.putExtra(EXTRA_WRITING_ID, ep.writingId);
            it.putExtra(EXTRA_EPISODE_ID, ep.episodeId);
            startActivity(it);
        });
        rvEpisodes.setAdapter(adp);

        // ---- Bottom nav (ถ้ามีเมนู) ----
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) bottomNav.setOnItemSelectedListener(item -> true);
    }

    private int pickLatestWritingId() {
        Cursor c = null;
        try {
            c = db.getReadableDatabase()
                    .rawQuery("SELECT id FROM " + DBHelper.TABLE_WRITINGS + " ORDER BY id DESC LIMIT 1", null);
            if (c.moveToFirst()) return c.getInt(0);
            return -1;
        } finally {
            if (c != null) c.close();
        }
    }

    private void bindStoryHeader(int writingId) {
        Cursor cur = null;
        try {
            cur = db.getWritingById(writingId);
            if (cur != null && cur.moveToFirst()) {
                String title   = cur.getString(cur.getColumnIndexOrThrow("title"));
                String tagline = cur.getString(cur.getColumnIndexOrThrow("tagline"));
                String image   = cur.getString(cur.getColumnIndexOrThrow("image_path"));
                String tagsCsv = cur.getString(cur.getColumnIndexOrThrow("tag"));

                // การ์ดหัวเรื่อง (include: @id/storyCard จาก item_story_reading)
                View card = findViewById(R.id.storyCard);
                if (card != null) {
                    TextView  tvTitle   = card.findViewById(R.id.textTitle);
                    TextView  tvTagline = card.findViewById(R.id.textBlurb);
                    ImageView ivCover   = card.findViewById(R.id.imageView);
                    if (tvTitle != null)   tvTitle.setText(title);
                    if (tvTagline != null) tvTagline.setText(tagline);
                    // โหลดรูปถ้าต้องการด้วย Glide/Picasso
                }

                // ---- จัดแท็กให้อยู่ "ด้านขวาของไอคอน" แบบโปรแกรมมิ่ง + ทำสีม่วง ----
                LinearLayout tagContainer = findViewById(R.id.tagContainer);
                ImageView imgHome = findViewById(R.id.imgHome);
                if (tagContainer != null && imgHome != null) {
                    // บังคับ constraints: startToEnd=imgHome, top/bottom align กับ imgHome, endToEnd=parent
                    ConstraintLayout.LayoutParams lp =
                            (ConstraintLayout.LayoutParams) tagContainer.getLayoutParams();
                    lp.startToEnd = R.id.imgHome;
                    lp.topToTop   = R.id.imgHome;
                    lp.bottomToBottom = R.id.imgHome;
                    lp.endToEnd   = ConstraintLayout.LayoutParams.PARENT_ID;
                    tagContainer.setLayoutParams(lp);

                    // เติมชิปแท็ก
                    tagContainer.removeAllViews();
                    if (tagsCsv != null && !tagsCsv.trim().isEmpty()) {
                        for (String t : tagsCsv.split(",")) {
                            TextView chip = buildPurpleTag(t.trim());
                            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            clp.setMarginEnd(dp(8));
                            chip.setLayoutParams(clp);
                            tagContainer.addView(chip);
                        }
                    }
                }
            }
        } finally {
            if (cur != null) cur.close();
        }
    }

    // ---------- ช่วยสร้างชิปแท็ก "สีม่วง" ----------
    private TextView buildPurpleTag(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(12f);
        tv.setIncludeFontPadding(false);
        tv.setTextColor(ContextCompat.getColor(this, android.R.color.white));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(ContextCompat.getColor(this, R.color.purple1)); // พื้นหลังม่วง
        bg.setCornerRadius(dp(20));
        tv.setBackground(bg);
        tv.setPadding(dp(12), dp(6), dp(12), dp(6));
        return tv;
    }

    private int dp(int v) {
        return Math.round(getResources().getDisplayMetrics().density * v);
    }
}

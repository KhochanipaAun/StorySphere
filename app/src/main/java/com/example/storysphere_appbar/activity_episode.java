package com.example.storysphere_appbar;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spanned;
import android.graphics.text.LineBreaker; // break/justification constants
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.List;

public class activity_episode extends AppCompatActivity {

    private DBHelper db;
    private DrawerLayout drawer;
    private RecyclerView rvEpisodes;

    // BottomSheet Comment
    private BottomSheetBehavior<FrameLayout> commentsSheet;
    private RecyclerView rvComments;
    private CommentAdapter commentAdapter;
    private EditText edtComment;
    private View btnSendComment;

    private TextView tvEpisodeNumber, tvEpisodeTitle, tvEpisodeHeading, tvEpisodeContent;

    private int writingId;
    private int currentEpisodeId;
    private List<Episode> episodeList;
    private int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode);

        // ปุ่ม back มุมซ้ายบน (ใน Toolbar custom)
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Intent up = new Intent(activity_episode.this, ReadingMainActivity.class);
                up.putExtra(ReadingMainActivity.EXTRA_WRITING_ID, writingId);
                up.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(up);
            });
        }

        db = new DBHelper(this);
        db.ensureCommentTables(); // สร้างตารางคอมเมนต์/ไลก์ถ้ายังไม่มี

        // รับพารามิเตอร์
        writingId        = getIntent().getIntExtra(ReadingMainActivity.EXTRA_WRITING_ID, -1);
        currentEpisodeId = getIntent().getIntExtra(ReadingMainActivity.EXTRA_EPISODE_ID, -1);

        // Bind views
        drawer            = findViewById(R.id.drawerLayout);
        rvEpisodes        = findViewById(R.id.rvEpisodes);
        tvEpisodeNumber   = findViewById(R.id.tvEpisodeNumber);
        tvEpisodeTitle    = findViewById(R.id.tvEpisodeTitle);
        tvEpisodeHeading  = findViewById(R.id.tvEpisodeHeading);
        tvEpisodeContent  = findViewById(R.id.tvEpisodeContent);

        // Drawer toggle
        View actionEpisodes = findViewById(R.id.actionEpisodes);
        if (actionEpisodes != null) actionEpisodes.setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));
        View btnClose = findViewById(R.id.btnCloseDrawer);
        if (btnClose != null) btnClose.setOnClickListener(v -> drawer.closeDrawer(GravityCompat.START));

        // โหลดตอนทั้งหมดของเรื่อง
        episodeList = db.getEpisodesByWritingId(writingId);

        // รายการตอนใน Drawer
        rvEpisodes.setLayoutManager(new LinearLayoutManager(this));
        rvEpisodes.setAdapter(new EpisodeDrawerAdapter(episodeList, ep -> {
            drawer.closeDrawer(GravityCompat.START);
            setPosition(indexOfEpisode(ep.episodeId));
        }));

        // ตำแหน่งเริ่มต้น
        if ((currentEpisodeId <= 0) && episodeList != null && !episodeList.isEmpty()) {
            currentEpisodeId = episodeList.get(0).episodeId;
        }
        setPosition(indexOfEpisode(currentEpisodeId));

        // ปุ่ม Back / Next
        View prev = findViewById(R.id.actionPrev);
        View next = findViewById(R.id.actionNext);
        if (prev != null) prev.setOnClickListener(v -> { if (position > 0) setPosition(position - 1); });
        if (next != null) next.setOnClickListener(v -> {
            if (episodeList != null && position < episodeList.size() - 1) setPosition(position + 1);
        });

        // ตั้งค่า BottomSheet คอมเมนต์
        setupCommentsSheet();
    }

    /** เตรียม BottomSheet คอมเมนต์ + เดินสายปุ่ม */
    private void setupCommentsSheet() {
        FrameLayout sheet = findViewById(R.id.bottomSheetComments);
        if (sheet == null) return;

        commentsSheet = BottomSheetBehavior.from(sheet);
        sheet.setVisibility(View.VISIBLE);
        commentsSheet.setHideable(true);
        commentsSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

        rvComments    = sheet.findViewById(R.id.rvComments);
        edtComment    = sheet.findViewById(R.id.edtComment);
        btnSendComment= sheet.findViewById(R.id.btnSendComment);

        if (rvComments != null) {
            rvComments.setLayoutManager(new LinearLayoutManager(this));

            // เส้นคั่นเว้น avatar (ถ้ามี divider_inset.xml)
            try {
                DividerItemDecoration deco =
                        new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
                Drawable d = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.divider_inset);
                if (d != null) deco.setDrawable(d);
                rvComments.addItemDecoration(deco);
            } catch (Exception ignore) {}

            // ใช้ CommentAdapter เวอร์ชันมี Listener
            commentAdapter = new CommentAdapter(new CommentAdapter.OnCommentActionListener() {
                @Override public void onLikeToggle(CommentAdapter.Comment c) {
                    // (ออปชัน) ทำระบบ like comment ในภายหลัง
                }
                @Override public void onReply(CommentAdapter.Comment c) {
                    if (edtComment != null) {
                        edtComment.requestFocus();
                        edtComment.setText("@" + c.userName + " ");
                        edtComment.setSelection(edtComment.getText().length());
                    }
                }
                @Override public void onReport(CommentAdapter.Comment c) {
                    Toast.makeText(activity_episode.this, "Reported", Toast.LENGTH_SHORT).show();
                }
                @Override public void onDelete(CommentAdapter.Comment c) {
                    String me = db.getLoggedInUserEmail();
                    if (me == null || me.isEmpty()) return;
                    if (db.deleteCommentIfOwner(c.id, me)) {
                        reloadCommentsIntoAdapter();
                    } else {
                        Toast.makeText(activity_episode.this, "Delete failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            rvComments.setAdapter(commentAdapter);
        }

        // เปิดแผ่นคอมเมนต์เมื่อกดปุ่ม
        View actionComment = findViewById(R.id.actionComment);
        if (actionComment != null) {
            actionComment.setOnClickListener(v -> {
                reloadCommentsIntoAdapter();                  // โหลดจาก DB ตาม episode ปัจจุบัน
                commentsSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
                scrollCommentsToBottom();
            });
        }

        // ปุ่มส่งคอมเมนต์
        if (btnSendComment != null) {
            btnSendComment.setOnClickListener(v -> {
                if (edtComment == null) return;
                String text = edtComment.getText() == null ? "" : edtComment.getText().toString().trim();
                if (text.isEmpty()) {
                    edtComment.setError("โปรดพิมพ์คอมเมนต์");
                    return;
                }

                long rowId = db.addComment(null /* resolve เป็นผู้ใช้ปัจจุบันภายใน DBHelper */,
                        writingId, currentEpisodeId, text);
                if (rowId == -1) {
                    Toast.makeText(this, "ส่งคอมเมนต์ไม่สำเร็จ", Toast.LENGTH_SHORT).show();
                    return;
                }

                // เคลียร์ + รีโหลด
                edtComment.setText("");
                reloadCommentsIntoAdapter();
                scrollCommentsToBottom();

                // แจ้งหน้าอื่นถ้ามีนับคอมเมนต์/สถิติ
                LocalBroadcastManager.getInstance(this).sendBroadcast(
                        new Intent(ReadingMainActivity.ACTION_WRITING_CHANGED)
                                .putExtra(ReadingMainActivity.EXTRA_WRITING_ID, writingId)
                );
            });
        }
    }

    /** โหลดคอมเมนต์จากตาราง comments โดยตรง แล้วใส่ลง Adapter */
    private void reloadCommentsIntoAdapter() {
        if (rvComments == null || commentAdapter == null) return;

        ArrayList<CommentAdapter.Comment> ui = new ArrayList<>();
        String me = db.getLoggedInUserEmail();
        if (me == null) me = "";

        // NOTE: ถ้าตารางคุณใช้คอลัมน์ชื่อ "text" ให้เปลี่ยน 'content' ใน SELECT และ getString(2) เป็น 'text'
        android.database.sqlite.SQLiteDatabase rdb = db.getReadableDatabase();
        android.database.Cursor c = rdb.rawQuery(
                "SELECT id, user_email, content, created_at " +
                        "FROM comments " +
                        "WHERE writing_id=? AND (episode_id=? OR episode_id IS NULL) " +
                        "ORDER BY created_at ASC",
                new String[]{ String.valueOf(writingId), String.valueOf(currentEpisodeId) });

        while (c.moveToNext()) {
            int    id        = c.getInt(0);
            String email     = c.getString(1);
            String content   = c.getString(2);
            long   createdAt = c.getLong(3);

            String display = null;
            try { display = db.getUserDisplayName(email); } catch (Exception ignored) {}
            if (display == null || display.trim().isEmpty()) display = email;

            boolean isWriter = false;
            try { isWriter = db.isUserWriter(email, writingId); } catch (Exception ignored) {}

            boolean mine = me.equalsIgnoreCase(email);

            ui.add(new CommentAdapter.Comment(
                    id,
                    display,
                    email,
                    null,                            // avatarUrl (ยังไม่ใช้)
                    content != null ? content : "",
                    isWriter,
                    createdAt,
                    false, 0,                        // likedByMe/likeCount (ยังไม่ใช้)
                    mine
            ));
        }
        c.close();

        commentAdapter.submitList(ui);
    }

    /** เลื่อน RecyclerView ไปท้ายสุด */
    private void scrollCommentsToBottom() {
        if (rvComments == null || rvComments.getAdapter() == null) return;
        int n = rvComments.getAdapter().getItemCount();
        if (n > 0) rvComments.scrollToPosition(n - 1);
    }

    /** แสดงตอนตาม id + บันทึกประวัติอ่าน (History) */
    private void showEpisode(int episodeId) {
        Episode ep = db.getEpisodeById(episodeId);
        if (ep == null) return;

        currentEpisodeId = ep.episodeId;
        writingId        = ep.writingId;

        if (tvEpisodeNumber != null) tvEpisodeNumber.setText("#" + ep.episodeNo);
        if (tvEpisodeTitle  != null) tvEpisodeTitle.setText(ep.title);
        if (tvEpisodeHeading!= null) tvEpisodeHeading.setText(ep.title);

        // แปลง HTML ให้ TextView
        String html = ep.contentHtml == null ? "" : ep.contentHtml;
        html = normalizeHtmlForTextView(html);
        Spanned sp = androidx.core.text.HtmlCompat.fromHtml(html, androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY);

        if (tvEpisodeContent != null) {
            tvEpisodeContent.setText(sp);

            // break strategy: API 23+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                tvEpisodeContent.setBreakStrategy(LineBreaker.BREAK_STRATEGY_SIMPLE);
            }

            // justification mode: API 33+
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                tvEpisodeContent.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_NONE);
            }
        }

        // เลื่อนกลับบนสุด
        NestedScrollView ns = findViewById(R.id.scrollContent);
        if (ns != null) ns.smoothScrollTo(0, 0);

        // บันทึก History
        String email = db.getLoggedInUserEmail();
        if (email == null || email.trim().isEmpty()) email = "guest";
        db.addHistory(email, writingId, ep.episodeId);

        // ถ้า BottomSheet เปิดอยู่ ให้รีโหลดคอมเมนต์ของตอนนี้
        if (commentsSheet != null && commentsSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            reloadCommentsIntoAdapter();
        }
    }

    /** ตั้งค่าตำแหน่งตอนใหม่ + อัปเดตหน้าจอ + ปุ่มขอบ */
    private void setPosition(int newPos) {
        if (episodeList == null || episodeList.isEmpty()) return;
        if (newPos < 0) newPos = 0;
        if (newPos > episodeList.size() - 1) newPos = episodeList.size() - 1;

        position = newPos;
        Episode ep = episodeList.get(position);
        showEpisode(ep.episodeId);
        updatePrevNextState();
    }

    /** หา index ของ episodeId ใน list */
    private int indexOfEpisode(int episodeId) {
        if (episodeList == null) return 0;
        for (int i = 0; i < episodeList.size(); i++) {
            if (episodeList.get(i).episodeId == episodeId) return i;
        }
        return 0;
    }

    /** ทำปุ่ม Back/Next จางและ disabled เมื่อถึงขอบ */
    private void updatePrevNextState() {
        View prev = findViewById(R.id.actionPrev);
        View next = findViewById(R.id.actionNext);
        boolean canPrev = position > 0;
        boolean canNext = episodeList != null && position < episodeList.size() - 1;

        if (prev != null) { prev.setEnabled(canPrev); prev.setAlpha(canPrev ? 1f : 0.4f); }
        if (next != null) { next.setEnabled(canNext); next.setAlpha(canNext ? 1f : 0.4f); }
    }

    @Override
    public void onBackPressed() {
        if (commentsSheet != null && commentsSheet.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            commentsSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
            return;
        }
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // แปลง style text-align เป็นแท็กที่ TextView รองรับ
    private String normalizeHtmlForTextView(String html) {
        if (html == null) return "";
        html = html.replaceAll("(?is)<p[^>]*text-align\\s*:\\s*center[^>]*>(.*?)</p>", "<center>$1</center>");
        html = html.replaceAll("(?is)<div[^>]*text-align\\s*:\\s*center[^>]*>(.*?)</div>", "<center>$1</center>");
        html = html.replaceAll("(?is)<p[^>]*text-align\\s*:\\s*right[^>]*>(.*?)</p>", "<div align='right'>$1</div>");
        html = html.replaceAll("(?is)<div[^>]*text-align\\s*:\\s*right[^>]*>(.*?)</div>", "<div align='right'>$1</div>");
        html = html.replaceAll("(?is)<span[^>]*text-align\\s*:\\s*center[^>]*>(.*?)</span>", "<center>$1</center>");
        html = html.replaceAll("(?is)<span[^>]*text-align\\s*:\\s*right[^>]*>(.*?)</span>", "<div align='right'>$1</div>");
        return html;
    }
}

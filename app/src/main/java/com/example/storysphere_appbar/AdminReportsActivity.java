package com.example.storysphere_appbar;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * หน้าจอ Admin: แสดงรายการ Report ที่สถานะ OPEN
 * - ปุ่ม Resolve: ปิดเคส (ต้องมี db.resolveReport(reportId, note))
 * - ปุ่ม Ban: เปลี่ยน role ผู้ใช้เป็น 'banned' (ห้ามเขียน/คอมเมนต์)
 *
 * ต้องมีใน DBHelper:
 *   Cursor  getOpenReportsCursor()
 *   boolean resolveReport(int reportId, @Nullable String moderatorNote)
 */
public class AdminReportsActivity extends AppCompatActivity {

    private RecyclerView rv;
    private ReportAdapter adapter;
    private DBHelper db;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);

        db = new DBHelper(this);

        // ----- Toolbar + ปุ่มย้อนกลับแบบชัวร์ ๆ -----
        Toolbar tb = findViewById(R.id.toolbar); // XML อาจเป็น MaterialToolbar ก็ได้
        if (tb != null) {
            setSupportActionBar(tb);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true); // แสดงไอคอน ←
                getSupportActionBar().setTitle("Reports");
            }
            tb.setNavigationOnClickListener(v -> finish());            // คลิกแล้วปิดหน้า
        }

        SwipeRefreshLayout swipe = findViewById(R.id.swipe);
        if (swipe != null) {
            swipe.setColorSchemeResources(R.color.purple1, R.color.darkpurple);
            swipe.setOnRefreshListener(() -> {
                reload();
                swipe.setRefreshing(false);
            });
        }

        // ----- RecyclerView -----
        rv = findViewById(R.id.recyclerReports);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adapter = new ReportAdapter(new ArrayList<>(), new ReportAdapter.Action() {
            @Override public void onResolve(ReportRow row) {
                boolean ok = db.resolveReport(row.id, "resolved by admin");
                if (ok) {
                    Toast.makeText(AdminReportsActivity.this, "Resolved", Toast.LENGTH_SHORT).show();
                    reload();
                } else {
                    Toast.makeText(AdminReportsActivity.this, "Resolve failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onBan(ReportRow row) {
                String targetEmail = !TextUtils.isEmpty(row.commentOwnerEmail) ? row.commentOwnerEmail : null;
                if (targetEmail == null) {
                    Toast.makeText(AdminReportsActivity.this, "ไม่พบอีเมลเจ้าของคอมเมนต์", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean ok = banUserByEmail(targetEmail);
                Toast.makeText(
                        AdminReportsActivity.this,
                        ok ? ("Banned: " + targetEmail) : "Ban ไม่สำเร็จ",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
        rv.setAdapter(adapter);

        reload();
    }

    // รองรับระบบส่ง event ปุ่ม Up
    @Override public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // เผื่อผู้ผลิตบางรุ่นยิงเป็น options item แทน
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ----- โหลดข้อมูลรายงานทั้งหมดที่สถานะ OPEN -----
    private void reload() {
        List<ReportRow> rows = new ArrayList<>();
        try (Cursor c = db.getOpenReportsCursor()) {
            if (c != null) {
                int idxId      = c.getColumnIndex("id");
                int idxReason  = c.getColumnIndex("reason");
                int idxReporter= c.getColumnIndex("reporter_email");
                int idxCreated = c.getColumnIndex("created_at");
                int idxCText   = c.getColumnIndex("comment_text");
                int idxCOwner  = c.getColumnIndex("comment_owner");
                int idxWTitle  = c.getColumnIndex("writing_title");

                while (c.moveToNext()) {
                    ReportRow r = new ReportRow();
                    r.id                = idxId >= 0 ? c.getInt(idxId) : 0;
                    r.reason            = idxReason >= 0 ? c.getString(idxReason) : null;
                    r.reporterEmail     = idxReporter >= 0 ? c.getString(idxReporter) : null;
                    // ถ้า created_at เก็บเป็น seconds → แปลงเป็น ms; ถ้าเป็น ms อยู่แล้ว ให้เอา c.getLong(idxCreated) ตรง ๆ
                    r.createdAtMillis   = idxCreated >= 0 ? c.getLong(idxCreated) * 1000L : 0L;
                    r.commentText       = idxCText >= 0 ? c.getString(idxCText) : null;
                    r.commentOwnerEmail = idxCOwner >= 0 ? c.getString(idxCOwner) : null;
                    r.writingTitle      = idxWTitle >= 0 ? c.getString(idxWTitle) : null;
                    rows.add(r);
                }
            }
        }
        adapter.replace(rows);
    }

    /**
     * เซ็ต role='banned' ให้ผู้ใช้ด้วย email
     */
    private boolean banUserByEmail(@NonNull String email) {
        try {
            db.getWritableDatabase().execSQL(
                    "UPDATE users SET role='banned' WHERE email=?",
                    new Object[]{ email.trim() }
            );
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    // ===== Model =====
    public static class ReportRow {
        public int id;
        public String reason;
        public String reporterEmail;
        public long createdAtMillis;
        public String commentText;        // nullable
        public String commentOwnerEmail;  // nullable
        public String writingTitle;       // nullable
    }

    // ===== Adapter =====
    public static class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.VH> {

        interface Action {
            void onResolve(ReportRow row);
            void onBan(ReportRow row);
        }

        private final List<ReportRow> data;
        private final Action action;

        public ReportAdapter(List<ReportRow> data, Action action) {
            this.data = (data != null) ? data : new ArrayList<>();
            this.action = action;
            setHasStableIds(true);
        }

        public void replace(List<ReportRow> newData) {
            data.clear();
            if (newData != null) data.addAll(newData);
            notifyDataSetChanged();
        }

        @Override public long getItemId(int position) {
            return data.get(position).id;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_report, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            ReportRow r = data.get(position);

            // แสดงเวลา
            String when;
            if (r.createdAtMillis > 0) {
                Date d = new Date(r.createdAtMillis);
                DateFormat df = android.text.format.DateFormat.getMediumDateFormat(h.itemView.getContext());
                DateFormat tf = android.text.format.DateFormat.getTimeFormat(h.itemView.getContext());
                when = df.format(d) + " " + tf.format(d);
            } else {
                when = "-";
            }

            h.txtTitle.setText(!TextUtils.isEmpty(r.writingTitle) ? r.writingTitle : "(no title)");
            h.txtComment.setText(!TextUtils.isEmpty(r.commentText) ? r.commentText : "(no comment)");
            h.txtReporter.setText("Reporter: " + (r.reporterEmail != null ? r.reporterEmail : "-"));
            h.txtOwner.setText("Owner: " + (r.commentOwnerEmail != null ? r.commentOwnerEmail : "-"));
            h.txtReason.setText("Reason: " + (r.reason != null ? r.reason : "-"));
            h.txtWhen.setText(when);

            h.btnResolve.setOnClickListener(v -> { if (action != null) action.onResolve(r); });
            h.btnBan.setOnClickListener(v -> { if (action != null) action.onBan(r); });
        }

        @Override public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView txtTitle, txtComment, txtReporter, txtOwner, txtReason, txtWhen;
            Button btnResolve, btnBan;
            VH(@NonNull View v) {
                super(v);
                txtTitle    = v.findViewById(R.id.txtTitle);
                txtComment  = v.findViewById(R.id.txtComment);
                txtReporter = v.findViewById(R.id.txtReporter);
                txtOwner    = v.findViewById(R.id.txtOwner);
                txtReason   = v.findViewById(R.id.txtReason);
                txtWhen     = v.findViewById(R.id.txtWhen);
                btnResolve  = v.findViewById(R.id.btnResolve);
                btnBan      = v.findViewById(R.id.btnBan);
            }
        }
    }
}

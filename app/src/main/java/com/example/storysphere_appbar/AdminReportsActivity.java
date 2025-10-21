package com.example.storysphere_appbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class AdminReportsActivity extends AppCompatActivity {

    // ใช้ชื่อ action ให้สอดคล้องกับแพ็กเกจแอปนี้
    public static final String ACTION_REPORTS_CHANGED =
            "com.example.storysphere_appbar.REPORTS_CHANGED";

    private DBHelper db;
    private SwipeRefreshLayout swipe;
    private RecyclerView recycler;
    private LinearLayout emptyState;
    private ProgressBar progress;

    private ReportAdapter adapter;
    private final List<DBHelper.ReportItem> data = new ArrayList<>();

    // รับสัญญาณเมื่อมีรีพอร์ตใหม่ถูกสร้างจากฝั่งผู้ใช้
    private final BroadcastReceiver reportsChangedReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            loadReports();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);

        db = new DBHelper(this);

        // Toolbar
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Reports");
            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        swipe = findViewById(R.id.swipe);
        recycler = findViewById(R.id.recyclerReports);
        emptyState = findViewById(R.id.emptyState);
        progress = findViewById(R.id.progress);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReportAdapter(data, new ReportAdapter.OnReportAction() {
            @Override public void onBan(String offenderEmail, int reportId) {
                confirmBan(offenderEmail, reportId);
            }
            @Override public void onClear(int reportId) {
                confirmResolve(reportId);
            }
        });
        recycler.setAdapter(adapter);

        if (swipe != null) {
            swipe.setOnRefreshListener(this::loadReports);
        }

        loadReports();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                reportsChangedReceiver, new IntentFilter(ACTION_REPORTS_CHANGED)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(reportsChangedReceiver);
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setLoading(boolean loading) {
        if (progress != null) progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (swipe != null && swipe.isRefreshing() && !loading) swipe.setRefreshing(false);
    }

    private void showEmpty(boolean show) {
        if (emptyState != null) emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        if (recycler != null) recycler.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void loadReports() {
        setLoading(true);
        data.clear();

        Cursor c = null;
        try {
            c = db.getOpenReportsCursor();
            if (c != null && c.moveToFirst()) {
                int idxId           = c.getColumnIndexOrThrow("id");
                int idxReason       = c.getColumnIndexOrThrow("reason");
                int idxReporter     = c.getColumnIndexOrThrow("reporter_email");
                int idxCreatedAt    = c.getColumnIndexOrThrow("created_at");
                int idxCommentText  = c.getColumnIndexOrThrow("comment_text");
                int idxCommentOwner = c.getColumnIndexOrThrow("comment_owner");
                int idxWritingTitle = c.getColumnIndexOrThrow("writing_title");

                do {
                    DBHelper.ReportItem r = new DBHelper.ReportItem();
                    r.id               = c.getInt(idxId);
                    r.reason           = c.getString(idxReason);
                    r.reporterEmail    = c.getString(idxReporter);
                    r.createdAt    = c.getLong(idxCreatedAt);
                    r.commentContent   = c.getString(idxCommentText);
                    r.commentUserEmail = c.getString(idxCommentOwner);
                    r.writingTitle     = c.getString(idxWritingTitle);
                    // ถ้ามี episodeTitle ใน schema ให้ set เพิ่มที่นี่
                    data.add(r);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Toast.makeText(this, "Load failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (c != null) c.close();
        }

        adapter.notifyDataSetChanged();
        showEmpty(data.isEmpty());
        setLoading(false);
    }

    private void confirmBan(String email, int reportId) {
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "ไม่พบอีเมลผู้กระทำ", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Ban user?")
                .setMessage("แบนผู้ใช้: " + email + "\nและปิดเคสรีพอร์ตนี้?")
                .setPositiveButton("Ban & Resolve", (dialog, which) -> {
                    boolean okBan = false;
                    try {
                        // ต้องมีเมธอดนี้ใน DBHelper (ถ้าไม่มี ให้เพิ่ม)
                        okBan = db.setUserBanned(email, true);
                    } catch (Throwable ignore) { /* no-op */ }

                    boolean okResolve = db.resolveReport(reportId, "Banned by admin");

                    if (okBan && okResolve) {
                        Toast.makeText(this, "ดำเนินการเรียบร้อย", Toast.LENGTH_SHORT).show();
                        loadReports();
                    } else if (okResolve) {
                        Toast.makeText(this, "ปิดเคสแล้ว (แต่แบนผู้ใช้ไม่สำเร็จ)", Toast.LENGTH_SHORT).show();
                        loadReports();
                    } else {
                        Toast.makeText(this, "ดำเนินการไม่สำเร็จ", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmResolve(int reportId) {
        new AlertDialog.Builder(this)
                .setTitle("Resolve report?")
                .setMessage("ปิดเคสรีพอร์ต #" + reportId + " ?")
                .setPositiveButton("Resolve", (dialog, which) -> {
                    boolean ok = db.resolveReport(reportId, "Resolved by admin");
                    if (ok) {
                        Toast.makeText(this, "ปิดเคสเรียบร้อย", Toast.LENGTH_SHORT).show();
                        loadReports();
                    } else {
                        Toast.makeText(this, "ปิดเคสไม่สำเร็จ", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

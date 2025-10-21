package com.example.storysphere_appbar;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.util.List;

public class UserActivity extends AppCompatActivity {

    private DBHelper db;
    private RecyclerView rv;
    private TextView txtEmpty;
    private NotificationAdapter adapter;
    private String currentUserEmail;

    private final BroadcastReceiver notifReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (DBHelper.ACTION_NOTIFICATIONS_CHANGED.equals(intent.getAction())) {
                loadData();
            }
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        db = new DBHelper(this);
        currentUserEmail = db.getLoggedInUserEmail();

        // Toolbar (ถ้าใน activity_user.xml มี id=toolbar)
        MaterialToolbar tb = findViewById(R.id.toolbar);
        if (tb != null) {
            setSupportActionBar(tb);
            tb.setNavigationOnClickListener(v -> onBackPressed());
        }

        // RecyclerView + Empty view
        rv = findViewById(R.id.recyclerViewActivity);
        txtEmpty = findViewById(R.id.txtEmpty); // แนะนำเพิ่ม TextView นี้ใน layout (ดูข้อ 3)

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter((item, pos) -> {
            db.markNotificationRead(item.id);
            openByPayload(item);
        });
        rv.setAdapter(adapter);

        loadData();

        // ฟัง broadcast เพื่อรีเฟรชเมื่อมี noti ใหม่/อ่านแล้ว/ลบ
        LocalBroadcastManager.getInstance(this).registerReceiver(
                notifReceiver, new IntentFilter(DBHelper.ACTION_NOTIFICATIONS_CHANGED));

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_activity);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0,0);
                finish();
                return true;

            } else if (id == R.id.nav_library) {
                startActivity(new Intent(this, LibraryHistoryActivity.class));
                overridePendingTransition(0,0);
                finish();
                return true;

            } else if (id == R.id.nav_writing) {
                startActivity(new Intent(this, activity_writing.class));
                overridePendingTransition(0,0);
                finish();
                return true;

            } else if (id == R.id.nav_activity) {
                return true; // อยู่หน้าปัจจุบันแล้ว
            }
            return false;
        });
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notifReceiver);
        super.onDestroy();
    }

    private void loadData() {
        List<DBHelper.NotificationItem> items = db.getNotifications(currentUserEmail, 200);
        adapter.submit(items);
        if (txtEmpty != null) {
            txtEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    /** เปิดหน้าที่เกี่ยวข้องตาม payload/type */
    private void openByPayload(DBHelper.NotificationItem item) {
        try {
            if (item.payload != null && !item.payload.trim().isEmpty()) {
                JSONObject obj = new JSONObject(item.payload);

                if ("FOLLOW".equalsIgnoreCase(item.type)) {
                    String followerEmail = obj.optString("followerEmail", null);
                    if (followerEmail != null) {
                        Intent i = new Intent(this, UserDetailActivity.class); // เปลี่ยนเป็น UserDetailActivity ถ้าคลาสคุณใช้ชื่อนั้น
                        i.putExtra("email", followerEmail);
                        startActivity(i);
                        return;
                    }
                } else if ("NEW_EPISODE".equalsIgnoreCase(item.type)) {
                    int writingId = obj.optInt("writingId", -1);
                    int episodeNo = obj.optInt("episodeNo", -1);
                    if (writingId > 0 && episodeNo > 0) {
                        Intent i = new Intent(this, activity_episode.class);
                        i.putExtra("writing_id", writingId);
                        i.putExtra("episode_no", episodeNo);
                        startActivity(i);
                        return;
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}

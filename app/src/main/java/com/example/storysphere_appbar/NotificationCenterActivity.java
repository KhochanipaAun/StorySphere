package com.example.storysphere_appbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONObject;

import java.util.List;

public class NotificationCenterActivity extends AppCompatActivity {

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

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        db = new DBHelper(this);
        currentUserEmail = getLoggedInEmail();

        MaterialToolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> onBackPressed());

        rv = findViewById(R.id.rvNotifs);
        txtEmpty = findViewById(R.id.txtEmpty);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter((DBHelper.NotificationItem item, int pos) -> {
            db.markNotificationRead(item.id);
            openByPayload(item);
            adapter.notifyItemChanged(pos);
        });
        rv.setAdapter(adapter);

        loadData();

        LocalBroadcastManager.getInstance(this).registerReceiver(
                notifReceiver, new IntentFilter(DBHelper.ACTION_NOTIFICATIONS_CHANGED));
    }

    @Override protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notifReceiver);
        super.onDestroy();
    }

    private void loadData() {
        List<DBHelper.NotificationItem> items = db.getNotifications(currentUserEmail, 200);
        adapter.submit(items);
        txtEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ใช้ชนิด DBHelper.NotificationItem ให้ตรงกัน
    private void openByPayload(DBHelper.NotificationItem item) {
        try {
            if (item.payload != null && !item.payload.trim().isEmpty()) {
                JSONObject obj = new JSONObject(item.payload);

                if ("FOLLOW".equalsIgnoreCase(item.type)) {
                    String followerEmail = obj.optString("followerEmail", null);
                    if (followerEmail != null) {
                        // แก้ชื่อคลาสให้ถูกกับโปรเจกต์จริง ๆ
                        Intent i = new Intent(this, UserDetailActivity.class);
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

    private String getLoggedInEmail() {
        SharedPreferences sp = getSharedPreferences("sess", MODE_PRIVATE);
        return sp.getString("email", null);
    }
}

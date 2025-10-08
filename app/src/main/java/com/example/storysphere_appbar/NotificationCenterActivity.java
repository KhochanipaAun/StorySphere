package com.example.storysphere_appbar;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NotificationCenterActivity extends AppCompatActivity implements NotificationAdapter.OnNotifClick {
    private RecyclerView rv;
    private DBHelper db;
    private NotificationAdapter adapter;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_center);
        db = new DBHelper(this);

        rv = findViewById(R.id.rvNotifs);
        rv.setLayoutManager(new LinearLayoutManager(this));

        String me = db.getLoggedInUserEmail();
        List<DBHelper.Notification> items = db.getUnreadNotifications(me);
        adapter = new NotificationAdapter(items, this);
        rv.setAdapter(adapter);
    }

    @Override public void onOpen(DBHelper.Notification n) {
        boolean ok = db.markNotificationRead(n.id);
        if (ok) Toast.makeText(this, "อ่านแล้ว", Toast.LENGTH_SHORT).show();
        // TODO: เปิด deeplink/payload ถ้ามี
    }
}

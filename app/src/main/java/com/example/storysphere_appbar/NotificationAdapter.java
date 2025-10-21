package com.example.storysphere_appbar;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    public interface OnItemClick {
        void onClick(DBHelper.NotificationItem item, int position);
    }

    private final List<DBHelper.NotificationItem> data = new ArrayList<>();
    private final OnItemClick click;

    public NotificationAdapter(OnItemClick click) {
        this.click = click;
    }

    public void submit(List<DBHelper.NotificationItem> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        DBHelper.NotificationItem it = data.get(position);

        // Title/Content
        h.title.setText(it.title != null && !it.title.isEmpty() ? it.title : it.type);
        h.content.setText(it.message != null ? it.message : "");

        // Date (milliseconds → format)
        h.date.setText(DateFormat.format("dd MMM yyyy HH:mm", it.createdAt));

        // Icon (optional mapping by type)
        if ("FOLLOW".equalsIgnoreCase(it.type)) {
            // ใช้ไอคอนที่คุณมีในโปรเจ็กต์แทนได้
            h.icon.setImageResource(R.drawable.ic_user);
        } else if ("NEW_EPISODE".equalsIgnoreCase(it.type)) {
            h.icon.setImageResource(R.drawable.ic_activity);
        } else {
            h.icon.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // แตะรายการ
        h.itemView.setOnClickListener(v -> {
            if (click != null) click.onClick(it, position);
        });

        // สไตล์อ่านแล้ว/ยังไม่อ่าน (ถ้าต้องการ)
        float alpha = it.isRead ? 0.6f : 1.0f;
        h.itemView.setAlpha(alpha);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title, content, date;
        VH(@NonNull View v) {
            super(v);
            icon    = v.findViewById(R.id.notiIcon);
            title   = v.findViewById(R.id.notiTitle);
            content = v.findViewById(R.id.notiContent);
            date    = v.findViewById(R.id.notiDate);
        }
    }
}

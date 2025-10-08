package com.example.storysphere_appbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    public interface OnNotifClick {
        void onOpen(DBHelper.Notification n);
    }

    private final List<DBHelper.Notification> data;
    private final OnNotifClick click;

    public NotificationAdapter(List<DBHelper.Notification> data, OnNotifClick click) {
        this.data = data;
        this.click = click;
        setHasStableIds(true);
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_notification, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int i) {
        DBHelper.Notification n = data.get(i);
        h.txtTitle.setText(n.title != null ? n.title : n.type);
        h.txtMsg.setText(n.message != null ? n.message : "");
        h.itemView.setOnClickListener(v -> { if (click != null) click.onOpen(n); });
    }

    @Override public int getItemCount() { return data==null?0:data.size(); }
    @Override public long getItemId(int position) { return data.get(position).id; }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtTitle, txtMsg;
        VH(View v) {
            super(v);
            txtTitle = v.findViewById(R.id.txtTitle);
            txtMsg   = v.findViewById(R.id.txtMsg);
        }
    }
}

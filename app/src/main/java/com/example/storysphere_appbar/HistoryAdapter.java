package com.example.storysphere_appbar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {
    private final Context ctx;
    private final List<DBHelper.HistoryItem> data;
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault());
    private final DBHelper db;

    public HistoryAdapter(Context ctx, List<DBHelper.HistoryItem> data) {
        this.ctx = ctx;
        this.data = data;
        this.db   = new DBHelper(ctx);
    }

    public void replace(List<DBHelper.HistoryItem> newData) {
        data.clear();
        if (newData != null) data.addAll(newData);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView title, author, chapter;
        TextView tvBm, tvLike, tvView;
        View statRow;
        ImageView btnDelete;

        VH(@NonNull View v) {
            super(v);
            cover   = v.findViewById(R.id.imageView);
            title   = v.findViewById(R.id.textTitle);
            author  = v.findViewById(R.id.tvAuthor);
            chapter = v.findViewById(R.id.tvChapter);
            if (author == null) author = v.findViewById(R.id.textBlurb);

            statRow = v.findViewById(R.id.statRow);
            tvBm    = v.findViewById(R.id.tvBookmark);
            tvLike  = v.findViewById(R.id.tvHeart);
            tvView  = v.findViewById(R.id.tvEye);

            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_history_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        DBHelper.HistoryItem it = data.get(pos);

        if (h.title != null) {
            h.title.setText(it.writingTitle == null ? "(untitled)" : it.writingTitle);
        }

        if (h.author != null) {
            h.author.setText(it.episodeTitle == null ? "" : it.episodeTitle);
        }

        if (h.chapter != null) {
            String ep = (it.episodeTitle != null && !it.episodeTitle.isEmpty())
                    ? it.episodeTitle
                    : (it.episodeId != null ? "Episode #" + it.episodeId : "—");
            h.chapter.setText(ep + " • " + fmt.format(it.createdAt));
        }

        // สถิติ
        int wid = it.writingId;
        if (h.tvBm != null)   h.tvBm.setText(String.valueOf(db.countBookmarks(wid)));
        if (h.tvLike != null) h.tvLike.setText(String.valueOf(db.getLikes(wid)));
        if (h.tvView != null) h.tvView.setText(String.valueOf(db.getViews(wid)));

        // ลบประวัติเมื่อกดถังขยะ
        if (h.btnDelete != null) {
            h.btnDelete.setOnClickListener(v -> {
                int adapterPos = h.getAdapterPosition();
                if (adapterPos == RecyclerView.NO_POSITION) return;

                DBHelper.HistoryItem item = data.get(adapterPos);
                boolean ok = db.deleteHistoryById(item.id);   // ต้องมี item.id จาก DBHelper ใหม่
                if (ok) {
                    data.remove(adapterPos);
                    notifyItemRemoved(adapterPos);
                    Toast.makeText(ctx, "ลบประวัติแล้ว", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ctx, "ลบไม่สำเร็จ", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override public int getItemCount() { return data.size(); }
}

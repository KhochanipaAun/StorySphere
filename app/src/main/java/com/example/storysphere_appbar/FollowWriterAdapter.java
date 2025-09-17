package com.example.storysphere_appbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FollowWriterAdapter extends RecyclerView.Adapter<FollowWriterAdapter.VH> {

    public interface OnOpen { void onOpen(int writingId); }

    private final List<WritingItem> data;
    private final OnOpen open;

    public FollowWriterAdapter(List<WritingItem> data, OnOpen open) {
        this.data = data; this.open = open;
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_story_reading, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        WritingItem w = data.get(position);
        h.title.setText(w.getTitle());
        h.blurb.setText(w.getTagline() != null ? w.getTagline() : "");
        // โหลดรูปถ้ามี (ปรับวิธีโหลดตามที่คุณใช้จริง)
        // if (w.getImagePath() != null) h.cover.setImageURI(Uri.parse(w.getImagePath()));
        h.itemView.setOnClickListener(v -> open.onOpen(w.getId()));
    }

    @Override public int getItemCount() { return data == null ? 0 : data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView title, blurb;
        VH(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.imageView);   // จาก item_story_reading.xml
            title = itemView.findViewById(R.id.textTitle);
            blurb = itemView.findViewById(R.id.textBlurb);
        }
    }
}

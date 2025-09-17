package com.example.storysphere_appbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EpisodeReadingAdapter extends RecyclerView.Adapter<EpisodeReadingAdapter.VH> {

    public interface OnClick { void onClick(Episode ep); }

    private final List<Episode> data;
    private final OnClick click;

    public EpisodeReadingAdapter(List<Episode> data, OnClick click) {
        this.data = data;
        this.click = click;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_episode_reading, parent, false); // ← ใช้ไฟล์นี้
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Episode ep = data.get(position);
        h.episodeNumber.setText("#" + ep.episodeNo);
        h.episodeTitle.setText(ep.title);
        // ถ้ามีวันที่ในโมเดล ให้ตั้งค่าได้ เช่น:
        // h.episodeDate.setText(ep.createdAtText);
        h.itemView.setOnClickListener(v -> click.onClick(ep));
    }

    @Override public int getItemCount() { return data == null ? 0 : data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView episodeNumber, episodeTitle, episodeDate;
        VH(@NonNull View itemView) {
            super(itemView);
            episodeNumber = itemView.findViewById(R.id.episodeNumber);
            episodeTitle  = itemView.findViewById(R.id.episodeTitle);
            episodeDate   = itemView.findViewById(R.id.episodeDate);
        }
    }
}

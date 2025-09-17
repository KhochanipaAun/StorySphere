package com.example.storysphere_appbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.VH> {

    private DBHelper dbHelper; // lazy init ใน onCreateViewHolder

    public interface OnEditClick { void onEdit(Episode ep); }

    private final OnEditClick onEditClick;
    private List<Episode> data = new ArrayList<>();

    public EpisodeAdapter(List<Episode> initial, OnEditClick onEditClick) {
        if (initial != null) this.data = initial;
        this.onEditClick = onEditClick;
    }

    public void submit(List<Episode> list) {
        this.data = (list != null) ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (dbHelper == null) dbHelper = new DBHelper(parent.getContext()); // ✅ init ที่นี่
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_episode, parent, false);
        return new VH(v);
    }

    public void onBindViewHolder(@NonNull VH h, int position) {
        Episode ep = data.get(position);

        // #ลำดับตอน
        h.episodeNumber.setText("#" + ep.episodeNo);

        // ชื่อตอน
        h.episodeTitle.setText(ep.title != null ? ep.title : "");

        // วันที่อัปเดต
        String ts = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(new Date(ep.updatedAt));
        h.episodeDate.setText(ts);

        // --- Spinner: Public / Private ---
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(
                h.itemView.getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Public", "Private"}
        );
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        h.spinner.setAdapter(spAdapter);

        // กัน onItemSelected ยิงตอนตั้งค่าเริ่มต้น
        h.binding = true;
        h.spinner.setSelection(ep.isPrivate ? 1 : 0, false);
        h.binding = false;

        h.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (h.binding) return;                 // ข้ามตอนกำลัง bind
                boolean newPrivate = (pos == 1);       // 0=Public, 1=Private
                if (newPrivate != ep.isPrivate) {
                    ep.isPrivate = newPrivate;
                    // อัปเดต DB
                    dbHelper.updateEpisode(ep.episodeId, ep.title, ep.contentHtml, ep.isPrivate);
                    // อัปเดตเวลาแก้ไขล่าสุดฝั่ง UI
                    ep.updatedAt = System.currentTimeMillis();
                    int pos2 = h.getAdapterPosition();
                    if (pos2 != RecyclerView.NO_POSITION) {
                        notifyItemChanged(pos2);
                    }
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        // ไอคอนดินสอ: แก้ไขตอน
        h.editIcon.setOnClickListener(v -> {
            if (onEditClick != null) onEditClick.onEdit(ep);
        });
    }

    @Override
    public int getItemCount() { return data == null ? 0 : data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView episodeNumber, episodeTitle, episodeDate;
        Spinner spinner; // id: spinner_custom
        ImageView editIcon;
        boolean binding = false; // ✅ กัน onItemSelected ตอนตั้งค่าเริ่มต้น

        VH(@NonNull View v) {
            super(v);
            episodeNumber = v.findViewById(R.id.episodeNumber);
            episodeTitle  = v.findViewById(R.id.episodeTitle);
            episodeDate   = v.findViewById(R.id.episodeDate);
            spinner       = v.findViewById(R.id.spinner_custom);
            editIcon      = v.findViewById(R.id.editIcon);
        }
    }
}
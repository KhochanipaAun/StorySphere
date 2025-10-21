package com.example.storysphere_appbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.VH> {

    // คลิกหลัก + onDelete (Activity ต้อง override เพื่อสั่งลบ)
    public interface OnItemClick {
        void onClick(WritingItem item);
        default void onDelete(WritingItem item, int position) {}
    }

    private final List<WritingItem> full;   // ข้อมูลเต็ม
    private final List<WritingItem> data;   // ข้อมูลที่แสดง (หลังกรอง)
    private final OnItemClick click;

    public BooksAdapter(List<WritingItem> items, OnItemClick click) {
        List<WritingItem> src = (items != null) ? items : new ArrayList<>();
        this.full  = new ArrayList<>(src);
        this.data  = new ArrayList<>(src);
        this.click = click;
        setHasStableIds(true);
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.book_item, p, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        WritingItem w = data.get(i);

        h.txtBookId.setText(w.getId() > 0 ? "ID:" + w.getId() : "-");
        h.txtTitleBook.setText(safe(w.getTitle()));
        h.txtPenName.setText(safe(w.getTagline())); // หาก pen name อยู่ฟิลด์อื่น ให้เปลี่ยนเป็นฟิลด์นั้น
        h.txtTag.setText(safe(w.getTag()));
        h.txtStatusBook.setText(safe(w.getCategory()));

        h.itemView.setOnClickListener(v -> { if (click != null) click.onClick(w); });

        // ปุ่มถังขยะ
        h.imgDelete.setClickable(true);
        h.imgDelete.setFocusable(true);
        h.imgDelete.setOnClickListener(v -> {
            if (click != null) click.onDelete(w, h.getAdapterPosition());
        });
    }

    @Override public int getItemCount() { return data.size(); }

    @Override public long getItemId(int pos) {
        long id = data.get(pos).getId();
        return id != 0 ? id : pos;
    }

    /** หลังลบ DB สำเร็จ ให้หายจาก UI แบบนิ่ม ๆ */
    public void removeAt(int position) {
        if (position < 0 || position >= data.size()) return;
        WritingItem removed = data.remove(position);
        if (removed != null) full.remove(removed);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, data.size() - position);
    }

    /** กรองด้วย title/tagline/tag/category */
    public void filter(String q) {
        data.clear();
        if (q == null || q.trim().isEmpty()) {
            data.addAll(full);
        } else {
            String k = q.trim().toLowerCase(Locale.ROOT);
            for (WritingItem w : full) {
                if (safe(w.getTitle()).toLowerCase(Locale.ROOT).contains(k)
                        || safe(w.getTagline()).toLowerCase(Locale.ROOT).contains(k)
                        || safe(w.getTag()).toLowerCase(Locale.ROOT).contains(k)
                        || safe(w.getCategory()).toLowerCase(Locale.ROOT).contains(k)) {
                    data.add(w);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void resetFilter() {
        data.clear();
        data.addAll(full);
        notifyDataSetChanged();
    }

    public void updateData(List<WritingItem> newItems) {
        full.clear();
        data.clear();
        if (newItems != null) {
            full.addAll(newItems);
            data.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    private static String safe(String s) { return (s == null || s.trim().isEmpty()) ? "-" : s; }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imageView11, imgDelete;
        TextView txtBookId, txtTitleBook, txtPenName, txtTag, txtStatusBook;
        VH(View v) {
            super(v);
            imageView11   = v.findViewById(R.id.imageView11);
            imgDelete     = v.findViewById(R.id.imgDelete);
            txtBookId     = v.findViewById(R.id.txtBookId);
            txtTitleBook  = v.findViewById(R.id.txtTitleBook);
            txtPenName    = v.findViewById(R.id.txtPenName);
            txtTag        = v.findViewById(R.id.txtTag);
            txtStatusBook = v.findViewById(R.id.txtStatusBook);
        }
    }
}

package com.example.storysphere_appbar;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WritingAdapter extends RecyclerView.Adapter<WritingAdapter.ViewHolder> {

    private Context context;
    private List<WritingItem> items;

    public WritingAdapter(Context context, List<WritingItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public WritingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_writing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WritingItem item = items.get(position);

        holder.textTitle.setText(item.getTitle());
        holder.textDesc.setText(item.getTagline());  // ใช้ tagline แทนรายละเอียด
        holder.textAuthor.setText(item.getCategory() + " | " + item.getTag());

        // โหลดภาพจาก Uri ที่เก็บไว้ ถ้าไม่มีใช้ภาพ default
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            holder.imageView.setImageURI(Uri.parse(item.getImagePath()));
        } else {
            holder.imageView.setImageResource(R.drawable.ic_human_background);        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textTitle, textDesc, textAuthor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView2);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDesc = itemView.findViewById(R.id.textDesc);
            textAuthor = itemView.findViewById(R.id.textAuthor);
        }
    }
}
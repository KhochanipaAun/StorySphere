package com.example.storysphere_appbar;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryVH> {

    private final Context context;
    private final List<WritingItem> stories;

    public StoryAdapter(Context context, List<WritingItem> stories) {
        this.context = context;
        this.stories = stories;
    }

    @NonNull
    @Override
    public StoryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_story_reading, parent, false);
        return new StoryVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryVH h, int position) {
        WritingItem item = stories.get(position);

        h.title.setText(item.getTitle() == null ? "" : item.getTitle());
        h.blurb.setText(item.getTagline() == null ? "" : item.getTagline());

        // TODO: map สถิติตามจริงจาก DB ถ้ามี
        h.textBookmark.setText("1");
        h.textHeart.setText("2");
        h.textViewCount.setText("3");

        // โหลดรูป (ถ้าใช้ Glide ให้ปลดคอมเมนต์)
        // if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
        //     Glide.with(context).load(item.getImagePath()).into(h.cover);
        // } else {
        //     h.cover.setImageResource(R.drawable.ic_library);
        // }
        h.cover.setImageResource(R.drawable.ic_library); // fallback เริ่มต้น

        // คลิกเพื่อไปหน้าอ่านเรื่อง
        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, ReadingMainActivity.class);
            i.putExtra(ReadingMainActivity.EXTRA_WRITING_ID, item.getId());
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return stories == null ? 0 : stories.size();
    }

    static class StoryVH extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView title, blurb, textBookmark, textHeart, textViewCount;

        StoryVH(@NonNull View v) {
            super(v);
            cover = v.findViewById(R.id.imageView);

            // รองรับทั้ง id ชุดใหม่ (textTitle/textBlurb) และชุดเก่า (bookTitle/bookTagline)
            title = v.findViewById(R.id.textTitle);
            if (title == null) title = v.findViewById(R.id.bookTitle);

            blurb = v.findViewById(R.id.textBlurb);
            if (blurb == null) blurb = v.findViewById(R.id.bookTagline);

            textBookmark = v.findViewById(R.id.textBookmarkView);
            if (textBookmark == null) textBookmark = new TextView(v.getContext());

            textHeart = v.findViewById(R.id.textHeartView);
            if (textHeart == null) textHeart = new TextView(v.getContext());

            textViewCount = v.findViewById(R.id.textViewNumber);
            if (textViewCount == null) textViewCount = new TextView(v.getContext());
        }
    }
}

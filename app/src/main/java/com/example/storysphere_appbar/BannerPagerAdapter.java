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

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class BannerPagerAdapter extends RecyclerView.Adapter<BannerPagerAdapter.VH> {

    public interface OnBannerClick {
        void onClick(DBHelper.Banner banner);
    }

    private final Context ctx;
    private final List<DBHelper.Banner> data = new ArrayList<>();
    private final OnBannerClick onClick;

    public BannerPagerAdapter(@NonNull Context ctx,
                              List<DBHelper.Banner> initial,
                              OnBannerClick onClick) {
        this.ctx = ctx;
        if (initial != null) data.addAll(initial);
        this.onClick = onClick;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner, parent, false);

        // ✅ บังคับให้ page ของ ViewPager2 เป็น match_parent เสมอ
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        if (lp == null) {
            lp = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        } else {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        v.setLayoutParams(lp);

        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        if (pos < 0 || pos >= data.size()) return;
        DBHelper.Banner b = data.get(pos);

        // ✅ โหลดรูปด้วย Glide (รองรับ content://, http(s)://, และไฟล์)
        try {
            Glide.with(h.img.getContext())
                    .load(b.imagePath)
                    .placeholder(R.drawable.ic_home)
                    .error(R.drawable.ic_home)
                    .into(h.img);
        } catch (Exception e) {
            // Fallback กรณีไม่มี Glide ทำงาน
            try { h.img.setImageURI(Uri.parse(b.imagePath)); }
            catch (Exception ignore) { h.img.setImageResource(R.drawable.ic_home); }
        }

        // title (ทำงานได้แม้ layout ไม่มี tvTitle)
        if (h.title != null) {
            if (b.title != null && !b.title.trim().isEmpty()) {
                h.title.setText(b.title);
                h.title.setVisibility(View.VISIBLE);
            } else {
                h.title.setVisibility(View.GONE);
            }
        }

        h.itemView.setOnClickListener(v -> {
            if (onClick != null) onClick.onClick(b);
        });

        if (h.btnMore != null) {
            h.btnMore.setOnClickListener(v -> {
                if (onClick != null) onClick.onClick(b);
            });
        }
    }

    @Override public int getItemCount() { return data.size(); }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= data.size()) return RecyclerView.NO_ID;
        return data.get(position).id;
    }

    /** เรียกตอน refresh banners จากภายนอก */
    public void replaceData(List<DBHelper.Banner> newData) {
        data.clear();
        if (newData != null) data.addAll(newData);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        ImageView btnMore;    // optional ใน item_banner.xml
        TextView  title;      // optional ถ้าไม่มีใน layout จะเป็น null

        VH(@NonNull View v) {
            super(v);
            img     = v.findViewById(R.id.imgBanner);
            btnMore = v.findViewById(R.id.btnBannerMore);
            title   = v.findViewById(R.id.tvTitle); // อาจไม่มีใน layout ได้
        }
    }
}

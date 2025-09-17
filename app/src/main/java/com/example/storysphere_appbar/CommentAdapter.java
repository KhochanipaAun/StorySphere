package com.example.storysphere_appbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.VH> {

    public static class Comment {
        public final String userName;
        public final String text;
        public final boolean isWriter;

        public Comment(String userName, String text, boolean isWriter) {
            this.userName = userName;
            this.text = text;
            this.isWriter = isWriter;
        }
    }

    private final List<Comment> data;

    public CommentAdapter(List<Comment> data) {
        this.data = data;
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgAvatar, btnMore;
        TextView tvUserName, tvWriterBadge, tvCommentText, btnLike, btnReply;

        VH(@NonNull View v) {
            super(v);
            imgAvatar     = v.findViewById(R.id.imgAvatar);
            btnMore       = v.findViewById(R.id.btnMore);
            tvUserName    = v.findViewById(R.id.tvUserName);
            tvWriterBadge = v.findViewById(R.id.tvWriterBadge);
            tvCommentText = v.findViewById(R.id.tvCommentText);
            btnLike       = v.findViewById(R.id.btnLike);
            btnReply      = v.findViewById(R.id.btnReply);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Comment c = data.get(position);
        h.tvUserName.setText(c.userName);
        h.tvCommentText.setText(c.text);
        h.tvWriterBadge.setVisibility(c.isWriter ? View.VISIBLE : View.GONE);

        // TODO ใส่โหลดรูป avatar จริงด้วย Glide/Picasso ถ้ามี url
        // Glide.with(h.imgAvatar).load(url).circleCrop().into(h.imgAvatar);

        // ตัวอย่างคลิก
        h.btnLike.setOnClickListener(v -> {/* like */});
        h.btnReply.setOnClickListener(v -> {/* reply */});
        h.btnMore.setOnClickListener(v -> {/* report menu */});
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }
}

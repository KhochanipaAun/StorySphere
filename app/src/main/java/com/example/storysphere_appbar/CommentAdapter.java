package com.example.storysphere_appbar;

import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

/**
 * CommentAdapter:
 * - ใช้ ListAdapter + DiffUtil (เรียก submitList(...) เพื่ออัปเดต)
 * - รองรับ callback ผ่าน OnCommentActionListener (Like/Reply/Report/Delete)
 * - ผูก layout: item_comment.xml (ต้องมี id: imgAvatar, btnMore, tvUserName, tvWriterBadge, tvCommentText, btnLike, btnReply)
 */
public class CommentAdapter extends ListAdapter<CommentAdapter.Comment, CommentAdapter.VH> {

    /** โครงสร้างข้อมูลที่ UI ต้องใช้ */
    public static class Comment {
        public final int id;
        public final String userName;
        public final String userEmail;
        public final String avatarUrl;     // ตอนนี้ยังไม่โหลดจริง (TODO ใส่ Glide ถ้าต้องการ)
        public final String text;
        public final boolean isWriter;
        public final long createdAt;
        public final boolean likedByMe;
        public final int likeCount;
        public final boolean mine;         // เป็นคอมเมนต์ของผู้ใช้เองหรือไม่ (สำหรับแสดงปุ่ม Delete)

        public Comment(int id,
                       String userName,
                       String userEmail,
                       String avatarUrl,
                       String text,
                       boolean isWriter,
                       long createdAt,
                       boolean likedByMe,
                       int likeCount,
                       boolean mine) {
            this.id = id;
            this.userName = userName;
            this.userEmail = userEmail;
            this.avatarUrl = avatarUrl;
            this.text = text;
            this.isWriter = isWriter;
            this.createdAt = createdAt;
            this.likedByMe = likedByMe;
            this.likeCount = likeCount;
            this.mine = mine;
        }

        public Comment withLikeState(boolean likedByMe, int likeCount) {
            return new Comment(id, userName, userEmail, avatarUrl, text, isWriter, createdAt, likedByMe, likeCount, mine);
        }
    }

    /** callback สำหรับ action ของคอมเมนต์ */
    public interface OnCommentActionListener {
        void onLikeToggle(Comment c);
        void onReply(Comment c);
        void onReport(Comment c);
        void onDelete(Comment c);
    }

    private final OnCommentActionListener listener;

    public CommentAdapter(OnCommentActionListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Comment> DIFF = new DiffUtil.ItemCallback<Comment>() {
        @Override public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
            return oldItem.id == newItem.id;
        }

        @Override public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
            return eq(oldItem.userName, newItem.userName)
                    && eq(oldItem.userEmail, newItem.userEmail)
                    && eq(oldItem.avatarUrl, newItem.avatarUrl)
                    && eq(oldItem.text, newItem.text)
                    && oldItem.isWriter == newItem.isWriter
                    && oldItem.createdAt == newItem.createdAt
                    && oldItem.likedByMe == newItem.likedByMe
                    && oldItem.likeCount == newItem.likeCount
                    && oldItem.mine == newItem.mine;
        }
    };

    private static boolean eq(String a, String b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    /** ViewHolder */
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
        Comment c = getItem(position);

        // ชื่อ + badge
        h.tvUserName.setText(c.userName);
        h.tvWriterBadge.setVisibility(c.isWriter ? View.VISIBLE : View.GONE);

        // เนื้อหาคอมเมนต์
        h.tvCommentText.setText(c.text);

        // ปุ่ม Like/Reply
        h.btnLike.setText(c.likedByMe ? "Liked" : "Like");
        h.btnLike.setAlpha(c.likedByMe ? 1f : 0.85f);
        h.btnLike.setOnClickListener(v -> listener.onLikeToggle(c));
        h.btnReply.setOnClickListener(v -> listener.onReply(c));

        // อวาตาร์ (ตอนนี้ใช้ไอคอนเริ่มต้นไปก่อน)
        h.imgAvatar.setImageResource(R.drawable.ic_launcher_foreground);
        // TODO: ถ้าต้องการโหลดรูปจริง ให้เพิ่ม Glide dependency แล้วใช้:
        // Glide.with(h.imgAvatar.getContext()).load(c.avatarUrl).circleCrop()
        //      .placeholder(R.drawable.ic_launcher_foreground).into(h.imgAvatar);

        // เมนู More (Report/Delete)
        h.btnMore.setOnClickListener(v -> showMoreMenu(v, c));
    }

    /** แสดง popup เมนูเพิ่มเติม */
    private void showMoreMenu(View anchor, Comment c) {
        PopupMenu pm = new PopupMenu(anchor.getContext(), anchor);
        MenuInflater inflater = pm.getMenuInflater();

        pm.getMenu().add(0, 1, 0, "Report");
        if (c.mine) {
            pm.getMenu().add(0, 2, 1, "Delete");
        }

        pm.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) { listener.onReport(c); return true; }
            if (id == 2) { listener.onDelete(c); return true; }
            return false;
        });
        pm.show();
    }

    /** ใช้เมื่ออยากอัปเดตสถานะไลก์ของคอมเมนต์ตัวใดตัวหนึ่งแบบเฉพาะจุด */
    public void updateLikeState(int commentId, boolean likedByMe, int likeCount) {
        int n = getItemCount();
        for (int i = 0; i < n; i++) {
            Comment c = getItem(i);
            if (c.id == commentId) {
                java.util.ArrayList<Comment> copy = new java.util.ArrayList<>(getCurrentList());
                copy.set(i, c.withLikeState(likedByMe, likeCount));
                submitList(copy);
                break;
            }
        }
    }
}

package com.example.storysphere_appbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.VH> {

    public interface OnReportAction {
        void onBan(String offenderEmail, int reportId);
        void onClear(int reportId);
    }

    private final List<DBHelper.ReportItem> data;
    private final OnReportAction action;

    public ReportAdapter(List<DBHelper.ReportItem> data, OnReportAction action) {
        this.data = data;
        this.action = action;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_report, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        DBHelper.ReportItem r = data.get(i);
        h.txtWriter.setText(r.writingTitle != null ? r.writingTitle : "-");
        h.txtEpisode.setText(r.episodeTitle != null ? r.episodeTitle : "-");
        h.txtComment.setText(r.commentContent);
        h.txtReporter.setText(r.reporterEmail != null ? r.reporterEmail : "-");
        h.txtOffender.setText(r.commentUserEmail);

        h.btnBan.setOnClickListener(v -> {
            if (action != null) action.onBan(r.commentUserEmail, r.id);
        });
        h.btnClear.setOnClickListener(v -> {
            if (action != null) action.onClear(r.id);
        });
    }

    @Override public int getItemCount() { return data == null ? 0 : data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtWriter, txtEpisode, txtComment, txtReporter, txtOffender;
        ImageButton btnBan, btnClear;
        VH(View v) {
            super(v);
            txtWriter   = v.findViewById(R.id.txtWriter);
            txtEpisode  = v.findViewById(R.id.txtEpisode);
            txtComment  = v.findViewById(R.id.txtComment);
            txtReporter = v.findViewById(R.id.txtReporter);
            txtOffender = v.findViewById(R.id.txtOffender);
            btnBan      = v.findViewById(R.id.btnBan);
            btnClear    = v.findViewById(R.id.btnClear);
        }
    }
}

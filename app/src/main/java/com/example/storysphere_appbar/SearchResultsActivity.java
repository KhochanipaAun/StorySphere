package com.example.storysphere_appbar;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        String query = getIntent().getStringExtra("q");
        TextView tvHeader = findViewById(R.id.tvHeader);
        tvHeader.setText("Results for: " + query);

        // TODO: เปลี่ยนเป็นดึงจากแหล่งข้อมูลจริงของคุณ
        List<String> allTitles = Arrays.asList(
                "Romance in Paris", "Drama Queen", "Comedy Night",
                "Fantasy World", "Sci-fi Galaxy", "Mystery Case",
                "Romance Again", "Dark Drama", "Light Comedy"
        );

        List<String> filtered = new ArrayList<>();
        for (String t : allTitles) {
            if (t.toLowerCase().contains(query.toLowerCase())) {
                filtered.add(t);
            }
        }

        RecyclerView rv = findViewById(R.id.rvResults);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new SimpleTitleAdapter(filtered));
    }

    // อแดปเตอร์ง่าย ๆ แสดงชื่อเรื่องเป็นรายการ
    static class SimpleTitleAdapter extends RecyclerView.Adapter<TitleVH> {
        private final List<String> data;
        SimpleTitleAdapter(List<String> data) { this.data = data; }

        @Override public TitleVH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View v = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new TitleVH(v);
        }
        @Override public void onBindViewHolder(TitleVH h, int pos) { h.bind(data.get(pos)); }
        @Override public int getItemCount() { return data.size(); }
    }
    static class TitleVH extends RecyclerView.ViewHolder {
        private final android.widget.TextView tv;
        TitleVH(android.view.View itemView) {
            super(itemView);
            tv = itemView.findViewById(android.R.id.text1);
        }
        void bind(String title) { tv.setText(title); }
    }
}

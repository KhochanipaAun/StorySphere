package com.example.storysphere_appbar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DeleteWritingActivity extends AppCompatActivity {

    Spinner spinnerTitle, spinnerEpisode;
    Button btnDelete;

    HashMap<String, List<String>> dataMap = new HashMap<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_writing);

        spinnerTitle = findViewById(R.id.spinnerTitle);
        spinnerEpisode = findViewById(R.id.spinnerEpisode);
        btnDelete = findViewById(R.id.btnDelete);

        // จำลองข้อมูลจากฐานข้อมูล
        mockDatabaseData();

        // ตั้งค่า Title Spinner
        List<String> titles = new ArrayList<>(dataMap.keySet());
        ArrayAdapter<String> adapterTitle = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, titles);
        spinnerTitle.setAdapter(adapterTitle);

        // ตอนแรกให้แสดงตอนของเรื่องแรก
        updateEpisodeSpinner(titles.get(0));

        // เปลี่ยน Episode list ตาม Title ที่เลือก
        spinnerTitle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTitle = titles.get(position);
                updateEpisodeSpinner(selectedTitle);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // ปุ่มลบ
        btnDelete.setOnClickListener(v -> {
            String selectedTitle = spinnerTitle.getSelectedItem().toString();
            String selectedEpisode = spinnerEpisode.getSelectedItem().toString();
            Toast.makeText(DeleteWritingActivity.this, "Deleting: " + selectedTitle + " - " + selectedEpisode, Toast.LENGTH_SHORT).show();
        });
    }

    // อัปเดต Spinner ของ Episode ตาม Title
    private void updateEpisodeSpinner(String title) {
        List<String> episodes = dataMap.get(title);
        ArrayAdapter<String> adapterEpisode = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, episodes);
        spinnerEpisode.setAdapter(adapterEpisode);
    }

    // จำลองข้อมูล
    private void mockDatabaseData() {
        dataMap.put("My First Story", Arrays.asList("Episode 1", "Episode 2", "Final Chapter"));
        dataMap.put("Love in Spring", Arrays.asList("Spring Begins", "Warm Winds", "Goodbye"));
        dataMap.put("Dark Night", Arrays.asList("Shadow Rising", "Into the Fog", "Midnight Silence"));
    }
}

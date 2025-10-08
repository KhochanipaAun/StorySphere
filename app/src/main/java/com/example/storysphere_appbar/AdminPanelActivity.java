package com.example.storysphere_appbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AdminPanelActivity extends AppCompatActivity {

    private TextView txtBookCount, txtUserCount;
    private ImageView imgHome, imgBell;
    private CardView cardBook, cardUsers, cardBanner;

    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = new DBHelper(this);

        // bind views
        txtBookCount = findViewById(R.id.txtBookCount);
        txtUserCount = findViewById(R.id.txtUserCount);
        imgHome      = findViewById(R.id.imgHome);
        imgBell      = findViewById(R.id.imgBell);
        cardBook     = findViewById(R.id.cardView);
        cardUsers    = findViewById(R.id.cardView2);
        cardBanner   = findViewById(R.id.cardBanner);

        // การ์ดไปแต่ละหน้า
        if (cardBook != null)  cardBook.setOnClickListener(v ->
                startActivity(new Intent(this, BooksListActivity.class)));

        if (cardUsers != null) cardUsers.setOnClickListener(v ->
                startActivity(new Intent(this, UserListActivity.class)));

        if (cardBanner != null) cardBanner.setOnClickListener(v ->
                startActivity(new Intent(this, AdminBannerActivity.class)));

        // กระดิ่ง: ไปหน้ารีพอร์ต
        if (imgBell != null) imgBell.setOnClickListener(v ->
                startActivity(new Intent(this, AdminReportsActivity.class)));

        // ปุ่ม Home: กลับ MainActivity แบบล้างสแตกกันวนกลับ
        if (imgHome != null) {
            imgHome.setOnClickListener(v -> {
                db.clearLoginSession();
                Intent i = new Intent(AdminPanelActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("from_admin_home", true);  // กัน MainActivity รีเดิร์กกลับมาหน้านี้
                startActivity(i);
                finish(); // ปิดหน้า admin ทิ้งไปเลย
            });
        }

        // โหลดตัวเลขรอบแรก
        refreshCountersSafely();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // กลับมาหน้านี้ให้รีเฟรชจำนวนล่าสุด
        refreshCountersSafely();
    }

    private void refreshCountersSafely() {
        try {
            txtBookCount.setText(String.valueOf(db.countAllWritings()));
            txtUserCount.setText(String.valueOf(db.countAllUsers()));
        } catch (Throwable ignore) {
            txtBookCount.setText("0");
            txtUserCount.setText("0");
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        db.clearLoginSession();
        Intent i = new Intent(AdminPanelActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

}

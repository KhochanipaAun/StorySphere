package com.example.storysphere_appbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class activity_sign_up extends AppCompatActivity {

    EditText edtName, edtEmail, edtPassword;
    Button bttLogin;
    TextView ClickLogin;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        bttLogin = findViewById(R.id.bttLogin);
        ClickLogin = findViewById(R.id.ClickLogin);

        dbHelper = new DBHelper(this);

        bttLogin.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบ", Toast.LENGTH_SHORT).show();
                return;
            }

            // ตรวจสอบว่าอีเมลซ้ำหรือไม่
            if (dbHelper.getUserByEmail(email).moveToFirst()) {
                Toast.makeText(this, "อีเมลนี้ถูกใช้ไปแล้ว", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean inserted = dbHelper.insertUser(name, email, password);
            if (inserted) {
                Toast.makeText(this, "สมัครสมาชิกเรียบร้อย", Toast.LENGTH_SHORT).show();
                // ไปหน้า Login หลังสมัครสำเร็จ
                Intent intent = new Intent(activity_sign_up.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "เกิดข้อผิดพลาดในการสมัครสมาชิก", Toast.LENGTH_SHORT).show();
            }
        });

        ClickLogin.setOnClickListener(v -> {
            // กลับหน้า Login (MainActivity)
            finish();
        });
    }
}
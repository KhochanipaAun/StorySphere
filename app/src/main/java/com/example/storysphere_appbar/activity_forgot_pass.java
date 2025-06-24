package com.example.storysphere_appbar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class activity_forgot_pass extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    Button bttLogin;
    TextView ClickLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        bttLogin = findViewById(R.id.bttLogin);
        ClickLogin = findViewById(R.id.ClickLogin);

        bttLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString();
            String newPassword = edtPassword.getText().toString();

            SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
            String storedEmail = prefs.getString("email", "");

            if (!email.equals(storedEmail)) {
                Toast.makeText(this, "ไม่พบอีเมลนี้ในระบบ", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("password", newPassword);
            editor.apply();

            Toast.makeText(this, "เปลี่ยนรหัสผ่านเรียบร้อย", Toast.LENGTH_SHORT).show();
            finish(); // กลับไปหน้า Login
        });

        ClickLogin.setOnClickListener(v -> {
            finish();
        });
    }
}
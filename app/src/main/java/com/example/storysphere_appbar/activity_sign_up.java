package com.example.storysphere_appbar;

import android.content.SharedPreferences;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        bttLogin = findViewById(R.id.bttLogin);
        ClickLogin = findViewById(R.id.ClickLogin);

        bttLogin.setOnClickListener(v -> {
            String name = edtName.getText().toString();
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบ", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("name", name);
            editor.putString("email", email);
            editor.putString("password", password);
            editor.apply();

            Toast.makeText(this, "สมัครสมาชิกเรียบร้อย", Toast.LENGTH_SHORT).show();
            finish(); // กลับไปหน้า Login
        });

        ClickLogin.setOnClickListener(v -> {
            finish();
        });
    }
}
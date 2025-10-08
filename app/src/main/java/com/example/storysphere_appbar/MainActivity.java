package com.example.storysphere_appbar;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button bttLogin;
    private TextView ClickSignUp, Forgotpass, txtShowPassword;

    private DBHelper dbHelper;
    private boolean isPasswordVisible = false;

    // Hardcoded admin (สำหรับทดสอบ)
    private static final String HARDCODED_ADMIN_EMAIL = "storysphere63@gmail.com";
    private static final String HARDCODED_ADMIN_PASSWORD = "Storysphere987";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);

        // ===== Auto-redirect ถ้ามี session ค้าง =====
        String loggedInEmail = dbHelper.getLoggedInUserEmail();
        if (loggedInEmail != null && !loggedInEmail.isEmpty()) {
            String userRole = dbHelper.getUserRole(loggedInEmail);
            if (userRole == null || userRole.trim().isEmpty()) userRole = "user";
            navigateUserBasedOnRole(loggedInEmail, userRole);
            return;
        }

        // ===== Bind UI =====
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        bttLogin = findViewById(R.id.bttLogin);
        ClickSignUp = findViewById(R.id.ClickSignUp);
        Forgotpass = findViewById(R.id.Forgotpass);
        txtShowPassword = findViewById(R.id.txtShowPass);

        bttLogin.setOnClickListener(v -> loginUser());

        // ไปหน้า SignUp (กัน session ค้าง)
        ClickSignUp.setOnClickListener(v -> {
            dbHelper.clearLoginSession();
            startActivity(new Intent(MainActivity.this, activity_sign_up.class));
        });

        Forgotpass.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, activity_forgot_pass.class)));

        txtShowPassword.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // ===== Validation =====
        if (email.isEmpty()) { edtEmail.setError("กรุณากรอกอีเมล"); edtEmail.requestFocus(); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { edtEmail.setError("กรุณากรอกอีเมลที่ถูกต้อง"); edtEmail.requestFocus(); return; }
        if (password.isEmpty()) { edtPassword.setError("กรุณากรอกรหัสผ่าน"); edtPassword.requestFocus(); return; }

        boolean ok = false;
        String role = null;

        // ===== Hardcoded admin bypass =====
        if (email.equals(HARDCODED_ADMIN_EMAIL) && password.equals(HARDCODED_ADMIN_PASSWORD)) {
            ok = true;
            role = "admin";
            Log.d("MainActivity", "Bypass admin login");
        } else {
            // ===== เช็ค DB ด้วย Cursor =====
            Cursor c = null;
            try {
                c = dbHelper.getUserByEmail(email);
                if (c != null && c.moveToFirst()) {
                    String storedPassword = c.getString(c.getColumnIndexOrThrow("password"));
                    ok = password.equals(storedPassword); // (โปรดเปลี่ยนเป็น hashing ก่อน production)
                    int idxRole = c.getColumnIndex("role");
                    role = (idxRole >= 0) ? c.getString(idxRole) : null;
                } else {
                    Log.d("MainActivity", "User not found: " + email);
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Cursor error", e);
                ok = false;
            } finally {
                if (c != null) try { c.close(); } catch (Exception ignore) {}
            }

            if (ok && (role == null || role.isEmpty())) {
                role = dbHelper.getUserRole(email);
            }
        }

        if (ok) {
            Toast.makeText(this, "เข้าสู่ระบบสำเร็จ!", Toast.LENGTH_SHORT).show();

            // ensure admin record ใน DB (เฉพาะตอนเข้าด้วยอีเมล admin)
            if (email.equals(HARDCODED_ADMIN_EMAIL)) {
                try {
                    if (!dbHelper.checkEmailExists(email)) {
                        dbHelper.insertUser("Admin", email, HARDCODED_ADMIN_PASSWORD, "admin");
                    } else {
                        dbHelper.updateUser(email, "Admin", null, null);
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Ensure admin record failed", e);
                }
            }

            // บันทึก session
            dbHelper.saveLoginSession(email);

            // นำทางตาม role
            navigateUserBasedOnRole(email, role);
        } else {
            Toast.makeText(this, "อีเมลหรือรหัสผ่านไม่ถูกต้อง", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateUserBasedOnRole(String email, String role) {
        if (role == null || role.trim().isEmpty()) role = "user";
        Intent intent = "admin".equalsIgnoreCase(role)
                ? new Intent(MainActivity.this, AdminPanelActivity.class)
                : new Intent(MainActivity.this, HomeActivity.class);

        intent.putExtra("email", email);
        intent.putExtra("role", role);
        startActivity(intent);
        finish();
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            txtShowPassword.setText("Show");
        } else {
            edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            txtShowPassword.setText("Hide");
        }
        edtPassword.setSelection(edtPassword.getText().length());
        isPasswordVisible = !isPasswordVisible;
    }
}

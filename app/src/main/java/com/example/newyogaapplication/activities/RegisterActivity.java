package com.example.newyogaapplication.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newyogaapplication.R;
import com.example.newyogaapplication.classes.Role;
import com.example.newyogaapplication.classes.YogaUser;
import com.example.newyogaapplication.database.YogaUserDB;
import com.example.newyogaapplication.utils.HashUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword, etUsername;
    private Button btnRegister;
    private YogaUserDB userDb;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etUsername = findViewById(R.id.etUsername);
        btnRegister = findViewById(R.id.btnRegister);

        userDb = new YogaUserDB(this);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://thenewyoga-604c0-default-rtdb.asia-southeast1.firebasedatabase.app/");
        userRef = firebaseDatabase.getReference("yoga_users");

        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            String username = etUsername.getText().toString().trim();

            // Kiểm tra tính hợp lệ của email
            if (!isValidEmail(email)) {
                Toast.makeText(RegisterActivity.this, "Invalid email format", Toast.LENGTH_SHORT).show();
                return;
            }

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hash mật khẩu trước khi lưu
            String hashedPassword = HashUtils.hashPassword(password);

            // Generate Firebase key first
            String firebaseKey = userRef.push().getKey();
            if (firebaseKey == null) {
                Toast.makeText(RegisterActivity.this, "Failed to generate Firebase key!", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = UUID.randomUUID().toString();
            YogaUser newUser = new YogaUser(
                    userId,
                    firebaseKey,  // Assign Firebase key here
                    email,
                    hashedPassword,
                    username,
                    Role.TEACHER
            );

            // Kiểm tra email trùng lặp
            if (userDb.isEmailExists(email)) {
                Toast.makeText(RegisterActivity.this, "Email already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save user in SQLite with Firebase key
            long result = userDb.addUser(newUser);
            if (result != -1) {
                // Update user in Firebase after saving in SQLite
                userRef.child(firebaseKey).setValue(newUser).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registration successful and synced with Firebase!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Failed to sync with Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(RegisterActivity.this, "Registration failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm kiểm tra email có hợp lệ không
    private boolean isValidEmail(String email) {
        String emailPattern = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.compile(emailPattern).matcher(email).matches();
    }
}

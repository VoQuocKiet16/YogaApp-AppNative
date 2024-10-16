package com.example.newyogaapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newyogaapplication.R;
import com.example.newyogaapplication.classes.YogaUser;
import com.example.newyogaapplication.sync.SyncManager;
import com.example.newyogaapplication.utils.HashUtils;
import com.example.newyogaapplication.utils.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private DatabaseReference userRef;  // Firebase reference
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        sessionManager = new SessionManager(this);

        // Initialize Firebase reference for users
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://thenewyoga-604c0-default-rtdb.asia-southeast1.firebasedatabase.app/");
        userRef = firebaseDatabase.getReference("yoga_users");

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            // If logged in, redirect to MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close the login activity
        }

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validate input fields
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hash the input password
            String hashedPassword = HashUtils.hashPassword(password);

            // Authenticate with Firebase
            authenticateUserFromFirebase(email, hashedPassword);
        });

        // Set up the Register button to switch to RegisterActivity
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Start sync process using SyncManager
        SyncManager syncManager = new SyncManager(this);
        syncManager.startSyncing();
    }

    // Method to authenticate user from Firebase
    private void authenticateUserFromFirebase(String email, String hashedPassword) {
        userRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Loop through all matching users (there should only be one)
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        YogaUser user = snapshot.getValue(YogaUser.class);

                        if (user != null && user.getPassword().equals(hashedPassword)) {
                            // Save login session with user's email
                            sessionManager.createLoginSession(user.getEmail());

                            // Redirect to MainActivity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // Close login activity
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Failed to authenticate: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

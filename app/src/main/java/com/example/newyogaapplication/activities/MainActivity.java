package com.example.newyogaapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newyogaapplication.R;
import com.example.newyogaapplication.classes.Role;
import com.example.newyogaapplication.classes.YogaUser;
import com.example.newyogaapplication.database.YogaUserDB;
import com.example.newyogaapplication.sync.SyncManager;
import com.example.newyogaapplication.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private TextView tvTitle;
    private Button btnManageAccounts, btnMyClass, btnManageHistories;
    private ImageButton imgLogout;
    private SessionManager sessionManager;
    private YogaUserDB userDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize session manager
        sessionManager = new SessionManager(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        // Initialize database helpers
        userDbHelper = new YogaUserDB(this);

        // Get logged-in user's email
        String loggedInUserEmail = sessionManager.getUserEmail();

        // Fetch the logged-in user details from SQLite
        YogaUser loggedInUser = userDbHelper.getUserByEmail(loggedInUserEmail);

        // Reference TextView and set welcome message
        tvTitle = findViewById(R.id.title);
        if (loggedInUserEmail != null) {
            tvTitle.setText("Welcome, " + loggedInUserEmail);
        }

        // Manage Courses button
        Button btnManageCourses = findViewById(R.id.btnManageCourses);

        // Manage Classes button
        Button btnManageClasses = findViewById(R.id.btnManageClasses);

        // Manage Accounts button
        Button btnManageAccounts = findViewById(R.id.btnManageAccounts);

        // Manage Accounts button
        Button btnManageHistories = findViewById(R.id.btnManageHistories);

        // My Class button to go to TeacherActivity
        btnMyClass = findViewById(R.id.btnMyClass);

        // Logout button
        imgLogout = findViewById(R.id.btnLogout);

        // Set the button visibility based on the role
        if (loggedInUser != null) {
            if (loggedInUser.getRole() == Role.ADMIN) {
                // Show the admin buttons
                btnManageCourses.setVisibility(View.VISIBLE);
                btnManageClasses.setVisibility(View.VISIBLE);
                btnManageAccounts.setVisibility(View.VISIBLE);
                btnManageHistories.setVisibility(View.VISIBLE);

                // Hide the teacher-specific button
                btnMyClass.setVisibility(View.GONE);
            } else if (loggedInUser.getRole() == Role.TEACHER) {
                // Show the teacher-specific button
                btnMyClass.setVisibility(View.VISIBLE);

                // Hide the admin buttons
                btnManageCourses.setVisibility(View.GONE);
                btnManageClasses.setVisibility(View.GONE);
                btnManageAccounts.setVisibility(View.GONE);
                btnManageHistories.setVisibility(View.GONE);
            }
        }

        // Set click listeners for each button
        btnManageCourses.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ManageCoursesActivity.class);
            startActivity(intent);
        });

        btnManageClasses.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ManageClassesActivity.class);
            startActivity(intent);
        });

        btnManageHistories.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ManageHistoryActivity.class);
            startActivity(intent);
        });

        btnManageAccounts.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ManageAccountActivity.class);
            startActivity(intent);
        });



        btnMyClass.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TeacherActivity.class);
            startActivity(intent);
        });

        imgLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            redirectToLogin();
        });

        // Start sync process using SyncManager
        SyncManager syncManager = new SyncManager(this);
        syncManager.startSyncing();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

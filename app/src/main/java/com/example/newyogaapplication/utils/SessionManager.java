package com.example.newyogaapplication.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.newyogaapplication.activities.LoginActivity;

public class SessionManager {

    // SharedPreferences and Editor
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    // SharedPreferences file name and keys
    private static final String PREF_NAME = "yogaAppSession";
    private static final String IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_EMAIL = "userEmail";

    // Constructor
    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Save login state and email
    public void createLoginSession(String email) {
        editor.putBoolean(IS_LOGGED_IN, true);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();  // Apply changes
    }

    // Check if the user is logged in
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(IS_LOGGED_IN, false);
    }

    // Get logged-in user's email
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    // Clear session and logout
    public void logoutUser() {
        editor.clear();
        editor.apply();  // Apply changes

        // Redirect user to LoginActivity after logout
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}

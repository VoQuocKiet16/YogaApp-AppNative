package com.example.newyogaapplication.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newyogaapplication.R;
import com.example.newyogaapplication.adapters.YogaAccountAdapter;
import com.example.newyogaapplication.classes.YogaUser;
import com.example.newyogaapplication.database.YogaUserDB;
import com.example.newyogaapplication.sync.SyncWorker;
import com.example.newyogaapplication.utils.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageAccountActivity extends AppCompatActivity {

    private RecyclerView recyclerViewAccounts;
    private YogaUserDB userDbHelper;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userRef;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_account); // Use correct layout

        sessionManager = new SessionManager(this);
        userDbHelper = new YogaUserDB(this);

        // Firebase initialization
        firebaseDatabase = FirebaseDatabase.getInstance("https://thenewyoga-604c0-default-rtdb.asia-southeast1.firebasedatabase.app/");
        userRef = firebaseDatabase.getReference("yoga_users");

        // Initialize RecyclerView
        recyclerViewAccounts = findViewById(R.id.recyclerView);
        recyclerViewAccounts.setLayoutManager(new LinearLayoutManager(this));


        SyncWorker syncWorker = new SyncWorker(this);
        syncWorker.syncUsers();


        loadUsersFromSQLite();
    }


    private void loadUsersFromSQLite() {
        List<YogaUser> userList = userDbHelper.getAllUsers();
        String loggedInUserEmail = sessionManager.getUserEmail();  // Get email of logged-in user

        // Filter out the logged-in user from the list
        List<YogaUser> filteredUsers = new ArrayList<>();
        for (YogaUser user : userList) {
            if (!user.getEmail().equals(loggedInUserEmail)) {
                filteredUsers.add(user);
            }
        }

        // Set the filtered list to the adapter
        YogaAccountAdapter adapter = new YogaAccountAdapter(this, filteredUsers, userDbHelper, userRef, loggedInUserEmail);
        recyclerViewAccounts.setAdapter(adapter);
    }
}

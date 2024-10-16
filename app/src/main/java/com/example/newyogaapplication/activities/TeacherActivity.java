package com.example.newyogaapplication.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newyogaapplication.R;
import com.example.newyogaapplication.adapters.YogaClassAdapter;
import com.example.newyogaapplication.classes.YogaClass;
import com.example.newyogaapplication.database.YogaClassDB;
import com.example.newyogaapplication.utils.SessionManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TeacherActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private YogaClassAdapter adapter;
    private List<YogaClass> classList;
    private YogaClassDB classDbHelper;
    private DatabaseReference classRef;
    private FirebaseDatabase firebaseDatabase;
    private SessionManager sessionManager;
    private String loggedInTeacherEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        recyclerView = findViewById(R.id.recyclerViewTeacherClasses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        classDbHelper = new YogaClassDB(this);
        sessionManager = new SessionManager(this);
        loggedInTeacherEmail = sessionManager.getUserEmail();

        firebaseDatabase = FirebaseDatabase.getInstance("https://thenewyoga-604c0-default-rtdb.asia-southeast1.firebasedatabase.app/");
        classRef = firebaseDatabase.getReference("yoga_classes");

        classList = new ArrayList<>();
        adapter = new YogaClassAdapter(this, classList, classRef, classDbHelper);
        recyclerView.setAdapter(adapter);

        syncTeacherClassesFromFirebase();
    }

    private void syncTeacherClassesFromFirebase() {
        classRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                classList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    YogaClass yogaClass = snapshot.getValue(YogaClass.class);
                    if (yogaClass != null && yogaClass.getTeacher().equals(loggedInTeacherEmail)) {
                        // Only add classes where the logged-in teacher is the teacher
                        classList.add(yogaClass);

                        YogaClass existingClass = classDbHelper.getYogaClass(yogaClass.getId());
                        if (existingClass == null) {
                            classDbHelper.addYogaClass(yogaClass);
                        } else {
                            classDbHelper.updateYogaClass(yogaClass);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(TeacherActivity.this, "Failed to sync classes", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

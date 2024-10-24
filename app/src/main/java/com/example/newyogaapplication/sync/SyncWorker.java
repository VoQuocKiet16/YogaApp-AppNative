package com.example.newyogaapplication.sync;

import android.content.Context;
import android.widget.Toast;

import com.example.newyogaapplication.classes.YogaClass;
import com.example.newyogaapplication.classes.YogaCourse;
import com.example.newyogaapplication.classes.YogaUser;
import com.example.newyogaapplication.database.YogaClassDB;
import com.example.newyogaapplication.database.YogaCourseDB;
import com.example.newyogaapplication.database.YogaUserDB;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class SyncWorker {

    private YogaCourseDB courseDbHelper;
    private YogaClassDB classDbHelper;
    private YogaUserDB userDbHelper;
    private DatabaseReference courseRef;
    private DatabaseReference classRef;
    private DatabaseReference userRef;
    private Context context;

    public SyncWorker(Context context) {
        this.context = context;
        courseDbHelper = new YogaCourseDB(context);
        classDbHelper = new YogaClassDB(context);
        userDbHelper = new YogaUserDB(context);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://thenewyoga-604c0-default-rtdb.asia-southeast1.firebasedatabase.app/");
        courseRef = firebaseDatabase.getReference("yoga_courses");
        classRef = firebaseDatabase.getReference("yoga_classes");
        userRef = firebaseDatabase.getReference("yoga_users");
    }

    public void syncFirebaseWithSQLite() {
        syncCourses();
        syncClasses();
        syncUsers();

    }

    public void syncSQLiteWithFirebase() {
        syncCoursesToFirebase();
        syncClassesToFirebase();
        syncUsersToFirebase();
    }

    // Đồng bộ từ Firebase đến SQLite
    private void syncCourses() {
        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    YogaCourse course = snapshot.getValue(YogaCourse.class);
                    if (course != null) {
                        YogaCourse existingCourse = courseDbHelper.getYogaCourseById(course.getId());
                        if (existingCourse != null) {
                            courseDbHelper.updateYogaCourse(course);
                        } else {
                            courseDbHelper.addYogaCourse(course);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Failed to sync courses from Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void syncClasses() {
        classRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    YogaClass yogaClass = snapshot.getValue(YogaClass.class);
                    if (yogaClass != null) {
                        YogaClass existingClass = classDbHelper.getYogaClass(yogaClass.getId());
                        if (existingClass != null) {
                            classDbHelper.updateYogaClass(yogaClass);
                        } else {
                            classDbHelper.addYogaClass(yogaClass);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Failed to sync classes from Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void syncUsers() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    YogaUser user = snapshot.getValue(YogaUser.class);
                    if (user != null && user.getUserId() != null) {
                        YogaUser existingUser = userDbHelper.getUserById(user.getUserId());
                        if (existingUser != null) {
                            userDbHelper.updateUser(user);
                        } else {
                            userDbHelper.addUser(user);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Failed to sync users from Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Đồng bộ từ SQLite lên Firebase
    private void syncCoursesToFirebase() {
        List<YogaCourse> coursesInSQLite = courseDbHelper.getAllYogaCourses();
        for (YogaCourse course : coursesInSQLite) {
            if (course.getFirebaseKey() == null || course.getFirebaseKey().isEmpty()) {
                String firebaseKey = courseRef.push().getKey();
                course.setFirebaseKey(firebaseKey);
                courseRef.child(firebaseKey).setValue(course);
                courseDbHelper.updateYogaCourse(course);
            } else {
                courseRef.child(course.getFirebaseKey()).setValue(course);
            }
        }
    }

    private void syncClassesToFirebase() {
        List<YogaClass> classesInSQLite = classDbHelper.getAllYogaClasses();
        for (YogaClass yogaClass : classesInSQLite) {
            if (yogaClass.getFirebaseKey() == null || yogaClass.getFirebaseKey().isEmpty()) {
                String firebaseKey = classRef.push().getKey();
                yogaClass.setFirebaseKey(firebaseKey);
                classRef.child(firebaseKey).setValue(yogaClass);
                classDbHelper.updateYogaClass(yogaClass);
            } else {
                classRef.child(yogaClass.getFirebaseKey()).setValue(yogaClass);
            }
        }
    }

    private void syncUsersToFirebase() {
        List<YogaUser> usersInSQLite = userDbHelper.getAllUsers();
        for (YogaUser user : usersInSQLite) {
            if (user.getFirebaseKey() == null || user.getFirebaseKey().isEmpty()) {
                String firebaseKey = userRef.push().getKey();
                user.setFirebaseKey(firebaseKey);
                userRef.child(firebaseKey).setValue(user);
                userDbHelper.updateUser(user);
            } else {
                userRef.child(user.getFirebaseKey()).setValue(user);
            }
        }
    }

    // Sync histories from SQLite to Firebase
}

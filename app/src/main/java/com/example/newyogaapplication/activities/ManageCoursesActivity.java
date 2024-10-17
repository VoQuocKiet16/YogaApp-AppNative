package com.example.newyogaapplication.activities;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newyogaapplication.R;
import com.example.newyogaapplication.classes.YogaCourse;
import com.example.newyogaapplication.adapters.YogaCourseAdapter;
import com.example.newyogaapplication.database.YogaCourseDB;
import com.example.newyogaapplication.utils.NetworkChangeReceiver;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ManageCoursesActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    YogaCourseAdapter adapter;
    YogaCourseDB courseDbHelper;
    List<YogaCourse> courseList;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference classRef;
    NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_courses);

        recyclerView = findViewById(R.id.recyclerView);
        courseDbHelper = new YogaCourseDB(this);

        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);

        // Firebase initialization
        firebaseDatabase = FirebaseDatabase.getInstance("https://thenewyoga-604c0-default-rtdb.asia-southeast1.firebasedatabase.app/");
        classRef = firebaseDatabase.getReference("yoga_courses");

        // Sync local database with Firebase on app launch
        syncFirebaseWithSQLite();

        // Fetch all courses from SQLite
        courseList = courseDbHelper.getAllYogaCourses();
        adapter = new YogaCourseAdapter(this, courseList, classRef, courseDbHelper);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Button to add course
        Button btnAddCourse = findViewById(R.id.btnAddCourse);
        btnAddCourse.setOnClickListener(v -> showAddCourseDialog());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }

    // Synchronize Firebase data with local SQLite
    private void syncFirebaseWithSQLite() {
        classRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    YogaCourse course = snapshot.getValue(YogaCourse.class);
                    if (course != null) {
                        YogaCourse existingCourse = courseDbHelper.getYogaCourseById(course.getId());
                        if (existingCourse != null) {
                            courseDbHelper.updateYogaCourse(course);  // Update existing course in SQLite
                        } else {
                            courseDbHelper.addYogaCourse(course);     // Insert new course into SQLite
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ManageCoursesActivity.this, "Failed to sync Firebase data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showAddCourseDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_yoga_course);

        // Initialize views
        Spinner spinnerDayOfWeek = dialog.findViewById(R.id.spinnerDayOfWeek);
        Spinner spinnerClassType = dialog.findViewById(R.id.spinnerClassType);
        EditText etTimeOfCourse = dialog.findViewById(R.id.etTimeOfCourse);
        EditText etCapacity = dialog.findViewById(R.id.etCapacity);
        EditText etDuration = dialog.findViewById(R.id.etDuration);
        EditText etPricePerClass = dialog.findViewById(R.id.etPricePerClass);
        EditText etDescription = dialog.findViewById(R.id.etDescription);
        EditText etNameCourse = dialog.findViewById(R.id.etNameCourse);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        // Populate spinners with arrays
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(this,
                R.array.days_of_week, android.R.layout.simple_spinner_item);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDayOfWeek.setAdapter(dayAdapter);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.yoga_class_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClassType.setAdapter(typeAdapter);

        // Add click listener for Time of Course
        etTimeOfCourse.setOnClickListener(v -> {
            // Open TimePickerDialog to choose the time of course
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(ManageCoursesActivity.this, (view, hourOfDay, minuteOfHour) -> {
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                etTimeOfCourse.setText(selectedTime);
            }, hour, minute, true);  // Use 24-hour format
            timePicker.show();
        });

        // Add click listener for Duration
        etDuration.setOnClickListener(v -> {
            // Open NumberPickerDialog to choose the duration
            NumberPicker numberPicker = new NumberPicker(ManageCoursesActivity.this);
            numberPicker.setMinValue(1);  // Minimum value (1 minute)
            numberPicker.setMaxValue(300); // Maximum value (300 minutes)

            AlertDialog.Builder builder = new AlertDialog.Builder(ManageCoursesActivity.this);
            builder.setTitle("Select Duration (in minutes)");
            builder.setView(numberPicker);
            builder.setPositiveButton("OK", (dialogInterface, which) -> {
                etDuration.setText(String.valueOf(numberPicker.getValue()));
            });
            builder.setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.dismiss());
            builder.create().show();
        });

        // Save button click listener
        btnSave.setOnClickListener(v -> {
            String day = spinnerDayOfWeek.getSelectedItem().toString();
            String time = etTimeOfCourse.getText().toString().trim();
            String capacityStr = etCapacity.getText().toString().trim();
            String durationStr = etDuration.getText().toString().trim();
            String priceStr = etPricePerClass.getText().toString().trim();
            String type = spinnerClassType.getSelectedItem().toString();
            String description = etDescription.getText().toString().trim();
            String nameCourse = etNameCourse.getText().toString().trim();

            // Validate inputs
            if (validateCourseInputs(day, time, capacityStr, durationStr, priceStr, nameCourse)) {
                int capacity;
                double price;
                try {
                    capacity = Integer.parseInt(capacityStr);
                    price = Double.parseDouble(priceStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(ManageCoursesActivity.this, "Please enter valid numbers for capacity and price", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Generate Firebase key
                String firebaseKey = classRef.push().getKey();
                if (firebaseKey == null) {
                    Toast.makeText(ManageCoursesActivity.this, "Error generating Firebase key", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create new course object
                YogaCourse newCourse = new YogaCourse(firebaseKey, firebaseKey, day, time, capacity, durationStr, price, type, description, nameCourse);

                // Add course to SQLite and Firebase
                long newCourseId = addNewCourse(newCourse);
                if (newCourseId != -1) {
                    // Update the UI if added successfully to SQLite
                    newCourse.setId(String.valueOf(newCourseId));

                    // Add course to Firebase
                    classRef.child(firebaseKey).setValue(newCourse).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ManageCoursesActivity.this, "Course added to Firebase!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ManageCoursesActivity.this, "Failed to add course to Firebase", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(ManageCoursesActivity.this, "Failed to add course to SQLite", Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private boolean validateCourseInputs(String day, String time, String capacityStr, String durationStr, String priceStr, String nameCourse) {
        if (nameCourse.isEmpty()) {
            Toast.makeText(ManageCoursesActivity.this, "Please enter course name", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (day.isEmpty() || time.isEmpty() || capacityStr.isEmpty() || durationStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(ManageCoursesActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private long addNewCourse(YogaCourse newCourse) {
        long newCourseId = courseDbHelper.addYogaCourse(newCourse);

        if (newCourseId != -1) {
            newCourse.setFirebaseKey(newCourse.getFirebaseKey());
            courseDbHelper.updateYogaCourse(newCourse);

            classRef.child(newCourse.getFirebaseKey()).setValue(newCourse).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    courseList.add(newCourse);
                    adapter.notifyItemInserted(courseList.size() - 1);
                    Toast.makeText(ManageCoursesActivity.this, "Course added successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ManageCoursesActivity.this, "Failed to add course to Firebase", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ManageCoursesActivity.this, "Failed to add course to SQLite", Toast.LENGTH_SHORT).show();
        }
        return newCourseId;
    }


    @Override
    protected void onResume() {
        super.onResume();
        courseList.clear();
        courseList.addAll(courseDbHelper.getAllYogaCourses());
        adapter.notifyDataSetChanged();
    }
}
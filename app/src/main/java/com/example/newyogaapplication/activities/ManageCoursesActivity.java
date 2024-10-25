package com.example.newyogaapplication.activities;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.example.newyogaapplication.adapters.YogaCourseAdapter;
import com.example.newyogaapplication.classes.YogaCourse;
import com.example.newyogaapplication.database.YogaCourseDB;
import com.example.newyogaapplication.sync.SyncManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ManageCoursesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private YogaCourseAdapter adapter;
    private YogaCourseDB courseDbHelper;
    private List<YogaCourse> courseList;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference courseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_courses);

        initUIComponents();
        initFirebase();
        syncFirebaseWithSQLite();
        setupRecyclerView();
        setupAddCourseButton();
        setupDeleteAllButton();

        // Start sync process using SyncManager
        SyncManager syncManager = new SyncManager(this);
        syncManager.startSyncing();
    }

    private void initFirebase() {
        firebaseDatabase = FirebaseDatabase.getInstance("https://thenewyoga-604c0-default-rtdb.asia-southeast1.firebasedatabase.app/");
        courseRef = firebaseDatabase.getReference("yoga_courses");
    }

    private void initUIComponents() {
        recyclerView = findViewById(R.id.recyclerView);
        courseDbHelper = new YogaCourseDB(this);
    }

    private void setupRecyclerView() {
        courseList = courseDbHelper.getAllYogaCourses();
        adapter = new YogaCourseAdapter(this, courseList, courseRef, courseDbHelper);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupAddCourseButton() {
        Button btnAddCourse = findViewById(R.id.btnAddCourse);
        btnAddCourse.setOnClickListener(v -> showAddCourseDialog());
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void syncFirebaseWithSQLite() {
        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                handleFirebaseSync(dataSnapshot);
                if (isNetworkAvailable()) {
                    syncUnsyncedCoursesToFirebase();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ManageCoursesActivity.this, "Failed to sync Firebase data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFirebaseSync(DataSnapshot dataSnapshot) {
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            YogaCourse course = snapshot.getValue(YogaCourse.class);
            if (course != null) {
                YogaCourse existingCourse = courseDbHelper.getYogaCourseById(course.getId());
                if (existingCourse == null) {
                    courseDbHelper.addYogaCourse(course);
                } else {
                    courseDbHelper.updateYogaCourse(course);
                }
            }
        }

        List<YogaCourse> allCoursesInSQLite = courseDbHelper.getAllYogaCourses();
        for (YogaCourse localCourse : allCoursesInSQLite) {
            if (!dataSnapshot.hasChild(localCourse.getFirebaseKey())) {
                courseDbHelper.deleteYogaCourse(localCourse.getId());
            }
        }

        updateCourseList(courseDbHelper.getAllYogaCourses());
    }

    private void syncUnsyncedCoursesToFirebase() {
        List<YogaCourse> unsyncedCourses = courseDbHelper.getUnsyncedYogaCourses();
        for (YogaCourse course : unsyncedCourses) {
            syncCourseToFirebase(course);
        }
        updateCourseList(courseDbHelper.getAllYogaCourses());
    }

    private void syncCourseToFirebase(YogaCourse course) {
        if (course.getFirebaseKey() == null) {
            String firebaseKey = courseRef.push().getKey();
            course.setFirebaseKey(firebaseKey);
        }
        courseRef.child(course.getFirebaseKey()).setValue(course).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                course.setSynced(true);  // Đặt isSynced thành true
                courseDbHelper.updateYogaCourse(course);  // Cập nhật trạng thái trong SQLite
                updateCourseList(courseDbHelper.getAllYogaCourses());  // Cập nhật danh sách khóa học
                Toast.makeText(ManageCoursesActivity.this, "Course synced with Firebase!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ManageCoursesActivity.this, "Failed to sync course with Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showAddCourseDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_yoga_course);

        Spinner spinnerDayOfWeek = dialog.findViewById(R.id.spinnerDayOfWeek);
        Spinner spinnerClassType = dialog.findViewById(R.id.spinnerClassType);
        EditText etTimeOfCourse = dialog.findViewById(R.id.etTimeOfCourse);
        EditText etCapacity = dialog.findViewById(R.id.etCapacity);
        EditText etDuration = dialog.findViewById(R.id.etDuration);
        EditText etPricePerClass = dialog.findViewById(R.id.etPricePerClass);
        EditText etDescription = dialog.findViewById(R.id.etDescription);
        EditText etNameCourse = dialog.findViewById(R.id.etNameCourse);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        setupSpinners(spinnerDayOfWeek, spinnerClassType);
        setupTimePicker(etTimeOfCourse);
        setupDurationPicker(etDuration);

        btnSave.setOnClickListener(v -> {
            if (validateInputs(spinnerDayOfWeek, spinnerClassType, etTimeOfCourse, etCapacity, etDuration, etPricePerClass, etDescription, etNameCourse)) {
                addNewCourse(spinnerDayOfWeek, spinnerClassType, etTimeOfCourse, etCapacity, etDuration, etPricePerClass, etDescription, etNameCourse);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void updateCourseList(List<YogaCourse> updatedCourses) {
        courseList.clear();
        courseList.addAll(updatedCourses);
        adapter.notifyDataSetChanged();
    }

    private void setupSpinners(Spinner spinnerDayOfWeek, Spinner spinnerClassType) {
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(this,
                R.array.days_of_week, android.R.layout.simple_spinner_item);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDayOfWeek.setAdapter(dayAdapter);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.yoga_class_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClassType.setAdapter(typeAdapter);
    }

    private void setupTimePicker(EditText etTimeOfCourse) {
        etTimeOfCourse.setOnClickListener(v -> {
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);
            TimePickerDialog timePicker = new TimePickerDialog(ManageCoursesActivity.this, (view, hourOfDay, minuteOfHour) -> {
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                etTimeOfCourse.setText(selectedTime);
            }, hour, minute, true);
            timePicker.show();
        });
    }

    private void setupDurationPicker(EditText etDuration) {
        etDuration.setOnClickListener(v -> {
            NumberPicker numberPicker = new NumberPicker(ManageCoursesActivity.this);
            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(300);

            AlertDialog.Builder builder = new AlertDialog.Builder(ManageCoursesActivity.this);
            builder.setTitle("Select Duration (in minutes)");
            builder.setView(numberPicker);
            builder.setPositiveButton("OK", (dialogInterface, which) -> etDuration.setText(String.valueOf(numberPicker.getValue())));
            builder.setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.dismiss());
            builder.create().show();
        });
    }

    private boolean validateInputs(Spinner spinnerDayOfWeek, Spinner spinnerClassType, EditText etTimeOfCourse,
                                   EditText etCapacity, EditText etDuration, EditText etPricePerClass, EditText etDescription, EditText etNameCourse) {
        if (etNameCourse.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter course name", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (spinnerDayOfWeek.getSelectedItem() == null || spinnerClassType.getSelectedItem() == null ||
                etTimeOfCourse.getText().toString().isEmpty() || etCapacity.getText().toString().isEmpty() ||
                etDuration.getText().toString().isEmpty() || etPricePerClass.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void addNewCourse(Spinner spinnerDayOfWeek, Spinner spinnerClassType, EditText etTimeOfCourse,
                              EditText etCapacity, EditText etDuration, EditText etPricePerClass, EditText etDescription, EditText etNameCourse) {
        String day = spinnerDayOfWeek.getSelectedItem().toString();
        String time = etTimeOfCourse.getText().toString().trim();
        String capacityStr = etCapacity.getText().toString().trim();
        String durationStr = etDuration.getText().toString().trim();
        String priceStr = etPricePerClass.getText().toString().trim();
        String type = spinnerClassType.getSelectedItem().toString();
        String description = etDescription.getText().toString().trim();
        String nameCourse = etNameCourse.getText().toString().trim();

        if (validateInputs(spinnerDayOfWeek, spinnerClassType, etTimeOfCourse, etCapacity, etDuration, etPricePerClass, etDescription, etNameCourse)) {
            int capacity;
            double price;

            try {
                capacity = Integer.parseInt(capacityStr);
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid capacity or price value. Please enter valid numbers.", Toast.LENGTH_SHORT).show();
                return;
            }

            String localId = java.util.UUID.randomUUID().toString();
            String firebaseKey = courseRef.push().getKey();  // Tạo firebaseKey từ Firebase
            YogaCourse newCourse = new YogaCourse(localId, firebaseKey, day, time, capacity, durationStr, price, type, description, nameCourse, false); // isSynced là false

            long newCourseId = courseDbHelper.addYogaCourse(newCourse);

            if (newCourseId != -1) {
                newCourse.setFirebaseKey(firebaseKey);  // Gán firebaseKey
                syncCourseToFirebase(newCourse);  // Đồng bộ lên Firebase nếu có mạng
                courseList.add(newCourse);  // Cập nhật danh sách hiển thị
                adapter.notifyItemInserted(courseList.size() - 1);
                Toast.makeText(ManageCoursesActivity.this, "Course added successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ManageCoursesActivity.this, "Failed to add course to SQLite", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupDeleteAllButton() {
        Button btnDeleteAllCourses = findViewById(R.id.btnDeleteAllCourses);
        btnDeleteAllCourses.setOnClickListener(v -> showDeleteAllConfirmation());
    }

    private void showDeleteAllConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete All Courses")
                .setMessage("Are you sure you want to delete all courses?")
                .setPositiveButton("Delete", (dialog, which) -> deleteAllCourses())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create().show();
    }

    private void deleteAllCourses() {
        // Xoá tất cả khoá học trong SQLite
        courseDbHelper.deleteAllYogaCourses();
        courseList.clear();
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "All courses deleted from SQLite", Toast.LENGTH_SHORT).show();

        // Xoá tất cả khoá học trong Firebase
        courseRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "All courses deleted from Firebase", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to delete courses from Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        updateCourseList(courseDbHelper.getAllYogaCourses());
        if (isNetworkAvailable()) {
            syncUnsyncedCoursesToFirebase();
        }
    }
}

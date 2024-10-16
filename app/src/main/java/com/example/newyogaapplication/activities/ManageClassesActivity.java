package com.example.newyogaapplication.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newyogaapplication.classes.CourseItem;
import com.example.newyogaapplication.R;
import com.example.newyogaapplication.classes.Role;
import com.example.newyogaapplication.classes.YogaClass;
import com.example.newyogaapplication.adapters.YogaClassAdapter;
import com.example.newyogaapplication.classes.YogaUser;
import com.example.newyogaapplication.database.YogaClassDB;
import com.example.newyogaapplication.classes.YogaCourse;
import com.example.newyogaapplication.database.YogaCourseDB;
import com.example.newyogaapplication.database.YogaUserDB;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ManageClassesActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    YogaClassAdapter adapter;
    YogaClassDB dbHelper;
    List<YogaClass> classList;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference classRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_classes);

        recyclerView = findViewById(R.id.recyclerView);
        dbHelper = new YogaClassDB(this);

        firebaseDatabase = FirebaseDatabase.getInstance("https://thenewyoga-604c0-default-rtdb.asia-southeast1.firebasedatabase.app/");
        classRef = firebaseDatabase.getReference("yoga_classes");

        syncFirebaseWithSQLite();

        classList = dbHelper.getAllYogaClasses();
        adapter = new YogaClassAdapter(this, classList, classRef, dbHelper);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        Button btnAddClass = findViewById(R.id.btnAddClass);
        btnAddClass.setOnClickListener(v -> {
            showAddClassDialog();
        });

        Spinner spinnerCourseFilter = findViewById(R.id.spinnerCourseFilter);
        EditText etSearchTeacher = findViewById(R.id.etSearchTeacher);
        Button btnSearch = findViewById(R.id.btnSearch);

        YogaCourseDB courseDBHelper = new YogaCourseDB(this);
        List<YogaCourse> courseList = courseDBHelper.getAllYogaCourses();
        List<CourseItem> courseItems = new ArrayList<>();

        courseItems.add(new CourseItem(null, "All Courses"));

        for (YogaCourse course : courseList) {
            courseItems.add(new CourseItem(course.getId(), course.getNameCourse()));
        }

        ArrayAdapter<CourseItem> courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseItems);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourseFilter.setAdapter(courseAdapter);

        btnSearch.setOnClickListener(v -> {
            CourseItem selectedCourseItem = (CourseItem) spinnerCourseFilter.getSelectedItem();
            String selectedCourseId = selectedCourseItem.getCourseId();
            String teacherName = etSearchTeacher.getText().toString().trim();

            List<YogaClass> filteredClasses = dbHelper.searchClasses(selectedCourseId, teacherName);
            classList.clear();
            classList.addAll(filteredClasses);
            adapter.notifyDataSetChanged();
        });
    }

    // Đồng bộ hóa dữ liệu Firebase với SQLite
    private void syncFirebaseWithSQLite() {
        classRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    YogaClass yogaClass = snapshot.getValue(YogaClass.class);
                    if (yogaClass != null) {
                        YogaClass existingClass = dbHelper.getYogaClass(yogaClass.getId());
                        if (existingClass == null) {
                            dbHelper.addYogaClass(yogaClass);
                        } else {
                            dbHelper.updateYogaClass(yogaClass);
                        }
                    }
                }

                List<YogaClass> allClassesInSQLite = dbHelper.getAllYogaClasses();
                for (YogaClass localClass : allClassesInSQLite) {
                    if (!dataSnapshot.hasChild(localClass.getFirebaseKey())) {
                        dbHelper.deleteYogaClass(localClass.getId());
                    }
                }

                classList.clear();
                classList.addAll(dbHelper.getAllYogaClasses());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ManageClassesActivity.this, "Failed to sync Firebase data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void showAddClassDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_yoga_class);

        // Initialize views
        DatePicker datePicker = dialog.findViewById(R.id.datePicker);
        Spinner spinnerCourses = dialog.findViewById(R.id.spinnerCourses);
        Spinner spinnerTeachers = dialog.findViewById(R.id.spinnerTeachers);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        // Fetch the list of courses for the spinner
        YogaCourseDB courseDBHelper = new YogaCourseDB(this);
        List<YogaCourse> courseList = courseDBHelper.getAllYogaCourses();
        List<CourseItem> courseItems = new ArrayList<>();

        for (YogaCourse course : courseList) {
            courseItems.add(new CourseItem(course.getId(), course.getNameCourse()));
        }

        ArrayAdapter<CourseItem> courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseItems);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourses.setAdapter(courseAdapter);

        // Fetch the list of teachers (users with the role "TEACHER") and use email instead of username
        YogaUserDB userDB = new YogaUserDB(this);
        List<YogaUser> teacherList = userDB.getUsersByRole(Role.TEACHER);
        List<String> teacherEmails = new ArrayList<>();

        // Kiểm tra xem danh sách giáo viên có trống không
        if (teacherList.isEmpty()) {
            Toast.makeText(this, "No teachers available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nạp danh sách email giáo viên vào Spinner
        for (YogaUser teacher : teacherList) {
            teacherEmails.add(teacher.getEmail()); // Use email instead of username
        }

        ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, teacherEmails);
        teacherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTeachers.setAdapter(teacherAdapter);

        // Save button click listener
        btnSave.setOnClickListener(v -> {
            // Get user inputs
            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth() + 1;
            int year = datePicker.getYear();
            String selectedDate = year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day);
            String selectedTeacherEmail = spinnerTeachers.getSelectedItem().toString(); // Use email
            CourseItem selectedCourse = (CourseItem) spinnerCourses.getSelectedItem();

            // Validate inputs
            if (selectedTeacherEmail.isEmpty() || selectedCourse == null) {
                Toast.makeText(ManageClassesActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if selected date is in the past
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar selectedDateCal = Calendar.getInstance();
            selectedDateCal.set(year, month - 1, day);

            if (selectedDateCal.before(today)) {
                Toast.makeText(ManageClassesActivity.this, "Selected day cannot be in the past", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the selected day matches the course's scheduled day
            if (!isDayMatchingCourse(selectedCourse.getCourseId(), day, month, year)) {
                Toast.makeText(ManageClassesActivity.this, "Selected day must match the course's scheduled day", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add new class
            String firebaseKey = classRef.push().getKey();
            YogaClass newClass = new YogaClass(null, selectedCourse.getCourseId(), selectedDate, selectedTeacherEmail, firebaseKey); // Use email here

            long newClassId = dbHelper.addYogaClass(newClass);

            if (newClassId != -1) {
                newClass.setFirebaseKey(firebaseKey);
                classRef.child(firebaseKey).setValue(newClass).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        classList.add(newClass);
                        adapter.notifyItemInserted(classList.size() - 1);
                        Toast.makeText(ManageClassesActivity.this, "Class added successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ManageClassesActivity.this, "Failed to add class to Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(ManageClassesActivity.this, "Failed to add class to SQLite", Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
        });

        dialog.show();
    }





    private boolean isDayMatchingCourse(String courseId, int day, int month, int year) {
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month - 1, day);

        int actualDayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK);
        YogaCourseDB courseDBHelper = new YogaCourseDB(this);
        YogaCourse course = courseDBHelper.getYogaCourseById(courseId);
        if (course != null) {
            String courseDayOfWeek = course.getDayOfWeek();
            String[] daysOfWeek = getResources().getStringArray(R.array.days_of_week);
            int courseDayIndex = getIndexOfDayOfWeek(courseDayOfWeek, daysOfWeek);
            return (actualDayOfWeek - 1) == courseDayIndex;
        }
        return false;
    }

    private int getIndexOfDayOfWeek(String dayOfWeek, String[] daysOfWeek) {
        for (int i = 0; i < daysOfWeek.length; i++) {
            if (daysOfWeek[i].equalsIgnoreCase(dayOfWeek)) {
                return i + 1;
            }
        }
        return -1;
    }

    @Override
    protected void onResume() {
        super.onResume();
        classList.clear();
        classList.addAll(dbHelper.getAllYogaClasses());
        adapter.notifyDataSetChanged();
    }
}


package com.example.newyogaapplication.activities;

import android.app.Dialog;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import com.example.newyogaapplication.R;
import com.example.newyogaapplication.adapters.YogaClassAdapter;
import com.example.newyogaapplication.classes.CourseItem;
import com.example.newyogaapplication.classes.Role;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ManageClassesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private YogaClassAdapter adapter;
    private YogaClassDB dbHelper;
    private List<YogaClass> classList;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference classRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_classes);

        initViews();
        initFirebase();
        syncFirebaseWithSQLite();
        setupRecyclerView();
        setupAddClassButton();
        setupSearchFunctionality();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        dbHelper = new YogaClassDB(this);
    }

    private void initFirebase() {
        firebaseDatabase = FirebaseDatabase.getInstance("https://thenewyoga-604c0-default-rtdb.asia-southeast1.firebasedatabase.app/");
        classRef = firebaseDatabase.getReference("yoga_classes");
    }

    private void setupRecyclerView() {
        classList = dbHelper.getAllYogaClasses();
        adapter = new YogaClassAdapter(this, classList, classRef, dbHelper);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupAddClassButton() {
        Button btnAddClass = findViewById(R.id.btnAddClass);
        btnAddClass.setOnClickListener(v -> showAddClassDialog());
    }

    private void setupSearchFunctionality() {
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
            updateClassList(filteredClasses);
        });
    }

    private void updateClassList(List<YogaClass> filteredClasses) {
        classList.clear();
        classList.addAll(filteredClasses);
        adapter.notifyDataSetChanged();
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
        classRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                handleFirebaseSync(dataSnapshot);
                if (isNetworkAvailable()) {
                    syncUnsyncedClassesToFirebase();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ManageClassesActivity.this, "Failed to sync Firebase data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFirebaseSync(DataSnapshot dataSnapshot) {
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

        updateClassList(dbHelper.getAllYogaClasses());
    }

    private void syncUnsyncedClassesToFirebase() {
        List<YogaClass> unsyncedClasses = dbHelper.getUnsyncedYogaClasses();
        for (YogaClass yogaClass : unsyncedClasses) {
            syncClassToFirebase(yogaClass);
        }
    }

    private void syncClassToFirebase(YogaClass yogaClass) {
        if (yogaClass.getFirebaseKey() == null) {
            yogaClass.setFirebaseKey(classRef.push().getKey());
        }

        classRef.child(yogaClass.getFirebaseKey()).setValue(yogaClass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Đồng bộ thành công, cập nhật trạng thái isSynced
                yogaClass.setSynced(true);
                dbHelper.updateYogaClass(yogaClass);  // Cập nhật trong SQLite

                // Thông báo cho RecyclerView về thay đổi
                notifyItemChangedById(yogaClass.getId());

                Toast.makeText(ManageClassesActivity.this, "Class synced with Firebase!", Toast.LENGTH_SHORT).show();
            } else {
                // Xử lý khi đồng bộ thất bại
                Toast.makeText(ManageClassesActivity.this, "Failed to sync class with Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void notifyItemChangedById(String classId) {
        for (int i = 0; i < classList.size(); i++) {
            if (classList.get(i).getId().equals(classId)) {
                adapter.notifyItemChanged(i);  // Thông báo cho RecyclerView về sự thay đổi
                break;
            }
        }
    }


    private void showAddClassDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_yoga_class);

        DatePicker datePicker = dialog.findViewById(R.id.datePicker);
        Spinner spinnerCourses = dialog.findViewById(R.id.spinnerCourses);
        Spinner spinnerTeachers = dialog.findViewById(R.id.spinnerTeachers);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        populateCourseSpinner(spinnerCourses);
        populateTeacherSpinner(spinnerTeachers);

        btnSave.setOnClickListener(v -> {
            if (validateInputs(spinnerCourses, spinnerTeachers, datePicker)) {
                addNewClass(spinnerCourses, spinnerTeachers, datePicker);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void populateCourseSpinner(Spinner spinnerCourses) {
        YogaCourseDB courseDBHelper = new YogaCourseDB(this);
        List<YogaCourse> courseList = courseDBHelper.getAllYogaCourses();
        List<CourseItem> courseItems = new ArrayList<>();

        for (YogaCourse course : courseList) {
            courseItems.add(new CourseItem(course.getId(), course.getNameCourse()));
        }

        ArrayAdapter<CourseItem> courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseItems);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourses.setAdapter(courseAdapter);
    }

    private void populateTeacherSpinner(Spinner spinnerTeachers) {
        YogaUserDB userDB = new YogaUserDB(this);
        List<YogaUser> teacherList = userDB.getUsersByRole(Role.TEACHER);
        List<String> teacherEmails = new ArrayList<>();

        if (teacherList.isEmpty()) {
            Toast.makeText(this, "No teachers available", Toast.LENGTH_SHORT).show();
            return;
        }

        for (YogaUser teacher : teacherList) {
            teacherEmails.add(teacher.getEmail());
        }

        ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, teacherEmails);
        teacherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTeachers.setAdapter(teacherAdapter);
    }

    private boolean validateInputs(Spinner spinnerCourses, Spinner spinnerTeachers, DatePicker datePicker) {
        if (spinnerTeachers.getSelectedItem() == null || spinnerCourses.getSelectedItem() == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (isDateInThePast(datePicker)) {
            Toast.makeText(this, "Selected day cannot be in the past", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isDayMatchingCourse(((CourseItem) spinnerCourses.getSelectedItem()).getCourseId(), datePicker)) {
            Toast.makeText(this, "Selected day must match the course's scheduled day", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isDateInThePast(DatePicker datePicker) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

        return selectedDate.before(today);
    }

    private boolean isDayMatchingCourse(String courseId, DatePicker datePicker) {
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
        int actualDayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK);

        YogaCourseDB courseDBHelper = new YogaCourseDB(this);
        YogaCourse course = courseDBHelper.getYogaCourseById(courseId);
        if (course != null) {
            String courseDayOfWeek = course.getDayOfWeek();
            String[] daysOfWeek = getResources().getStringArray(R.array.days_of_week);
            int courseDayIndex = getIndexOfDayOfWeek(courseDayOfWeek, daysOfWeek);
            return actualDayOfWeek == courseDayIndex;
        }
        return false;
    }

    private int getIndexOfDayOfWeek(String dayOfWeek, String[] daysOfWeek) {
        for (int i = 0; i < daysOfWeek.length; i++) {
            if (daysOfWeek[i].equalsIgnoreCase(dayOfWeek)) {
                return i + 1; // Because Calendar.SUNDAY = 1, Calendar.MONDAY = 2, and so on.
            }
        }
        return -1;
    }

    private void addNewClass(Spinner spinnerCourses, Spinner spinnerTeachers, DatePicker datePicker) {
        String selectedDate = formatDate(datePicker);
        String selectedTeacherEmail = spinnerTeachers.getSelectedItem().toString();
        CourseItem selectedCourse = (CourseItem) spinnerCourses.getSelectedItem();

        String localId = java.util.UUID.randomUUID().toString();
        String firebaseKey = classRef.push().getKey();
        YogaClass newClass = new YogaClass(localId, selectedCourse.getCourseId(), selectedDate, selectedTeacherEmail, firebaseKey, false);

        long newClassId = dbHelper.addYogaClass(newClass);

        if (newClassId != -1) {
            newClass.setFirebaseKey(firebaseKey);
            classList.add(newClass);
            adapter.notifyItemInserted(classList.size() - 1);

            // Sau khi thêm lớp vào SQLite, đồng bộ với Firebase
            syncClassToFirebase(newClass);

            Toast.makeText(ManageClassesActivity.this, "Class added successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ManageClassesActivity.this, "Failed to add class to SQLite", Toast.LENGTH_SHORT).show();
        }
    }


    private String formatDate(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1;
        int year = datePicker.getYear();
        return year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateClassList(dbHelper.getAllYogaClasses());  // Làm mới danh sách từ SQLite

        // Nếu có mạng, đồng bộ các lớp chưa được đồng bộ
        if (isNetworkAvailable()) {
            syncUnsyncedClassesToFirebase();
        }
    }

}

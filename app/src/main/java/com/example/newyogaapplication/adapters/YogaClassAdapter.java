package com.example.newyogaapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newyogaapplication.R;
import com.example.newyogaapplication.activities.CourseDetailActivity;
import com.example.newyogaapplication.activities.TeacherActivity;
import com.example.newyogaapplication.classes.Role;
import com.example.newyogaapplication.classes.YogaClass;
import com.example.newyogaapplication.classes.YogaCourse;
import com.example.newyogaapplication.classes.YogaUser;
import com.example.newyogaapplication.database.YogaClassDB;
import com.example.newyogaapplication.database.YogaCourseDB;
import com.example.newyogaapplication.database.YogaUserDB;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class YogaClassAdapter extends RecyclerView.Adapter<YogaClassAdapter.ClassViewHolder> {

    private final Context context;
    private final List<YogaClass> classList;
    private final DatabaseReference classRef;
    private final YogaClassDB dbHelper;
    private final YogaCourseDB courseDbHelper;

    public YogaClassAdapter(Context context, List<YogaClass> classList, DatabaseReference classRef, YogaClassDB dbHelper) {
        this.context = context;
        this.classList = classList;
        this.classRef = classRef;
        this.dbHelper = dbHelper;
        this.courseDbHelper = new YogaCourseDB(context);
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_yoga_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        YogaClass yogaClass = classList.get(position);

        holder.tvClassDetails.setText(
                yogaClass.getDate() + " - " +
                        yogaClass.getTeacher() + " - " +
                        (yogaClass.isSynced() ? "Loaded" : "Loading")
        );


        if (context instanceof CourseDetailActivity || context instanceof TeacherActivity) {
            holder.imgEdit.setVisibility(View.GONE);
            holder.imgDelete.setVisibility(View.GONE);
        } else {
            holder.imgEdit.setVisibility(View.VISIBLE);
            holder.imgDelete.setVisibility(View.VISIBLE);

            holder.imgEdit.setOnClickListener(v -> showEditDialog(yogaClass));
            holder.imgDelete.setOnClickListener(v -> showDeleteConfirmation(yogaClass));
        }
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    // Method to show Edit Dialog
    private void showEditDialog(YogaClass yogaClass) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Class");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_class, null);
        builder.setView(dialogView);

        DatePicker datePicker = dialogView.findViewById(R.id.datePicker);
        Spinner spinnerCourseName = dialogView.findViewById(R.id.spinnerCourseName);
        Spinner spinnerTeachers = dialogView.findViewById(R.id.spinnerTeachers);

        setupSpinnerCourses(spinnerCourseName, yogaClass);
        setupSpinnerTeachers(spinnerTeachers, yogaClass);
        setupDatePicker(datePicker, yogaClass.getDate());

        builder.setPositiveButton("Update", (dialog, which) -> updateClass(yogaClass, spinnerCourseName, spinnerTeachers, datePicker));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    // Helper methods to set up UI components
    private void setupSpinnerCourses(Spinner spinnerCourseName, YogaClass yogaClass) {
        List<String> courseNames = courseDbHelper.getAllCourseNames();
        ArrayAdapter<String> courseNameAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, courseNames);
        courseNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourseName.setAdapter(courseNameAdapter);

        YogaCourse yogaCourse = courseDbHelper.getYogaCourseById(yogaClass.getCourseId());
        if (yogaCourse != null) {
            int courseNamePosition = courseNames.indexOf(yogaCourse.getNameCourse());
            if (courseNamePosition != -1) {
                spinnerCourseName.setSelection(courseNamePosition);
            }
        }
    }

    private void setupSpinnerTeachers(Spinner spinnerTeachers, YogaClass yogaClass) {
        YogaUserDB userDB = new YogaUserDB(context);
        List<String> teacherEmails = new ArrayList<>();
        for (YogaUser teacher : userDB.getUsersByRole(Role.TEACHER)) {
            teacherEmails.add(teacher.getEmail());
        }

        ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, teacherEmails);
        teacherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTeachers.setAdapter(teacherAdapter);

        int teacherPosition = teacherEmails.indexOf(yogaClass.getTeacher());
        if (teacherPosition != -1) {
            spinnerTeachers.setSelection(teacherPosition);
        }
    }

    private void setupDatePicker(DatePicker datePicker, String date) {
        String[] dateParts = date.split("-");
        datePicker.updateDate(Integer.parseInt(dateParts[0]), Integer.parseInt(dateParts[1]) - 1, Integer.parseInt(dateParts[2]));
    }

    // Method to handle updating a class
    private void updateClass(YogaClass yogaClass, Spinner spinnerCourseName, Spinner spinnerTeachers, DatePicker datePicker) {
        String selectedTeacherEmail = spinnerTeachers.getSelectedItem().toString();
        String selectedCourseName = spinnerCourseName.getSelectedItem().toString();
        String selectedDate = formatDate(datePicker);

        if (isInputValid(selectedTeacherEmail, selectedCourseName, datePicker)) {
            YogaCourse selectedCourse = courseDbHelper.getYogaCourseByName(selectedCourseName);
            yogaClass.setCourseId(selectedCourse.getId());
            yogaClass.setTeacher(selectedTeacherEmail);
            yogaClass.setDate(selectedDate);

            updateYogaClassInDB(yogaClass);
            updateYogaClassInFirebase(yogaClass);
        }
    }

    private boolean isInputValid(String selectedTeacherEmail, String selectedCourseName, DatePicker datePicker) {
        if (selectedTeacherEmail.isEmpty()) {
            showToast("Please select a teacher");
            return false;
        }

        if (selectedCourseName.isEmpty()) {
            showToast("Please select a course");
            return false;
        }

        if (isPastDate(datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth())) {
            showToast("Selected date cannot be in the past");
            return false;
        }

        YogaCourse selectedCourse = courseDbHelper.getYogaCourseByName(selectedCourseName);
        if (selectedCourse != null && !isDayMatchingCourse(selectedCourse.getId(), datePicker.getDayOfMonth(), datePicker.getMonth() + 1, datePicker.getYear())) {
            showToast("Selected day must match the course's scheduled day");
            return false;
        }

        return true;
    }

    // Methods to update class in SQLite and Firebase
    private void updateYogaClassInDB(YogaClass yogaClass) {
        int rowsAffected = dbHelper.updateYogaClass(yogaClass);
        if (rowsAffected > 0) {
            notifyItemChangedById(yogaClass.getId());
            showToast("Class updated successfully");
        } else {
            showToast("Failed to update class in SQLite");
        }
    }

    private void updateYogaClassInFirebase(YogaClass yogaClass) {
        if (yogaClass.getFirebaseKey() != null) {
            classRef.child(yogaClass.getFirebaseKey()).setValue(yogaClass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    showToast("Class updated successfully in Firebase");
                } else {
                    showToast("Failed to update class in Firebase");
                }
            });
        }
    }

    // Helper methods
    private void showDeleteConfirmation(YogaClass yogaClass) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Class")
                .setMessage("Are you sure you want to delete this class?")
                .setPositiveButton("Delete", (dialog, which) -> deleteClass(yogaClass))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create().show();
    }

    private void deleteClass(YogaClass yogaClass) {
        dbHelper.deleteYogaClass(yogaClass.getId());
        removeClassById(yogaClass.getId());
        showToast("Class deleted from SQLite");

        if (yogaClass.getFirebaseKey() != null) {
            classRef.child(yogaClass.getFirebaseKey()).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    showToast("Class deleted successfully from Firebase");
                } else {
                    showToast("Failed to delete class from Firebase");
                }
            });
        }
    }


    private void removeClassById(String classId) {
        for (int i = 0; i < classList.size(); i++) {
            if (classList.get(i).getId().equals(classId)) {
                classList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    private void notifyItemChangedById(String classId) {
        for (int i = 0; i < classList.size(); i++) {
            if (classList.get(i).getId().equals(classId)) {
                notifyItemChanged(i);
                break;
            }
        }
    }

    private boolean isPastDate(int year, int month, int day) {
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month - 1, day);
        return selectedDate.before(Calendar.getInstance());
    }

    private boolean isDayMatchingCourse(String courseId, int day, int month, int year) {
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month - 1, day);

        int actualDayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK);
        YogaCourse course = courseDbHelper.getYogaCourseById(courseId);

        if (course != null) {
            String courseDayOfWeek = course.getDayOfWeek();
            String[] daysOfWeek = context.getResources().getStringArray(R.array.days_of_week);
            int courseDayIndex = getIndexOfDayOfWeek(courseDayOfWeek, daysOfWeek);
            return actualDayOfWeek == courseDayIndex;
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

    private String formatDate(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1;
        int year = datePicker.getYear();
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView tvClassDetails;
        ImageView imgEdit;
        ImageView imgDelete;

        public ClassViewHolder(View itemView) {
            super(itemView);
            tvClassDetails = itemView.findViewById(R.id.tvClassDetails);
            imgEdit = itemView.findViewById(R.id.imgEdit);
            imgDelete = itemView.findViewById(R.id.imgDelete);
        }
    }
}

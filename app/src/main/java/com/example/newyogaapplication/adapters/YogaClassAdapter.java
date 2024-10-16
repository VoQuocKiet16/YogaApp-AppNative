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
    private Context context;
    private List<YogaClass> classList;
    private DatabaseReference classRef;
    private YogaClassDB dbHelper;
    private YogaCourseDB courseDbHelper;

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
        holder.tvClassDetails.setText(yogaClass.getDate() + " by " + yogaClass.getTeacher());

        // Check if the context is CourseDetailActivity
        if (context instanceof CourseDetailActivity || context instanceof TeacherActivity) {
            // Hide edit and delete icons in CourseDetailActivity
            holder.imgEdit.setVisibility(View.GONE);
            holder.imgDelete.setVisibility(View.GONE);
        } else {
            // Show edit and delete icons in other activities
            holder.imgEdit.setVisibility(View.VISIBLE);
            holder.imgDelete.setVisibility(View.VISIBLE);

            // Set onClick listener for edit actions
            holder.imgEdit.setOnClickListener(v -> showEditDialog(yogaClass, position));

            // Set onClick listener for delete actions
            holder.imgDelete.setOnClickListener(v -> showDeleteConfirmation(yogaClass, position));
        }
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    public void showEditDialog(YogaClass yogaClass, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Class");

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_edit_class, null);
        builder.setView(dialogView);

        DatePicker datePicker = dialogView.findViewById(R.id.datePicker);
        Spinner spinnerCourseName = dialogView.findViewById(R.id.spinnerCourseName);
        Spinner spinnerTeachers = dialogView.findViewById(R.id.spinnerTeachers); // Add spinner for teachers

        // Populate spinner with course names from SQLite
        List<String> courseNames = courseDbHelper.getAllCourseNames();
        ArrayAdapter<String> courseNameAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, courseNames);
        courseNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourseName.setAdapter(courseNameAdapter);

        // Set existing data in the dialog
        String[] dateParts = yogaClass.getDate().split("-");
        datePicker.updateDate(Integer.parseInt(dateParts[0]), Integer.parseInt(dateParts[1]) - 1, Integer.parseInt(dateParts[2]));

        YogaCourse yogaCourse = courseDbHelper.getYogaCourseById(yogaClass.getCourseId());
        if (yogaCourse != null) {
            int courseNamePosition = courseNames.indexOf(yogaCourse.getNameCourse());
            if (courseNamePosition != -1) {
                spinnerCourseName.setSelection(courseNamePosition);
            }
        }

        // Populate spinner with teachers (use email instead of username)
        YogaUserDB userDB = new YogaUserDB(context);
        List<YogaUser> teacherList = userDB.getUsersByRole(Role.TEACHER);
        List<String> teacherEmails = new ArrayList<>();

        for (YogaUser teacher : teacherList) {
            teacherEmails.add(teacher.getEmail()); // Use email instead of username
        }

        ArrayAdapter<String> teacherAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, teacherEmails);
        teacherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTeachers.setAdapter(teacherAdapter);

        // Set the selected teacher (use email)
        int teacherPosition = teacherEmails.indexOf(yogaClass.getTeacher());
        if (teacherPosition != -1) {
            spinnerTeachers.setSelection(teacherPosition);
        }

        // Update button click listener
        builder.setPositiveButton("Update", (dialog, which) -> {
            String selectedTeacherEmail = spinnerTeachers.getSelectedItem().toString(); // Use email
            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth() + 1;
            int year = datePicker.getYear();
            String selectedDate = year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day);
            String selectedCourseName = spinnerCourseName.getSelectedItem().toString();

            if (selectedTeacherEmail.isEmpty()) {
                Toast.makeText(context, "Please select a teacher", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedCourseName.isEmpty()) {
                Toast.makeText(context, "Please select a course", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isPastDate(year, month, day)) {
                Toast.makeText(context, "Selected date cannot be in the past", Toast.LENGTH_SHORT).show();
                return;
            }

            YogaCourse selectedCourse = courseDbHelper.getYogaCourseByName(selectedCourseName);
            if (selectedCourse != null) {
                if (!isDayMatchingCourse(selectedCourse.getId(), day, month, year, context)) {
                    Toast.makeText(context, "Selected day must match the course's scheduled day", Toast.LENGTH_SHORT).show();
                    return;
                }
                yogaClass.setCourseId(selectedCourse.getId());
            } else {
                Toast.makeText(context, "Selected course not found", Toast.LENGTH_SHORT).show();
                return;
            }

            yogaClass.setTeacher(selectedTeacherEmail); // Set teacher email instead of username
            yogaClass.setDate(selectedDate);

            int rowsAffected = dbHelper.updateYogaClass(yogaClass);
            if (rowsAffected > 0) {
                notifyItemChanged(position);
                Toast.makeText(context, "Class updated successfully", Toast.LENGTH_SHORT).show();

                // Update in Firebase
                if (yogaClass.getFirebaseKey() != null) {
                    classRef.child(yogaClass.getFirebaseKey()).setValue(yogaClass).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Class updated successfully in Firebase", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to update class in Firebase", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(context, "Failed to update class in SQLite", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    // Helper method to check if the date is in the past
    private boolean isPastDate(int year, int month, int day) {
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month - 1, day);
        Calendar today = Calendar.getInstance();
        return selectedDate.before(today);
    }

    // Method to check if the selected day matches the course's day
    private boolean isDayMatchingCourse(String courseId, int day, int month, int year, Context context) {
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month - 1, day);

        int actualDayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK);
        YogaCourse course = courseDbHelper.getYogaCourseById(courseId);

        if (course != null) {
            String courseDayOfWeek = course.getDayOfWeek();
            String[] daysOfWeek = context.getResources().getStringArray(R.array.days_of_week);
            int courseDayIndex = getIndexOfDayOfWeek(courseDayOfWeek, daysOfWeek);
            return (actualDayOfWeek - 1) == courseDayIndex;
        }
        return false;
    }

    // Helper method to get the index of the day of the week
    private int getIndexOfDayOfWeek(String dayOfWeek, String[] daysOfWeek) {
        for (int i = 0; i < daysOfWeek.length; i++) {
            if (daysOfWeek[i].equalsIgnoreCase(dayOfWeek)) {
                return i + 1;
            }
        }
        return -1;
    }

    // Method to show Delete Confirmation
    private void showDeleteConfirmation(YogaClass yogaClass, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Class");
        builder.setMessage("Are you sure you want to delete this class?");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            if (yogaClass.getFirebaseKey() != null) {
                classRef.child(yogaClass.getFirebaseKey()).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dbHelper.deleteYogaClass(yogaClass.getId());
                        classList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Class deleted successfully from Firebase and SQLite", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to delete class from Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                dbHelper.deleteYogaClass(yogaClass.getId());
                classList.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "Class deleted from SQLite", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Inner class to represent the class view
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

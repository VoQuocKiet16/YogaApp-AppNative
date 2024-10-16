package com.example.newyogaapplication.adapters;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newyogaapplication.R;
import com.example.newyogaapplication.activities.CourseDetailActivity;
import com.example.newyogaapplication.classes.YogaCourse;
import com.example.newyogaapplication.database.YogaCourseDB;
import com.google.firebase.database.DatabaseReference;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class YogaCourseAdapter extends RecyclerView.Adapter<YogaCourseAdapter.CourseViewHolder> {
    private Context context;
    private List<YogaCourse> courseList;
    private DatabaseReference classRef;
    private YogaCourseDB courseDbHelper;

    // Constructor modified to accept YogaCourseDB instead of YogaCourseDBHelper
    public YogaCourseAdapter(Context context, List<YogaCourse> courseList, DatabaseReference classRef, YogaCourseDB courseDbHelper) {
        this.context = context;
        this.courseList = courseList;
        this.classRef = classRef;
        this.courseDbHelper = courseDbHelper;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_yoga_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        YogaCourse course = courseList.get(position);
        holder.tvClassDetails.setText(course.getNameCourse() + " - " + course.getClassType() + " on " + course.getDayOfWeek() + " at " + course.getTime());

        // Set onClick listener for edit actions
        holder.imgEdit.setOnClickListener(v -> showEditDialog(course, position));

        // Set onClick listener for delete actions
        holder.imgDelete.setOnClickListener(v -> showDeleteConfirmation(course, position));

        // Set onClick listener for detail actions
        holder.imgDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, CourseDetailActivity.class);
            intent.putExtra("course", course);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    // Method to show Edit Dialog
    private void showEditDialog(YogaCourse course, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Course");

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_edit_course, null);
        builder.setView(dialogView);

        // Initialize EditText and other views
        EditText edtCourseName = dialogView.findViewById(R.id.etNameCourse);
        Spinner spinnerDayOfWeek = dialogView.findViewById(R.id.spinnerDayOfWeek);
        Spinner spinnerClassType = dialogView.findViewById(R.id.spinnerClassType);
        EditText etTimeOfCourse = dialogView.findViewById(R.id.etTimeOfCourse);
        EditText etCapacity = dialogView.findViewById(R.id.etCapacity);
        EditText etDuration = dialogView.findViewById(R.id.etDuration);
        EditText etPricePerClass = dialogView.findViewById(R.id.etPricePerClass);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);

        // Pre-fill existing course data
        if (course != null) {
            edtCourseName.setText(course.getNameCourse());
            etTimeOfCourse.setText(course.getTime());
            etCapacity.setText(String.valueOf(course.getCapacity()));
            etDuration.setText(course.getDuration());
            etPricePerClass.setText(String.valueOf(course.getPricePerClass()));
            etDescription.setText(course.getDescription());

            // Set spinners to the correct selections
            ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(context,
                    R.array.days_of_week, android.R.layout.simple_spinner_item);
            dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDayOfWeek.setAdapter(dayAdapter);
            spinnerDayOfWeek.setSelection(dayAdapter.getPosition(course.getDayOfWeek()));

            ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(context,
                    R.array.yoga_class_types, android.R.layout.simple_spinner_item);
            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerClassType.setAdapter(typeAdapter);
            spinnerClassType.setSelection(typeAdapter.getPosition(course.getClassType()));
        }

        // Set Time of Course using TimePicker
        etTimeOfCourse.setOnClickListener(v -> {
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(context, (view, hourOfDay, minuteOfHour) -> {
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                etTimeOfCourse.setText(selectedTime);
            }, hour, minute, true);
            timePicker.show();
        });

        // Set Duration using NumberPicker
        etDuration.setOnClickListener(v -> {
            NumberPicker numberPicker = new NumberPicker(context);
            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(300); // Max 300 minutes

            AlertDialog.Builder durationDialog = new AlertDialog.Builder(context);
            durationDialog.setTitle("Select Duration (in minutes)");
            durationDialog.setView(numberPicker);
            durationDialog.setPositiveButton("OK", (dialogInterface, which) -> etDuration.setText(String.valueOf(numberPicker.getValue())));
            durationDialog.setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.dismiss());
            durationDialog.show();
        });

        // Save button click listener
        builder.setPositiveButton("Update", (dialog, which) -> {
            // Get user inputs
            String updatedName = edtCourseName.getText().toString().trim();
            String day = spinnerDayOfWeek.getSelectedItem().toString();
            String time = etTimeOfCourse.getText().toString().trim();
            String capacityStr = etCapacity.getText().toString().trim();
            String durationStr = etDuration.getText().toString().trim();
            String priceStr = etPricePerClass.getText().toString().trim();
            String type = spinnerClassType.getSelectedItem().toString();
            String description = etDescription.getText().toString().trim();

            // Validate input fields
            if (updatedName.isEmpty() || day.isEmpty() || time.isEmpty() || capacityStr.isEmpty() || durationStr.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate numbers for capacity and price
            int capacity;
            double price;
            try {
                capacity = Integer.parseInt(capacityStr);
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Please enter valid numbers for capacity and price", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update course object with new values
            course.setNameCourse(updatedName);
            course.setDayOfWeek(day);
            course.setTime(time);
            course.setCapacity(capacity);
            course.setDuration(durationStr);
            course.setPricePerClass(price);
            course.setClassType(type);
            course.setDescription(description);

            // Update in SQLite
            int rowsAffected = courseDbHelper.updateYogaCourse(course);
            if (rowsAffected > 0) {
                // Notify adapter about data change
                notifyItemChanged(position);
                Toast.makeText(context, "Course updated successfully in SQLite", Toast.LENGTH_SHORT).show();

                // Update in Firebase
                if (course.getFirebaseKey() != null) {
                    classRef.child(course.getFirebaseKey()).setValue(course).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Course updated successfully in Firebase", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to update course in Firebase", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(context, "Failed to update course in SQLite", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    // Method to show Delete Confirmation
    private void showDeleteConfirmation(YogaCourse course, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Course");
        builder.setMessage("Are you sure you want to delete this course?");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            // Delete from SQLite using YogaCourseDB
            courseDbHelper.deleteYogaCourse(course.getId());

            // Delete from Firebase
            if (course.getFirebaseKey() != null) {
                classRef.child(course.getFirebaseKey()).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // After successful Firebase deletion, remove from the local list
                        courseList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Course deleted successfully from Firebase and SQLite", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to delete course from Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Course does not exist in Firebase, handle only local deletion
                courseList.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "Course deleted from SQLite", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Inner class to represent the course view
    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvClassDetails;
        ImageView imgEdit;
        ImageView imgDelete;
        ImageView imgDetail;

        public CourseViewHolder(View itemView) {
            super(itemView);
            tvClassDetails = itemView.findViewById(R.id.tvClassDetails);
            imgEdit = itemView.findViewById(R.id.imgEdit);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            imgDetail = itemView.findViewById(R.id.imgDetail);
        }
    }
}

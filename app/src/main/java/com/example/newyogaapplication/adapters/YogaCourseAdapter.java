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
    private final Context context;
    private final List<YogaCourse> courseList;
    private final DatabaseReference classRef;
    private final YogaCourseDB courseDbHelper;

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
        holder.tvClassDetails.setText(
                course.getNameCourse() + " - " +
                        course.getClassType() + " on " +
                        course.getDayOfWeek() + " at " +
                        course.getTime() + " - " +
                        (course.isSynced() ? "Loaded" : "Loading")
        );

        holder.imgEdit.setOnClickListener(v -> showEditDialog(course));
        holder.imgDelete.setOnClickListener(v -> showDeleteConfirmation(course));
        holder.imgDetail.setOnClickListener(v -> viewCourseDetail(course));
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    // Hiển thị hộp thoại chỉnh sửa khóa học
    private void showEditDialog(YogaCourse course) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_course, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Edit Course")
                .setView(dialogView);

        // Gán giá trị vào các trường EditText và Spinner
        EditText edtCourseName = dialogView.findViewById(R.id.etNameCourse);
        Spinner spinnerDayOfWeek = dialogView.findViewById(R.id.spinnerDayOfWeek);
        Spinner spinnerClassType = dialogView.findViewById(R.id.spinnerClassType);
        EditText etTimeOfCourse = dialogView.findViewById(R.id.etTimeOfCourse);
        EditText etCapacity = dialogView.findViewById(R.id.etCapacity);
        EditText etDuration = dialogView.findViewById(R.id.etDuration);
        EditText etPricePerClass = dialogView.findViewById(R.id.etPricePerClass);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);

        fillCourseData(course, edtCourseName, spinnerDayOfWeek, spinnerClassType, etTimeOfCourse, etCapacity, etDuration, etPricePerClass, etDescription);

        // Set TimePicker cho Time of Course
        setupTimePicker(etTimeOfCourse);

        // Set NumberPicker cho Duration
        setupDurationPicker(etDuration);

        builder.setPositiveButton("Update", (dialog, which) -> updateCourse(course, edtCourseName, spinnerDayOfWeek, spinnerClassType, etTimeOfCourse, etCapacity, etDuration, etPricePerClass, etDescription))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    // Điền dữ liệu khóa học vào các trường
    private void fillCourseData(YogaCourse course, EditText edtCourseName, Spinner spinnerDayOfWeek, Spinner spinnerClassType, EditText etTimeOfCourse, EditText etCapacity, EditText etDuration, EditText etPricePerClass, EditText etDescription) {
        edtCourseName.setText(course.getNameCourse());
        etTimeOfCourse.setText(course.getTime());
        etCapacity.setText(String.valueOf(course.getCapacity()));
        etDuration.setText(course.getDuration());
        etPricePerClass.setText(String.valueOf(course.getPricePerClass()));
        etDescription.setText(course.getDescription());

        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(context, R.array.days_of_week, android.R.layout.simple_spinner_item);
        spinnerDayOfWeek.setAdapter(dayAdapter);
        spinnerDayOfWeek.setSelection(dayAdapter.getPosition(course.getDayOfWeek()));

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(context, R.array.yoga_class_types, android.R.layout.simple_spinner_item);
        spinnerClassType.setAdapter(typeAdapter);
        spinnerClassType.setSelection(typeAdapter.getPosition(course.getClassType()));
    }

    // Cài đặt TimePicker
    private void setupTimePicker(EditText etTimeOfCourse) {
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
    }

    // Cài đặt NumberPicker cho Duration
    private void setupDurationPicker(EditText etDuration) {
        etDuration.setOnClickListener(v -> {
            NumberPicker numberPicker = new NumberPicker(context);
            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(300);

            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle("Select Duration (in minutes)")
                    .setView(numberPicker)
                    .setPositiveButton("OK", (dialogInterface, which) -> etDuration.setText(String.valueOf(numberPicker.getValue())))
                    .setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.dismiss());
            builder.create().show();
        });
    }

    // Cập nhật khóa học sau khi chỉnh sửa
    private void updateCourse(YogaCourse course, EditText edtCourseName, Spinner spinnerDayOfWeek, Spinner spinnerClassType,
                              EditText etTimeOfCourse, EditText etCapacity, EditText etDuration, EditText etPricePerClass, EditText etDescription) {
        // Cập nhật giá trị từ EditText và Spinner vào YogaCourse
        course.setNameCourse(edtCourseName.getText().toString().trim());
        course.setDayOfWeek(spinnerDayOfWeek.getSelectedItem().toString());
        course.setTime(etTimeOfCourse.getText().toString().trim());
        course.setDuration(etDuration.getText().toString().trim());
        course.setClassType(spinnerClassType.getSelectedItem().toString());
        course.setDescription(etDescription.getText().toString().trim());

        // Bắt lỗi khi chuyển đổi capacity và pricePerClass
        try {
            int capacity = Integer.parseInt(etCapacity.getText().toString().trim());
            course.setCapacity(capacity);
        } catch (NumberFormatException e) {
            Toast.makeText(context, "Invalid capacity value. Please enter a valid number.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double price = Double.parseDouble(etPricePerClass.getText().toString().trim());
            course.setPricePerClass(price);
        } catch (NumberFormatException e) {
            Toast.makeText(context, "Invalid price value. Please enter a valid number.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cập nhật khóa học trong SQLite
        if (courseDbHelper.updateYogaCourse(course) > 0) {
            // Thông báo adapter về thay đổi
            notifyItemChangedById(course.getId());
            Toast.makeText(context, "Course updated successfully in SQLite", Toast.LENGTH_SHORT).show();

            // Cập nhật lên Firebase
            updateCourseInFirebase(course);
        } else {
            Toast.makeText(context, "Failed to update course in SQLite", Toast.LENGTH_SHORT).show();
        }
    }


    private void notifyItemChangedById(String courseId) {
        for (int i = 0; i < courseList.size(); i++) {
            if (courseList.get(i).getId().equals(courseId)) {
                notifyItemChanged(i);  // Notify RecyclerView that the item has changed
                break;
            }
        }
    }

    // Cập nhật khóa học trong Firebase
    private void updateCourseInFirebase(YogaCourse course) {
        if (course.getFirebaseKey() != null) {
            classRef.child(course.getFirebaseKey()).setValue(course).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Course updated successfully in Firebase", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to update course in Firebase", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Hiển thị xác nhận xóa
    private void showDeleteConfirmation(YogaCourse course) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Course")
                .setMessage("Are you sure you want to delete this course?")
                .setPositiveButton("Delete", (dialog, which) -> deleteCourse(course))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create().show();
    }

    // Xóa khóa học
    private void deleteCourse(YogaCourse course) {
        courseDbHelper.deleteYogaCourse(course.getId());
        removeCourseById(course.getId());
        Toast.makeText(context, "Course deleted from SQLite", Toast.LENGTH_SHORT).show();

        if (course.getFirebaseKey() != null) {
            classRef.child(course.getFirebaseKey()).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Course deleted from Firebase", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to delete course from Firebase", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    // Xóa khóa học khỏi danh sách và RecyclerView
    private void removeCourseById(String courseId) {
        for (int i = 0; i < courseList.size(); i++) {
            if (courseList.get(i).getId().equals(courseId)) {
                courseList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    // Mở chi tiết khóa học
    private void viewCourseDetail(YogaCourse course) {
        Intent intent = new Intent(context, CourseDetailActivity.class);
        intent.putExtra("course", course);
        context.startActivity(intent);
    }

    // ViewHolder cho mỗi khóa học
    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvClassDetails;
        ImageView imgEdit, imgDelete, imgDetail;

        public CourseViewHolder(View itemView) {
            super(itemView);
            tvClassDetails = itemView.findViewById(R.id.tvClassDetails);
            imgEdit = itemView.findViewById(R.id.imgEdit);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            imgDetail = itemView.findViewById(R.id.imgDetail);
        }
    }
}

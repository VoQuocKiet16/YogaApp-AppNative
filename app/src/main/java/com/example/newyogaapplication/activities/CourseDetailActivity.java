package com.example.newyogaapplication.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newyogaapplication.R;
import com.example.newyogaapplication.classes.YogaClass;
import com.example.newyogaapplication.adapters.YogaClassAdapter;
import com.example.newyogaapplication.database.YogaClassDB;
import com.example.newyogaapplication.classes.YogaCourse;

import java.util.List;

public class CourseDetailActivity extends AppCompatActivity {

    private YogaClassDB classDbHelper;
    private YogaCourse course;
    private RecyclerView recyclerViewClasses;
    private YogaClassAdapter classAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        course = (YogaCourse) getIntent().getSerializableExtra("course");

        TextView tvCourseName = findViewById(R.id.tvCourseName);
        TextView tvDayOfWeek = findViewById(R.id.tvDayOfWeek);
        TextView tvTime = findViewById(R.id.tvTime);
        TextView tvCapacity = findViewById(R.id.tvCapacity);
        TextView tvDuration = findViewById(R.id.tvDuration);
        TextView tvPricePerClass = findViewById(R.id.tvPricePerClass);
        TextView tvDescription = findViewById(R.id.tvDescription);
        recyclerViewClasses = findViewById(R.id.recyclerViewClasses);

        tvCourseName.setText(course.getNameCourse());
        tvDayOfWeek.setText(course.getDayOfWeek());
        tvTime.setText(course.getTime());
        tvCapacity.setText(String.valueOf(course.getCapacity()));
        tvDuration.setText(course.getDuration());
        tvPricePerClass.setText(String.valueOf(course.getPricePerClass()));
        tvDescription.setText(course.getDescription());

        classDbHelper = new YogaClassDB(this);
        List<YogaClass> classList = classDbHelper.getClassesByCourseId(course.getId());

        classAdapter = new YogaClassAdapter(this, classList, null, classDbHelper);
        recyclerViewClasses.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewClasses.setAdapter(classAdapter);
    }
}

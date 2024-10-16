package com.example.newyogaapplication.classes;

public class CourseItem {
    private String courseId;
    private String courseName;

    public CourseItem(String courseId, String courseName) {
        this.courseId = courseId;
        this.courseName = courseName;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    @Override
    public String toString() {
        return courseName; // Để hiển thị tên khóa học trong Spinner
    }
}


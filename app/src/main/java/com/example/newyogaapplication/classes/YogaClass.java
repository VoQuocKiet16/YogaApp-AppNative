package com.example.newyogaapplication.classes;


public class YogaClass {
    private String id;          // ID of the class
    private String courseId;    // ID of the course
    private String date;        // Class date (yyyy-MM-dd)
    private String teacher;     // Teacher's name
    private String firebaseKey; // Firebase key

    public YogaClass() {
        // Default constructor for Firebase
    }

    public YogaClass(String id, String courseId, String date, String teacher, String firebaseKey) {
        this.id = id;
        this.courseId = courseId;
        this.date = date;
        this.teacher = teacher;
        this.firebaseKey = firebaseKey;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }

    public String getFirebaseKey() { return firebaseKey; }
    public void setFirebaseKey(String firebaseKey) { this.firebaseKey = firebaseKey; }
}


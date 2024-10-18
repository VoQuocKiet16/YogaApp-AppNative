package com.example.newyogaapplication.classes;

import java.io.Serializable;

public class YogaCourse implements Serializable {
    private String id; // ID of the course
    private String firebaseKey; // Firebase key
    private String dayOfWeek; // Day of the week
    private String time; // Course time
    private int capacity; // Capacity of attendees
    private String duration; // Duration of the course
    private double pricePerClass; // Price per class
    private String classType; // Type of class
    private String description; // Description (optional)
    private String nameCourse; // Name of the course
    private boolean isSynced; // Sync status

    // Constructor with isSynced
    public YogaCourse(String id, String firebaseKey, String dayOfWeek, String time,
                      int capacity, String duration, double pricePerClass,
                      String classType, String description, String nameCourse, boolean isSynced) {
        this.id = id;
        this.firebaseKey = firebaseKey;
        this.dayOfWeek = dayOfWeek;
        this.time = time;
        this.capacity = capacity;
        this.duration = duration;
        this.pricePerClass = pricePerClass;
        this.classType = classType;
        this.description = description;
        this.nameCourse = nameCourse;
        this.isSynced = isSynced;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public double getPricePerClass() {
        return pricePerClass;
    }

    public void setPricePerClass(double pricePerClass) {
        this.pricePerClass = pricePerClass;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNameCourse() {
        return nameCourse;
    }

    public void setNameCourse(String nameCourse) {
        this.nameCourse = nameCourse;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

    // No-argument constructor (required for Firebase)
    public YogaCourse() {
        // Default constructor required for Firebase
    }

    @Override
    public String toString() {
        return "YogaCourse{" +
                "id='" + id + '\'' +
                ", firebaseKey='" + firebaseKey + '\'' +
                ", dayOfWeek='" + dayOfWeek + '\'' +
                ", time='" + time + '\'' +
                ", capacity=" + capacity +
                ", duration='" + duration + '\'' +
                ", pricePerClass=" + pricePerClass +
                ", classType='" + classType + '\'' +
                ", description='" + description + '\'' +
                ", nameCourse='" + nameCourse + '\'' +
                ", isSynced=" + isSynced +
                '}';
    }
}

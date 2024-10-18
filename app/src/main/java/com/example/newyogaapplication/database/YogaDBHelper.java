package com.example.newyogaapplication.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class YogaDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "YogaApp.db";
    private static final int DATABASE_VERSION = 2;  // Tăng phiên bản cơ sở dữ liệu

    // Tables for Yoga Classes and Yoga Courses
    public static final String TABLE_YOGA_COURSES = "YogaCourses";
    public static final String TABLE_YOGA_CLASSES = "YogaClasses";
    public static final String TABLE_YOGA_USERS = "YogaUsers";

    // Common columns
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FIREBASE_KEY = "firebaseKey";
    public static final String COLUMN_IS_SYNCED = "isSynced";  // Trường isSynced cho trạng thái đồng bộ

    // Course-specific columns
    public static final String COLUMN_DAY_OF_WEEK = "dayOfWeek";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_CAPACITY = "capacity";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_PRICE_PER_CLASS = "pricePerClass";
    public static final String COLUMN_CLASS_TYPE = "classType";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_NAME_COURSE = "nameCourse";

    // Class-specific columns
    public static final String COLUMN_COURSE_ID = "courseId";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TEACHER = "teacher";

    // User-specific columns
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_USER_ID = "userId";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_ROLE = "role"; // New role column

    public YogaDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Yoga Courses Table
        String CREATE_YOGA_COURSES_TABLE = "CREATE TABLE " + TABLE_YOGA_COURSES + "("
                + COLUMN_ID + " TEXT PRIMARY KEY,"
                + COLUMN_FIREBASE_KEY + " TEXT,"
                + COLUMN_DAY_OF_WEEK + " TEXT,"
                + COLUMN_TIME + " TEXT,"
                + COLUMN_CAPACITY + " INTEGER,"
                + COLUMN_DURATION + " TEXT,"
                + COLUMN_PRICE_PER_CLASS + " REAL,"
                + COLUMN_CLASS_TYPE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_NAME_COURSE + " TEXT,"
                + COLUMN_IS_SYNCED + " INTEGER DEFAULT 0"  // Thêm trường isSynced
                + ")";
        db.execSQL(CREATE_YOGA_COURSES_TABLE);

        // Create Yoga Classes Table
        String CREATE_YOGA_CLASSES_TABLE = "CREATE TABLE " + TABLE_YOGA_CLASSES + "("
                + COLUMN_ID + " TEXT PRIMARY KEY,"
                + COLUMN_COURSE_ID + " TEXT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_TEACHER + " TEXT,"
                + COLUMN_FIREBASE_KEY + " TEXT,"
                + COLUMN_IS_SYNCED + " INTEGER DEFAULT 0"  // Thêm trường isSynced
                + ")";
        db.execSQL(CREATE_YOGA_CLASSES_TABLE);

        // Create Users Table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_YOGA_USERS + "("
                + COLUMN_USER_ID + " TEXT PRIMARY KEY,"  // Unique user ID
                + COLUMN_FIREBASE_KEY + " TEXT,"         // Firebase Key
                + COLUMN_EMAIL + " TEXT,"                // Email
                + COLUMN_PASSWORD + " TEXT,"             // Password
                + COLUMN_USERNAME + " TEXT,"             // Username
                + COLUMN_ROLE + " TEXT"                  // Role
                + ")";
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Thêm trường isSynced nếu cơ sở dữ liệu trước chưa có
            db.execSQL("ALTER TABLE " + TABLE_YOGA_COURSES + " ADD COLUMN " + COLUMN_IS_SYNCED + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_YOGA_CLASSES + " ADD COLUMN " + COLUMN_IS_SYNCED + " INTEGER DEFAULT 0");
        }
    }
}

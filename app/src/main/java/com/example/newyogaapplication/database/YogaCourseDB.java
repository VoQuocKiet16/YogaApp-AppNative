package com.example.newyogaapplication.database;

import static com.example.newyogaapplication.database.YogaDBHelper.COLUMN_NAME_COURSE;
import static com.example.newyogaapplication.database.YogaDBHelper.TABLE_YOGA_COURSES;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.newyogaapplication.classes.YogaCourse;

import java.util.ArrayList;
import java.util.List;

public class YogaCourseDB {
    private YogaDBHelper dbHelper;

    public YogaCourseDB(Context context) {
        dbHelper = new YogaDBHelper(context);
    }

    // Add a new Yoga Course
    public long addYogaCourse(YogaCourse yogaCourse) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(YogaDBHelper.COLUMN_ID, yogaCourse.getId());
        values.put(YogaDBHelper.COLUMN_FIREBASE_KEY, yogaCourse.getFirebaseKey());
        values.put(YogaDBHelper.COLUMN_DAY_OF_WEEK, yogaCourse.getDayOfWeek());
        values.put(YogaDBHelper.COLUMN_TIME, yogaCourse.getTime());
        values.put(YogaDBHelper.COLUMN_CAPACITY, yogaCourse.getCapacity());
        values.put(YogaDBHelper.COLUMN_DURATION, yogaCourse.getDuration());
        values.put(YogaDBHelper.COLUMN_PRICE_PER_CLASS, yogaCourse.getPricePerClass());
        values.put(YogaDBHelper.COLUMN_CLASS_TYPE, yogaCourse.getClassType());
        values.put(YogaDBHelper.COLUMN_DESCRIPTION, yogaCourse.getDescription());
        values.put(COLUMN_NAME_COURSE, yogaCourse.getNameCourse());

        long courseId = db.insert(TABLE_YOGA_COURSES, null, values);
        db.close();
        return courseId;
    }

    // Update a Yoga Course
    public int updateYogaCourse(YogaCourse yogaCourse) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(YogaDBHelper.COLUMN_FIREBASE_KEY, yogaCourse.getFirebaseKey());
        values.put(YogaDBHelper.COLUMN_DAY_OF_WEEK, yogaCourse.getDayOfWeek());
        values.put(YogaDBHelper.COLUMN_TIME, yogaCourse.getTime());
        values.put(YogaDBHelper.COLUMN_CAPACITY, yogaCourse.getCapacity());
        values.put(YogaDBHelper.COLUMN_DURATION, yogaCourse.getDuration());
        values.put(YogaDBHelper.COLUMN_PRICE_PER_CLASS, yogaCourse.getPricePerClass());
        values.put(YogaDBHelper.COLUMN_CLASS_TYPE, yogaCourse.getClassType());
        values.put(YogaDBHelper.COLUMN_DESCRIPTION, yogaCourse.getDescription());
        values.put(COLUMN_NAME_COURSE, yogaCourse.getNameCourse());

        return db.update(TABLE_YOGA_COURSES, values, YogaDBHelper.COLUMN_ID + " = ?", new String[]{yogaCourse.getId()});
    }

    // Delete a Yoga Course
    public void deleteYogaCourse(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_YOGA_COURSES, YogaDBHelper.COLUMN_ID + " = ?", new String[]{id});
        db.close();
    }

    // Get a Yoga Course by ID
    public YogaCourse getYogaCourseById(String courseId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_YOGA_COURSES, null, YogaDBHelper.COLUMN_ID + "=?", new String[]{courseId}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            YogaCourse yogaCourse = new YogaCourse(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(4),
                    cursor.getString(5),
                    cursor.getDouble(6),
                    cursor.getString(7),
                    cursor.getString(8),
                    cursor.getString(9)
            );
            cursor.close();
            return yogaCourse;
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    // Get all Yoga Courses
    public List<YogaCourse> getAllYogaCourses() {
        List<YogaCourse> yogaCourseList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_YOGA_COURSES;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                YogaCourse yogaCourse = new YogaCourse(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getInt(4),
                        cursor.getString(5),
                        cursor.getDouble(6),
                        cursor.getString(7),
                        cursor.getString(8),
                        cursor.getString(9)
                );
                yogaCourseList.add(yogaCourse);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        return yogaCourseList;
    }

    // Get all Course Names
    public List<String> getAllCourseNames() {
        List<String> courseNames = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_NAME_COURSE + " FROM " + TABLE_YOGA_COURSES, null);

        if (cursor.moveToFirst()) {
            do {
                courseNames.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        return courseNames;
    }

    public YogaCourse getYogaCourseByName(String selectedCourseName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        YogaCourse course = null;

        Cursor cursor = db.query(
                TABLE_YOGA_COURSES,
                null,
                COLUMN_NAME_COURSE + " = ?",
                new String[]{selectedCourseName},
                null, // groupBy
                null, // having
                null, // orderBy
                null // limit
        );

        if (cursor != null && cursor.moveToFirst()) {
            course = new YogaCourse(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(4),
                    cursor.getString(5),
                    cursor.getDouble(6),
                    cursor.getString(7),
                    cursor.getString(8),
                    cursor.getString(9)
            );
        }
        if (cursor != null) {
            cursor.close();
        }
        return course;
    }


}

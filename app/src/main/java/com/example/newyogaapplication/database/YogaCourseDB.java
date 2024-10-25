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
        values.put(YogaDBHelper.COLUMN_IS_SYNCED, yogaCourse.isSynced() ? 1 : 0);  // Sync status

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
        values.put(YogaDBHelper.COLUMN_IS_SYNCED, yogaCourse.isSynced() ? 1 : 0);  // Sync status

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
                    cursor.getString(9),
                    cursor.getInt(10) == 1  // isSynced
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
                        cursor.getString(9),
                        cursor.getInt(10) == 1  // isSynced
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
                    cursor.getString(9),
                    cursor.getInt(10) == 1  // isSynced
            );
        }
        if (cursor != null) {
            cursor.close();
        }
        return course;
    }

    // Get all unsynced Yoga Courses (isSynced = false)
    public List<YogaCourse> getUnsyncedYogaCourses() {
        List<YogaCourse> unsyncedCourses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Truy vấn để lấy các khóa học chưa được đồng bộ
        Cursor cursor = db.query(YogaDBHelper.TABLE_YOGA_COURSES, null, YogaDBHelper.COLUMN_IS_SYNCED + " = ?", new String[]{"0"}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                YogaCourse course = new YogaCourse(
                        cursor.getString(0),  // id
                        cursor.getString(1),  // firebaseKey
                        cursor.getString(2),  // dayOfWeek
                        cursor.getString(3),  // time
                        cursor.getInt(4),     // capacity
                        cursor.getString(5),  // duration
                        cursor.getDouble(6),  // pricePerClass
                        cursor.getString(7),  // classType
                        cursor.getString(8),  // description
                        cursor.getString(9),  // nameCourse
                        cursor.getInt(10) == 1  // isSynced
                );
                unsyncedCourses.add(course);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        return unsyncedCourses;
    }

    public void deleteAllYogaCourses() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_YOGA_COURSES, null, null);
        db.close();
    }
}

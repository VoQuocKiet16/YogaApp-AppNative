package com.example.newyogaapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.newyogaapplication.classes.YogaClass;

import java.util.ArrayList;
import java.util.List;

public class YogaClassDB {
    private YogaDBHelper dbHelper;

    public YogaClassDB(Context context) {
        dbHelper = new YogaDBHelper(context);
    }

    // Add a new Yoga Class
    public long addYogaClass(YogaClass yogaClass) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (yogaClass.getId() == null || yogaClass.getId().isEmpty()) {
            yogaClass.setId(generateNewId());
        }

        values.put(YogaDBHelper.COLUMN_ID, yogaClass.getId());
        values.put(YogaDBHelper.COLUMN_COURSE_ID, yogaClass.getCourseId());
        values.put(YogaDBHelper.COLUMN_DATE, yogaClass.getDate());
        values.put(YogaDBHelper.COLUMN_TEACHER, yogaClass.getTeacher());
        values.put(YogaDBHelper.COLUMN_FIREBASE_KEY, yogaClass.getFirebaseKey());
        values.put(YogaDBHelper.COLUMN_IS_SYNCED, yogaClass.isSynced() ? 1 : 0);  // Sync status

        long classId = db.insert(YogaDBHelper.TABLE_YOGA_CLASSES, null, values);
        db.close();
        return classId;
    }

    private String generateNewId() {
        return java.util.UUID.randomUUID().toString();
    }

    // Update an existing Yoga Class
    public int updateYogaClass(YogaClass yogaClass) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(YogaDBHelper.COLUMN_DATE, yogaClass.getDate());
        values.put(YogaDBHelper.COLUMN_TEACHER, yogaClass.getTeacher());
        values.put(YogaDBHelper.COLUMN_FIREBASE_KEY, yogaClass.getFirebaseKey());
        values.put(YogaDBHelper.COLUMN_COURSE_ID, yogaClass.getCourseId());
        values.put(YogaDBHelper.COLUMN_IS_SYNCED, yogaClass.isSynced() ? 1 : 0);  // Sync status

        return db.update(YogaDBHelper.TABLE_YOGA_CLASSES, values, YogaDBHelper.COLUMN_ID + " = ?", new String[]{yogaClass.getId()});
    }

    // Delete a Yoga Class
    public void deleteYogaClass(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(YogaDBHelper.TABLE_YOGA_CLASSES, YogaDBHelper.COLUMN_ID + " = ?", new String[]{id});
        db.close();
    }

    // Get a Yoga Class by ID
    public YogaClass getYogaClass(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(YogaDBHelper.TABLE_YOGA_CLASSES, null, YogaDBHelper.COLUMN_ID + "=?", new String[]{id}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            YogaClass yogaClass = new YogaClass(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getInt(5) == 1 // isSynced
            );
            cursor.close();
            return yogaClass;
        }

        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    // Get all Yoga Classes
    public List<YogaClass> getAllYogaClasses() {
        List<YogaClass> yogaClassList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + YogaDBHelper.TABLE_YOGA_CLASSES;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                YogaClass yogaClass = new YogaClass(
                        cursor.getString(0),  // id
                        cursor.getString(1),  // courseId
                        cursor.getString(2),  // date
                        cursor.getString(3),  // teacher
                        cursor.getString(4),  // firebaseKey
                        cursor.getInt(5) == 1 // isSynced
                );
                yogaClassList.add(yogaClass);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return yogaClassList;
    }

    // Get all Yoga Classes by Course ID
    public List<YogaClass> getClassesByCourseId(String courseId) {
        List<YogaClass> classList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(YogaDBHelper.TABLE_YOGA_CLASSES, null, YogaDBHelper.COLUMN_COURSE_ID + "=?", new String[]{courseId}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                YogaClass yogaClass = new YogaClass(
                        cursor.getString(0),  // id
                        cursor.getString(1),  // courseId
                        cursor.getString(2),  // date
                        cursor.getString(3),  // teacher
                        cursor.getString(4),  // firebaseKey
                        cursor.getInt(5) == 1 // isSynced
                );
                classList.add(yogaClass);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return classList;
    }

    // Search for classes by courseId or teacher's name
    public List<YogaClass> searchClasses(String courseId, String teacherName) {
        List<YogaClass> classList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        StringBuilder query = new StringBuilder("SELECT * FROM " + YogaDBHelper.TABLE_YOGA_CLASSES + " WHERE 1=1");

        List<String> argsList = new ArrayList<>();
        if (courseId != null) {
            query.append(" AND " + YogaDBHelper.COLUMN_COURSE_ID + " = ?");
            argsList.add(courseId);
        }
        if (teacherName != null && !teacherName.isEmpty()) {
            query.append(" AND " + YogaDBHelper.COLUMN_TEACHER + " LIKE ?");
            argsList.add("%" + teacherName + "%");
        }

        Cursor cursor = db.rawQuery(query.toString(), argsList.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                YogaClass yogaClass = new YogaClass(
                        cursor.getString(0),  // id
                        cursor.getString(1),  // courseId
                        cursor.getString(2),  // date
                        cursor.getString(3),  // teacher
                        cursor.getString(4),  // firebaseKey
                        cursor.getInt(5) == 1 // isSynced
                );
                classList.add(yogaClass);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return classList;
    }

    // Get unsynced classes (for syncing to Firebase)
    public List<YogaClass> getUnsyncedClasses() {
        List<YogaClass> unsyncedClasses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(YogaDBHelper.TABLE_YOGA_CLASSES, null, YogaDBHelper.COLUMN_IS_SYNCED + "=0", null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                YogaClass yogaClass = new YogaClass(
                        cursor.getString(0),  // id
                        cursor.getString(1),  // courseId
                        cursor.getString(2),  // date
                        cursor.getString(3),  // teacher
                        cursor.getString(4),  // firebaseKey
                        cursor.getInt(5) == 1 // isSynced
                );
                unsyncedClasses.add(yogaClass);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return unsyncedClasses;
    }

    public List<YogaClass> getUnsyncedYogaClasses() {
        List<YogaClass> unsyncedClasses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query to select all classes where isSynced is false (0)
        String selectQuery = "SELECT * FROM " + YogaDBHelper.TABLE_YOGA_CLASSES + " WHERE " + YogaDBHelper.COLUMN_IS_SYNCED + " = 0";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                YogaClass yogaClass = new YogaClass(
                        cursor.getString(0),  // id
                        cursor.getString(1),  // courseId
                        cursor.getString(2),  // date
                        cursor.getString(3),  // teacher
                        cursor.getString(4),  // firebaseKey
                        cursor.getInt(5) == 1 // isSynced
                );
                unsyncedClasses.add(yogaClass);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        db.close();
        return unsyncedClasses;
    }

}

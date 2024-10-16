package com.example.newyogaapplication.database;

import static com.example.newyogaapplication.database.YogaDBHelper.COLUMN_EMAIL;
import static com.example.newyogaapplication.database.YogaDBHelper.COLUMN_FIREBASE_KEY;
import static com.example.newyogaapplication.database.YogaDBHelper.COLUMN_ID;
import static com.example.newyogaapplication.database.YogaDBHelper.COLUMN_PASSWORD;
import static com.example.newyogaapplication.database.YogaDBHelper.COLUMN_ROLE;
import static com.example.newyogaapplication.database.YogaDBHelper.COLUMN_USERNAME;
import static com.example.newyogaapplication.database.YogaDBHelper.COLUMN_USER_ID;
import static com.example.newyogaapplication.database.YogaDBHelper.TABLE_YOGA_USERS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.newyogaapplication.classes.Role;
import com.example.newyogaapplication.classes.YogaUser;

import java.util.ArrayList;
import java.util.List;

public class YogaUserDB {
    private YogaDBHelper dbHelper;

    public YogaUserDB(Context context) {
        dbHelper = new YogaDBHelper(context);
    }

    // Method to add a new user
    public long addUser(YogaUser user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, user.getUserId());
        values.put(COLUMN_FIREBASE_KEY, user.getFirebaseKey());
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_USERNAME, user.getUsername());
        values.put(COLUMN_ROLE, user.getRoleAsString());  // Storing role as a string

        long result = db.insert(TABLE_YOGA_USERS, null, values);
        db.close();
        return result;
    }

    // Method to validate user login credentials
    public YogaUser validateLogin(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_YOGA_USERS,
                null,
                COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{email, password},
                null, null, null
        );

        YogaUser user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new YogaUser(
                    cursor.getString(0),   // userId
                    cursor.getString(1),   // firebaseKey
                    cursor.getString(2),   // email
                    cursor.getString(3),   // password
                    cursor.getString(4),   // username
                    Role.fromString(cursor.getString(5))  // role
            );
            cursor.close();
        }
        return user;
    }

    // Method to fetch all users
    public List<YogaUser> getAllUsers() {
        List<YogaUser> userList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_YOGA_USERS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                YogaUser user = new YogaUser(
                        cursor.getString(0),  // userId
                        cursor.getString(1),  // firebaseKey
                        cursor.getString(2),  // email
                        cursor.getString(3),  // password
                        cursor.getString(4),  // username
                        Role.fromString(cursor.getString(5))  // role
                );
                userList.add(user);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return userList;
    }

    // Method to delete a user (optional)
    public void deleteUser(String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("YogaUsers", "id=?", new String[]{userId});
    }

    // Method to update user role (optional)
    public void updateUserRole(String userId, Role newRole) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ROLE, newRole.getRoleName());

        db.update(TABLE_YOGA_USERS, values, COLUMN_USER_ID + " = ?", new String[]{userId});
        db.close();
    }

    public YogaUser getUserByEmailAndPassword(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(TABLE_YOGA_USERS,
                null,
                COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?",
                new String[]{email, password},
                null,
                null,
                null);

        YogaUser user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new YogaUser(
                    cursor.getString(0),   // userId
                    cursor.getString(1),   // firebaseKey
                    cursor.getString(2),   // email
                    cursor.getString(3),   // password
                    cursor.getString(4),   // username
                    Role.fromString(cursor.getString(5))  // role
            );
            cursor.close();
        }

        return user;
    }

    public YogaUser getUserById(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_YOGA_USERS, null, COLUMN_USER_ID + " = ?", new String[]{userId}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            YogaUser user = new YogaUser(
                    cursor.getString(0),   // userId
                    cursor.getString(1),   // firebaseKey
                    cursor.getString(2),   // email
                    cursor.getString(3),   // password
                    cursor.getString(4),   // username
                    Role.fromString(cursor.getString(5))  // role
            );
            cursor.close();
            return user;
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }



    public int updateUser(YogaUser user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_USERNAME, user.getUsername());
        values.put(COLUMN_ROLE, user.getRole().toString()); // Lưu vai trò dưới dạng chuỗi

        // Sử dụng đúng tên cột cho ID, đảm bảo `COLUMN_ID` là tên chính xác
        return db.update(TABLE_YOGA_USERS, values, COLUMN_USER_ID  + " = ?", new String[]{user.getUserId()});
    }


    public List<YogaUser> getUsersByRole(Role role) {
        List<YogaUser> userList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                YogaDBHelper.TABLE_YOGA_USERS,
                null,
                YogaDBHelper.COLUMN_ROLE + "=?",
                new String[]{role.name()},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                YogaUser user = new YogaUser(
                        cursor.getString(0),   // userId
                        cursor.getString(1),   // firebaseKey
                        cursor.getString(2),   // email
                        cursor.getString(3),   // password
                        cursor.getString(4),   // username
                        Role.fromString(cursor.getString(5))  // Vai trò (role)
                );
                userList.add(user);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return userList;
    }



    public boolean isEmailExists(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + YogaDBHelper.TABLE_YOGA_USERS + " WHERE " + YogaDBHelper.COLUMN_EMAIL + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    // YogaUserDB.java
    public boolean isEmailExists(String email, String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                YogaDBHelper.TABLE_YOGA_USERS,
                null,
                YogaDBHelper.COLUMN_EMAIL + " = ? AND " + YogaDBHelper.COLUMN_USER_ID + " != ?",
                new String[]{email, userId},
                null,
                null,
                null
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }


    public YogaUser getUserByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query to fetch user details by email
        Cursor cursor = db.query(
                YogaDBHelper.TABLE_YOGA_USERS,  // Table name
                null,  // Columns (null will return all columns)
                YogaDBHelper.COLUMN_EMAIL + "=?",  // Where clause
                new String[]{email},  // Where arguments (email)
                null,  // Group by
                null,  // Having
                null   // Order by
        );

        YogaUser user = null;

        if (cursor != null && cursor.moveToFirst()) {
            // Construct the YogaUser object from the cursor
            user = new YogaUser(
                    cursor.getString(0),   // userId
                    cursor.getString(1),   // firebaseKey
                    cursor.getString(2),   // email
                    cursor.getString(3),   // password
                    cursor.getString(4),   // username
                    Role.fromString(cursor.getString(5))  // Vai trò (role)
            );
            cursor.close();
        }

        db.close();
        return user;  // Return the user object, or null if not found
    }

}

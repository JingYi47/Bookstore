package com.thanhvan.bookstoremanager.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.thanhvan.bookstoremanager.model.User;

public class UserDao {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;


    public UserDao(Context context) {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }


    public void close() {
        dbHelper.close();
    }


    public long registerUser(User user) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_USERNAME, user.getUsername());
        values.put(DatabaseHelper.COLUMN_USER_PASSWORD, user.getPassword());
        values.put(DatabaseHelper.COLUMN_USER_EMAIL, user.getEmail());

        long result = -1;
        try {

            result = db.insert(DatabaseHelper.TABLE_USERS, null, values);
            if (result != -1) {
                Log.d("UserDao", "User registered: " + user.getUsername());
            } else {
                Log.e("UserDao", "Failed to register user: " + user.getUsername() + ". Username might already exist.");
            }
        } catch (Exception e) {
            Log.e("UserDao", "Error registering user: " + e.getMessage());
        }
        return result;
    }


    public User checkLogin(String username, String password) {
        User user = null;
        Cursor cursor = null;
        try {

            String selection = DatabaseHelper.COLUMN_USER_USERNAME + " = ? AND " +
                    DatabaseHelper.COLUMN_USER_PASSWORD + " = ?";

            String[] selectionArgs = {username, password};


            cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    new String[]{
                            DatabaseHelper.COLUMN_USER_ID,
                            DatabaseHelper.COLUMN_USER_USERNAME,
                            DatabaseHelper.COLUMN_USER_PASSWORD,
                            DatabaseHelper.COLUMN_USER_EMAIL
                    },
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );


            if (cursor != null && cursor.moveToFirst()) {

                int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID);
                int usernameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_USERNAME);
                int passwordIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_PASSWORD);
                int emailIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_EMAIL);


                if (idIndex != -1 && usernameIndex != -1 && passwordIndex != -1 && emailIndex != -1) {

                    int id = cursor.getInt(idIndex);
                    String dbUsername = cursor.getString(usernameIndex);
                    String dbPassword = cursor.getString(passwordIndex);
                    String dbEmail = cursor.getString(emailIndex);


                    user = new User(id, dbUsername, dbPassword, dbEmail);
                    Log.d("UserDao", "Login successful for user: " + username);
                } else {
                    Log.e("UserDao", "One or more columns not found in cursor.");
                }
            } else {
                Log.d("UserDao", "Login failed for user: " + username);
            }
        } catch (Exception e) {
            Log.e("UserDao", "Error during login check: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return user;
    }


    public boolean isUsernameExists(String username) {
        Cursor cursor = null;
        try {
            String selection = DatabaseHelper.COLUMN_USER_USERNAME + " = ?";
            String[] selectionArgs = {username};

            cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    new String[]{DatabaseHelper.COLUMN_USER_ID},
                    selection,
                    selectionArgs,
                    null, null, null
            );

            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("UserDao", "Error checking username existence: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
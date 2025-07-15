package com.thanhvan.bookstoremanager.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.thanhvan.bookstoremanager.model.User;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public UserDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        if (db == null || !db.isOpen()) {
            try {
                db = dbHelper.getWritableDatabase();
                Log.d("UserDao", "Database opened for writing.");
            } catch (Exception e) {
                Log.e("UserDao", "Error opening database for writing: " + e.getMessage());
            }
        }
    }

    public void close() {
        if (db != null && db.isOpen()) {
            try {
                db.close();
                Log.d("UserDao", "Database closed.");
            } catch (Exception e) {
                Log.e("UserDao", "Error closing database: " + e.getMessage());
            }
        }
    }

    public long registerUser(User user) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_USERNAME, user.getUsername());
        values.put(DatabaseHelper.COLUMN_USER_PASSWORD, user.getPassword());
        values.put(DatabaseHelper.COLUMN_USER_EMAIL, user.getEmail());
        values.put(DatabaseHelper.COLUMN_USER_ROLE, user.getRole());

        long result = -1;
        try {
            result = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        } catch (Exception e) {
            Log.e("UserDao", "Error registering user: " + e.getMessage());
        }
        return result;
    }

    public User checkLogin(String identifier, String password) {
        open();
        User user = null;
        Cursor cursor = null;
        try {
            String selection = "(" + DatabaseHelper.COLUMN_USER_USERNAME + " = ? OR " +
                    DatabaseHelper.COLUMN_USER_EMAIL + " = ?) AND " +
                    DatabaseHelper.COLUMN_USER_PASSWORD + " = ?";
            String[] selectionArgs = {identifier, identifier, password};

            cursor = db.query(DatabaseHelper.TABLE_USERS, null, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID));
                String dbUsername = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_USERNAME));
                String dbPassword = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PASSWORD));
                String dbEmail = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL));
                String dbRole = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ROLE));
                user = new User(id, dbUsername, dbPassword, dbEmail, dbRole);
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
        open();
        Cursor cursor = null;
        try {
            String selection = DatabaseHelper.COLUMN_USER_USERNAME + " = ?";
            String[] selectionArgs = {username};
            cursor = db.query(DatabaseHelper.TABLE_USERS, new String[]{DatabaseHelper.COLUMN_USER_ID}, selection, selectionArgs, null, null, null);
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

    public User getUserByUsername(String username) {
        open();
        Cursor cursor = null;
        User user = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                    DatabaseHelper.COLUMN_USER_USERNAME + "=?",
                    new String[]{username}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID));
                String user_name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_USERNAME));
                String password = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PASSWORD));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL));
                String role = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ROLE));
                user = new User(id, user_name, password, email, role);
            }
        } catch (Exception e) {
            Log.e("UserDao", "Error getting user by username: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return user;
    }

    public boolean updateUserPassword(String username, String newPassword) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_PASSWORD, newPassword);
        int rowsAffected = 0;
        try {
            rowsAffected = db.update(DatabaseHelper.TABLE_USERS, values,
                    DatabaseHelper.COLUMN_USER_USERNAME + " = ?", new String[]{username});
        } catch (Exception e) {
            Log.e("UserDao", "Error updating user password: " + e.getMessage());
        }
        return rowsAffected > 0;
    }
}

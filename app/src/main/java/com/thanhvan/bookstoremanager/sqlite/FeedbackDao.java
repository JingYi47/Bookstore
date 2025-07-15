package com.thanhvan.bookstoremanager.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.thanhvan.bookstoremanager.model.Feedback;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDao {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public FeedbackDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        if (db == null || !db.isOpen()) {
            try {
                db = dbHelper.getWritableDatabase();
                Log.d("FeedbackDao", "Database opened for writing.");
            } catch (Exception e) {
                Log.e("FeedbackDao", "Error opening database for writing: " + e.getMessage());
            }
        }
    }

    public void close() {
        if (db != null && db.isOpen()) {
            try {
                db.close();
                Log.d("FeedbackDao", "Database closed.");
            } catch (Exception e) {
                Log.e("FeedbackDao", "Error closing database: " + e.getMessage());
            }
        }
    }

    public long addFeedback(Feedback feedback) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_FEEDBACK_NAME, feedback.getName());
        values.put(DatabaseHelper.COLUMN_FEEDBACK_PHONE, feedback.getPhone());
        values.put(DatabaseHelper.COLUMN_FEEDBACK_EMAIL, feedback.getEmail());
        values.put(DatabaseHelper.COLUMN_FEEDBACK_CONTENT, feedback.getContent());
        values.put(DatabaseHelper.COLUMN_FEEDBACK_TIMESTAMP, feedback.getTimestamp());

        long result = -1;
        try {
            result = db.insert(DatabaseHelper.TABLE_FEEDBACKS, null, values);
        } catch (Exception e) {
            Log.e("FeedbackDao", "Error adding feedback: " + e.getMessage());
        }
        return result;
    }

    public List<Feedback> getAllFeedbacks() {
        open();
        List<Feedback> feedbackList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_FEEDBACKS,
                    null, null, null, null, null, DatabaseHelper.COLUMN_FEEDBACK_TIMESTAMP + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FEEDBACK_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FEEDBACK_NAME));
                    String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FEEDBACK_PHONE));
                    String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FEEDBACK_EMAIL));
                    String content = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FEEDBACK_CONTENT));
                    long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FEEDBACK_TIMESTAMP));

                    Feedback feedback = new Feedback(id, name, phone, email, content, timestamp);
                    feedbackList.add(feedback);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("FeedbackDao", "Error getting all feedbacks: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return feedbackList;
    }
}

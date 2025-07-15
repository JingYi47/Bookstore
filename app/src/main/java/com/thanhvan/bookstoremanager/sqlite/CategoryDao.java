package com.thanhvan.bookstoremanager.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.thanhvan.bookstoremanager.model.Category;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public CategoryDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        if (db == null || !db.isOpen()) {
            try {
                db = dbHelper.getWritableDatabase();
                Log.d("CategoryDao", "Database opened for writing.");
            } catch (Exception e) {
                Log.e("CategoryDao", "Error opening database for writing: " + e.getMessage());
            }
        }
    }

    public void close() {
        if (db != null && db.isOpen()) {
            try {
                db.close();
                Log.d("CategoryDao", "Database closed.");
            } catch (Exception e) {
                Log.e("CategoryDao", "Error closing database: " + e.getMessage());
            }
        }
    }

    public long addCategory(Category category) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CATEGORY_NAME, category.getName());
        long result = -1;
        try {
            result = db.insert(DatabaseHelper.TABLE_CATEGORIES, null, values);
        } catch (Exception e) {
            Log.e("CategoryDao", "Error adding category: " + e.getMessage());
        }
        return result;
    }

    public int updateCategory(Category category) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CATEGORY_NAME, category.getName());
        int rowsAffected = 0;
        try {
            rowsAffected = db.update(DatabaseHelper.TABLE_CATEGORIES, values,
                    DatabaseHelper.COLUMN_CATEGORY_ID + " = ?",
                    new String[]{String.valueOf(category.getId())});
        } catch (Exception e) {
            Log.e("CategoryDao", "Error updating category: " + e.getMessage());
        }
        return rowsAffected;
    }

    public int deleteCategory(int categoryId) {
        open();
        int rowsAffected = 0;
        try {
            rowsAffected = db.delete(DatabaseHelper.TABLE_CATEGORIES,
                    DatabaseHelper.COLUMN_CATEGORY_ID + " = ?",
                    new String[]{String.valueOf(categoryId)});
        } catch (Exception e) {
            Log.e("CategoryDao", "Error deleting category: " + e.getMessage());
        }
        return rowsAffected;
    }

    public List<Category> getAllCategories() {
        open();
        List<Category> categories = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_CATEGORIES,
                    null, null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    categories.add(cursorToCategory(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("CategoryDao", "Error getting all categories: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return categories;
    }

    public Category getCategoryById(int id) {
        open();
        Cursor cursor = null;
        Category category = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_CATEGORIES,
                    null, DatabaseHelper.COLUMN_CATEGORY_ID + " = ?",
                    new String[]{String.valueOf(id)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                category = cursorToCategory(cursor);
            }
        } catch (Exception e) {
            Log.e("CategoryDao", "Error getting category by ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return category;
    }

    public List<Category> searchCategories(String query) {
        open();
        List<Category> categories = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_CATEGORIES,
                    null, DatabaseHelper.COLUMN_CATEGORY_NAME + " LIKE ?",
                    new String[]{"%" + query + "%"}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    categories.add(cursorToCategory(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("CategoryDao", "Error searching categories: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return categories;
    }

    public long getCategoryIdByName(String categoryName) {
        open();
        Cursor cursor = null;
        long categoryId = -1;
        try {
            cursor = db.query(DatabaseHelper.TABLE_CATEGORIES,
                    new String[]{DatabaseHelper.COLUMN_CATEGORY_ID},
                    DatabaseHelper.COLUMN_CATEGORY_NAME + " = ?",
                    new String[]{categoryName},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                categoryId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID));
            }
        } catch (Exception e) {
            Log.e("CategoryDao", "Error getting category ID by name: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return categoryId;
    }

    private Category cursorToCategory(Cursor cursor) {
        Category category = new Category();
        int idIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_ID);
        int nameIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_NAME);

        category.setId(cursor.getInt(idIndex));
        category.setName(cursor.getString(nameIndex));
        return category;
    }
}

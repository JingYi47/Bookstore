package com.thanhvan.bookstoremanager.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.thanhvan.bookstoremanager.model.Discount;
import java.util.ArrayList;
import java.util.List;

public class DiscountDao {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public DiscountDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        if (db == null || !db.isOpen()) {
            db = dbHelper.getWritableDatabase();
            Log.d("DiscountDao", "Database opened for writing.");
        }
    }

    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
            Log.d("DiscountDao", "Database closed.");
        }
    }

    public long addDiscount(Discount discount) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_DISCOUNT_NAME, discount.getName());
        values.put(DatabaseHelper.KEY_DISCOUNT_PERCENTAGE, discount.getDiscountPercentage());
        values.put(DatabaseHelper.KEY_MIN_ORDER_VALUE, discount.getMinOrderValue());
        values.put(DatabaseHelper.KEY_DESCRIPTION, discount.getDescription());
        values.put(DatabaseHelper.KEY_IS_ACTIVE, discount.isActive() ? 1 : 0);

        long result = -1;
        try {
            result = db.insert(DatabaseHelper.TABLE_DISCOUNTS, null, values);
        } catch (Exception e) {
            Log.e("DiscountDao", "Error adding discount: " + e.getMessage());
        }
        return result;
    }

    public List<Discount> getAllActiveDiscounts() {
        open();
        List<Discount> discountList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_DISCOUNTS + " WHERE " + DatabaseHelper.KEY_IS_ACTIVE + " = 1 ORDER BY " + DatabaseHelper.KEY_MIN_ORDER_VALUE + " DESC";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(selectQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_DISCOUNT_NAME));
                    double percentage = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_DISCOUNT_PERCENTAGE));
                    double minOrder = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MIN_ORDER_VALUE));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_DESCRIPTION));
                    boolean isActive = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_IS_ACTIVE)) == 1;

                    Discount discount = new Discount(id, name, percentage, minOrder, description, isActive);
                    discountList.add(discount);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DiscountDao", "Error getting all active discounts: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return discountList;
    }

    public Discount getDiscountById(int discountId) {
        open();
        Cursor cursor = null;
        Discount discount = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_DISCOUNTS,
                    new String[]{DatabaseHelper.KEY_ID, DatabaseHelper.KEY_DISCOUNT_NAME, DatabaseHelper.KEY_DISCOUNT_PERCENTAGE, DatabaseHelper.KEY_MIN_ORDER_VALUE, DatabaseHelper.KEY_DESCRIPTION, DatabaseHelper.KEY_IS_ACTIVE},
                    DatabaseHelper.KEY_ID + "=?",
                    new String[]{String.valueOf(discountId)},
                    null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_DISCOUNT_NAME));
                double percentage = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_DISCOUNT_PERCENTAGE));
                double minOrder = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MIN_ORDER_VALUE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_DESCRIPTION));
                boolean isActive = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_IS_ACTIVE)) == 1;

                discount = new Discount(id, name, percentage, minOrder, description, isActive);
            }
        } catch (Exception e) {
            Log.e("DiscountDao", "Error getting discount by ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return discount;
    }

    public int updateDiscount(Discount discount) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_DISCOUNT_NAME, discount.getName());
        values.put(DatabaseHelper.KEY_DISCOUNT_PERCENTAGE, discount.getDiscountPercentage());
        values.put(DatabaseHelper.KEY_MIN_ORDER_VALUE, discount.getMinOrderValue());
        values.put(DatabaseHelper.KEY_DESCRIPTION, discount.getDescription());
        values.put(DatabaseHelper.KEY_IS_ACTIVE, discount.isActive() ? 1 : 0);

        int rowsAffected = 0;
        try {
            rowsAffected = db.update(DatabaseHelper.TABLE_DISCOUNTS, values, DatabaseHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(discount.getId())});
        } catch (Exception e) {
            Log.e("DiscountDao", "Error updating discount: " + e.getMessage());
        }
        return rowsAffected;
    }

    public void deleteDiscount(int discountId) {
        open();
        try {
            db.delete(DatabaseHelper.TABLE_DISCOUNTS, DatabaseHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(discountId)});
        } catch (Exception e) {
            Log.e("DiscountDao", "Error deleting discount: " + e.getMessage());
        }
    }
}

package com.thanhvan.bookstoremanager.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.thanhvan.bookstoremanager.model.CartItem;
import java.util.ArrayList;
import java.util.List;

public class CartItemDao {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public CartItemDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        if (db == null || !db.isOpen()) {
            db = dbHelper.getWritableDatabase();
            Log.d("CartItemDao", "Database opened for writing.");
        }
    }

    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
            Log.d("CartItemDao", "Database closed.");
        }
    }

    public long addCartItem(CartItem item) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_BOOK_ID, item.getProductId());
        values.put(DatabaseHelper.COLUMN_CART_PRODUCT_TITLE, item.getProductTitle());
        values.put(DatabaseHelper.COLUMN_CART_PRODUCT_PRICE, item.getProductPrice());
        values.put(DatabaseHelper.COLUMN_CART_PRODUCT_QUANTITY, item.getProductQuantity());
        values.put(DatabaseHelper.COLUMN_CART_PRODUCT_IMAGE_URL, item.getImageUrl());
        values.put(DatabaseHelper.COLUMN_CART_PRODUCT_CATEGORY, item.getProductCategory());
        long result = -1;
        try {
            result = db.insert(DatabaseHelper.TABLE_CART_ITEMS, null, values);
        } catch (Exception e) {
            Log.e("CartItemDao", "Error inserting cart item: " + e.getMessage());
        }
        return result;
    }

    public List<CartItem> getAllCartItems() {
        open();
        List<CartItem> cartItems = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_CART_ITEMS, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String productId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOK_ID));
                    String productTitle = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CART_PRODUCT_TITLE));
                    double productPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CART_PRODUCT_PRICE));
                    int productQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CART_PRODUCT_QUANTITY));
                    String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CART_PRODUCT_IMAGE_URL));
                    String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CART_PRODUCT_CATEGORY));
                    CartItem item = new CartItem(productTitle, productPrice, productQuantity, imageUrl, category, productId);
                    cartItems.add(item);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("CartItemDao", "Error getting all cart items: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return cartItems;
    }

    public int updateCartItemQuantity(String productId, int newQuantity) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CART_PRODUCT_QUANTITY, newQuantity);
        int rowsAffected = 0;
        try {
            rowsAffected = db.update(DatabaseHelper.TABLE_CART_ITEMS, values,
                    DatabaseHelper.COLUMN_BOOK_ID + " = ?", new String[]{productId});
        } catch (Exception e) {
            Log.e("CartItemDao", "Error updating cart item quantity: " + e.getMessage());
        }
        return rowsAffected;
    }

    public int deleteCartItem(String productId) {
        open();
        int rowsAffected = 0;
        try {
            rowsAffected = db.delete(DatabaseHelper.TABLE_CART_ITEMS,
                    DatabaseHelper.COLUMN_BOOK_ID + " = ?", new String[]{productId});
        } catch (Exception e) {
            Log.e("CartItemDao", "Error deleting cart item: " + e.getMessage());
        }
        return rowsAffected;
    }

    public void clearCart() {
        open();
        try {
            db.delete(DatabaseHelper.TABLE_CART_ITEMS, null, null);
        } catch (Exception e) {
            Log.e("CartItemDao", "Error clearing cart: " + e.getMessage());
        }
    }

    public boolean isProductInCart(String productId) {
        open();
        Cursor cursor = null;
        boolean exists = false;
        try {
            cursor = db.query(DatabaseHelper.TABLE_CART_ITEMS,
                    new String[]{DatabaseHelper.COLUMN_BOOK_ID},
                    DatabaseHelper.COLUMN_BOOK_ID + " = ?",
                    new String[]{productId}, null, null, null);
            exists = cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("CartItemDao", "Error checking product in cart: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return exists;
    }

    public int getProductQuantity(String productId) {
        open();
        int quantity = 0;
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_CART_ITEMS,
                    new String[]{DatabaseHelper.COLUMN_CART_PRODUCT_QUANTITY},
                    DatabaseHelper.COLUMN_BOOK_ID + " = ?",
                    new String[]{productId}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int quantityIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CART_PRODUCT_QUANTITY);
                if (quantityIndex != -1) {
                    quantity = cursor.getInt(quantityIndex);
                }
            }
        } catch (Exception e) {
            Log.e("CartItemDao", "Error getting product quantity from cart: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return quantity;
    }
}

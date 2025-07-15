package com.thanhvan.bookstoremanager.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.thanhvan.bookstoremanager.model.Product;
import java.util.ArrayList;
import java.util.List;

public class BookDao {

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    private CategoryDao categoryDao;

    public BookDao(Context context) {
        dbHelper = new DatabaseHelper(context);
        categoryDao = new CategoryDao(context);
    }

    public void open() throws SQLException {
        if (db == null || !db.isOpen()) {
            try {
                db = dbHelper.getWritableDatabase();
                Log.d("BookDao", "Database opened for writing.");
            } catch (Exception e) {
                Log.e("BookDao", "Error opening database for writing: " + e.getMessage());
            }
        }
        categoryDao.open();
    }

    public void close() {
        if (db != null && db.isOpen()) {
            try {
                db.close();
                Log.d("BookDao", "Database closed.");
            } catch (Exception e) {
                Log.e("BookDao", "Error closing database: " + e.getMessage());
            }
        }
        categoryDao.close();
    }

    public long addProduct(Product product) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_BOOK_ID, product.getId());
        values.put(DatabaseHelper.COLUMN_BOOK_TITLE, product.getTitle());
        values.put(DatabaseHelper.COLUMN_BOOK_AUTHOR, product.getAuthor());
        values.put(DatabaseHelper.COLUMN_BOOK_CATEGORY, product.getCategory());

        long categoryId = categoryDao.getCategoryIdByName(product.getCategory());
        if (categoryId != -1) {
            values.put(DatabaseHelper.COLUMN_BOOK_CATEGORY_ID, categoryId);
        } else {
            values.put(DatabaseHelper.COLUMN_BOOK_CATEGORY_ID, (Integer) null);
            Log.e("BookDao", "Category not found for: " + product.getCategory() + ". Setting category_id to NULL.");
        }

        values.put(DatabaseHelper.COLUMN_BOOK_RATING, product.getRating());
        values.put(DatabaseHelper.COLUMN_BOOK_PRICE, product.getPrice());
        values.put(DatabaseHelper.COLUMN_BOOK_ORIGINAL_PRICE, product.getOriginalPrice());
        values.put(DatabaseHelper.COLUMN_BOOK_IMAGE_URL, product.getImageUrl());

        long result = -1;
        try {
            result = db.insert(DatabaseHelper.TABLE_BOOKS, null, values);
            if (result != -1) {
                Log.d("BookDao", "Book inserted: " + product.getTitle());
            } else {
                Log.e("BookDao", "Failed to insert book: " + product.getTitle() + ". ID might already exist.");
            }
        } catch (Exception e) {
            Log.e("BookDao", "Error inserting book: " + e.getMessage());
        }
        return result;
    }

    public int updateProduct(Product product) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_BOOK_TITLE, product.getTitle());
        values.put(DatabaseHelper.COLUMN_BOOK_AUTHOR, product.getAuthor());
        values.put(DatabaseHelper.COLUMN_BOOK_CATEGORY, product.getCategory());

        long categoryId = categoryDao.getCategoryIdByName(product.getCategory());
        if (categoryId != -1) {
            values.put(DatabaseHelper.COLUMN_BOOK_CATEGORY_ID, categoryId);
        } else {
            values.put(DatabaseHelper.COLUMN_BOOK_CATEGORY_ID, (Integer) null);
        }

        values.put(DatabaseHelper.COLUMN_BOOK_RATING, product.getRating());
        values.put(DatabaseHelper.COLUMN_BOOK_PRICE, product.getPrice());
        values.put(DatabaseHelper.COLUMN_BOOK_ORIGINAL_PRICE, product.getOriginalPrice());
        values.put(DatabaseHelper.COLUMN_BOOK_IMAGE_URL, product.getImageUrl());

        return db.update(DatabaseHelper.TABLE_BOOKS, values,
                DatabaseHelper.COLUMN_BOOK_ID + " = ?", new String[]{product.getId()});
    }

    public int deleteProduct(String productId) {
        open();
        return db.delete(DatabaseHelper.TABLE_BOOKS,
                DatabaseHelper.COLUMN_BOOK_ID + " = ?", new String[]{productId});
    }

    public List<Product> getAllProducts() {
        open();
        List<Product> productList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_BOOKS, null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    productList.add(cursorToProduct(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("BookDao", "Error getting all books: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return productList;
    }

    public Product getProductById(String id) {
        open();
        Cursor cursor = null;
        Product product = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_BOOKS, null,
                    DatabaseHelper.COLUMN_BOOK_ID + " = ?",
                    new String[]{id}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                product = cursorToProduct(cursor);
            }
        } catch (Exception e) {
            Log.e("BookDao", "Error getting book by ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return product;
    }

    public List<Product> searchProducts(String query) {
        open();
        List<Product> products = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_BOOKS, null,
                    DatabaseHelper.COLUMN_BOOK_TITLE + " LIKE ?",
                    new String[]{"%" + query + "%"}, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    products.add(cursorToProduct(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("BookDao", "Error searching books: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return products;
    }

    public boolean bookExists(String bookId) {
        open();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_BOOKS,
                    new String[]{DatabaseHelper.COLUMN_BOOK_ID},
                    DatabaseHelper.COLUMN_BOOK_ID + " = ?",
                    new String[]{bookId}, null, null, null, "1");
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("BookDao", "Lỗi khi kiểm tra sách tồn tại: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private Product cursorToProduct(Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOK_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOK_TITLE));
        String author = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOK_AUTHOR));
        String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOK_CATEGORY));
        double rating = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOK_RATING));
        double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOK_PRICE));
        double originalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOK_ORIGINAL_PRICE));
        String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BOOK_IMAGE_URL));
        return new Product(id, title, author, category, rating, price, originalPrice, imageUrl);
    }
}

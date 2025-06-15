package com.thanhvan.bookstoremanager.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.thanhvan.bookstoremanager.model.Product; // Đảm bảo dòng này đúng

import java.util.ArrayList;
import java.util.List;

public class BookDao {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public BookDao(Context context) {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * Thêm một sản phẩm (sách) mới vào cơ sở dữ liệu.
     * @param product Đối tượng Product chứa thông tin sách.
     * @return ID của hàng mới được thêm vào, hoặc -1 nếu có lỗi.
     */
    // --- CHÚ Ý ĐẶC BIỆT ĐẾN PHƯƠNG THỨC insertBook NÀY ---
    public long insertBook(Product product) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_BOOK_ID, product.getId());
        values.put(DatabaseHelper.COLUMN_BOOK_TITLE, product.getTitle());
        values.put(DatabaseHelper.COLUMN_BOOK_AUTHOR, product.getAuthor());
        values.put(DatabaseHelper.COLUMN_BOOK_CATEGORY, product.getCategory());
        values.put(DatabaseHelper.COLUMN_BOOK_RATING, product.getRating());
        values.put(DatabaseHelper.COLUMN_BOOK_PRICE, product.getPrice());
        values.put(DatabaseHelper.COLUMN_BOOK_ORIGINAL_PRICE, product.getOriginalPrice());
        values.put(DatabaseHelper.COLUMN_BOOK_IMAGE_RES_ID, product.getImageResId());

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
    // --- KẾT THÚC PHƯƠNG THỨC insertBook ---


    /**
     * Lấy tất cả các sản phẩm (sách) từ cơ sở dữ liệu.
     * @return Danh sách các đối tượng Product.
     */
    public List<Product> getAllBooks() {
        List<Product> productList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_BOOKS,
                    new String[]{
                            DatabaseHelper.COLUMN_BOOK_ID,
                            DatabaseHelper.COLUMN_BOOK_TITLE,
                            DatabaseHelper.COLUMN_BOOK_AUTHOR,
                            DatabaseHelper.COLUMN_BOOK_CATEGORY,
                            DatabaseHelper.COLUMN_BOOK_RATING,
                            DatabaseHelper.COLUMN_BOOK_PRICE,
                            DatabaseHelper.COLUMN_BOOK_ORIGINAL_PRICE,
                            DatabaseHelper.COLUMN_BOOK_IMAGE_RES_ID
                    },
                    null, null, null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BOOK_ID);
                    int titleIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BOOK_TITLE);
                    int authorIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BOOK_AUTHOR);
                    int categoryIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BOOK_CATEGORY);
                    int ratingIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BOOK_RATING);
                    int priceIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BOOK_PRICE);
                    int originalPriceIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BOOK_ORIGINAL_PRICE);
                    int imageResIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BOOK_IMAGE_RES_ID);

                    if (idIndex != -1 && titleIndex != -1 && authorIndex != -1 && categoryIndex != -1 &&
                            ratingIndex != -1 && priceIndex != -1 && originalPriceIndex != -1 && imageResIdIndex != -1) {

                        Product product = new Product(
                                cursor.getString(idIndex),
                                cursor.getString(titleIndex),
                                cursor.getString(authorIndex),
                                cursor.getString(categoryIndex),
                                cursor.getDouble(ratingIndex),
                                cursor.getDouble(priceIndex),
                                cursor.getDouble(originalPriceIndex),
                                cursor.getInt(imageResIdIndex)
                        );
                        productList.add(product);
                    } else {
                        Log.e("BookDao", "One or more columns not found in cursor for books.");
                    }
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
}
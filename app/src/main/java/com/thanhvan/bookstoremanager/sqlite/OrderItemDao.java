package com.thanhvan.bookstoremanager.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.thanhvan.bookstoremanager.model.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class OrderItemDao {
    private SQLiteDatabase db; // Biến này được quản lý bởi open/close của OrderItemDao
    private DatabaseHelper dbHelper;

    public OrderItemDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        if (db == null || !db.isOpen()) {
            try {
                db = dbHelper.getWritableDatabase();
                Log.d("OrderItemDao", "Database opened for writing.");
            } catch (Exception e) {
                Log.e("OrderItemDao", "Error opening database for writing: " + e.getMessage());
            }
        }
    }

    public void close() {
        if (db != null && db.isOpen()) {
            try {
                db.close();
                Log.d("OrderItemDao", "Database closed.");
            } catch (Exception e) {
                Log.e("OrderItemDao", "Error closing database: " + e.getMessage());
            }
        }
    }

    // Phương thức addOrderItem BAN ĐẦU của bạn
    public long addOrderItem(OrderItem item) {
        open(); // Giữ nguyên open() ở đây theo yêu cầu của bạn

        long result = -1;
        try {
            // >>> GỌI HÀM INSERT CHÍNH VÀ TRUYỀN DB NỘI BỘ VÀO <<<
            result = insertOrderItem(item, db);
            if (result != -1) {
                Log.d("OrderItemDao", "Order item inserted: " + item.getProductName() + " for Order ID: " + item.getOrderId());
            } else {
                Log.e("OrderItemDao", "Failed to insert order item: " + item.getProductName());
            }
        } catch (Exception e) {
            Log.e("OrderItemDao", "Error inserting order item: " + e.getMessage());
        } finally {
            close(); // Giữ nguyên close() ở đây
        }
        return result;
    }

    // >>> PHƯƠNG THỨC MỚI: DÙNG KHI DB ĐÃ ĐƯỢC MỞ TỪ BÊN NGOÀI (ví dụ: OrderDao trong transaction) <<<
    public long addOrderItem(OrderItem item, SQLiteDatabase externalDb) {
        if (externalDb == null || !externalDb.isOpen()) {
            Log.e("OrderItemDao", "External database is not open for adding order item.");
            return -1;
        }
        // >>> GỌI HÀM INSERT CHÍNH VÀ TRUYỀN DB BÊN NGOÀI VÀO <<<
        // Phương thức này KHÔNG tự open()/close()
        return insertOrderItem(item, externalDb);
    }

    // >>> HÀM CHUNG ĐỂ THỰC HIỆN VIỆC INSERT OrderItem <<<
    private long insertOrderItem(OrderItem item, SQLiteDatabase database) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_ORDER_ITEM_ORDER_ID, item.getOrderId());
        values.put(DatabaseHelper.COL_ORDER_ITEM_PRODUCT_ID, item.getProductId());
        values.put(DatabaseHelper.COL_ORDER_ITEM_PRODUCT_NAME, item.getProductName());
        values.put(DatabaseHelper.COL_ORDER_ITEM_PRODUCT_PRICE, item.getProductPrice());
        values.put(DatabaseHelper.COL_ORDER_ITEM_QUANTITY, item.getProductQuantity());
        values.put(DatabaseHelper.COL_ORDER_ITEM_PRODUCT_IMAGE_URL, item.getProductImageUrl());
        values.put(DatabaseHelper.COL_ORDER_ITEM_PRODUCT_CATEGORY, item.getProductCategory());

        long result = -1;
        try {
            result = database.insert(DatabaseHelper.TABLE_ORDER_ITEMS, null, values);
        } catch (Exception e) {
            Log.e("OrderItemDao", "Error inserting order item (internal): " + e.getMessage());
        }
        return result;
    }

    public List<OrderItem> getOrderItemsByOrderId(int orderId) {
        open(); // Giữ nguyên open() ở đây

        List<OrderItem> orderItems = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_ORDER_ITEMS,
                    null,
                    DatabaseHelper.COL_ORDER_ITEM_ORDER_ID + " = ?",
                    new String[]{String.valueOf(orderId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    orderItems.add(cursorToOrderItem(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("OrderItemDao", "Error getting order items by order ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close(); // Giữ nguyên close() ở đây
        }
        return orderItems;
    }

    private OrderItem cursorToOrderItem(Cursor cursor) {
        OrderItem item = new OrderItem();

        item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ITEM_ID)));
        item.setOrderId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ITEM_ORDER_ID)));
        item.setProductId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ITEM_PRODUCT_ID)));
        item.setProductName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ITEM_PRODUCT_NAME)));
        item.setProductPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ITEM_PRODUCT_PRICE)));
        item.setProductQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ITEM_QUANTITY)));
        item.setProductImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ITEM_PRODUCT_IMAGE_URL)));
        item.setProductCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ITEM_PRODUCT_CATEGORY)));

        return item;
    }
}
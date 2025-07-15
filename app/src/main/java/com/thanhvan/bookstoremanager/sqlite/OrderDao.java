package com.thanhvan.bookstoremanager.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.thanhvan.bookstoremanager.model.Order;
import com.thanhvan.bookstoremanager.model.OrderItem;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {
    private SQLiteDatabase db; // Biến này được quản lý bởi open/close của OrderDao
    private DatabaseHelper dbHelper;
    private OrderItemDao orderItemDao;

    public OrderDao(Context context) {
        dbHelper = new DatabaseHelper(context);
        orderItemDao = new OrderItemDao(context);
    }

    public void open() {
        if (db == null || !db.isOpen()) {
            try {
                db = dbHelper.getWritableDatabase();
                Log.d("OrderDao", "Database opened for writing.");
            } catch (Exception e) {
                Log.e("OrderDao", "Error opening database for writing: " + e.getMessage());
            }
        }
        // >>> ĐÃ SỬA: Bỏ dòng gọi orderItemDao.open() <<<
        // orderItemDao.open(); // Bỏ dòng này đi
    }

    public void close() {
        if (db != null && db.isOpen()) {
            try {
                db.close();
                Log.d("OrderDao", "Database closed.");
            } catch (Exception e) {
                Log.e("OrderDao", "Error closing database: " + e.getMessage());
            }
        }
        // >>> ĐÃ SỬA: Bỏ dòng gọi orderItemDao.close() <<<
        // orderItemDao.close(); // Bỏ dòng này đi
    }

    public long addOrder(Order order, List<OrderItem> orderItems) {
        open(); // Giữ nguyên open() ở đây theo yêu cầu của bạn
        long orderId = -1;
        db.beginTransaction(); // Bắt đầu transaction
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_ORDER_USER_EMAIL, order.getUserEmail());
            values.put(DatabaseHelper.COL_ORDER_CODE, order.getOrderCode());
            values.put(DatabaseHelper.COL_ORDER_TOTAL_AMOUNT, order.getTotalAmount());
            values.put(DatabaseHelper.COL_ORDER_STATUS, order.getStatus());
            values.put(DatabaseHelper.COL_ORDER_TOTAL_QUANTITY, order.getTotalQuantity());
            values.put(DatabaseHelper.COL_ORDER_DATE, order.getOrderDate());
            values.put(DatabaseHelper.COL_ORDER_SHIPPING_ADDRESS, order.getShippingAddress());

            orderId = db.insert(DatabaseHelper.TABLE_ORDERS, null, values);

            if (orderId != -1) {
                // Nếu thêm Order thành công, thêm các OrderItem
                for (OrderItem item : orderItems) {
                    item.setOrderId((int) orderId); // Gán orderId cho item
                    // >>> ĐÃ SỬA: GỌI PHƯƠNG THỨC MỚI VÀ TRUYỀN DB CỦA OrderDao VÀO <<<
                    long orderItemResult = orderItemDao.addOrderItem(item, db); // TRUYỀN db của OrderDao
                    if (orderItemResult == -1) {
                        // Nếu một item thất bại, hủy toàn bộ transaction
                        orderId = -1;
                        break;
                    }
                }
            }

            if (orderId != -1) {
                db.setTransactionSuccessful(); // Đánh dấu transaction thành công
            }

        } catch (Exception e) {
            Log.e("OrderDao", "Error adding order: " + e.getMessage());
            orderId = -1; // Đảm bảo trả về lỗi
        } finally {
            db.endTransaction(); // Kết thúc transaction (commit hoặc rollback)
            close(); // Giữ nguyên close() ở đây
        }
        return orderId;
    }

    public List<Order> getOrdersByStatus(String status) {
        open(); // Giữ nguyên open() ở đây
        List<Order> orders = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_ORDERS,
                    null,
                    DatabaseHelper.COL_ORDER_STATUS + " = ?",
                    new String[]{status},
                    null, null,
                    DatabaseHelper.COL_ORDER_DATE + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    orders.add(cursorToOrder(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("OrderDao", "Error getting orders by status: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close(); // Giữ nguyên close() ở đây
        }
        return orders;
    }

    private Order cursorToOrder(Cursor cursor) {
        Order order = new Order();
        order.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ID)));
        order.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_USER_EMAIL)));
        order.setOrderCode(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_CODE)));
        order.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_TOTAL_AMOUNT)));
        order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_STATUS)));
        order.setTotalQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_TOTAL_QUANTITY)));
        order.setOrderDate(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_DATE)));
        order.setShippingAddress(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_SHIPPING_ADDRESS)));
        return order;
    }
}
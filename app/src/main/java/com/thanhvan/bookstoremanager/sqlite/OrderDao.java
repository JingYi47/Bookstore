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
    private SQLiteDatabase db;
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

    }

    public long addOrder(Order order, List<OrderItem> orderItems) {
        open();
        long orderId = -1;
        db.beginTransaction();
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

                    long orderItemResult = orderItemDao.addOrderItem(item, db);
                    if (orderItemResult == -1) {

                        orderId = -1;
                        break;
                    }
                }
            }

            if (orderId != -1) {
                db.setTransactionSuccessful();
            }

        } catch (Exception e) {
            Log.e("OrderDao", "Error adding order: " + e.getMessage());
            orderId = -1;
        } finally {
            db.endTransaction();
            close();
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
            close();
        }
        return orders;
    }

    public List<Order> getOrdersByUserEmail(String userEmail) {
        open(); // Mở kết nối DB
        List<Order> orders = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_ORDERS,
                    null,
                    DatabaseHelper.COL_ORDER_USER_EMAIL + " = ?", // Lọc theo email người dùng
                    new String[]{userEmail},
                    null, null,
                    DatabaseHelper.COL_ORDER_DATE + " DESC"); // Sắp xếp theo ngày, mới nhất trước

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    orders.add(cursorToOrder(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("OrderDao", "Lỗi khi lấy đơn hàng theo email người dùng: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close(); // Đóng kết nối DB
        }
        return orders;
    }

    public List<Order> getOrdersByUserEmailAndStatus(String userEmail, String status) {
        open(); // Mở kết nối DB
        List<Order> orders = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_ORDERS,
                    null,
                    DatabaseHelper.COL_ORDER_USER_EMAIL + " = ? AND " + DatabaseHelper.COL_ORDER_STATUS + " = ?",
                    new String[]{userEmail, status},
                    null, null,
                    DatabaseHelper.COL_ORDER_DATE + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    orders.add(cursorToOrder(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("OrderDao", "Lỗi khi lấy đơn hàng theo email người dùng và trạng thái: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close(); // Đóng kết nối DB
        }
        return orders;
    }

    public Order getOrderById(int orderId) {
        open();
        Order order = null;
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_ORDERS,
                    null,
                    DatabaseHelper.COL_ORDER_ID + " = ?",
                    new String[]{String.valueOf(orderId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                order = cursorToOrder(cursor);
            }
        } catch (Exception e) {
            Log.e("OrderDao", "Lỗi khi lấy đơn hàng theo ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close();
        }
        return order;
    }

    public Order getOrderByOrderCode(String orderCode) {
        open();
        Order order = null;
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_ORDERS,
                    null,
                    DatabaseHelper.COL_ORDER_CODE + " = ?",
                    new String[]{orderCode},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                order = cursorToOrder(cursor);
            }
        } catch (Exception e) {
            Log.e("OrderDao", "Lỗi khi lấy đơn hàng theo mã code: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close();
        }
        return order;
    }

    // CẬP NHẬT TRẠNG THÁI ĐƠN HÀNG
    public boolean updateOrderStatus(int orderId, String newStatus) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_ORDER_STATUS, newStatus);
        int rowsAffected = 0;
        try {
            rowsAffected = db.update(DatabaseHelper.TABLE_ORDERS, values,
                    DatabaseHelper.COL_ORDER_ID + " = ?",
                    new String[]{String.valueOf(orderId)});
        } catch (Exception e) {
            Log.e("OrderDao", "Lỗi khi cập nhật trạng thái đơn hàng: " + e.getMessage());
        } finally {
            close();
        }
        return rowsAffected > 0;
    }

    public boolean updateOrderStatus(String orderCode, String newStatus) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_ORDER_STATUS, newStatus);
        int rowsAffected = 0;
        try {
            rowsAffected = db.update(DatabaseHelper.TABLE_ORDERS, values,
                    DatabaseHelper.COL_ORDER_CODE + " = ?",
                    new String[]{orderCode});
        } catch (Exception e) {
            Log.e("OrderDao", "Lỗi khi cập nhật trạng thái đơn hàng bằng code: " + e.getMessage());
        } finally {
            close();
        }
        return rowsAffected > 0;
    }


    public List<Order> getAllOrders() {
        open();
        List<Order> orders = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_ORDERS,
                    null,
                    null,
                    null,
                    null, null,
                    DatabaseHelper.COL_ORDER_DATE + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    orders.add(cursorToOrder(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("OrderDao", "Lỗi khi lấy tất cả đơn hàng: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close();
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
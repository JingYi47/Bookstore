package com.thanhvan.bookstoremanager.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "BookstoreManager.db";
    public static final int DATABASE_VERSION = 28;

    // Table Users
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_USERNAME = "username";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_ROLE = "role";
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_USERNAME + " TEXT NOT NULL UNIQUE,"
            + COLUMN_USER_PASSWORD + " TEXT NOT NULL,"
            + COLUMN_USER_EMAIL + " TEXT NOT NULL UNIQUE,"
            + COLUMN_USER_ROLE + " TEXT DEFAULT 'user'"
            + ");";

    // Table Categories
    public static final String TABLE_CATEGORIES = "Categories";
    public static final String COLUMN_CATEGORY_ID = "id";
    public static final String COLUMN_CATEGORY_NAME = "name";
    private static final String CREATE_TABLE_CATEGORIES = "CREATE TABLE "
            + TABLE_CATEGORIES + "("
            + COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_CATEGORY_NAME + " TEXT UNIQUE NOT NULL"
            + ");";

    // Table Books
    public static final String TABLE_BOOKS = "books";
    public static final String COLUMN_BOOK_ID = "id";
    public static final String COLUMN_BOOK_TITLE = "title";
    public static final String COLUMN_BOOK_AUTHOR = "author";
    public static final String COLUMN_BOOK_CATEGORY = "category";
    public static final String COLUMN_BOOK_CATEGORY_ID = "category_id";
    public static final String COLUMN_BOOK_RATING = "rating";
    public static final String COLUMN_BOOK_PRICE = "price";
    public static final String COLUMN_BOOK_ORIGINAL_PRICE = "original_price";
    public static final String COLUMN_BOOK_IMAGE_URL = "image_url";
    private static final String CREATE_TABLE_BOOKS = "CREATE TABLE " + TABLE_BOOKS + "("
            + COLUMN_BOOK_ID + " TEXT PRIMARY KEY,"
            + COLUMN_BOOK_TITLE + " TEXT,"
            + COLUMN_BOOK_AUTHOR + " TEXT,"
            + COLUMN_BOOK_CATEGORY + " TEXT,"
            + COLUMN_BOOK_CATEGORY_ID + " INTEGER,"
            + COLUMN_BOOK_RATING + " REAL,"
            + COLUMN_BOOK_PRICE + " REAL,"
            + COLUMN_BOOK_ORIGINAL_PRICE + " REAL,"
            + COLUMN_BOOK_IMAGE_URL + " TEXT,"
            + "FOREIGN KEY(" + COLUMN_BOOK_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_CATEGORY_ID + ") ON DELETE SET NULL"
            + ");";

    // Table Address
    public static final String TABLE_ADDRESS = "address";
    public static final String COLUMN_ADDRESS_ID = "id";
    public static final String COLUMN_ADDRESS_USER_EMAIL = "user_email";
    public static final String COLUMN_ADDRESS_NAME = "name";
    public static final String COLUMN_ADDRESS_PHONE = "phone";
    public static final String COLUMN_ADDRESS_FULL_ADDRESS = "full_address";
    public static final String COLUMN_ADDRESS_LATITUDE = "latitude";
    public static final String COLUMN_ADDRESS_LONGITUDE = "longitude";
    private static final String CREATE_TABLE_ADDRESS = "CREATE TABLE " + TABLE_ADDRESS + "("
            + COLUMN_ADDRESS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_ADDRESS_USER_EMAIL + " TEXT NOT NULL,"
            + COLUMN_ADDRESS_NAME + " TEXT NOT NULL,"
            + COLUMN_ADDRESS_PHONE + " TEXT NOT NULL,"
            + COLUMN_ADDRESS_FULL_ADDRESS + " TEXT NOT NULL,"
            + COLUMN_ADDRESS_LATITUDE + " REAL DEFAULT 0.0,"
            + COLUMN_ADDRESS_LONGITUDE + " REAL DEFAULT 0.0,"
            + "FOREIGN KEY(" + COLUMN_ADDRESS_USER_EMAIL + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_EMAIL + ") ON UPDATE CASCADE ON DELETE CASCADE"
            + ");";

    // Table Cart Items
    public static final String TABLE_CART_ITEMS = "cart_items";
    public static final String COLUMN_CART_PRODUCT_TITLE = "product_title";
    public static final String COLUMN_CART_PRODUCT_CATEGORY = "product_category";
    public static final String COLUMN_CART_PRODUCT_PRICE = "product_price";
    public static final String COLUMN_CART_PRODUCT_QUANTITY = "product_quantity";
    public static final String COLUMN_CART_PRODUCT_IMAGE_URL = "image_url";
    private static final String CREATE_TABLE_CART_ITEMS = "CREATE TABLE " + TABLE_CART_ITEMS + "("
            + COLUMN_BOOK_ID + " TEXT PRIMARY KEY,"
            + COLUMN_CART_PRODUCT_TITLE + " TEXT,"
            + COLUMN_CART_PRODUCT_CATEGORY + " TEXT,"
            + COLUMN_CART_PRODUCT_PRICE + " REAL,"
            + COLUMN_CART_PRODUCT_QUANTITY + " INTEGER,"
            + COLUMN_CART_PRODUCT_IMAGE_URL + " TEXT,"
            + "FOREIGN KEY(" + COLUMN_BOOK_ID + ") REFERENCES " + TABLE_BOOKS + "(" + COLUMN_BOOK_ID + ") ON DELETE CASCADE"
            + ");";

    // Table Discounts
    public static final String TABLE_DISCOUNTS = "Discounts";
    public static final String KEY_ID = "id";
    public static final String KEY_DISCOUNT_NAME = "name";
    public static final String KEY_DISCOUNT_PERCENTAGE = "discount_percentage";
    public static final String KEY_MIN_ORDER_VALUE = "min_order_value";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_IS_ACTIVE = "is_active";
    private static final String CREATE_TABLE_DISCOUNTS = "CREATE TABLE "
            + TABLE_DISCOUNTS + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_DISCOUNT_NAME + " TEXT,"
            + KEY_DISCOUNT_PERCENTAGE + " REAL,"
            + KEY_MIN_ORDER_VALUE + " REAL,"
            + KEY_DESCRIPTION + " TEXT,"
            + KEY_IS_ACTIVE + " INTEGER"
            + ");";

    // Table Feedbacks
    public static final String TABLE_FEEDBACKS = "Feedbacks";
    public static final String COLUMN_FEEDBACK_ID = "id";
    public static final String COLUMN_FEEDBACK_NAME = "name";
    public static final String COLUMN_FEEDBACK_PHONE = "phone";
    public static final String COLUMN_FEEDBACK_EMAIL = "email";
    public static final String COLUMN_FEEDBACK_CONTENT = "content";
    public static final String COLUMN_FEEDBACK_TIMESTAMP = "timestamp";
    private static final String CREATE_TABLE_FEEDBACKS = "CREATE TABLE "
            + TABLE_FEEDBACKS + "("
            + COLUMN_FEEDBACK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_FEEDBACK_NAME + " TEXT,"
            + COLUMN_FEEDBACK_PHONE + " TEXT,"
            + COLUMN_FEEDBACK_EMAIL + " TEXT,"
            + COLUMN_FEEDBACK_CONTENT + " TEXT NOT NULL,"
            + COLUMN_FEEDBACK_TIMESTAMP + " INTEGER NOT NULL"
            + ");";

    // Table Orders
    public static final String TABLE_ORDERS = "orders";
    public static final String COL_ORDER_ID = "id";
    public static final String COL_ORDER_USER_EMAIL = "user_email";
    public static final String COL_ORDER_CODE = "order_code";
    public static final String COL_ORDER_TOTAL_AMOUNT = "total_amount";
    public static final String COL_ORDER_STATUS = "status";
    public static final String COL_ORDER_TOTAL_QUANTITY = "total_quantity";
    public static final String COL_ORDER_DATE = "order_date";
    public static final String COL_ORDER_SHIPPING_ADDRESS = "shipping_address";
    private static final String CREATE_TABLE_ORDERS = "CREATE TABLE " + TABLE_ORDERS + "("
            + COL_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_ORDER_USER_EMAIL + " TEXT NOT NULL,"
            + COL_ORDER_CODE + " TEXT UNIQUE,"
            + COL_ORDER_TOTAL_AMOUNT + " REAL NOT NULL,"
            + COL_ORDER_STATUS + " TEXT NOT NULL,"
            + COL_ORDER_TOTAL_QUANTITY + " INTEGER NOT NULL,"
            + COL_ORDER_DATE + " INTEGER,"
            + COL_ORDER_SHIPPING_ADDRESS + " TEXT,"
            + "FOREIGN KEY(" + COL_ORDER_USER_EMAIL + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_EMAIL + ") ON UPDATE CASCADE ON DELETE CASCADE"
            + ");";

    // Table Order Items
    public static final String TABLE_ORDER_ITEMS = "order_items";
    public static final String COL_ORDER_ITEM_ID = "id";
    public static final String COL_ORDER_ITEM_ORDER_ID = "order_id";
    public static final String COL_ORDER_ITEM_PRODUCT_ID = "product_id";
    public static final String COL_ORDER_ITEM_PRODUCT_NAME = "product_name";
    public static final String COL_ORDER_ITEM_PRODUCT_PRICE = "product_price";
    public static final String COL_ORDER_ITEM_QUANTITY = "quantity";
    public static final String COL_ORDER_ITEM_PRODUCT_IMAGE_URL = "product_image_url";
    public static final String COL_ORDER_ITEM_PRODUCT_CATEGORY = "product_category";
    private static final String CREATE_TABLE_ORDER_ITEMS = "CREATE TABLE " + TABLE_ORDER_ITEMS + "("
            + COL_ORDER_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_ORDER_ITEM_ORDER_ID + " INTEGER NOT NULL,"
            + COL_ORDER_ITEM_PRODUCT_ID + " TEXT NOT NULL,"
            + COL_ORDER_ITEM_PRODUCT_NAME + " TEXT NOT NULL,"
            + COL_ORDER_ITEM_PRODUCT_PRICE + " REAL NOT NULL,"
            + COL_ORDER_ITEM_QUANTITY + " INTEGER NOT NULL,"
            + COL_ORDER_ITEM_PRODUCT_IMAGE_URL + " TEXT,"
            + COL_ORDER_ITEM_PRODUCT_CATEGORY + " TEXT,"
            + "FOREIGN KEY(" + COL_ORDER_ITEM_ORDER_ID + ") REFERENCES " + TABLE_ORDERS + "(" + COL_ORDER_ID + ") ON DELETE CASCADE,"
            + "FOREIGN KEY(" + COL_ORDER_ITEM_PRODUCT_ID + ") REFERENCES " + TABLE_BOOKS + "(" + COLUMN_BOOK_ID + ") ON DELETE SET NULL"
            + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        Log.d("DatabaseHelper", "Table 'users' created.");
        db.execSQL(CREATE_TABLE_CATEGORIES);
        Log.d("DatabaseHelper", "Table 'Categories' created.");
        db.execSQL(CREATE_TABLE_BOOKS);
        Log.d("DatabaseHelper", "Table 'books' created.");
        db.execSQL(CREATE_TABLE_ADDRESS);
        Log.d("DatabaseHelper", "Table 'address' created.");
        db.execSQL(CREATE_TABLE_ORDERS);
        Log.d("DatabaseHelper", "Table 'orders' created.");
        db.execSQL(CREATE_TABLE_ORDER_ITEMS);
        Log.d("DatabaseHelper", "Table 'order_items' created.");
        db.execSQL(CREATE_TABLE_CART_ITEMS);
        Log.d("DatabaseHelper", "Table 'cart_items' created.");
        db.execSQL(CREATE_TABLE_DISCOUNTS);
        Log.d("DatabaseHelper", "Table 'Discounts' created.");
        db.execSQL(CREATE_TABLE_FEEDBACKS);
        Log.d("DatabaseHelper", "Table 'Feedbacks' created.");
        db.execSQL("INSERT INTO " + TABLE_USERS + " (" + COLUMN_USER_USERNAME + ", " + COLUMN_USER_PASSWORD + ", " + COLUMN_USER_EMAIL + ", " + COLUMN_USER_ROLE + ") VALUES ('admin', 'admin123', 'admin@example.com', 'admin');");
        Log.d("DatabaseHelper", "Default admin user inserted: admin/admin123");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseHelper", "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ADDRESS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DISCOUNTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FEEDBACKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        Log.d("DatabaseHelper", "All tables dropped for upgrade.");
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON;");
        Log.d("DatabaseHelper", "PRAGMA foreign_keys=ON; executed.");
    }
}

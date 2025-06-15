package com.thanhvan.bookstoremanager.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {


    public static final String DATABASE_NAME = "BookstoreManager.db";
    public static final int DATABASE_VERSION = 2;


    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_USERNAME = "username";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_EMAIL = "email";


    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_USERNAME + " TEXT UNIQUE,"
            + COLUMN_USER_PASSWORD + " TEXT,"
            + COLUMN_USER_EMAIL + " TEXT"
            + ")";
    // Tên bảng và các cột cho bảng 'books'
    public static final String TABLE_BOOKS = "books";
    public static final String COLUMN_BOOK_ID = "id";
    public static final String COLUMN_BOOK_TITLE = "title";
    public static final String COLUMN_BOOK_AUTHOR = "author";
    public static final String COLUMN_BOOK_CATEGORY = "category";
    public static final String COLUMN_BOOK_RATING = "rating";
    public static final String COLUMN_BOOK_PRICE = "price";
    public static final String COLUMN_BOOK_ORIGINAL_PRICE = "original_price";
    public static final String COLUMN_BOOK_IMAGE_RES_ID = "image_res_id";

    // Câu lệnh SQL để tạo bảng 'books'
    private static final String CREATE_TABLE_BOOKS = "CREATE TABLE " + TABLE_BOOKS + "("
            + COLUMN_BOOK_ID + " TEXT PRIMARY KEY," // ID sách là TEXT và PRIMARY KEY
            + COLUMN_BOOK_TITLE + " TEXT,"
            + COLUMN_BOOK_AUTHOR + " TEXT,"
            + COLUMN_BOOK_CATEGORY + " TEXT,"
            + COLUMN_BOOK_RATING + " REAL," // REAL cho số thực
            + COLUMN_BOOK_PRICE + " REAL,"
            + COLUMN_BOOK_ORIGINAL_PRICE + " REAL,"
            + COLUMN_BOOK_IMAGE_RES_ID + " INTEGER" // INTEGER cho resource ID của drawable
            + ")";




    public DatabaseHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        Log.d("DatabaseHelper", "Table 'users' created.");
        db.execSQL(CREATE_TABLE_BOOKS);
        Log.d("DatabaseHelper", "Table 'books' created.");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        Log.d("DatabaseHelper", "Table 'users' dropped. Recreating...");

        onCreate(db);
    }
}
package com.thanhvan.bookstoremanager.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.thanhvan.bookstoremanager.model.Address;
import java.util.ArrayList;
import java.util.List;

public class AddressDao {

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public AddressDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        if (db == null || !db.isOpen()) {
            try {
                db = dbHelper.getWritableDatabase();
                Log.d("AddressDao", "Database opened for writing.");
            } catch (Exception e) {
                Log.e("AddressDao", "Error opening database for writing: " + e.getMessage());
            }
        }
    }

    public void close() {
        if (db != null && db.isOpen()) {
            try {
                db.close();
                Log.d("AddressDao", "Database closed.");
            } catch (Exception e) {
                Log.e("AddressDao", "Error closing database: " + e.getMessage());
            }
        }
    }

    public long addAddress(Address address) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ADDRESS_USER_EMAIL, address.getUserEmail());
        values.put(DatabaseHelper.COLUMN_ADDRESS_NAME, address.getName());
        values.put(DatabaseHelper.COLUMN_ADDRESS_PHONE, address.getPhone());
        values.put(DatabaseHelper.COLUMN_ADDRESS_FULL_ADDRESS, address.getFullAddress());
        values.put(DatabaseHelper.COLUMN_ADDRESS_LATITUDE, address.getLatitude());
        values.put(DatabaseHelper.COLUMN_ADDRESS_LONGITUDE, address.getLongitude());

        long result = -1;
        try {
            result = db.insert(DatabaseHelper.TABLE_ADDRESS, null, values);
        } catch (Exception e) {
            Log.e("AddressDao", "Error adding address: " + e.getMessage());
        }
        return result;
    }

    public List<Address> getAllAddressesForUser(String userEmail) {
        open();
        List<Address> addressList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_ADDRESS,
                    new String[]{
                            DatabaseHelper.COLUMN_ADDRESS_ID,
                            DatabaseHelper.COLUMN_ADDRESS_USER_EMAIL,
                            DatabaseHelper.COLUMN_ADDRESS_NAME,
                            DatabaseHelper.COLUMN_ADDRESS_PHONE,
                            DatabaseHelper.COLUMN_ADDRESS_FULL_ADDRESS,
                            DatabaseHelper.COLUMN_ADDRESS_LATITUDE,
                            DatabaseHelper.COLUMN_ADDRESS_LONGITUDE
                    },
                    DatabaseHelper.COLUMN_ADDRESS_USER_EMAIL + " = ?",
                    new String[]{userEmail},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ADDRESS_ID);
                    int userEmailIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ADDRESS_USER_EMAIL);
                    int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ADDRESS_NAME);
                    int phoneIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ADDRESS_PHONE);
                    int fullAddressIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ADDRESS_FULL_ADDRESS);
                    int latIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ADDRESS_LATITUDE);
                    int lonIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ADDRESS_LONGITUDE);

                    int id = (idIndex != -1) ? cursor.getInt(idIndex) : -1;
                    String retrievedUserEmail = (userEmailIndex != -1) ? cursor.getString(userEmailIndex) : "";
                    String name = (nameIndex != -1) ? cursor.getString(nameIndex) : "";
                    String phone = (phoneIndex != -1) ? cursor.getString(phoneIndex) : "";
                    String fullAddress = (fullAddressIndex != -1) ? cursor.getString(fullAddressIndex) : "";
                    double latitude = (latIndex != -1) ? cursor.getDouble(latIndex) : 0.0;
                    double longitude = (lonIndex != -1) ? cursor.getDouble(lonIndex) : 0.0;

                    Address address = new Address(id, retrievedUserEmail, name, phone, fullAddress, latitude, longitude);
                    addressList.add(address);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("AddressDao", "Error getting all addresses for user: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return addressList;
    }

    public Address getAddressByIdAndUser(int addressId, String userEmail) {
        open();
        Cursor cursor = null;
        Address address = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_ADDRESS,
                    new String[]{
                            DatabaseHelper.COLUMN_ADDRESS_ID,
                            DatabaseHelper.COLUMN_ADDRESS_USER_EMAIL,
                            DatabaseHelper.COLUMN_ADDRESS_NAME,
                            DatabaseHelper.COLUMN_ADDRESS_PHONE,
                            DatabaseHelper.COLUMN_ADDRESS_FULL_ADDRESS,
                            DatabaseHelper.COLUMN_ADDRESS_LATITUDE,
                            DatabaseHelper.COLUMN_ADDRESS_LONGITUDE
                    },
                    DatabaseHelper.COLUMN_ADDRESS_ID + " = ? AND " + DatabaseHelper.COLUMN_ADDRESS_USER_EMAIL + " = ?",
                    new String[]{String.valueOf(addressId), userEmail},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ADDRESS_ID);
                int userEmailIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ADDRESS_USER_EMAIL);
                int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ADDRESS_NAME);
                int phoneIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ADDRESS_PHONE);
                int fullAddressIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ADDRESS_FULL_ADDRESS);
                int latIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ADDRESS_LATITUDE);
                int lonIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ADDRESS_LONGITUDE);

                int id = (idIndex != -1) ? cursor.getInt(idIndex) : -1;
                String retrievedUserEmail = (userEmailIndex != -1) ? cursor.getString(userEmailIndex) : "";
                String name = (nameIndex != -1) ? cursor.getString(nameIndex) : "";
                String phone = (phoneIndex != -1) ? cursor.getString(phoneIndex) : "";
                String fullAddress = (fullAddressIndex != -1) ? cursor.getString(fullAddressIndex) : "";
                double latitude = (latIndex != -1) ? cursor.getDouble(latIndex) : 0.0;
                double longitude = (lonIndex != -1) ? cursor.getDouble(lonIndex) : 0.0;

                address = new Address(id, retrievedUserEmail, name, phone, fullAddress, latitude, longitude);
            }
        } catch (Exception e) {
            Log.e("AddressDao", "Error getting address by ID and user: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return address;
    }

    public int updateAddress(Address address) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ADDRESS_USER_EMAIL, address.getUserEmail());
        values.put(DatabaseHelper.COLUMN_ADDRESS_NAME, address.getName());
        values.put(DatabaseHelper.COLUMN_ADDRESS_PHONE, address.getPhone());
        values.put(DatabaseHelper.COLUMN_ADDRESS_FULL_ADDRESS, address.getFullAddress());
        values.put(DatabaseHelper.COLUMN_ADDRESS_LATITUDE, address.getLatitude());
        values.put(DatabaseHelper.COLUMN_ADDRESS_LONGITUDE, address.getLongitude());

        int rowsAffected = 0;
        try {
            rowsAffected = db.update(DatabaseHelper.TABLE_ADDRESS, values,
                    DatabaseHelper.COLUMN_ADDRESS_ID + " = ? AND " + DatabaseHelper.COLUMN_ADDRESS_USER_EMAIL + " = ?",
                    new String[]{String.valueOf(address.getId()), address.getUserEmail()});
        } catch (Exception e) {
            Log.e("AddressDao", "Error updating address: " + e.getMessage());
        }
        return rowsAffected;
    }

    public int deleteAddress(int addressId, String userEmail) {
        open();
        int rowsAffected = 0;
        try {
            rowsAffected = db.delete(DatabaseHelper.TABLE_ADDRESS,
                    DatabaseHelper.COLUMN_ADDRESS_ID + " = ? AND " + DatabaseHelper.COLUMN_ADDRESS_USER_EMAIL + " = ?",
                    new String[]{String.valueOf(addressId), userEmail});
        } catch (Exception e) {
            Log.e("AddressDao", "Error deleting address: " + e.getMessage());
        }
        return rowsAffected;
    }
}

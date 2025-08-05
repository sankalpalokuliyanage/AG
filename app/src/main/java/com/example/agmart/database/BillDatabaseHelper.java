package com.example.agmart.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BillDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bills.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_BILLS = "bills";

    public BillDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_BILLS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, path TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILLS);
        onCreate(db);
    }

    public void insertBill(String filePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("path", filePath);
        db.insert(TABLE_BILLS, null, values);
        db.close();
    }
}

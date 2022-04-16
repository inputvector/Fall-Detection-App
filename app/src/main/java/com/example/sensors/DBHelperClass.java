package com.example.sensors;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DBHelperClass extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String TABLE_NAME = "SensorTable";
    private static final String COL1 = "X_ACC";
    private static final String COL2 = "Y_ACC";
    private static final String COL3 = "Z_ACC";
    private static final String COL4 = "X_GYRO";
    private static final String COL5 = "Y_GYRO";
    private static final String COL6 = "Z_GYRO";

    public DBHelperClass(@Nullable Context context) {

        super(context,TABLE_NAME,null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String createTable = "CREATE TABLE " + TABLE_NAME +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL1 + " REAL," +
                COL2 + " REAL," +
                COL3 + " REAL," +
                COL4 + " REAL," +
                COL5 + " REAL," +
                COL6 + " REAL)";
        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL(new StringBuilder().append("DROP IF TABLE EXISTS ").append(TABLE_NAME).toString());
        onCreate(sqLiteDatabase);
    }
    public void addData(float[] input) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, input[0]);
        contentValues.put(COL2, input[1]);
        contentValues.put(COL3, input[2]);
        contentValues.put(COL4, input[3]);
        contentValues.put(COL5, input[4]);
        contentValues.put(COL6, input[5]);
        Log.e("CONT",contentValues.toString());
        db.insert(TABLE_NAME, null, contentValues);
    }

    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }
}

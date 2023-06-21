package com.wojtech.calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class Database extends SQLiteOpenHelper {
    public static final String DB_NAME = "calendar";
    public static final int DB_VERSION = 1;
    public static final String TABLE_HOLIDAYS = "holidays";
    public static final String HOLIDAYS_ID = "id";
    public static final String HOLIDAYS_NAME = "name";
    public static final String HOLIDAYS_DATE = "date";
    public static final String HOLIDAYS_TAG = "tag";

    public static final String TAG_USER = "usr";
    public static final String TAG_API = "api";

    public Database(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+TABLE_HOLIDAYS+"(" +
                HOLIDAYS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                HOLIDAYS_NAME + " VARCHAR(30) NOT NULL," +
                HOLIDAYS_DATE + " DATE NOT NULL UNIQUE," +
                HOLIDAYS_TAG + " VARCHAR(3) CHECK("+HOLIDAYS_TAG+" IN ('"+TAG_USER+"', '"+TAG_API+"')) NOT NULL" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_HOLIDAYS);
        onCreate(db);
    }

    /**
     * Adds holiday to the database
     * @param name name
     * @param date date
     * @param tag tag static variable
     * @return true if new record was inserted into the database
     */
    public boolean addHoliday(String name, String date, String tag){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(HOLIDAYS_NAME, name);
        values.put(HOLIDAYS_DATE, date);
        values.put(HOLIDAYS_TAG, tag);
        long result = db.insert(TABLE_HOLIDAYS, null, values);
        db.close();
        return result > -1;
    }

    /**
     * Gets all saved holidays for previous, this and next month in given date
     * @param date date
     * @return HashMap with all holidays of a month
     */
    public ArrayList<HashMap<String, String>> getMonthsHolidays(String date) {
        String prev_month_year, next_month_year;
        try {
            Calendar c = Calendar.getInstance();
            Date current_date = CalendarUtils.PL_DATE_FORMAT.parse(date);
            assert current_date != null;
            c.setTime(current_date);
            c.add(Calendar.MONTH, -1);
            prev_month_year = CalendarUtils.MONTH_YEAR_FORMAT.format(c.getTime());
            c.add(Calendar.MONTH, 2);
            next_month_year = CalendarUtils.MONTH_YEAR_FORMAT.format(c.getTime());
        } catch (Exception e){ return null; }
        ArrayList<HashMap<String, String>> holidays = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HOLIDAYS, new String[]{HOLIDAYS_ID, HOLIDAYS_NAME, HOLIDAYS_DATE, HOLIDAYS_TAG},
                HOLIDAYS_DATE+" LIKE ? OR "+ HOLIDAYS_DATE+" LIKE ? OR "+HOLIDAYS_DATE+" LIKE ?", new String[]{"__."+prev_month_year, "__"+date.substring(2), "__."+next_month_year}, null, null, null);
        while (cursor.moveToNext()){
            HashMap<String, String> holiday = new HashMap<>();
            holiday.put(HOLIDAYS_ID, cursor.getString(cursor.getColumnIndexOrThrow(HOLIDAYS_ID)));
            holiday.put(HOLIDAYS_NAME, cursor.getString(cursor.getColumnIndexOrThrow(HOLIDAYS_NAME)));
            holiday.put(HOLIDAYS_DATE, cursor.getString(cursor.getColumnIndexOrThrow(HOLIDAYS_DATE)));
            holiday.put(HOLIDAYS_TAG, cursor.getString(cursor.getColumnIndexOrThrow(HOLIDAYS_TAG)));
            holidays.add(holiday);
        }
        cursor.close();
        return holidays;
    }

    /**
     * Deletes holiday with given id from the database
     * @param id holiday id
     * @return true if item was deleted
     */
    public boolean deleteHoliday(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_HOLIDAYS, HOLIDAYS_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }
    /**
     * Deletes holiday with given date from the database
     * @param date date
     * @return true if item was deleted
     */
    public boolean deleteHoliday(String date){
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_HOLIDAYS, HOLIDAYS_DATE + " = ?", new String[]{date});
        db.close();
        return result > 0;
    }

    /**
     * Checks whether holiday of given date is saved in the database
     * @param date date
     * @return true if holiday of given date is saved in the database
     */
    public boolean isSavedHoliday(String date){
        boolean exists = false;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HOLIDAYS, new String[]{HOLIDAYS_ID}, HOLIDAYS_DATE+" = ?", new String[]{date},null, null, null);
        if (cursor.moveToFirst()) exists = true;
        cursor.close();
        db.close();
        return exists;
    }
}

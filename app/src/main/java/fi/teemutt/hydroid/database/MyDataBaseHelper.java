package fi.teemutt.hydroid.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import java.util.ArrayList;
import java.util.Random;

import fi.teemutt.hydroid.R;
import fi.teemutt.hydroid.models.DrinkEvent;
import fi.teemutt.hydroid.models.Portion;

/**
 * Created by Teemu on 11.11.2016.
 *
 */

public class MyDataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "hydroid.db";
    private static final int DATABASE_VERSION = 1;

    // Table creation sql
    private static final String SQL_CREATE_EVENTS = "create table Events (_id integer primary key, date text not null, size integer not null);";
    private static final String SQL_DELETE_EVENTS = "drop table if exists Events;";

    private static final String SQL_CREATE_PORTIONS = "create table Portions (_id integer primary key, size integer not null, drawable integer not null);";
    private static final String SQL_DELETE_PORTIONS = "drop table if exists Portions";
    private static final String SQL_INIT_PORTIONS = "insert into Portions (size, drawable) values (120, " + R.drawable.ic_coffee + "), (200, " + R.drawable.ic_glass + "), (350, " + R.drawable.ic_mug + "), (500, " + R.drawable.ic_bottle + ");";

    // Schemas
    private static final String TABLE_EVENTS = "Events";
    private static final String EVENT_ID = "_id";
    private static final String EVENT_DATE = "date";
    private static final String EVENT_SIZE = "size";

    private static final String TABLE_PORTIONS = "Portions";
    private static final String PORTION_ID = "_id";
    private static final String PORTION_SIZE = "size";
    private static final String PORTION_DRAWABLE = "drawable";

    private static MyDataBaseHelper mInstance = null;

    // Private to prevent use. Use getInstance instead.
    private MyDataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static MyDataBaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MyDataBaseHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_EVENTS);
        sqLiteDatabase.execSQL(SQL_CREATE_PORTIONS);
        sqLiteDatabase.execSQL(SQL_INIT_PORTIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_EVENTS);
        sqLiteDatabase.execSQL(SQL_DELETE_PORTIONS);
        onCreate(sqLiteDatabase);
    }

    // Used for development, not used in release.
    public void createTestData() {
        Random random = new Random();
        for (int i = 12; i != 0; i--) {
            String now = ZonedDateTime.now().minusDays(i).toInstant().toString();
            for (int j = 0; j < 6; j++)
                createEvent(now, random.nextInt(23) * 15 + 100);
        }
    }

    // Used for development, not used in release.
    public void clearData() {
        SQLiteDatabase database = mInstance.getWritableDatabase();
        database.execSQL(SQL_DELETE_EVENTS);
        database.execSQL(SQL_DELETE_PORTIONS);

        database.execSQL(SQL_CREATE_EVENTS);
        database.execSQL(SQL_CREATE_PORTIONS);
        database.execSQL(SQL_INIT_PORTIONS);
    }

    // |----------------------------------------------------------------------|
    // |                               EVENTS                                 |
    // |----------------------------------------------------------------------|
    public long createEvent(String dateString, int size) {
        SQLiteDatabase database = mInstance.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(EVENT_DATE, dateString);
        values.put(EVENT_SIZE, size);
        return database.insert(TABLE_EVENTS, null, values);
    }

    public ArrayList<DrinkEvent> getEventsForDay(ZonedDateTime day) {
        ZonedDateTime endOfDay = day.plusDays(1);
        SQLiteDatabase database = mInstance.getReadableDatabase();
        String[] columns = {EVENT_ID, EVENT_DATE, EVENT_SIZE};
        String selection = "date(" + EVENT_DATE + ") >= ? and date(" + EVENT_DATE + ") < ?";
        String[] selectionArgs = {day.toInstant().toString(), endOfDay.toInstant().toString()};
        String sortOrder = "time(" + EVENT_DATE + ") asc";
        Cursor cursor = database.query(
                TABLE_EVENTS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
        ArrayList<DrinkEvent> events = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            long _id = cursor.getLong(0);
            String utcString = cursor.getString(1);
            Instant instant = Instant.parse(utcString);
            ZonedDateTime date = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
            int size = cursor.getInt(2);
            DrinkEvent event = new DrinkEvent(_id, date, size);
            events.add(event);
            cursor.moveToNext();
        }
        cursor.close();
        return events;
    }

    public int deleteEvent(long _id) {
        SQLiteDatabase database = mInstance.getWritableDatabase();
        String selection = EVENT_ID + " = ?";
        String[] selectionArgs = {String.valueOf(_id)};
        return database.delete(TABLE_EVENTS, selection, selectionArgs);
    }

    public DrinkEvent getEvent(long _id) {
        SQLiteDatabase db = mInstance.getReadableDatabase();
        String[] columns = {EVENT_DATE, EVENT_SIZE};
        String selection = EVENT_ID + " = ?";
        String[] selectionArgs = {String.valueOf(_id)};
        Cursor cursor = db.query(
                TABLE_EVENTS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        String utcString = cursor.getString(0);
        Instant instant = Instant.parse(utcString);
        ZonedDateTime date = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        int size = cursor.getInt(1);
        cursor.close();
        return new DrinkEvent(_id, date, size);
    }

    public int saveEvent(long _id, int size, String dateString) {
        SQLiteDatabase db = mInstance.getWritableDatabase();
        String selection = EVENT_ID + " = ?";
        String[] selectionArgs = {String.valueOf(_id)};
        ContentValues values = new ContentValues();
        values.put(EVENT_SIZE, size);
        values.put(EVENT_DATE, dateString);
        return db.update(TABLE_EVENTS, values, selection, selectionArgs);
    }

    // |----------------------------------------------------------------------|
    // |                               PORTIONS                               |
    // |----------------------------------------------------------------------|
    public ArrayList<Portion> getPortions() {
        SQLiteDatabase database = mInstance.getReadableDatabase();
        String[] columns = {PORTION_ID, PORTION_SIZE, PORTION_DRAWABLE};
        Cursor cursor = database.query(
                TABLE_PORTIONS,
                columns,
                null,
                null,
                null,
                null,
                null
        );
        ArrayList<Portion> portions = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            long _id = cursor.getLong(0);
            int size = cursor.getInt(1);
            int drawable = cursor.getInt(2);
            Portion portion = new Portion(_id, size, drawable);
            portions.add(portion);
            cursor.moveToNext();
        }
        cursor.close();
        return portions;
    }

    public Portion getPortion(long _id) {
        SQLiteDatabase database = mInstance.getReadableDatabase();
        String[] columns = {PORTION_ID, PORTION_SIZE, PORTION_DRAWABLE};
        String selection = PORTION_ID + " = ?";
        String[] selectionArgs = {String.valueOf(_id)};
        Cursor cursor = database.query(
                TABLE_PORTIONS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        int size = cursor.getInt(1);
        int drawable = cursor.getInt(2);
        cursor.close();
        return new Portion(_id, size, drawable);
    }

    public long createPortion(int size, int drawable) {
        SQLiteDatabase database = mInstance.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PORTION_SIZE, size);
        values.put(PORTION_DRAWABLE, drawable);
        return database.insert(TABLE_PORTIONS, null, values);
    }

    public int savePortion(long _id, int size, int drawable) {
        SQLiteDatabase database = mInstance.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PORTION_SIZE, size);
        values.put(PORTION_DRAWABLE, drawable);
        String selection = PORTION_ID + " = ?";
        String[] selectionArgs = {String.valueOf(_id)};
        return database.update(TABLE_PORTIONS, values, selection, selectionArgs);
    }

    public int deletePortion(long _id) {
        SQLiteDatabase database = mInstance.getWritableDatabase();
        String selection = PORTION_ID + " = ?";
        String[] selectionArgs = {String.valueOf(_id)};
        return database.delete(TABLE_PORTIONS, selection, selectionArgs);
    }
}

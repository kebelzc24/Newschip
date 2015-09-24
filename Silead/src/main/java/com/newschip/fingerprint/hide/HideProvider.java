package com.newschip.fingerprint.hide;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class HideProvider extends ContentProvider {
    public static final String AUTHORITY = "com.newschip.fingerprint.HideProvider";

    private SQLiteHeler mSQLiteHeler;
    private ContentResolver mContentResolver;
    private final static int MATCH_CODE_HIDE = 1;

    private static final String DATABASE_NAME = "hide.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_HIDE = "path";

    private static final String ITEM_FILE_NAME = "file_name";
    private static final String ITEM_OLD_PATH = "old_path";
    private static final String ITEM_NEW_PATH = "new_path";

    private static final UriMatcher sMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
    static {
        sMatcher.addURI(AUTHORITY, TABLE_HIDE, MATCH_CODE_HIDE);
    }

    private class SQLiteHeler extends SQLiteOpenHelper {

        public SQLiteHeler(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            db.execSQL("CREATE TABLE " + TABLE_HIDE + " (" + ITEM_FILE_NAME
                    + "," + ITEM_NEW_PATH + "," + ITEM_OLD_PATH + ")");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
        }

    }

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        mSQLiteHeler = new SQLiteHeler(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        // TODO Auto-generated method stub
        SQLiteDatabase db = mSQLiteHeler.getReadableDatabase();
        Cursor cursor;
        cursor = db.query(TABLE_HIDE, projection, selection, selectionArgs,
                null, null, sortOrder);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        SQLiteDatabase db = mSQLiteHeler.getWritableDatabase();
        mContentResolver = getContext().getContentResolver();
        db.insert(TABLE_HIDE, null, values);
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        SQLiteDatabase db = mSQLiteHeler.getWritableDatabase();
        mContentResolver = getContext().getContentResolver();
        int count;
        count = db.delete(TABLE_HIDE, selection, selectionArgs);
        mContentResolver.notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // TODO Auto-generated method stub
        SQLiteDatabase db = mSQLiteHeler.getWritableDatabase();
        mContentResolver = getContext().getContentResolver();
        int count;
        count = db.update(TABLE_HIDE, values, selection, selectionArgs);
        mContentResolver.notifyChange(uri, null);
        return count;
    }

    public void clear(Uri uri, String tab) {
        SQLiteDatabase db = mSQLiteHeler.getWritableDatabase();
        db.execSQL("DELETE FROM " + tab);
    }

}
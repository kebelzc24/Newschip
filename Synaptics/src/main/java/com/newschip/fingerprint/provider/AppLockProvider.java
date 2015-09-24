package com.newschip.fingerprint.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class AppLockProvider extends ContentProvider {
    public static final String AUTHORITY = "com.newschip.fingerprint.AppLockProvider";

    private SQLiteHeler mSQLiteHeler;
    private ContentResolver mContentResolver;
    private final static int MATCH_CODE_PROTECT = 1;
    private final static int MATCH_CODE_USAGE = 2;
    private final static int MATCH_CODE_HIDE = 3;
    private final static int MATCH_CODE_SWITCH_STATE = 4;
    private final static int MATCH_CODE_PROTECT_STATE = 5;

    private static final String DATABASE_NAME = "fingerprint.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_PROTECT = "app_protect";
    private static final String TABLE_USAGE = "finger_useage";
    private static final String TABLE_HIDE = "hide_app";
    private static final String ITEM_PKG = "packagename";
    private static final String ITEM_LABEL = "packagelabel";
    private static final String ITEM_UID = "uid";
    private static final String ITEM_FINGER_INDEX = "fingerindex";
    private static final String ITEM_FINGER_NAME = "fingername";
    private static final String ITEM_RELEATE_APP = "releate_app";
    private static final String TABLE_SWITCH_STATE = "switch_state";
    private static final String ITEM_SWITCH_STATE = "switch_state";
    private static final String TABLE_PROTECT_STATE = "protect_state";
    private static final String ITEM_PROTECT_STATE = "protect_state";

    private static final UriMatcher sMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
    static {
        sMatcher.addURI(AUTHORITY, TABLE_PROTECT, MATCH_CODE_PROTECT);
        sMatcher.addURI(AUTHORITY, TABLE_USAGE, MATCH_CODE_USAGE);
        sMatcher.addURI(AUTHORITY, TABLE_HIDE, MATCH_CODE_HIDE);
        sMatcher.addURI(AUTHORITY, TABLE_SWITCH_STATE, MATCH_CODE_SWITCH_STATE);
        sMatcher.addURI(AUTHORITY, TABLE_PROTECT_STATE, MATCH_CODE_PROTECT_STATE);
    }

    private class SQLiteHeler extends SQLiteOpenHelper {

        public SQLiteHeler(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            db.execSQL("CREATE TABLE " + TABLE_PROTECT + " (" + ITEM_PKG + ","
                    + ITEM_LABEL + "," + ITEM_UID + ")");
            db.execSQL("CREATE TABLE " + TABLE_USAGE + " (" + ITEM_PKG + ","
                    + ITEM_LABEL + "," + ITEM_FINGER_INDEX + ","
                    + ITEM_FINGER_NAME + ")");
            db.execSQL("CREATE TABLE " + TABLE_HIDE + " (" + ITEM_PKG + ","
                    + ITEM_RELEATE_APP + "," + ITEM_FINGER_INDEX + ","
                    + ITEM_FINGER_NAME + ")");
            db.execSQL("CREATE TABLE " + TABLE_SWITCH_STATE + " ("
                    + ITEM_SWITCH_STATE + ")");
            db.execSQL("CREATE TABLE " + TABLE_PROTECT_STATE + " ("
                    + ITEM_PROTECT_STATE + ")");
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
        String tab = getContentTab(uri);
        cursor = db.query(tab, projection, selection, selectionArgs, null,
                null, sortOrder);
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
        String tab = getContentTab(uri);
        db.insert(tab, null, values);
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        SQLiteDatabase db = mSQLiteHeler.getWritableDatabase();
        mContentResolver = getContext().getContentResolver();
        int count;
        String tab = getContentTab(uri);
        count = db.delete(tab, selection, selectionArgs);
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
        String tab = getContentTab(uri);
        count = db.update(tab, values, selection, selectionArgs);
        mContentResolver.notifyChange(uri, null);
        return count;
    }

    public void clear(Uri uri, String tab) {
        SQLiteDatabase db = mSQLiteHeler.getWritableDatabase();
        db.execSQL("DELETE FROM " + tab);
    }

    private String getContentTab(Uri uri) {
        String tab = null;
        if (sMatcher.match(uri) == MATCH_CODE_PROTECT) {
            tab = TABLE_PROTECT;
        } else if (sMatcher.match(uri) == MATCH_CODE_USAGE) {
            tab = TABLE_USAGE;
        } else if (sMatcher.match(uri) == MATCH_CODE_HIDE) {
            tab = TABLE_HIDE;
        } else if (sMatcher.match(uri) == MATCH_CODE_SWITCH_STATE) {
            tab = TABLE_SWITCH_STATE;
        } else if (sMatcher.match(uri) == MATCH_CODE_PROTECT_STATE) {
            tab = TABLE_PROTECT_STATE;
        }
        return tab;
    }

}
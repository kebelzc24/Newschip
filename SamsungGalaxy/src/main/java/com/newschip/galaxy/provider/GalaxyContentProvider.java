package com.newschip.galaxy.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by LQ on 2015/12/10.
 */
public class GalaxyContentProvider extends ContentProvider {
    public static final String AUTHORITY = "com.newschip.galaxy.GalaxyContentProvider";
    private static final String DATABASE_NAME = "fingerprint.db";
    private static final int DATABASE_VERSION = 1;
    private SQLiteHeler mSQLiteHeler;
    private ContentResolver mContentResolver;
    private final static int MATCH_CODE_PROTECT = 1;
    private final static int MATCH_CODE_SWITCH = 2;
    private final static int MATCH_CODE_STATE = 3;
    private final static int MATCH_CODE_MEDIA = 4;

    //应用锁
    public static final String TABLE_PROTECTED_APP = "protect_app";
    public static final String ITEM_PACKAGE = "package";
    //快速切换应用
    public static final String TABLE_SWITCH_APP = "switch_app";
    public static final String ITEM_APP_LABEL = "app_label";
    public static final String ITEM_FINGER_INDEX = "finger_index";
    //应用锁和快速切换应用开启状态
    public static final String TABLE_STATE = "state";
    public static final String ITEM_STATE_SWITCH = "switch_state";
    public static final String ITEM_STATE_PROTECT = "protect_state";
    public static final String ITEM_STATE_EASY_HOME = "easy_home_state";
    //多媒体文件路径
    public static final String TABLE_MEDIA = "media";
    public static final String ITEM_FILE_NAME = "file_name";
    public static final String ITEM_OLD_PATH = "old_path";
    public static final String ITEM_NEW_PATH = "new_path";


    private static final UriMatcher sMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        sMatcher.addURI(AUTHORITY, TABLE_PROTECTED_APP, MATCH_CODE_PROTECT);
        sMatcher.addURI(AUTHORITY, TABLE_SWITCH_APP, MATCH_CODE_SWITCH);
        sMatcher.addURI(AUTHORITY, TABLE_STATE, MATCH_CODE_STATE);
        sMatcher.addURI(AUTHORITY, TABLE_MEDIA, MATCH_CODE_MEDIA);
    }

    @Override
    public boolean onCreate() {
        mSQLiteHeler = new SQLiteHeler(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mSQLiteHeler.getReadableDatabase();
        Cursor cursor;
        String tab = getContentTab(uri);
        cursor = db.query(tab, projection, selection, selectionArgs, null,
                null, sortOrder);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mSQLiteHeler.getWritableDatabase();
        mContentResolver = getContext().getContentResolver();
        String tab = getContentTab(uri);
        db.insert(tab, null, values);
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mSQLiteHeler.getWritableDatabase();
        mContentResolver = getContext().getContentResolver();
        int count;
        String tab = getContentTab(uri);
        count = db.delete(tab, selection, selectionArgs);
        mContentResolver.notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
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
            tab = TABLE_PROTECTED_APP;
        } else if (sMatcher.match(uri) == MATCH_CODE_SWITCH) {
            tab = TABLE_SWITCH_APP;
        } else if (sMatcher.match(uri) == MATCH_CODE_STATE) {
            tab = TABLE_STATE;
        } else if (sMatcher.match(uri) == MATCH_CODE_MEDIA) {
            tab = TABLE_MEDIA;
        }
        return tab;
    }

    private class SQLiteHeler extends SQLiteOpenHelper {

        public SQLiteHeler(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            db.execSQL("CREATE TABLE " + TABLE_PROTECTED_APP + " (" + ITEM_PACKAGE + ","
                    + ITEM_APP_LABEL+ ")");
            db.execSQL("CREATE TABLE " + TABLE_SWITCH_APP + " (" + ITEM_PACKAGE + ","
                    + ITEM_APP_LABEL + "," + ITEM_FINGER_INDEX + ")");
            db.execSQL("CREATE TABLE " + TABLE_STATE + " (" + ITEM_STATE_PROTECT + ","
                    + ITEM_STATE_SWITCH  + ","+ITEM_STATE_EASY_HOME+")");
            db.execSQL("CREATE TABLE " + TABLE_MEDIA + " ("
                    + ITEM_FILE_NAME +","+ITEM_OLD_PATH+","+ITEM_NEW_PATH+ ")");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
        }

    }
}

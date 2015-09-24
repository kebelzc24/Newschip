package com.newschip.fingerprint.hide;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

public class ProviderHelper {

    private Uri mUri = Uri
            .parse("content://com.newschip.fingerprint.HideProvider/path");
    private ContentResolver mContentResolver;
    private static final String ITEM_FILE_NAME = "file_name";
    private static final String ITEM_OLD_PATH = "old_path";
    private static final String ITEM_NEW_PATH = "new_path";

    public ProviderHelper(Context context) {
        mContentResolver = context.getContentResolver();
    }
    
    public String getOldPath(String newPath){
        String path = null;
        Cursor cu = mContentResolver.query(mUri, null, null, null, null);
        if (cu != null) {
            while (cu.moveToNext()) {
                if (newPath.equals(cu.getString(1))) {
                   path = cu.getString(2);
                   break;
                }
            }
            cu.close();
        }
        return path;
    }

    public boolean isPathHasHide(String oldPath) {
        boolean hide = false;
        Cursor cu = mContentResolver.query(mUri, null,  null, null, null);
        if (cu != null) {
            while (cu.moveToNext()) {
                if (oldPath.equals(cu.getString(2))) {
                    hide = true;
                    break;
                }
            }
            cu.close();
        }
        return hide;
    }

    public void insertHidePath(String name,String newPath,String oldPath) {
        if(isPathHasHide(newPath)) return;
        ContentValues values = new ContentValues();
        values.put(ITEM_FILE_NAME, name);
        values.put(ITEM_NEW_PATH, newPath);
        values.put(ITEM_OLD_PATH, oldPath);
        mContentResolver.insert(mUri, values);
    }
    
    public void deleteHidePath(String newPath){
        mContentResolver.delete(mUri, ITEM_NEW_PATH + "=?",
                new String[] { "" + newPath });
    }
}

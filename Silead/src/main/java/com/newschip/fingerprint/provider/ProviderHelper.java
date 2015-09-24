package com.newschip.fingerprint.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class ProviderHelper {
    private final String CONTENT = "content://com.newschip.fingerprint.AppLockProvider/";
    private final String ITEM_PKG = "packagename";
    private final String ITEM_LABEL = "packagelabel";
    private String ITEM_ARG0;
    private final String ITEM_UID = "uid";
    private final String ITEM_FINGER_INDEX = "fingerindex";
    private final String ITEM_FINGER_NAME = "fingername";
    private final String ITEM_SWITCH_STATE = "switch_state";
    private final String ITEM_PROTECT_STATE = "protect_state";
    private ContentResolver mContentResolver;
    private Uri mUri;
    private String URI;
    public final String TABLE_PROTECT = "app_protect";
    public final String TABLE_USAGE = "finger_useage";

    public ProviderHelper(Context context, String tab) {
        super();
        // TODO Auto-generated constructor stub
        this.mContentResolver = context.getContentResolver();
        this.URI = CONTENT + tab;
        this.mUri = Uri.parse(URI);
        if (tab.equals(TABLE_PROTECT)) {
            this.ITEM_ARG0 = ITEM_UID;
        } else if (tab.equals(TABLE_USAGE)) {
            this.ITEM_ARG0 = ITEM_FINGER_INDEX;
        }
    }

    public boolean isPkgRelateFinger(String pkg) {
        return queryContentProvider(pkg, null);
    }

    public boolean isFingerRelatePkg(String pkg, String fingerName) {
        return queryContentProvider(pkg, fingerName);
    }

    /**
     * 通过指纹编号查找关联的应用包名 Method:getPackageNameWithFingerIndex arg:index指纹编号
     */
    public String getPackageNameWithFingerIndex(int index) {
        // 指纹编号在数据库第3列，包名在数据库第1列
        return queryContentProvider(index, 2, 0);
    }

    /**
     * 通过指纹编号查找指纹名称 Method:getFingerNameWithFingerIndex arg:index指纹编号
     */
    public String getFingerNameWithFingerIndex(int index) {
        // 指纹编号在数据库第3列，指纹名称在数据库第四列
        return queryContentProvider(index, 2, 3);
    }

    /**
     * 通过指纹名称查找关联的应用包名 Method:getPackageNameWithFingerName fingerName指纹名称
     */
    public String getPackageNameWithFingerName(String fingerName) {
        return queryContentProvider(fingerName, 3, 0);
    }

    public String getFingerNameWithPackageName(String pkg) {
        return queryContentProvider(pkg, 0, 3);
    }

    public void insertPackageData(Context context, String pkg, String label,
            String arg0) {
        ContentValues values = new ContentValues();
        values.put(ITEM_PKG, pkg);
        values.put(ITEM_LABEL, label);
        values.put(ITEM_ARG0, arg0);
        mContentResolver.insert(mUri, values);
    }

    public void insertFingerData(Context context, String pkg, String label,
            String arg0, String fingerName) {
        ContentValues values = new ContentValues();
        values.put(ITEM_PKG, pkg);
        values.put(ITEM_LABEL, label);
        values.put(ITEM_ARG0, arg0);
        values.put(ITEM_FINGER_NAME, fingerName);
        mContentResolver.insert(mUri, values);
    }

    public void deletePackageData(Context context, String pkg) {
        mContentResolver.delete(mUri, ITEM_PKG + "=?",
                new String[] { "" + pkg });
    }

    public void deleteFingerData(Context context) {
        mContentResolver.delete(mUri, null, null);
    }

    public void deleteFingerData(Context context, int index) {
        mContentResolver.delete(mUri, ITEM_FINGER_INDEX + "=?",
                new String[] { "" + index });

    }

    public boolean isPkgProtecet(String pkg) {
        boolean protect = false;
        String[] projection = { ITEM_PKG, null };
        Cursor cu = mContentResolver.query(mUri, projection, null, null, null);
        if (cu != null) {
            while (cu.moveToNext()) {
                String tmp = cu.getString(cu.getColumnIndexOrThrow(ITEM_PKG));
                if (tmp != null && tmp.equals(pkg)) {
                    protect = true;
                    break;
                }
            }
            cu.close();
        }
        return protect;
    }

    public void deleteFingerData(Context context, String fingerName) {
        mContentResolver.delete(mUri, ITEM_FINGER_NAME + "=?",
                new String[] { "" + fingerName });
    }

    public void updateFingerData(Context context, String index,
            String fingerName, boolean modify) {
        ContentValues cv = new ContentValues();
        cv.put(ITEM_FINGER_NAME, fingerName);
        mContentResolver.update(mUri, cv, ITEM_FINGER_INDEX + "=?",
                new String[] { "" + index });
    }

    public void updateFingerData(Context context, String index,
            String fingerName) {
        updateFingerData(context, null, null, index, fingerName);
    }

    public void insertFingerIndexData(Context context, String index) {
        ContentValues values = new ContentValues();
        values.put(ITEM_FINGER_INDEX, index);
        mContentResolver.insert(mUri, values);
    }

    public void updateFingerData(Context context, String pkg, String label,
            String index, String fingerName) {
        ContentValues cv = new ContentValues();
        cv.put(ITEM_PKG, pkg);
        cv.put(ITEM_LABEL, label);
        cv.put(ITEM_FINGER_INDEX, index);
        cv.put(ITEM_FINGER_NAME, fingerName);
        mContentResolver.update(mUri, cv, ITEM_FINGER_INDEX + "=?",
                new String[] { "" + index });
    }

    /**
     * Method:queryContentProvider
     */
    private boolean queryContentProvider(String pkg, String fingerName) {
        boolean relate = false;
        String[] projection = { ITEM_PKG, ITEM_LABEL, ITEM_FINGER_INDEX,
                ITEM_FINGER_NAME };
        Cursor cu = mContentResolver.query(mUri, projection, null, null, null);
        if (cu != null) {
            while (cu.moveToNext()) {
                if (pkg.equals(cu.getString(0))
                        && ((fingerName == null) ? true : fingerName.equals(cu
                                .getString(3)))) {
                    relate = true;
                    break;
                }
            }
            cu.close();
        }
        return relate;
    }

    public boolean isFingerIndexInProvider(String index) {
        boolean exit = false;
        String[] projection = { ITEM_PKG, ITEM_LABEL, ITEM_FINGER_INDEX,
                ITEM_FINGER_NAME };
        Cursor cu = mContentResolver.query(mUri, projection, null, null, null);
        if (cu != null) {
            while (cu.moveToNext()) {
                if (index.equals(cu.getString(2))) {
                    exit = true;
                    break;
                }
            }
            cu.close();
        }
        return exit;
    }

    /**
     * Method:queryContentProvider
     */
    private String queryContentProvider(String str, int index1, int index2) {
        String tmp = null;
        String[] projection = { ITEM_PKG, ITEM_LABEL, ITEM_FINGER_INDEX,
                ITEM_FINGER_NAME };
        Cursor cu = mContentResolver.query(mUri, projection, null, null, null);
        if (cu != null) {
            while (cu.moveToNext()) {
                if (str.equals(cu.getString(index1))) {
                    tmp = cu.getString(index2);
                    break;
                }
            }
            cu.close();
        }
        return tmp;
    }

    /**
     * Method:queryContentProvider
     */
    private String queryContentProvider(int index, int index1, int index2) {
        String tmp = null;
        String[] projection = { ITEM_PKG, ITEM_LABEL, ITEM_FINGER_INDEX,
                ITEM_FINGER_NAME };
        Cursor cu = mContentResolver.query(mUri, projection, null, null, null);
        if (cu != null) {
            while (cu.moveToNext()) {
                if (index == cu.getInt(index1)) {
                    tmp = cu.getString(index2);
                    break;
                }
            }
            cu.close();
        }
        return tmp;
    }

    /* state:0-close,1-open */
    public void setSwitchState(String state) {
        ContentValues cv = new ContentValues();
        cv.put(ITEM_SWITCH_STATE, state);
        mContentResolver.update(mUri, cv, null, null);
    }

    public void setProtectState(String state) {
        ContentValues cv = new ContentValues();
        cv.put(ITEM_PROTECT_STATE, state);
        mContentResolver.update(mUri, cv, null, null);
    }

    public boolean getSwitchState() {
        String tmp = null;
        String[] projection = { ITEM_SWITCH_STATE };
        Cursor cu = mContentResolver.query(mUri, projection, null, null, null);
        if (cu != null) {
            while (cu.moveToNext()) {
                tmp = cu.getString(0);
                break;
            }
            cu.close();
        }
        if (tmp == null) {
            insertSwitchState("0");
            return false;
        }
        if (tmp.equals("0")) {
            return false;
        } else {
            return true;
        }
    }

    public boolean getProtectState() {
        String tmp = null;
        String[] projection = { ITEM_PROTECT_STATE };
        Cursor cu = mContentResolver.query(mUri, projection, null, null, null);
        if (cu != null) {
            while (cu.moveToNext()) {
                tmp = cu.getString(0);
                break;
            }
            cu.close();
        }
        if (tmp == null) {
            insertProtectState("0");
            return false;
        }
        if (tmp.equals("0")) {
            return false;
        } else {
            return true;
        }
    }

    public void insertSwitchState(String state) {
        ContentValues values = new ContentValues();
        values.put(ITEM_SWITCH_STATE, state);
        mContentResolver.insert(mUri, values);
    }

    public void insertProtectState(String state) {
        ContentValues values = new ContentValues();
        values.put(ITEM_PROTECT_STATE, state);
        mContentResolver.insert(mUri, values);
    }

}

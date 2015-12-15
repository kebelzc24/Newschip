package com.newschip.galaxy.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by LQ on 2015/12/10.
 */
public class ProviderHelper {
    private static final String CONTENT = "content://com.newschip.galaxy.GalaxyContentProvider/";

    //查询app是否在加锁列表
    public static boolean isAppProtected(Context context, String pkg) {
        ContentResolver cr = context.getContentResolver();
        boolean protect = false;
        String[] projection = {GalaxyContentProvider.ITEM_PACKAGE, GalaxyContentProvider.ITEM_APP_LABEL};
        Uri mUri = Uri.parse(CONTENT + GalaxyContentProvider.TABLE_PROTECTED_APP);
        Cursor cu = cr.query(mUri, projection, null, null, null);
        if (cu != null) {
            while (cu.moveToNext()) {
                String tmp = cu.getString(cu.getColumnIndexOrThrow(GalaxyContentProvider.ITEM_PACKAGE));
                if (tmp != null && tmp.equals(pkg)) {
                    protect = true;
                    break;
                }
            }
            cu.close();
        }
        return protect;
    }

    //加锁app
    public static void setAppProtected(Context context, String pkg, String label) {
        ContentResolver cr = context.getContentResolver();
        Uri mUri = Uri.parse(CONTENT + GalaxyContentProvider.TABLE_PROTECTED_APP);
        ContentValues values = new ContentValues();
        values.put(GalaxyContentProvider.ITEM_PACKAGE, pkg);
        values.put(GalaxyContentProvider.ITEM_APP_LABEL, label);
        cr.insert(mUri, values);
    }

    //删除加锁app
    public static void removeProtectedApp(Context context, String pkg) {
        ContentResolver cr = context.getContentResolver();
        String[] projection = {GalaxyContentProvider.ITEM_PACKAGE, GalaxyContentProvider.ITEM_APP_LABEL};
        Uri mUri = Uri.parse(CONTENT + GalaxyContentProvider.TABLE_PROTECTED_APP);
        cr.delete(mUri, GalaxyContentProvider.ITEM_PACKAGE + "=?",
                new String[]{"" + pkg});
    }

    //查询App是否有关联相关指纹（快速切换应用）
    public static boolean isAppRelateFinger(Context context, String pkg) {
        boolean relate = false;
        String[] projection = {GalaxyContentProvider.ITEM_PACKAGE, GalaxyContentProvider.ITEM_APP_LABEL, GalaxyContentProvider.ITEM_FINGER_INDEX};
        ContentResolver cr = context.getContentResolver();
        Uri mUri = Uri.parse(CONTENT + GalaxyContentProvider.TABLE_SWITCH_APP);
        Cursor cu = cr.query(mUri, projection, null, null, null);
        if (cu != null) {
            while (cu.moveToNext()) {
                if (pkg.equals(cu.getString(0))) {
                    relate = true;
                    break;
                }
            }
            cu.close();
        }
        return relate;
    }

    //查询指纹是否有关联相关app（快速切换应用）
    public static boolean isFingerRelateApp(Context context, String pkg, int index) {
        boolean relate = false;
        String[] projection = {GalaxyContentProvider.ITEM_PACKAGE, GalaxyContentProvider.ITEM_APP_LABEL, GalaxyContentProvider.ITEM_FINGER_INDEX};
        ContentResolver cr = context.getContentResolver();
        Uri mUri = Uri.parse(CONTENT + GalaxyContentProvider.TABLE_SWITCH_APP);
        Cursor cu = cr.query(mUri, projection, null, null, null);
        if (cu != null) {
            while (cu.moveToNext()) {
                if (pkg.equals(cu.getString(0)) && String.valueOf(index).equals(cu
                        .getString(2))) {
                    relate = true;
                    break;
                }
            }
            cu.close();
        }
        return relate;
    }

    /**
     * 通过指纹编号查找关联的应用包名 Method:getPackageNameWithFingerIndex arg:index指纹编号
     */
    public static String getPackageWithFingerIndex(Context context, int index) {
        // 指纹编号在数据库第3列，包名在数据库第1列
        String tmp = null;
        String[] projection = {GalaxyContentProvider.ITEM_PACKAGE, GalaxyContentProvider.ITEM_APP_LABEL, GalaxyContentProvider.ITEM_FINGER_INDEX};
        ContentResolver cr = context.getContentResolver();
        Uri mUri = Uri.parse(CONTENT + GalaxyContentProvider.TABLE_SWITCH_APP);
        Cursor cu = cr.query(mUri, projection, null, null, null);
        if (cu != null) {
            while (cu.moveToNext()) {
                if (String.valueOf(index).equals(cu.getString(2))) {
                    tmp = cu.getString(0);
                    break;
                }
            }
            cu.close();
        }
        return tmp;
    }

    public static String getAppNameWithFingerIndex(Context context, int index) {
        // 指纹编号在数据库第3列，包名在数据库第1列
        String tmp = null;
        String[] projection = {GalaxyContentProvider.ITEM_PACKAGE, GalaxyContentProvider.ITEM_APP_LABEL, GalaxyContentProvider.ITEM_FINGER_INDEX};
        ContentResolver cr = context.getContentResolver();
        Uri mUri = Uri.parse(CONTENT + GalaxyContentProvider.TABLE_SWITCH_APP);
        Cursor cu = cr.query(mUri, projection, null, null, null);
        if (cu != null) {
            while (cu.moveToNext()) {
                if (String.valueOf(index).equals(cu.getString(2))) {
                    tmp = cu.getString(1);
                    break;
                }
            }
            cu.close();
        }
        return tmp;
    }

    // 指纹编号在数据库第3列，包名在数据库第1列
    public static int getFingerIndexWithPackage(Context context, String pkg) {

        String tmp = null;
        String[] projection = {GalaxyContentProvider.ITEM_PACKAGE, GalaxyContentProvider.ITEM_APP_LABEL, GalaxyContentProvider.ITEM_FINGER_INDEX};
        ContentResolver cr = context.getContentResolver();
        Uri mUri = Uri.parse(CONTENT + GalaxyContentProvider.TABLE_SWITCH_APP);
        Cursor cu = cr.query(mUri, projection, null, null, null);
        if (cu != null) {
            while (cu.moveToNext()) {
                if (pkg.equals(cu.getString(0))) {
                    tmp = cu.getString(2);
                    break;
                }
            }
            cu.close();
        }
        return tmp == null ? -1 : Integer.parseInt(tmp);
    }

    public static void updateFingerData(Context context, int index) {
        updateFingerData(context, null, null, index);
    }


    public static void updateFingerData(Context context, String pkg, String label,
                                        int index) {
        ContentResolver cr = context.getContentResolver();
        Uri mUri = Uri.parse(CONTENT + GalaxyContentProvider.TABLE_SWITCH_APP);
        ContentValues cv = new ContentValues();
        cv.put(GalaxyContentProvider.ITEM_PACKAGE, pkg);
        cv.put(GalaxyContentProvider.ITEM_APP_LABEL, label);
//        cv.put(GalaxyContentProvider.ITEM_FINGER_INDEX, index);
        cr.update(mUri, cv, GalaxyContentProvider.ITEM_FINGER_INDEX + "=?",
                new String[]{index+""});
    }

    public static boolean isFingerIndexExit(Context context, int index) {
        boolean exit = false;
        String[] projection = {GalaxyContentProvider.ITEM_PACKAGE, GalaxyContentProvider.ITEM_APP_LABEL, GalaxyContentProvider.ITEM_FINGER_INDEX};
        ContentResolver cr = context.getContentResolver();
        Uri mUri = Uri.parse(CONTENT + GalaxyContentProvider.TABLE_SWITCH_APP);
        Cursor cu = cr.query(mUri, projection, null, null, null);
        if (cu != null) {
            while (cu.moveToNext()) {
                if (String.valueOf(index).equals(cu.getString(2))) {
                    exit = true;
                    break;
                }
            }
            cu.close();
        }
        return exit;
    }

    public static void insertFingerIndex(Context context, int index) {
        ContentResolver cr = context.getContentResolver();
        Uri mUri = Uri.parse(CONTENT + GalaxyContentProvider.TABLE_SWITCH_APP);
        ContentValues values = new ContentValues();
        values.put(GalaxyContentProvider.ITEM_FINGER_INDEX, String.valueOf(index));
        cr.insert(mUri, values);
    }

    /* state:0-close,1-open */
    public static void enableSwitchState(Context context, boolean enable) {
        String state = "0";
        if (enable) {
            state = "1";
        }
        ContentResolver cr = context.getContentResolver();
//        Uri mUri = ContentUris.withAppendedId(Uri.parse(CONTENT + GalaxyContentProvider.TABLE_STATE), 0);
        Uri mUri = Uri.parse(CONTENT + GalaxyContentProvider.TABLE_STATE);
        ContentValues cv = new ContentValues();
//        cv.put(GalaxyContentProvider.ITEM_STATE_PROTECT,isEnableProtectState(context)?"1":"0");
        cv.put(GalaxyContentProvider.ITEM_STATE_SWITCH, state);
        cr.update(mUri, cv, null, null);
    }

    public static void enableProtectState(Context context, boolean enable) {
        String state = "0";
        if (enable) {
            state = "1";
        }
        ContentResolver cr = context.getContentResolver();
//        Uri mUri = ContentUris.withAppendedId(Uri.parse(CONTENT + GalaxyContentProvider.TABLE_STATE), 1);
        Uri mUri = Uri.parse(CONTENT + GalaxyContentProvider.TABLE_STATE);
        ContentValues cv = new ContentValues();
        cv.put(GalaxyContentProvider.ITEM_STATE_PROTECT, state);
//        cv.put(GalaxyContentProvider.ITEM_STATE_SWITCH,isEnableSwitchState(context)?"1":"0");
        cr.update(mUri, cv, null, null);
    }

    public static boolean isEnableSwitchState(Context context) {
        String tmp = null;
        String[] projection = {GalaxyContentProvider.ITEM_STATE_PROTECT, GalaxyContentProvider.ITEM_STATE_SWITCH};
        ContentResolver cr = context.getContentResolver();
        Uri mUri = Uri.parse(CONTENT + GalaxyContentProvider.TABLE_STATE);
        Cursor cu = cr.query(mUri, projection, null, null, null);
        if (cu != null) {
            while (cu.moveToNext()) {
                tmp = cu.getString(1);
                break;
            }
            cu.close();
        }
        if (tmp == null) {
            initState(context, mUri);
            return false;
        }
        if (tmp.equals("0")) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isEnableProtectState(Context context) {
        String tmp = null;
        String[] projection = {GalaxyContentProvider.ITEM_STATE_PROTECT, GalaxyContentProvider.ITEM_STATE_SWITCH};
        ContentResolver cr = context.getContentResolver();
        Uri mUri = Uri.parse(CONTENT + GalaxyContentProvider.TABLE_STATE);
        Cursor cu = cr.query(mUri, projection, null, null, null);
        if (cu != null) {
            while (cu.moveToNext()) {
                tmp = cu.getString(0);
                break;
            }
            cu.close();
        }
        if (tmp == null) {
            initState(context, mUri);
            return false;
        }
        if (tmp.equals("0")) {
            return false;
        } else {
            return true;
        }
    }

    private static void initState(Context context, Uri uri) {
        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(GalaxyContentProvider.ITEM_STATE_SWITCH, "0");
        values.put(GalaxyContentProvider.ITEM_STATE_PROTECT, "0");
        cr.insert(uri, values);
    }

}

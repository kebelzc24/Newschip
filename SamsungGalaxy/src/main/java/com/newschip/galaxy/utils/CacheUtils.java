package com.newschip.galaxy.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * @author 邱英健
 * @项目名: fingerprint
 * @包名: com.newschip.fingerprint.utils
 * @描述 缓存处理
 */
public class CacheUtils {
    private final static String SP_NAME = "fingerprint";
    private static SharedPreferences mPreferences;

    private static SharedPreferences getSp(Context context) {
        if (mPreferences == null) {
            mPreferences = context.getSharedPreferences(SP_NAME,
                    Context.MODE_PRIVATE);
        }

        return mPreferences;
    }

    public static boolean getBoolean(Context context, String key) {
        SharedPreferences sp = getSp(context);
        return sp.getBoolean(key, false);
    }

    public static boolean getBoolean(Context context, String key,
                                     boolean defValue) {
        SharedPreferences sp = getSp(context);
        return sp.getBoolean(key, defValue);
    }

    public static void setBoolean(Context context, String key, boolean value) {
        SharedPreferences sp = getSp(context);
        Editor edit = sp.edit();// 获取编辑器
        edit.putBoolean(key, value);
        edit.commit();
    }
}

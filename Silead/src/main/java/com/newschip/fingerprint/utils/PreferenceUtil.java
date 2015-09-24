package com.newschip.fingerprint.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * 共享参数工具类
 * <p/>
 * 初始化时传一个参数是默认保存token的，，两个参数的，可以自己写文件名称；
 */
public class PreferenceUtil {

    private static final String PREFERENCE_NAME = "fingerprint";
    public static final String KEY_INDENTIGY = "key_indentify";

    private static PreferenceUtil preferenceUtil;

    private SharedPreferences shareditorPreferences;

    private Editor editor;
    private String mPname;

    public PreferenceUtil(Context context) {

        init(context);
    }

    /**
     * @param context 当前上下文
     * @param pName   文件名称
     */
    private PreferenceUtil(Context context, String pName) {
        this.mPname = pName;
        init(context, mPname);
    }

    public void init(Context context) {
        init(context, PREFERENCE_NAME);
    }

    public void init(Context context, String pName) {
        if (shareditorPreferences == null || editor == null) {
            try {
                shareditorPreferences = context.getSharedPreferences(pName, 0);
                editor = shareditorPreferences.edit();
            } catch (Exception e) {
            }
        }
    }

    public static PreferenceUtil getInstance(Context context) {

        return getInstance(context, null);
    }

    /**
     * @param context 当前上下文
     * @param pName   文件名称 传null默认是token
     */
    public static PreferenceUtil getInstance(Context context, String pName) {
        if (preferenceUtil == null) {
            preferenceUtil = new PreferenceUtil(context, pName);
        }
        return preferenceUtil;
    }

    public void saveLong(String key, long l) {
        editor.putLong(key, l);
        editor.commit();
    }

    public long getLong(String key, long defaultlong) {
        return shareditorPreferences.getLong(key, defaultlong);
    }

    public void saveBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean getBoolean(String key, boolean defaultboolean) {
        return shareditorPreferences.getBoolean(key, defaultboolean);
    }

    public void saveInt(String key, int value) {
        if (editor != null) {
            editor.putInt(key, value);
            editor.commit();
        }
    }

    public int getInt(String key, int defaultInt) {
        return shareditorPreferences.getInt(key, defaultInt);
    }

    public String getString(String key, String defaultInt) {
        if (shareditorPreferences == null) {
            return "";
        }
        return shareditorPreferences.getString(key, defaultInt);
    }

    public String getString(Context context, String key, String defaultValue) {
        if (shareditorPreferences == null || editor == null) {
            shareditorPreferences = context.getSharedPreferences(
                    PREFERENCE_NAME, 0);
            editor = shareditorPreferences.edit();
        }
        if (shareditorPreferences != null) {
            return shareditorPreferences.getString(key, defaultValue);
        }
        return defaultValue;
    }

    public void saveString(String key, String value) {
        if (editor != null) {
            editor.putString(key, value);
            editor.commit();
        } else {
            Log.e("TAG", "editor is null");
        }
    }

    public void remove(String key) {
        if (editor != null) {
            editor.remove(key);
            editor.commit();
        } else {
            Log.e("TAG", "editor is null");
        }
    }

    public void clear() {
        if (editor != null) {
            editor.clear();
            editor.commit();
        } else {
            Log.e("TAG", "editor is null");
        }
    }

    public void destroy() {
        shareditorPreferences = null;
        editor = null;
        preferenceUtil = null;
    }
}

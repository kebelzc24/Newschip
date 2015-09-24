package com.newschip.fingerprint.utils;

public class ConstantUtils {
    public final static String USER_ID = "system";
    public final static String TAG = "NewsChip:";

    //activity action
    public final static String ACTION_APPLISTACTIVITY = "newschip.action.AppListActivity";
    public final static String ACTION_CHOOSELOCKFINGERPRINT = "newschip.action.ChooseLockFingerprint";
    public final static String ACTION_HIDEAPPACTIVITY = "newschip.action.HideAppActivity";
    public final static String ACTION_FASTSWITCHAPPACTIVITY = "newschip.action.SwitchAppActivity";
    public final static String ACTION_MHIDDENFILELAYOUT = "newschip.action.AlbumeNewSetActivity";

    public final static String ACTION_CONFIRMFINGERACTIVITY = "newschip.action.ConfirmLockFingerprint";

    //密码相关
    /**
     * 初始设置密码
     */
    public static final int SETTING_PASSWORD = 0;
    /**
     * 确认密码
     */
    public static final int SURE_SETTING_PASSWORD = 2;
    /**
     * 验证登录密码
     */
    public static final int LOGIN_PASSWORD = 1;
    /**
     * SharedPreferences的文件名
     */
    public static final String PREF_PASSWORD = "password";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PASSWORD_HAS_SET = "password_has_set";
    public static final String KEY_QUESTION = "question";
    public static final String KEY_ANSWER = "answer";

}

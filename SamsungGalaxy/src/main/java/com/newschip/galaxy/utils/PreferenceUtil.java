package com.newschip.galaxy.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences 工具类<br>
 *
 * @author liuzhicang
 *
 */
public class PreferenceUtil {


    public static final String FILE_GALAXY = "galaxy";
    public static final String KEY_REGISTER = "register";
    public static final String KEY_PASWORD = "password";

    private static PreferenceUtil preferenceUtil;
    private String mPname;
    private SharedPreferences.Editor editor;
    private SharedPreferences shareditorPreferences;

    public PreferenceUtil(Context context) {

        init(context);
    }
    public void init(Context context) {
        init(context, FILE_GALAXY);
    }
    /**
     * @param context 当前上下文
     * @param pName 文件名称
     */
    private PreferenceUtil(Context context, String pName) {
        this.mPname = pName;
        init(context, mPname);
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
    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param context
     * @param key
     *            键值对的key
     * @param object
     *            键值对的值
     */
    public static void put(Context context,String file, String key, Object object) {

        SharedPreferences sp = context.getSharedPreferences(file,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param context
     * @param key
     * @param defaultObject
     *            不能为NULL,取什么类型的数据就应该是什么类型的变量
     * @return
     */
    public static Object get(Context context,String file, String key, Object defaultObject) {
        SharedPreferences sp = context.getSharedPreferences(file,
                Context.MODE_PRIVATE);
        if (defaultObject instanceof String) {
            String stringResult = sp.getString(key, (String) defaultObject);
            return stringResult;
        } else if (defaultObject instanceof Integer) {
            Integer integerResult = sp.getInt(key, (Integer) defaultObject);
            return integerResult;
        } else if (defaultObject instanceof Boolean) {
            Boolean booleanResult = sp.getBoolean(key, (Boolean) defaultObject);
            return booleanResult;
        } else if (defaultObject instanceof Float) {
            Float floatResult = sp.getFloat(key, (Float) defaultObject);
            return floatResult;
        } else if (defaultObject instanceof Long) {
            Long longResult = sp.getLong(key, (Long) defaultObject);
            return longResult;
        }

        return null;
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param context
     * @param key
     */
    public static void remove(Context context,String file, String key) {
        SharedPreferences sp = context.getSharedPreferences(file,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 清除所有数据
     *
     * @param context
     */
    public static void clear(Context context,String file) {
        SharedPreferences sp = context.getSharedPreferences(file,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 查询某个key是否已经存在
     *
     * @param context
     * @param key
     * @return
     */
    public static boolean contains(Context context,String file, String key) {
        SharedPreferences sp = context.getSharedPreferences(file,
                Context.MODE_PRIVATE);
        boolean result = sp.contains(key);

        return result;
    }

    /**
     * 返回所有的键值对
     *
     * @param context
     * @return
     */
    public static Map<String, ?> getAll(Context context,String file) {
        SharedPreferences sp = context.getSharedPreferences(file,
                Context.MODE_PRIVATE);
        return sp.getAll();
    }

    /**
     * 创建一个解决SharedPreferencesCompat.apply方法的一个兼容类
     *
     *
     */
    private static class SharedPreferencesCompat {
        private static final Method sApplyMethod = findApplyMethod();

        /**
         * 反射查找apply的方法
         *
         * @return
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        private static Method findApplyMethod() {
            try {
                Class clz = SharedPreferences.Editor.class;
                return clz.getMethod("apply");
            } catch (NoSuchMethodException e) {
            }

            return null;
        }

        /**
         * 如果找到则使用apply执行，否则使用commit
         *
         * @param editor
         */
        public static void apply(SharedPreferences.Editor editor) {
            try {
                if (sApplyMethod != null) {
                    sApplyMethod.invoke(editor);
                    return;
                }
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
            editor.commit();
        }
    }

    public void saveBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean getBoolean(String key, boolean defaultboolean) {
        return shareditorPreferences.getBoolean(key, defaultboolean);
    }
}
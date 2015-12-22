package com.newschip.galaxy.utils;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.AppOpsManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class PackageUtils {


    public static String getLabelWithPackgeName(Context context, String pkg) {
        PackageManager pm = context.getPackageManager();
        String name = null;
        try {
            name = pm.getApplicationLabel(
                    pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA))
                    .toString();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }

    public static Drawable getIconWithPackgeName(Context context, String pkg) {
        if (pkg == null)
            return null;
        PackageManager pm = context.getPackageManager();
        Drawable image = null;
        try {
            image = pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA)
                    .loadIcon(pm);

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return image;
    }

    public static void runApp(Context context, String pkg) {
        if (pkg == null) {
            return;
        }
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        if (pkg.equals(getTopRunningPkg(context))) {
            return;
        }

        final ArrayList<RecentTaskInfo> taskList = (ArrayList<RecentTaskInfo>) am
                .getRecentTasks(10, ActivityManager.RECENT_IGNORE_UNAVAILABLE);

        boolean isInList = false;
        RecentTaskInfo info = null;
        for (int i = 1; i < taskList.size(); i++) {
            RecentTaskInfo taskInfo = taskList.get(i);
            if (pkg.equals(taskInfo.baseIntent.getComponent().getPackageName())) {
                isInList = true;
                info = taskInfo;
                break;
            }
        }
        if (false/* isInList && (info != null && info.id != -1) */) {
            am.moveTaskToFront(info.persistentId,
                    ActivityManager.MOVE_TASK_WITH_HOME);
        } else {
            PackageInfo pi;
            try {
                pi = context.getPackageManager().getPackageInfo(pkg, 0);
                Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
                resolveIntent.setPackage(pi.packageName);
                PackageManager pManager = context.getPackageManager();
                List<ResolveInfo> apps = pManager.queryIntentActivities(
                        resolveIntent, 0);

                ResolveInfo ri = apps.iterator().next();
                if (ri != null) {
                    pkg = ri.activityInfo.packageName;
                    String className = ri.activityInfo.name;
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    ComponentName cn = new ComponentName(pkg, className);
                    intent.setComponent(cn);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                    context.startActivity(intent);
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getTopRunningPkg(Context context) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Service.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            // getRunningTasks no longer support in android L
            return getForegroundTask(context);
        } else {
             return am.getRunningTasks(1).get(0).topActivity.getPackageName();
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static String getForegroundTask(Context context) {
        String currentApp = "null";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }

        Log.e("adapter", "Current App in foreground is: " + currentApp);
        return currentApp;
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean needPermissionForBlocking(Context context){
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return  (mode != AppOpsManager.MODE_ALLOWED);
        } catch (NameNotFoundException e) {
            return true;
        }
    }

    public static void goToLauncher(Context context){
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}

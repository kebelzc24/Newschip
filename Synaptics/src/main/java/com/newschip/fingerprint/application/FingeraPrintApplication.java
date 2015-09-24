package com.newschip.fingerprint.application;

import android.app.Application;
import android.content.Context;

public class FingeraPrintApplication extends Application {

    private static FingeraPrintApplication mInstance;
    private Context mContext;

    public static FingeraPrintApplication getInstance() {
        if (mInstance == null) {
            mInstance = new FingeraPrintApplication();
        }
        return mInstance;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mInstance = this;

        CrashHandler mCrashHandler = CrashHandler.getInstance();
        mCrashHandler.init(getApplicationContext());
        Thread.currentThread().setUncaughtExceptionHandler(mCrashHandler);
    }

    @Override
    public void onLowMemory() {
        // TODO Auto-generated method stub

        // android.os.Process.killProcess(android.os.Process.myPid());
        super.onLowMemory();

    }
}

package com.newschip.galaxy.service;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.newschip.galaxy.R;
import com.newschip.galaxy.activity.MainActivity;
import com.newschip.galaxy.activity.PasswordActivity;
import com.newschip.galaxy.fingerprint.FingerPrint;
import com.newschip.galaxy.provider.ProviderHelper;
import com.newschip.galaxy.utils.PackageUtils;
import com.newschip.galaxy.utils.ScreenLockListener;
import com.newschip.galaxy.utils.ToastUtils;


public class WatchDogService extends Service implements ScreenLockListener.ScreenStateListener, FingerPrint.OnIndentifyFinishListener {

    private final String TAG = "AppProtectService";
    private Context mContext;

    private static boolean mSwitchState = false;
    private static boolean mProtectState = false;
    private static boolean mEasyHomeState = false;

    private static boolean mState = false;
    private KeyguardManager mKeygaurdMgr;


    private Uri mSwitchUri = Uri
            .parse("content://com.newschip.fingerprint.AppLockProvider/switch_state");
    private Uri mProtectUri = Uri
            .parse("content://com.newschip.fingerprint.AppLockProvider/protect_state");


    private static boolean mFlags = true;

    private String mLastPkg = "";

    private boolean mNoneCheck = false;

    private ScreenLockListener mScreenLockListener;

    private Thread mProtectThread;

    private Handler mHandler = new Handler();
    private boolean mIdentifying = false;
    private FingerPrint mFingerPrint;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mContext = this;

        mKeygaurdMgr = (KeyguardManager) mContext
                .getSystemService(Context.KEYGUARD_SERVICE);
        // mScreenLockListener = new ScreenLockListener(mContext);
        // mScreenLockListener.setScreenStateListener(this);
        getContentResolver().registerContentObserver(mSwitchUri, true,
                mContentObserver);
        getContentResolver().registerContentObserver(mProtectUri, true,
                mContentObserver);
        mFingerPrint = new FingerPrint(this);


    }


    private ContentObserver mContentObserver = new ContentObserver(
            new Handler()) {
        public void onChange(boolean selfChange) {
            mSwitchState = getSwitchState();
            mProtectState = getProtectState();
            mEasyHomeState = getEasyHomeState();
        }

    };

    private boolean getSwitchState() {
        mSwitchState = ProviderHelper.isEnableSwitchState(mContext);
        return mSwitchState;
    }

    private boolean getProtectState() {
        mProtectState = ProviderHelper.isEnableProtectState(mContext);
        return mProtectState;
    }

    private boolean getEasyHomeState() {
        mEasyHomeState = ProviderHelper.isEnableEasyHomeState(mContext);
        return mEasyHomeState;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Intent notificationIntent = new Intent(this, WatchDogService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        Notification notification = new Notification.Builder(mContext)
                .setAutoCancel(true)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getResources().getString(R.string.app_runing))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .build();
        startForeground(1, notification);

        getSwitchState();
        getProtectState();
        getEasyHomeState();
        if (mProtectThread == null) {
            mProtectThread = new Thread(mAppProtectRunnable);
            mProtectThread.start();
        }

        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        stopForeground(true);
        cancelIdentify();
    }

    private Runnable mAppProtectRunnable = new Runnable() {

        @Override
        public synchronized void run() {
            // TODO Auto-generated method stub
            // ToastUtils.show(mContext, "dog start ...");
            // Log.d(TAG, "mFlags = " + mFlags);
            while (mProtectState || mSwitchState || mEasyHomeState) {
                if (mFlags) {
                    String packageName = PackageUtils.getTopRunningPkg(mContext);
                    Log.e("kebelzc24", "packageName = " + packageName);
                    if (mProtectState) {
                        if (!mLastPkg.equals(mContext.getPackageName())) {
                            if (!mLastPkg.equals(packageName)
                                    && ProviderHelper.isAppProtected(mContext, packageName)) {
                                if (mNoneCheck) {
                                    mNoneCheck = false;
                                } else {
                                    mIdentifying = false;
                                    cancelIdentify();
                                    Log.d(TAG, "pkg = " + packageName);
                                    Log.d(TAG, "mLastPkg = " + mLastPkg);
                                    Intent intent = new Intent(
                                            WatchDogService.this,
                                            PasswordActivity.class);
                                    // 服务里面是没有任务栈的，所以要指定一个新的任务栈，不然是无法在服务里面启动activity的
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            | Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                                    intent.putExtra(PasswordActivity.EXTRAL_PACKAGE, packageName);
                                    startActivity(intent);
                                }
                            }
                        }

                    }

                    mLastPkg = packageName;
                    Log.d(TAG, "mLastPkg = " + mLastPkg);
                    if (mSwitchState || mEasyHomeState) {
                        if (packageName.equals(mContext.getPackageName())
                                || packageName.equals("com.android.settings")) {
                            mIdentifying = false;
                            cancelIdentify();
                        } else {
//                        if (mIdentifying == false) {
//                            mIdentifying = true;
                            startIdentify();
//                        }

                        }

                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    mIdentifying = false;
                    cancelIdentify();
                }

            }

        }
    };


    @Override
    public void onScreenOn() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onScreenOff() {
        // TODO Auto-generated method stub
        Log.d(TAG, "onScreenOff()");
         mFlags = false;
        cancelIdentify();

    }

    @Override
    public void onUserPresent() {
        // TODO Auto-generated method stub
        Log.d(TAG, "onUserPresent()");
        mFlags = true;
        // new Thread(mAppProtectRunnable).start();
    }

    private void startIdentify() {
        mFingerPrint.setmOnIndentifyFinishListener(this);
        mFingerPrint.startIdentify();
    }

    private void cancelIdentify() {
        mFingerPrint.cancelIdentify();
        mFingerPrint.setmOnIndentifyFinishListener(null);
    }

    @Override
    public void onIdentifyReady() {

    }

    @Override
    public void onIdentifyStart() {
        if (!PackageUtils.getTopRunningPkg(mContext).equals(getPackageName())) {
            //do easy home
            PackageUtils.goToLauncher(mContext);
        }
    }

    @Override
    public void onIdentifyFinish(int index) {
        if (index == 0) {
//fail
        } else {
            String pkg = ProviderHelper.getPackageWithFingerIndex(mContext, index);
            if (!TextUtils.isEmpty(pkg)) {
                mNoneCheck = true;
                mIdentifying = false;
                PackageUtils.runApp(mContext, pkg);
            }
        }
        mFingerPrint.startIdentify();
    }
}

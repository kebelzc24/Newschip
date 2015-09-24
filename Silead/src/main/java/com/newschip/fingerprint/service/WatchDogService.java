package com.newschip.fingerprint.service;

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
import android.util.Log;

import com.newschip.fingerprint.R;
import com.newschip.fingerprint.activity.ConfirmLockFingerprint;
import com.newschip.fingerprint.provider.ProviderHelper;
import com.newschip.fingerprint.receiver.ScreenLockListener;
import com.newschip.fingerprint.receiver.ScreenLockListener.ScreenStateListener;
import com.newschip.fingerprint.utils.ConstantUtils;
import com.newschip.fingerprint.utils.PackageUtils;
import com.newschip.fingerprint.utils.ToastUtils;
import com.newschip.fingerprint.utils.VibratorUtils;
import com.silead.fp.utils.FpControllerNative;
import com.silead.fp.utils.FpControllerNative.SLFpsvcIndex;

public class WatchDogService extends Service implements ScreenStateListener,
        FpControllerNative.OnIdentifyRspCB {

    private final String TAG =  "AppProtectService";
    private Context mContext;

    private static boolean mSwitchState = false;
    private static boolean mProtectState = false;
    private ProviderHelper mFingerProviderHelper;

    private static boolean mState = false;
    private ProviderHelper mProtectProviderHelper;
    private ProviderHelper mUsageProviderHelper;
    private final String TABLE_USAGE = "finger_useage";
    private final String TABLE_PROTECT = "app_protect";
    private KeyguardManager mKeygaurdMgr;


    private Uri mSwitchUri = Uri
            .parse("content://com.newschip.fingerprint.AppLockProvider/switch_state");
    private Uri mProtectUri = Uri
            .parse("content://com.newschip.fingerprint.AppLockProvider/protect_state");


    private static boolean mFlags = false;

    private Intent mIntent;
    private String mLastPkg = "";

    private boolean mNoneCheck = false;
    private String mNoneCheckPkg = "";
    public final String INDENNTIFY_FILE = "finger_print";
    private ProviderHelper mAppProtectProviderHelper;

    private ScreenLockListener mScreenLockListener;
    private static int mFingerIndex = 0;

    private Thread mProtectThread;

    private FpControllerNative mFpControllerNative;
    private String[] mForegroundPkgs;
    private Handler mHandler = new Handler();
    public static final int SHOW_PASSWORD_ACTIVITY_DELAY = 200;
    private boolean mIdentifying = false;
    private KeyguardManager mKeyguardManager;


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
        mFingerProviderHelper = new ProviderHelper(mContext, TABLE_USAGE);

        mKeygaurdMgr = (KeyguardManager) mContext
                .getSystemService(Context.KEYGUARD_SERVICE);
        // mScreenLockListener = new ScreenLockListener(mContext);
        // mScreenLockListener.setScreenStateListener(this);
        getContentResolver().registerContentObserver(mSwitchUri, true,
                mContentObserver);
        getContentResolver().registerContentObserver(mProtectUri, true,
                mContentObserver);
        mAppProtectProviderHelper = new ProviderHelper(mContext, TABLE_PROTECT);
        mUsageProviderHelper = new ProviderHelper(mContext,TABLE_USAGE);

        mFpControllerNative = FpControllerNative.getInstance();
        initFPSystem();


    }



    private ContentObserver mContentObserver = new ContentObserver(
            new Handler()) {
        public void onChange(boolean selfChange) {
            mSwitchState = getSwitchState();
            mProtectState = getProtectState();
        };
    };
    private boolean getSwitchState() {
        ProviderHelper helper = new ProviderHelper(mContext, "switch_state");
        mSwitchState = helper.getSwitchState();
        return mSwitchState;
    }

    private boolean getProtectState() {
        ProviderHelper helper = new ProviderHelper(mContext, "protect_state");
        mProtectState = helper.getProtectState();
        return mProtectState;
    }

    public int initFPSystem() {
        return mFpControllerNative.initFPSystem();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Intent notificationIntent = new Intent(this, WatchDogService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        Notification notification = new Notification.Builder(mContext)
                .setAutoCancel(true)
                .setContentTitle(getText(R.string.newchip))
                .setContentText(getResources().getString(R.string.app_runing))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.icon)
                .setWhen(System.currentTimeMillis())
                .build();
        startForeground(1, notification);

        getSwitchState();
        getProtectState();
        mFpControllerNative.registerIdentifyListener(this);
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
        mFpControllerNative.unregisterIdentifyListener(this);
//        mScreenLockListener.deleteScreenStateListener();
        cancelOperation();
    }

    private Runnable mAppProtectRunnable = new Runnable() {

        @Override
        public synchronized void run() {
            // TODO Auto-generated method stub
            // ToastUtils.show(mContext, "dog start ...");
            // Log.d(TAG, "mFlags = " + mFlags);
            while (mProtectState || mSwitchState) {
                String packageName = PackageUtils.getForegroundTask(mContext);
                if (mProtectState) {
                    if (!mLastPkg.equals(mContext.getPackageName())) {
                        if (!mLastPkg.equals(packageName)
                                && mAppProtectProviderHelper
                                .isPkgProtecet(packageName)) {
                            if (mNoneCheck) {
                                mNoneCheck = false;
                            } else {
                                Log.d(TAG, "pkg = " + packageName);
                                Log.d(TAG, "mLastPkg = " + mLastPkg);
                                Intent intent = new Intent(
                                        WatchDogService.this,
                                        ConfirmLockFingerprint.class);
                                // 服务里面是没有任务栈的，所以要指定一个新的任务栈，不然是无法在服务里面启动activity的
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        | Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                                intent.putExtra("packageName", packageName);
                                startActivity(intent);
                            }
                        }
                    }

                }

                mLastPkg = packageName;
                Log.d(TAG, "mLastPkg = " + mLastPkg);
                if (mSwitchState) {
                    if (packageName.equals(mContext.getPackageName())
                            || packageName.equals("com.android.settings")) {
                        cancelOperation();

                    } else {
                        initIndentify();
                    }

                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    };

    private int identifyCredentialREQ(int fpIndex) {
        Log.d(TAG, " identifyCredentialREQ 222  mIdentifying = " + mIdentifying);
        if (mIdentifying) {
            Log.d(TAG, " identifyCredentialREQ  mIdentifying before return");
            return -1;
        }
        if (!hasFingerEnabled()) {
            Log.d(TAG, " identifyCredentialREQ  has no Finger Enabled");
            return -1;
        }
        mIdentifying = true;
        return mFpControllerNative.identifyCredentialREQ(fpIndex);
    }

    private boolean hasFingerEnabled() {
        Log.d(TAG, "$$$$$$ hasFingerEnabled ");
        SLFpsvcIndex fpsvcIndex = mFpControllerNative.GetFpInfo();
        boolean hasEnabled = false;
        for (int i = 0; i < fpsvcIndex.max; i++) {
            if (fpsvcIndex.FPInfo[i].enable == 1) {
                hasEnabled = true;
                break;
            }
        }
        Log.d(TAG, "hasEnabled = " + hasEnabled);
        return hasEnabled;
    }

    @Override
    public void onScreenOn() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onScreenOff() {
        // TODO Auto-generated method stub
        Log.d(TAG, "onScreenOff()");
        // mFlags = false;
        cancelOperation();

    }

    @Override
    public void onUserPresent() {
        // TODO Auto-generated method stub
        Log.d(TAG, "onUserPresent()");
        ToastUtils.show(mContext, "onUserPresent()");
        mFlags = true;
        // new Thread(mAppProtectRunnable).start();
    }

    private void initIndentify() {
        identifyCredentialREQ(0);
    }

    private void cancelOperation() {
        if (mIdentifying) {

            mFpControllerNative.FpCancelOperation();
            mIdentifying = false;
        }
    }

    @Override
    public void onIdentifyRsp(int index, int result, int fingerid) {
        Log.d(TAG, "$$$$ onIdentifyRsp $$$$ index = " + index + "result:"
                + result + "fingerid:" + fingerid + "result" + result);
        mIdentifying = false;
//        if (mKeyguardManager.inKeyguardRestrictedInputMode()) {
//            Log.d(TAG, " onIdentifyRsp keyguard is showing return !!! ");
//            return;
//        }
        if (result == FpControllerNative.IDENTIFY_SUCCESS) {
            VibratorUtils.Vibrate(mContext);
            if (!PackageUtils.getForegroundTask(mContext).equals(
                    mContext.getPackageName())) {
                Log.e(TAG,"FpControllerNative.IDENTIFY_SUCCESS...");
                mNoneCheck = true;
                cancelOperation();
                initIndentify();
                mNoneCheckPkg = mUsageProviderHelper
                        .getPackageNameWithFingerIndex(fingerid);
                PackageUtils.runApp(mContext, mNoneCheckPkg);
            }
        } else if (result == FpControllerNative.IDENTIFY_ERR_MATCH) {
            mHandler.post(new Runnable() {
                public void run() {
                    Log.d(TAG, "post to main thread ");
                }
            });
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    Log.d(TAG, "postDelayed before identifyCredentialREQ ");
                    identifyCredentialREQ(0);
                }
            }, 1500);

        }
    }

}

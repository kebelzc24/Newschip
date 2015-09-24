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
import android.os.Message;
import android.util.Log;

import com.newschip.fingerprint.FingerprintApiWrapper;
import com.newschip.fingerprint.FingerprintApiWrapper.FingerprintEvent;
import com.newschip.fingerprint.R;
import com.newschip.fingerprint.activity.ChooseLockFingerprint;
import com.newschip.fingerprint.activity.ConfirmLockFingerprint;
import com.newschip.fingerprint.provider.ProviderHelper;
import com.newschip.fingerprint.receiver.ScreenLockListener;
import com.newschip.fingerprint.receiver.ScreenLockListener.ScreenStateListener;
import com.newschip.fingerprint.utils.ConstantUtils;
import com.newschip.fingerprint.utils.PackageUtils;
import com.newschip.fingerprint.utils.ToastUtils;
import com.newschip.fingerprint.utils.VibratorUtils;
import com.validity.fingerprint.IdentifyResult;

public class WatchDogService extends Service implements
        FingerprintApiWrapper.EventListener, ScreenStateListener {

    private final String TAG = ConstantUtils.TAG + "AppProtectService";
    private Context mContext;

    private static boolean mSwitchState = false;
    private static boolean mProtectState = false;
    private ProviderHelper mFingerProviderHelper;
    private final String TABLE_USAGE = "finger_useage";
    private final String TABLE_PROTECT = "app_protect";
    private static FingerprintApiWrapper mFingerprint = null;
    private KeyguardManager mKeygaurdMgr;
    private IndentifyThread mIndentifyThread;

    private final int INDENTIFY_START = 90000;
    private final int INDENTIFY_STOP = 90001;

    // private static boolean mFlags = false;

    private Intent mIntent;
    private String mLastPkg = "";

    private boolean mNoneCheck = false;
    private String mNoneCheckPkg = "";
    public final String INDENNTIFY_FILE = "finger_print";
    private ProviderHelper mAppProtectProviderHelper;

    private Uri mSwitchUri = Uri
            .parse("content://com.newschip.fingerprint.AppLockProvider/switch_state");
    private Uri mProtectUri = Uri
            .parse("content://com.newschip.fingerprint.AppLockProvider/protect_state");
    private ScreenLockListener mScreenLockListener;
    private static int mFingerIndex = 0;

    private Thread mProtectThread;

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

        getSwitchState();
        getProtectState();
        // initFingerprint(mContext, 0);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
        Notification notification = new Notification.Builder(mContext)
                .setContentTitle("Newschip")
                .setContentText("应用保护中")
                .setSmallIcon(R.mipmap.icon)
                .setContentIntent(pintent)
                .build();
//        notification.notify();
//        Notification notification = new Notification(R.mipmap.icon,
//                "Newschip", System.currentTimeMillis());
//        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
//        notification.setLatestEventInfo(this, "Newschip", "应用保护中", pintent);

        // 让该service前台运行，避免手机休眠时系统自动杀掉该服务
        // 如果 id 为 0 ，那么状态栏的 notification 将不会显示。
        startForeground(1, notification);
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
        Log.d(TAG, "dog destroy..");
        stopForeground(true);

        if (mScreenLockListener != null) {
            mScreenLockListener.deleteScreenStateListener();
        }
        cancelFingerprint();
    }

    private Runnable mAppProtectRunnable = new Runnable() {

        @Override
        public synchronized void run() {
            // TODO Auto-generated method stub
            // ToastUtils.show(mContext, "dog start ...");
            // Log.d(TAG, "mFlags = " + mFlags);
            while (mProtectState || mSwitchState) {
                String packageName = PackageUtils.getTopRunningPkg(mContext);
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
                        cancelFingerprint();

                    } else {

                        initFingerprint(mContext, 1500);
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

    @Override
    public void onEvent(FingerprintEvent event) {
        // TODO Auto-generated method stub
        if (event == null) {
            Log.e(TAG, "Invalid event data.");
            return;
        }

        // log(eventdata.toString());
        mHandler.sendMessage(Message.obtain(mHandler, event.eventId, event));
    }

    private void initFingerprint(Context context, int delay) {
        if (mSwitchState) {
            startIdentifyThread(delay);
        }

    }

    private void cancelFingerprint() {
        if (mFingerprint != null) {
            // mFingerprint.cleanUp();
            mFingerprint.cancel();
            mFingerprint = null;
        }
        if (mIndentifyThread != null) {
            mIndentifyThread.stop();
        }
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
            case INDENTIFY_START:
                initFingerprint(mContext, 0);
                break;
            case INDENTIFY_STOP:
                cancelFingerprint();
                break;
            case FingerprintApiWrapper.VCS_EVT_VERIFY_COMPLETED:
            case FingerprintApiWrapper.VCS_EVT_IDENTIFY_COMPLETED:
                FingerprintEvent event = (FingerprintEvent) msg.obj;
                if (event.eventData != null
                        && (event.eventData instanceof com.validity.fingerprint.IdentifyResult)) {
                    IdentifyResult ir = (IdentifyResult) event.eventData;
                    mFingerIndex = ir.fingerIndex;
                    Log.d(TAG, "IdentifyResult index = " + ir.fingerIndex);
                }
                break;

            // Verify success?
            case FingerprintApiWrapper.VCS_EVT_VERIFY_SUCCESS:
            case FingerprintApiWrapper.VCS_EVT_IDENTIFY_SUCCESS:
                // mHeaderText.setText(R.string.verification_success);
                Log.d(TAG, "VCS_EVT_VERIFY_SUCCESS");

                VibratorUtils.Vibrate(mContext);
                mIndentifyThread.runing = false;
                initFingerprint(mContext, 0);
                if (!PackageUtils.getTopRunningPkg(mContext).equals(
                        mContext.getPackageName())) {
                    mNoneCheck = true;
                    mNoneCheckPkg = mFingerProviderHelper
                            .getPackageNameWithFingerIndex(mFingerIndex);
                    PackageUtils.runApp(mContext, mNoneCheckPkg);
                }
                break;
            case FingerprintApiWrapper.VCS_EVT_VERIFY_FAILED:
            case FingerprintApiWrapper.VCS_EVT_IDENTIFY_FAILED:
                int resultCode = FingerprintApiWrapper.VCS_RESULT_FAILED;
                boolean reverify = false,
                reInit = false;
                if (msg.obj != null) {
                    FingerprintEvent fpEvent = (FingerprintEvent) msg.obj;
                    if (fpEvent != null && fpEvent.eventData instanceof Integer) {
                        resultCode = (Integer) fpEvent.eventData;
                    } else {
                        Log.w(TAG,
                                "handleMessage()::Result flag is not an Integer");
                    }
                } else {
                    Log.w(TAG,
                            "handleMessage()::Additional event data not available");
                }
                Log.d(TAG, "fail resultCode = " + resultCode);

                switch (resultCode) {

                // verify cancelled?
                case FingerprintApiWrapper.VCS_RESULT_OPERATION_CANCELED:
                    return;

                    // sensor removed?
                case FingerprintApiWrapper.VCS_RESULT_SENSOR_IS_REMOVED:
                    reverify = true;
                    reInit = true;

                    // verify failed?
                default:
                    Log.e(TAG, "Verification Failed " + resultCode);

                    reverify = true;
                    break;
                }

                doCleanup();
                initFingerprint(mContext, 1500);

                break;

            default:
                break;
            } // switch

        } // handleMessage
    }; // Handler

    Runnable mIndentifyRunnable = new Runnable() {
        @Override
        public void run() {
            initFingerprint(mContext, 1500);
        }
    };

    private class IndentifyThread implements Runnable {

        private Thread thread;
        private int delay;
        private boolean runing = false;

        public synchronized void start(int delay) {
            this.delay = delay;
            runing = true;
            thread = new Thread(this, "thread");
            thread.start();
        }

        public synchronized void stop() {
            runing = false;
            if (thread == null) {
                return;
            }
            thread.interrupt();
            thread = null;
        }

        @Override
        public synchronized void run() {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // TODO Auto-generated method stub
            Log.d(TAG, "startIdentify()...");
            startIdentify();
        }

    }

    private void startIdentifyThread(int delay) {
        if (mIndentifyThread == null) {
            mIndentifyThread = new IndentifyThread();
        }
        if (!mIndentifyThread.runing) {
            mIndentifyThread.start(delay);
        }
    }

    private void startIdentify() {
        if (mKeygaurdMgr.inKeyguardRestrictedInputMode()) {
            mHandler.sendMessage(Message.obtain(mHandler, INDENTIFY_STOP));
        } else {
            if (mFingerprint == null) {
                mFingerprint = new FingerprintApiWrapper(mContext, this);
            }
            if (mFingerprint != null) {
                mFingerprint.identify(ConstantUtils.USER_ID);
            }
        }
    }

    /**
     * Cleanup finger wrapper instance
     */
    private void doCleanup() {
        if (mFingerprint != null) {
            mFingerprint.cleanUp();
            mFingerprint = null;
        }
        if (mIndentifyThread != null) {
            mIndentifyThread.stop();
        }
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
        cancelFingerprint();

    }

    @Override
    public void onUserPresent() {
        // TODO Auto-generated method stub
        Log.d(TAG, "onUserPresent()");
        ToastUtils.show(mContext, "onUserPresent()");
        // new Thread(mAppProtectRunnable).start();
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

}

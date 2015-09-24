package com.newschip.fingerprint.activity;

import java.io.File;
import java.util.concurrent.Semaphore;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.newschip.fingerprint.FingerprintApiWrapper;
import com.newschip.fingerprint.FingerprintApiWrapper.FingerprintEvent;
import com.newschip.fingerprint.FingerprintConfigReader;
import com.newschip.fingerprint.R;
import com.newschip.fingerprint.service.WatchDogService;
import com.newschip.fingerprint.utils.ConstantUtils;
import com.newschip.fingerprint.utils.PackageUtils;
import com.newschip.fingerprint.utils.VibratorUtils;

// Confirm fingerprint before allowing user to change lock settings
public class ConfirmLockFingerprint extends PasswordActivity implements 
                                                        FingerprintApiWrapper.EventListener {

    private static final String TAG = "ConfirmLockFingerprint";
    private static final boolean DBG = true;
    private static final String KEY_NUM_WRONG_ATTEMPTS = "num_wrong_attempts";
    private static final int CONFIRM_EXISTING_REQUEST = 100;

    private PowerManager mPowerMgr;
    private KeyguardManager mKeygaurdMgr;

    private ImageView mImageView;
    private Animation mTextBlinkAnimation;

    private Context mContext;
    private static FingerprintApiWrapper mFingerprint = null;
    private static Thread mThread, mCancelThread;
    private Semaphore mLock = new Semaphore(1);
    private Handler mDelayHandler = new Handler();

    private static int mTotalFailedPatternAttempts = 0;
    private CountDownTimer mCountdownTimer = null;
    private String mInstructions = null;
    private boolean mCloseOnPause = true, mIsPaused = false;
    private boolean mDisableHome = false, mDisableBack = false;
    private boolean mDisableMenu = false, mDisableSearch = false;
    private boolean mHapticFeedbackEnable = true;
    private int mSysHapticFeedback = 0;
    private String FINGER_PLACE_OR_SWIPE_LABEL = "Swipe to confirm";
    private String FINGER_LIFT_LABEL = "Please lift your finger";
    private static final int MSG_API_ERROR  = 501;
    private static final int MSG_WAIT       = 502;
    
    private Intent mIntent;
    private static boolean hasServiceStarted = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        log("onCreate()");
//        setContentView(R.layout.confirm_lock_fingerprint);
        mIntent = getIntent();


        if(!hasServiceStarted){
            startService(this);
            hasServiceStarted = false;
        }
        // used to determine if the screen is on OR keyguard is up
        mPowerMgr = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mKeygaurdMgr = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);

        // retrieve number of failed attempts
        if (savedInstanceState != null) {
            mTotalFailedPatternAttempts = savedInstanceState.getInt(KEY_NUM_WRONG_ATTEMPTS);
        }


        init();
    }

    @Override
    protected void onResume() {
        log("onResume()");

        super.onResume();

        checkKeyguardModeAndResume();

    }

    @Override
    protected void onPause() {
        log("onPause()");

        super.onPause();

        mIsPaused = true;

        //Enable HOME while pausing 
        setWindowType(WindowManager.LayoutParams.TYPE_APPLICATION);

        //Restore haptic feedback setting
        if (!mHapticFeedbackEnable && mSysHapticFeedback != 0) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.HAPTIC_FEEDBACK_ENABLED, mSysHapticFeedback);
        }

        stopVerify();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState()!");
        // deliberately not calling super since we are managing this in full
        outState.putInt(KEY_NUM_WRONG_ATTEMPTS, mTotalFailedPatternAttempts);
    }

    @Override
    protected void onDestroy() {
        log("onDestroy()");

        Thread cleanUpThread = new Thread( new Runnable() {
            public void run() {
                if (mFingerprint != null) {
                    mFingerprint.cleanUp();
                    mFingerprint = null;
                }
                mLock.release();
                log("onDestroy() done");
            }
        });

        try {
            mLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        cleanUpThread.start();

        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            setResult(resultCode);
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        boolean interceptKey = false;

        if (keyCode == KeyEvent.KEYCODE_HOME) {
            interceptKey = mDisableHome;
        } else if  (keyCode == KeyEvent.KEYCODE_BACK) {
            interceptKey = mDisableBack;
        } else if  (keyCode == KeyEvent.KEYCODE_MENU) {
            interceptKey = mDisableMenu;
        } else if  (keyCode == KeyEvent.KEYCODE_SEARCH) {
            interceptKey = mDisableSearch;
        }

        if (interceptKey) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onAttachedToWindow() {

        setWindowType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);

        super.onAttachedToWindow();
    }


    /**
     * Initialize UI elements, labels, read UI configurations, etc
     */
    private void init() {

//        mAppIconImage.setVisibility(View.VISIBLE);
//        mAppIconImage.setImageDrawable(PackageUtils.getIconWithPackgeName(mContext, mIntent.getStringExtra("packageName")));
        //Initialize blink animation
        mTextBlinkAnimation = new AlphaAnimation(0.3f, 1.0f);
        mTextBlinkAnimation.setDuration(600);
        mTextBlinkAnimation.setInterpolator(new AccelerateInterpolator());
        mTextBlinkAnimation.setRepeatCount(Animation.INFINITE);
        mTextBlinkAnimation.setRepeatMode(Animation.REVERSE);

        //Fetch string labels
        FINGER_PLACE_OR_SWIPE_LABEL = getString(R.string.swipe_to_confirm);
        FINGER_LIFT_LABEL   = getString(R.string.lift_your_finger);

        //Read UI configuration settings
        FingerprintConfigReader.ConfigData configData = FingerprintConfigReader.getData();
        if (configData != null) {

            //Read button flags if any need to be disabled 
            mDisableHome = configData.disableButtons.home;
            mDisableBack = configData.disableButtons.back;
            mDisableMenu = configData.disableButtons.menu;
            mDisableSearch = configData.disableButtons.search;                
            mHapticFeedbackEnable = configData.hapticFeedback;

            FINGER_PLACE_OR_SWIPE_LABEL = (configData.fingerPlaceOrSwipeLabel != null)
                                    ? configData.fingerPlaceOrSwipeLabel + " to confirm"
                                    : getString(R.string.swipe_to_confirm);

            FINGER_LIFT_LABEL = (configData.fingerLiftLabel != null)
                                    ? configData.fingerLiftLabel
                                    : getString(R.string.lift_your_finger);

        }

        mImageView = (ImageView) findViewById(R.id.fingerprint);

        //Show static image from /data folder if present, else show default image
        File mFile = new File(Environment.getDataDirectory(), "fingerprint.png");
        if(mFile.exists()) {
            Uri uri = Uri.fromFile(mFile);
            mImageView.setImageURI(uri);
        }

    }

    /**
     * Change window type if HOME button needs to be disabled.
     * It only works for android older than ics.
     */
    private void setWindowType(int windowType) {
        //if HOME button not configured to disable, do nothing
        if (!mDisableHome) return;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            getWindow().setType(windowType);
        }
    }

    /**
     * Wait until phone is unlocked if keyguard is active, and then start fingerprint verification.
     */
    private void checkKeyguardModeAndResume() {

        mCloseOnPause = true;

        mIsPaused = false;

        new Thread(new Runnable() {

            public void run() {

                boolean screenLocked = false;

                while(mKeygaurdMgr.inKeyguardRestrictedInputMode()) {

                    screenLocked = true;

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //Exit the thread if app goes to pause
                    if (mIsPaused) {
                        log("returning from resume due to pause...");
                        return;
                    }
                }

                final boolean isScreenLocked = screenLocked;
                runOnUiThread(new Runnable() {
                    public void run() {

                        // disable home key
                        setWindowType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);

                        // if locked out, we update the countdown display

                        // finish last mThread before continuing

                        //if resumed from unlock screen, give some room for cleanup
                        if (isScreenLocked) {
                        }

                        //Disable haptic feedback
                        if (!mHapticFeedbackEnable) {
                            mSysHapticFeedback = Settings.System.getInt(getContentResolver(),
                                Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);
                            if (mSysHapticFeedback != 0) {
                                Settings.System.putInt(getContentResolver(),
                                    Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);
                            }
                        }

                        //if resumed from unlock screen, give some room to screen unlock cleanup
//                        if (isScreenLocked) {
//                            startVerifyWithDelay();
//                        } else {
//                            startVerify();
//                        }
                        startVerifyWithDelay();

                    }
                });

            } // run()
        }).start();

    }




    private void stopWaitTimer() {
        if (mCountdownTimer != null) {
            mCountdownTimer.cancel();
        }
        mTotalFailedPatternAttempts = 0;
    }

    /**
     * Handle fingerprint callback events and take appropriate action.
     */
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            String statusMsg = "";
            Log.d(TAG, "msg.what = "+msg.what);
            switch(msg.what) {

                // during initialization
                case MSG_API_ERROR:
                case FingerprintApiWrapper.VCS_RESULT_FAILED:
                    statusMsg = getString(R.string.sensor_problem_use_alt_method);
                    break;

                case FingerprintApiWrapper.VCS_EVT_SENSOR_READY_FOR_USE:
                case MSG_WAIT:
                    statusMsg = getString(R.string.please_wait);
                    break;

                case FingerprintApiWrapper.VCS_EVT_SENSOR_FINGERPRINT_CAPTURE_START:
                    statusMsg = FINGER_PLACE_OR_SWIPE_LABEL;
                    break;

                case FingerprintApiWrapper.VCS_EVT_FINGER_SETTLED:
                    statusMsg = FINGER_LIFT_LABEL;
                    if (mHapticFeedbackEnable) {
                        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
                        if (rootView != null) {
                            rootView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                        }
                    }
                    break;

                case FingerprintApiWrapper.VCS_EVT_EIV_FINGERPRINT_CAPTURED:
                    statusMsg = getString(R.string.fingerprint_captured) ;
                    break;

                case FingerprintApiWrapper.VCS_EVT_VERIFY_SUCCESS:
                case FingerprintApiWrapper.VCS_EVT_IDENTIFY_SUCCESS:
                    Log.d(TAG, "VCS_EVT_IDENTIFY_SUCCESS");
                    VibratorUtils.Vibrate(mContext);
//                    mResultText.setText(getResources().getString(R.string.verification_success));
//                    statusMsg = getString(R.string.verification_success);
                    onIdentifySuccess();
                    break;

                case FingerprintApiWrapper.VCS_EVT_VERIFY_FAILED:
                case FingerprintApiWrapper.VCS_EVT_IDENTIFY_FAILED:
//                    mResultText.setText(getResources().getString(R.string.verification_failed));
                    int resultCode = FingerprintApiWrapper.VCS_RESULT_FAILED;
                    boolean reverify = false, reInit = false;
                    if (msg.obj != null) {
                        FingerprintEvent fpEvent = (FingerprintEvent) msg.obj;
                        if (fpEvent != null && fpEvent.eventData instanceof Integer) {
                            resultCode = (Integer) fpEvent.eventData;
                        } else {
                            Log.w(TAG, "handleMessage()::Result flag is not an Integer");
                        }
                    } else {
                        Log.w(TAG, "handleMessage()::Additional event data not available");
                    }

                    switch (resultCode) {

                    //verify cancelled?
                    case FingerprintApiWrapper.VCS_RESULT_OPERATION_CANCELED:
                        return;

                    //sensor removed?
                    case FingerprintApiWrapper.VCS_RESULT_SENSOR_IS_REMOVED:
                        statusMsg = getString(R.string.sensor_removed);
                        reverify = true; reInit = true;

                    //verify failed?
                    default:
                        Log.e(TAG, "Verification Failed " +  resultCode);
                        statusMsg = getString(R.string.verification_failed);

                        mTotalFailedPatternAttempts++;
                        reverify = true;
                        break;
                    }

                    if (reverify) {
                        if (reInit) doCleanup();
                        startVerifyWithDelay();
                    }

                    break;

                default:
                    log("handleMessage() -unhandled event: " + msg.what);
                    break;
            }   // switch

            //update UI
            if (!statusMsg.equals("")) {
            }

        }   // handleMessage
    };  // Handler

    /**
     * Set result and finish the activity on successful user verification.
     */
    private void onIdentifySuccess() {
        mTotalFailedPatternAttempts = 0;
        setResult(RESULT_OK);
        String pkg = mIntent.getStringExtra("packageName");
        Log.d(TAG, "pkg = "+pkg);
        if(pkg != null){
            PackageUtils.runApp(mContext, pkg);
        } else {
            startActivity(new Intent(ConfirmLockFingerprint.this,MainActivity.class));
        }
        finish();
    }

    /**
     * Start or stop animating instruction text
     */
    private void showAnimations(boolean visible) {


    }

    /**
     * Log a debug message.
     */
    private void log(String message) {

        if(!DBG) return;

        Log.i(TAG, message);
    }

    //Fingerprint API calls

    /**
     * Start verifying the user with fingerprint.
     */
    private void startVerify() {

        if(mPowerMgr.isScreenOn()) {

            log("startVerify()");

            // create new thread for fingerprint enrolling
            mThread = new Thread( new Runnable() {
                public void run() {

                //Create Fingerprint API wrapper instance
                if (mFingerprint == null) {

                    log("Create Fingerprint API instance...");

                    mFingerprint = new FingerprintApiWrapper(ConfirmLockFingerprint.this, 
                            ConfirmLockFingerprint.this);
                }

                //Call identify API
                int result = mFingerprint.identify(ConstantUtils.USER_ID);

                if (result == FingerprintApiWrapper.VCS_RESULT_OK) {
                    mHandler.sendMessage(Message.obtain(mHandler, MSG_WAIT));
                } else if (result == FingerprintApiWrapper.VCS_RESULT_ALREADY_INPROGRESS) { //do nothing
                    Log.w(TAG, "identify() call ignored! Operation already in progress!");
                } else {
                    Log.e(TAG, "identify() failed!");
                    mHandler.sendMessage(Message.obtain(mHandler, MSG_API_ERROR));
                }

                mLock.release();

                } // run()
            });

            try {
                mLock.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mThread.start();

        }
    }

    /**
     * Start user verification with a delay of 1500 milli seconds.
     * This is used to reinitate verification process after a verification failure or
     * on immediate unlock of phone.
     */
    private void startVerifyWithDelay() {
        log("startVerifyWithDelay()");
        mDelayHandler.removeCallbacksAndMessages(null);
        mDelayHandler.postDelayed(new Runnable() {
            public void run() {
                startVerify();
             }
        }, 1500);
    }

    /**
     * Cancel on going user verification operation.
     */
    private void stopVerify() {

        log("stopVerify()");

        mDelayHandler.removeCallbacksAndMessages(null);

        mCancelThread = new Thread( new Runnable() {

            public void run() {

                //stop failed attempts timer if already running
                stopWaitTimer();

                //Stop delay timer if running to update status message

                if (mFingerprint != null) {
                    mFingerprint.cancel();
                }

                mLock.release();

                if (mCloseOnPause) {
                    setResult(RESULT_CANCELED);
                    finish();
                }

            } // run()
        });

        try {
            mLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mCancelThread.start();

    }

    /**
     * Cleanup finger wrapper instance
     */
    private void doCleanup() {

        log("doCleanup()");

        try {
            mLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mFingerprint != null) {
            mFingerprint.cleanUp();
            mFingerprint = null;
        }

        mLock.release();
    }

    /** Callback method */
    @Override
    public void onEvent(FingerprintEvent eventdata) {
        if (eventdata !=  null) {
            mHandler.sendMessage(Message.obtain(mHandler, eventdata.eventId, eventdata));
        } else {
            Log.e(TAG, "Invalid event data.");
        }
    }

    private void startService(Context context){
//        Intent intent = new Intent(context, WatchDogService.class);
//        context.startService(intent);
    }
}

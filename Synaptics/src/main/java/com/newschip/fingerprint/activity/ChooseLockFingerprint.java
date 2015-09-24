package com.newschip.fingerprint.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.AdapterView.OnItemClickListener;

import com.newschip.fingerprint.FingerprintApiWrapper;
import com.newschip.fingerprint.FingerprintApiWrapper.EnrollCaptureStatus;
import com.newschip.fingerprint.FingerprintApiWrapper.FingerprintBitmap;
import com.newschip.fingerprint.FingerprintApiWrapper.FingerprintEvent;
import com.newschip.fingerprint.FingerprintConfigReader;
import com.newschip.fingerprint.R;
import com.newschip.fingerprint.dialog.SweetAlertDialog;
import com.newschip.fingerprint.dialog.SweetAlertDialog.OnSweetClickListener;
import com.newschip.fingerprint.provider.ProviderHelper;
import com.newschip.fingerprint.splash.SplashActivity;
import com.newschip.fingerprint.utils.ConstantUtils;
import com.newschip.fingerprint.utils.ToastUtils;

// Setup the fingerprint security
public class ChooseLockFingerprint extends BaseActivity implements
        OnClickListener, FingerprintApiWrapper.EventListener {

    private static final String TAG = "ChooseLockFingerprint";
    private static final boolean DBG = true;
    private static int mFpMask = 0;
    private String mDeleteFingerIndex;
    private int mPosition;

    private enum Stage {
        FingerprintMenu, EnrollFingerprint, PracticeMode
    }

    private static final String CONFIRM_CREDENTIALS = "confirm_credentials";
    private static final int MIN_PASSWORD_LENGTH = 4;

    private DevicePolicyManager mDPM;
    private PowerManager mPowerMgr;
    private KeyguardManager mKeygaurdMgr;

    private Button mAlternateButton, mNextButton;
    private PopupWindow mPopupWindow;
    private AlertDialog mAlertDialog;
    private TextView mHeaderText, mEnrollPercentage, mStatusText;
    private ImageView mFingerprintImg, mSwipeStatus;
    private VideoView mVideoView;
    private ProgressBar mEnrollProgress;
    private Animation mTextBlinkAnimation, mFadeInAnimation;
    private Context mContext;

    private static FingerprintApiWrapper mFingerprint = null;
    private Semaphore mLock = new Semaphore(1);
    private static Thread mThread;
    private FingerprintConfigReader.ConfigData mConfigData = null;
    private MenuInitTask mMenuInitTask;
    private CountDownTimer mCountdownTimer = null;
    private boolean mHapticFeedbackEnable = true;
    private int mSysHapticFeedback = 0;
    private boolean mGetFpImagesStarted = false;
    private static int mFpIndex = 1;
    private ListView mListView;
    private SimpleAdapter mListAdapter;

    private static final int MSG_API_ERROR = 501;
    private static final int MSG_WAIT = 502;
    private ArrayList<HashMap<String, Object>> mFingerList = new ArrayList<HashMap<String, Object>>();
    private static final String ITEM_NAME = "itemName";
    private static final String ITEM_FINGER = "itemFinger";

    private Stage mUiStage = Stage.FingerprintMenu;
    private int mPreOnResumeQuality = (int) DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED; // lock
                                                                                              // used
                                                                                              // preOnResume
    private int mOnResumeQuality = (int) DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED; // lock
                                                                                           // used
                                                                                           // onResume

    private String FINGER_PLACE_OR_SWIPE_LABEL = "Swipe to enroll";
    private String FINGER_LIFT_LABEL = "Lift your finger";
    private String FINGER_GENERIC_ACTION_LABEL = "Please swipe";

    private ProviderHelper mStateHelper;
    private ProviderHelper mProviderHelper;
    public final String TABLE_STATE = "state";
    public final String TABLE_USAGE = "finger_useage";

    private LinearLayout mEnrolFingerPrintLayout;
    private LinearLayout mRemoveFingerPrintLayout;
    private LinearLayout mHelpLayout;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        // used to determine if the screen is on OR keyguard is up
        mPowerMgr = (PowerManager) mContext
                .getSystemService(Context.POWER_SERVICE);
        mKeygaurdMgr = (KeyguardManager) mContext
                .getSystemService(Context.KEYGUARD_SERVICE);
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mStateHelper = new ProviderHelper(mContext, TABLE_STATE);
        mProviderHelper = new ProviderHelper(mContext, TABLE_USAGE);

        loadValidityConfig();

        initAnimations();

        initViews(mUiStage);
    }

    private void startEnroll() {

        if (mPowerMgr.isScreenOn()) {

            // create new thread for fingerprint enrolling
            mThread = new Thread(new Runnable() {
                public void run() {

                    initValidityLib();

                    // Enroll fingerprint
                    log("start enroll user!");

                    // Invoke enroll API
                    int result = mFingerprint.enroll(ConstantUtils.USER_ID,
                            mFpIndex,
                            FingerprintApiWrapper.VCS_ENROLL_MODE_REENROLL);
                    if (result != FingerprintApiWrapper.VCS_RESULT_OK) {
                        Log.e(TAG, "enroll() failed, fpIndex=" + mFpIndex);
                        mHandler.sendMessage(Message.obtain(mHandler,
                                MSG_API_ERROR));
                    } else {
                        log("enroll() success, fpIndex=" + mFpIndex);
                        mHandler.sendMessage(Message.obtain(mHandler, MSG_WAIT));
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

    private void cancel() {

        mThread = new Thread(new Runnable() {

            public void run() {

                if (mFingerprint != null) {
                    mFingerprint.cancel();
                    log("Cancel complete!");
                } else {
                    Log.e(TAG, "cancel()::fingerprint object does not exists!");
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

    private void getFingerprintImage() {

        if (mPowerMgr.isScreenOn()) {

            // create new thread for fingerprint enrolling
            mThread = new Thread(new Runnable() {

                public void run() {

                    initValidityLib();

                    int result = mFingerprint.getFingerprintImage();
                    int msg = (result == FingerprintApiWrapper.VCS_RESULT_OK) ? MSG_WAIT
                            : MSG_API_ERROR;

                    mHandler.sendMessage(Message.obtain(mHandler, msg));

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

    @Override
    protected int getLayoutView() {
        return R.layout.choose_lock_fingerprint_menu;
    }

    private void initViews(Stage stage) {
        switch (stage) {
        case FingerprintMenu:
            setContentView(R.layout.choose_lock_fingerprint_menu);
            setCommonTitleBarTitle(R.string.manager_finger_print);

            mEnrolFingerPrintLayout = (LinearLayout) findViewById(R.id.ll_enroll_fingerprint);
            mRemoveFingerPrintLayout = (LinearLayout) findViewById(R.id.ll_remove_fingerprint);
            mEnrolFingerPrintLayout.setOnClickListener(this);
            // mRemoveFingerPrintLayout.setOnClickListener(this);
            mHelpLayout = (LinearLayout) findViewById(R.id.ll_help);
            mHelpLayout.setOnClickListener(this);
            mListView = (ListView) findViewById(R.id.listview);

            break;

        case EnrollFingerprint:
            // Read configuration file
            loadValidityConfig();

            setContentView(R.layout.choose_lock_fingerprint_enroll);

            mHeaderText = (TextView) findViewById(R.id.header_text);
            mHeaderText.startAnimation(mTextBlinkAnimation);

            mEnrollPercentage = (TextView) findViewById(R.id.enroll_percentage);
            mSwipeStatus = (ImageView) findViewById(R.id.swipe_status);
            mEnrollProgress = (ProgressBar) findViewById(R.id.enroll_progress);

            mFingerprintImg = (ImageView) findViewById(R.id.fingerprint);
            mFingerprintImg.setVisibility(View.VISIBLE);
            // mFingerprintImg.startAnimation(mTextBlinkAnimation);

            mNextButton = (Button) findViewById(R.id.next_button);

            initSensorView();

            break;

        case PracticeMode:

            loadValidityConfig();

            setContentView(R.layout.finger_swipe_practice);

            mHeaderText = (TextView) findViewById(R.id.header_text);

            mNextButton = (Button) findViewById(R.id.next_button);

            mSwipeStatus = (ImageView) findViewById(R.id.swipe_status);
            mStatusText = (TextView) findViewById(R.id.status_text);

            mFingerprintImg = (ImageView) findViewById(R.id.fingerprint);
            File fpImgfile = new File(Environment.getDataDirectory(),
                    "fingerprint.png");
            if (fpImgfile.exists()) {
                Uri uri = Uri.fromFile(fpImgfile);
                mFingerprintImg.setImageURI(uri);
            }
            mFingerprintImg.setVisibility(View.VISIBLE);

            initSensorView();

            break;
        }
    }

    private void updateStage(Stage stage) {
        mUiStage = stage;
        updateUi();
    }

    private void updateUi() {

        switch (mUiStage) {
        case FingerprintMenu:

            setTitle(getText(R.string.fingerprint_options));

            // Show remove options if fingers already enrolled
            mMenuInitTask = new MenuInitTask();
            mMenuInitTask.execute(this);
            break;

        case EnrollFingerprint:
            setTitle(getText(R.string.enroll_fingerprint));

            showAnimations(false);
            setStatusText(getString(R.string.please_wait));// Banner text
            mNextButton.setEnabled(false); // Retry button
            mNextButton.setText(R.string.retry); // Enable upon failed attempt

            // reset flashing bar
            if (mSwipeStatus != null) {
                mSwipeStatus.setImageResource(R.mipmap.led_swipe_none);
            }

            // reset progress bar
            if (mEnrollProgress != null) {
                Rect bounds = mEnrollProgress.getProgressDrawable().getBounds();
                mEnrollProgress.setProgressDrawable(getResources().getDrawable(
                        R.drawable.enroll_progress_bar));
                mEnrollProgress.getProgressDrawable().setBounds(bounds);
                mEnrollProgress.setProgress(0);
            }

            // reset %ge text
            if (mEnrollPercentage != null) {
                mEnrollPercentage.setText("0%");
            }

            // Disable Home key
            setWindowType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);

            // start enrollment
            startEnroll();

            break;

        case PracticeMode:

            setTitle(R.string.practice_mode);

            mGetFpImagesStarted = false;

            showAnimations(false);

            if (mVideoView != null && mVideoView.isShown()) {
                setStatusText(getString(R.string.watch_video));
            } else {
                setStatusText(getString(R.string.please_wait));
                getFingerprintImage();
            }

            // Disable Home key
            setWindowType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);

            break;
        }
    }

    protected void onResume() {
        Log.i(TAG, "onResume()");
        super.onResume();

        // Disable haptic feedback
        if (!mHapticFeedbackEnable) {
            mSysHapticFeedback = Settings.System.getInt(getContentResolver(),
                    Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);
            if (mSysHapticFeedback != 0) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);
            }
        }
        updateStage(mUiStage);
    }

    protected void onPause() {
        Log.i(TAG, "onPause()");

        if (mUiStage == Stage.EnrollFingerprint
                || mUiStage == Stage.PracticeMode) {

            // Cancel fingerprint operation
            cancel();

            // back to menu screen
            initViews(Stage.FingerprintMenu);
            updateStage(Stage.FingerprintMenu);
        }

        // Cancel pending tasks
        if (mMenuInitTask != null) {
            mMenuInitTask.cancel();
        }

        if (mCountdownTimer != null) {
            mCountdownTimer.cancel();
        }

        dismissPopupMessage();

        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }

        // Restore haptic feedback setting
        if (!mHapticFeedbackEnable && mSysHapticFeedback != 0) {
            Settings.System
                    .putInt(getContentResolver(),
                            Settings.System.HAPTIC_FEEDBACK_ENABLED,
                            mSysHapticFeedback);
        }

        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();

        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;

        try {
            mLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {

            if (mFingerprint != null) {
                mFingerprint.cleanUp();
                mFingerprint = null;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        boolean interceptKey = false;

        if ((mUiStage == Stage.EnrollFingerprint || mUiStage == Stage.PracticeMode)
                && mConfigData != null) {
            if (keyCode == KeyEvent.KEYCODE_HOME) {
                interceptKey = mConfigData.disableButtons.home;
            } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                interceptKey = mConfigData.disableButtons.back;
            } else if (keyCode == KeyEvent.KEYCODE_MENU) {
                interceptKey = mConfigData.disableButtons.menu;
            } else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
                interceptKey = mConfigData.disableButtons.search;
            }
        }

        if (interceptKey) {
            Log.i(TAG, "***onKeyDown() ignoring key press event***");
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Change window type if HOME button needs to be disabled. It only works for
     * android older than ics.
     */
    private void setWindowType(int windowType) {
        // if HOME button not configured to disable, do nothing
        if (mConfigData == null)
            return;

        if (!mConfigData.disableButtons.home)
            return;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            getWindow().setType(windowType);
        }
    }

    public void onClick(View v) {
        int minLength = mDPM.getPasswordMinimumLength(null);
        if (minLength < MIN_PASSWORD_LENGTH)
            minLength = MIN_PASSWORD_LENGTH;
        int maxLength;

        switch (v.getId()) {
        case R.id.ll_enroll_fingerprint:
            if (mFpIndex > 10) { // All ten fingers enrolled
                popupMessage(getString(R.string.all_fingers_enrolled), null);
                return;
            }

            if (mConfigData != null && mConfigData.practiceMode) { // Practice
                                                                   // Mode
                initViews(Stage.PracticeMode);
                updateStage(Stage.PracticeMode);
            } else { // Enroll Fingerprint
                initViews(Stage.EnrollFingerprint);
                updateStage(Stage.EnrollFingerprint);
            }
            break;
        case R.id.ll_help:
            startActivity(new Intent(this, SplashActivity.class));
            break;
        case R.id.ll_remove_fingerprint:
            if (mFpIndex > 1) { // only allow IF fingerprints were enrolled
                // do a popup are you sure yes/no, handle answers onClick
                // popupRemoveFingerprints();
                showAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE);
            }
            break;
        case R.id.alternate_button:
            break;
        case R.id.cancel_button:
            cancel();
            // back to menu screen
            initViews(Stage.FingerprintMenu);
            updateStage(Stage.FingerprintMenu);
            break;
        case R.id.next_button:
            switch (mUiStage) {
            case FingerprintMenu:
                // set fingerprint as security and exit
                finish();
                break;
            case EnrollFingerprint:
                // reset header text and start enroll
                updateUi();
                break;
            case PracticeMode:
                // cancel();
                try {
                    mLock.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mFingerprint != null) {
                    mFingerprint.cancel();
                    log("Cancel complete!");
                } else {
                    Log.e(TAG, "cancel()::fingerprint object does not exists!");
                }
                mLock.release();

                initViews(Stage.EnrollFingerprint);
                updateStage(Stage.EnrollFingerprint);
                break;
            }
            break;

        /* popupRemoveFingerprints() */
        case R.id.yes_button:
            mPopupWindow.dismiss();
            removeFingerprints();
            initViews(Stage.FingerprintMenu);
            updateStage(Stage.FingerprintMenu);
            break;
        case R.id.no_button:
            // don't do anything
            mPopupWindow.dismiss();
            break;
        }
    }

    private int getNextFingerIndex() {
        int fpIndex = 1;
        final int fingermask = mFingerprint
                .getEnrolledFingerList(ConstantUtils.USER_ID);
        if (fingermask != -1) {
            int i = 1;
            for (i = 1; i <= 10; i++) {
                if (!(((fingermask >>> i) & 1) != 0)) {
                    if (fpIndex < i)
                        fpIndex = i;
                    break;
                }
            }
            // If all 10 fingers enrolled
            if (i > 10 && fpIndex == 1) {
                fpIndex = 11;
            }
        }
        Log.i(TAG, "Fingermask: " + fingermask + ", Next finger to enroll: "
                + fpIndex);
        return fpIndex;
    }

    private void removeFingerprints() {
        initValidityLib();
        try {
            mLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int result = mFingerprint.removeEnrolledFinger(ConstantUtils.USER_ID,
                11);
        if (result == FingerprintApiWrapper.VCS_RESULT_OK) {
            mStateHelper.setSwitchState("0");
            mProviderHelper.deleteFingerData(mContext);
            mFpIndex = 1;
            log("Fingerprints removed");
        }
        mLock.release();
    }

    private void removeFingerprints(String index) {
        initValidityLib();
        try {
            mLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int result = mFingerprint.removeEnrolledFinger(ConstantUtils.USER_ID,
                Integer.valueOf(index));
        if (result == FingerprintApiWrapper.VCS_RESULT_OK) {
            mProviderHelper.deleteFingerData(mContext, Integer.valueOf(index));
            mFpIndex = mFpIndex - 1;
            log("Fingerprints removed");
        }
        mLock.release();
    }

    private void popupRemoveFingerprints() {
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mPopupView = inflater.inflate(
                R.layout.choose_lock_fingerprint_pop, null, false);
        mPopupWindow = new PopupWindow(mPopupView, 400, 500, true);

        findViewById(R.id.spacer_bottom).post(new Runnable() {
            public void run() {
                mPopupWindow.showAtLocation(findViewById(R.id.spacer_bottom),
                        Gravity.CENTER, 0, 200);
            }
        });
    }

    private void dismissPopupMessage() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    private void popupMessage(final String message, final String title) {

        if (!mPowerMgr.isScreenOn()) {
            Log.e(TAG, "popupMessage():: Screen is off! message:" + message);
            return;
        }

        dismissPopupMessage();

        mAlertDialog = new AlertDialog.Builder(ChooseLockFingerprint.this)
                .setMessage(message)
                .setPositiveButton(R.string.dlg_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                mAlertDialog.dismiss();
                            }
                        }).create();
        if (title != null && !title.equals("")) {
            mAlertDialog.setTitle(title);
        }
        mAlertDialog.show();
    }

    /** Callback implementation */
    @Override
    public void onEvent(FingerprintEvent eventdata) {

        if (eventdata == null) {
            Log.e(TAG, "onEventsCB()::Invalid event data.");
            return;
        }

        mHandler.sendMessage(Message.obtain(mHandler, eventdata.eventId,
                eventdata));
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (!(mUiStage == Stage.EnrollFingerprint || mUiStage == Stage.PracticeMode)) {
                return;
            }

            String statusMsg = "", toastMsg = "";
            String enroll_failed = getString(R.string.enrollment_failed);
            FingerprintEvent event_data = null;
            Log.d(TAG, "msg.what = " + msg.what);
            switch (msg.what) {

            // during initialization
            case MSG_API_ERROR:
            case FingerprintApiWrapper.VCS_RESULT_FAILED:
                statusMsg = getString(R.string.error);
                toastMsg = getString(R.string.please_try_again);
                break;

            case MSG_WAIT:
            case FingerprintApiWrapper.VCS_EVT_SENSOR_READY_FOR_USE:
                statusMsg = getString(R.string.please_wait);
                break;

            case FingerprintApiWrapper.VCS_EVT_SENSOR_FINGERPRINT_CAPTURE_START:
            case FingerprintApiWrapper.VCS_EVT_ENROLL_NEXT_CAPTURE_START:
                if (mUiStage == Stage.PracticeMode) {
                    statusMsg = FINGER_GENERIC_ACTION_LABEL;
                } else {
                    statusMsg = FINGER_PLACE_OR_SWIPE_LABEL;
                }
                break;

            case FingerprintApiWrapper.VCS_EVT_FINGER_SETTLED:
                statusMsg = FINGER_LIFT_LABEL;
                if (mHapticFeedbackEnable) {
                    View rootView = getWindow().getDecorView().findViewById(
                            android.R.id.content);
                    if (rootView != null)
                        rootView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                }
                break;

            case FingerprintApiWrapper.VCS_EVT_ENROLL_CAPTURE_STATUS:
                statusMsg = getString(R.string.fingerprint_captured);

                event_data = (FingerprintEvent) msg.obj;
                if (event_data == null) {
                    Log.e(TAG, "Invalid event data");
                    return;
                }

                EnrollCaptureStatus enrollStatus = mFingerprint
                        .getEnrollStatus(event_data.eventData);
                if (enrollStatus == null) {
                    Log.e(TAG, "Invalid enroll status object");
                    return;
                }

                // flashing LED
                if (mSwipeStatus != null) {
                    int led = (enrollStatus.templateResult == 0) ? R.mipmap.led_swipe_good
                            : R.mipmap.led_swipe_bad;
                    mSwipeStatus.setImageResource(led);
                    mSwipeStatus.startAnimation(mFadeInAnimation);
                }

                // enrollment progress
                Log.i(TAG, "Enroll progress:" + enrollStatus.progress);
                if (enrollStatus.templateResult == 0) { // good swipe
                    if (mEnrollProgress != null) {
                        mEnrollProgress.setProgress(enrollStatus.progress);
                    }
                    if (mEnrollPercentage != null) {
                        mEnrollPercentage.setText(enrollStatus.progress + "%");
                    }
                } else { // bad swipe
                    showBadSwipe(mEnrollProgress.getProgress());
                }

                break;

            case FingerprintApiWrapper.VCS_EVT_EIV_FINGERPRINT_CAPTURED:
                if (mUiStage == Stage.PracticeMode) {
                    statusMsg = getString(R.string.fingerprint_captured);
                    log("Fingerprint captured");

                    event_data = (FingerprintEvent) msg.obj;
                    if (event_data == null) {
                        Log.e(TAG, "Invalid event data");
                        return;
                    }

                    FingerprintBitmap vcsFp = mFingerprint
                            .getFingerprint(event_data.eventData);
                    if (vcsFp == null) {
                        Log.e(TAG, "Invalid fingerprint object");
                        return;
                    }

                    if (!(vcsFp.fingerprint instanceof Bitmap)) {
                        Log.e(TAG, "Invalid fingerprint bitmap");
                        return;
                    }

                    final Bitmap fp = (Bitmap) vcsFp.fingerprint;
                    final int imageQlty = vcsFp.quality;
                    runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                if (mFingerprintImg != null) {

                                    // flashing LED
                                    if (mSwipeStatus != null) {
                                        int led = (imageQlty == FingerprintApiWrapper.VCS_IMAGE_QUALITY_GOOD) ? R.mipmap.led_swipe_good
                                                : R.mipmap.led_swipe_bad;
                                        mSwipeStatus.setImageResource(led);
                                        mSwipeStatus.setAnimation(null);
                                        mSwipeStatus
                                                .setVisibility(View.VISIBLE);
                                    }

                                    // stop playback and hide video view
                                    if (mVideoView != null) {
                                        if (mVideoView.isPlaying()) {
                                            mVideoView.stopPlayback();
                                        }
                                        mVideoView.setVisibility(View.GONE);
                                    }

                                    // Show fingerprint
                                    mFingerprintImg.setImageBitmap(fp);
                                    mFingerprintImg.setVisibility(View.VISIBLE);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Decoding fingerprint failed!");
                                e.printStackTrace();
                            }

                            showImageQualityFeedback(imageQlty);

                        }
                    });

                    return;
                }
                break;

            case FingerprintApiWrapper.VCS_EVT_ENROLL_SUCCESS:
                mFpIndex++;
                onEnrollmentSuccess();
                return;
                // break;

            case FingerprintApiWrapper.VCS_EVT_ENROLL_FAILED:
                int opResult = FingerprintApiWrapper.VCS_RESULT_FAILED;
                if (msg.obj != null) {
                    FingerprintEvent fpEvent = (FingerprintEvent) msg.obj;
                    if (fpEvent != null && fpEvent.eventData instanceof Integer) {
                        opResult = (Integer) fpEvent.eventData;
                    } else {
                        Log.w(TAG,
                                "handleMessage()::Result flag is not an Integer");
                    }
                } else {
                    Log.w(TAG,
                            "handleMessage()::Additional event data not available");
                }

                statusMsg = enroll_failed;

                switch (opResult) {

                // verify cancelled?
                case FingerprintApiWrapper.VCS_RESULT_OPERATION_CANCELED:
                    mNextButton.setEnabled(true);
                    break;

                // sensor removed?
                case FingerprintApiWrapper.VCS_RESULT_SENSOR_IS_REMOVED:
                    toastMsg = getString(R.string.sensor_removed);
                    break;

                // sensor not found?
                case FingerprintApiWrapper.VCS_RESULT_SENSOR_NOT_FOUND:
                    toastMsg = getString(R.string.sensor_not_found);
                    break;

                // verify failed?
                case FingerprintApiWrapper.VCS_RESULT_USER_FINGER_ALREADY_ENROLLED:
                    toastMsg = getString(R.string.finger_previously_enrolled);
                    mNextButton.setEnabled(true);
                    break;

                case FingerprintApiWrapper.VCS_RESULT_TOO_MANY_BAD_SWIPES:
                    toastMsg = getString(R.string.too_many_bad_swipes);
                    mNextButton.setEnabled(true);
                    break;

                case FingerprintApiWrapper.VCS_RESULT_MATCHER_ADD_IMAGE_FAILED:
                    toastMsg = getString(R.string.mather_add_image_failed);
                    mNextButton.setEnabled(true);
                    break;

                case FingerprintApiWrapper.VCS_RESULT_USER_DOESNT_EXIST:
                default:
                    toastMsg = statusMsg;
                    mNextButton.setEnabled(true);
                    break;
                } // switch(eventdata.opResult)
                break; // case VCS_EVT_ENROLL_COMPLETED

            default:
                log("handleMessage() -unhandled event: " + msg.what);
                break;
            } // switch(msg.what)

            // update UI
            if (!statusMsg.equals("")) {
                setStatusText(statusMsg);
                showAnimations(statusMsg.equals(FINGER_PLACE_OR_SWIPE_LABEL)
                        || statusMsg.equals(FINGER_GENERIC_ACTION_LABEL));
            }

            if (!toastMsg.equals(""))
                Toast.makeText(mContext, toastMsg, Toast.LENGTH_SHORT).show();

            if (enroll_failed.equals(statusMsg)) {
                int progress = mEnrollProgress.getProgress();
                Rect bounds = mEnrollProgress.getProgressDrawable().getBounds();
                LayerDrawable drawableProgressbar = (LayerDrawable) getResources()
                        .getDrawable(R.drawable.enroll_progress_bar);
                Drawable drawable = drawableProgressbar.getDrawable(4);
                drawableProgressbar.setDrawableByLayerId(android.R.id.progress,
                        drawable);
                mEnrollProgress.setProgressDrawable(drawableProgressbar);
                mEnrollProgress.getProgressDrawable().setBounds(bounds);
                mEnrollProgress.setProgress(--progress);
            }

        } // handleMessage
    }; // Handler

    private void showAnimations(boolean visible) {

        if (mHeaderText != null) {
            if (visible) {
                mHeaderText.startAnimation(mTextBlinkAnimation);
            } else {
                mHeaderText.setAnimation(null);
            }
        }

    }

    private void initAnimations() {
        mTextBlinkAnimation = new AlphaAnimation(0.3f, 1.0f);
        mTextBlinkAnimation.setDuration(600);
        mTextBlinkAnimation.setInterpolator(new AccelerateInterpolator());
        mTextBlinkAnimation.setRepeatCount(Animation.INFINITE);
        mTextBlinkAnimation.setRepeatMode(Animation.REVERSE);

        mFadeInAnimation = AnimationUtils.loadAnimation(
                ChooseLockFingerprint.this, android.R.anim.fade_in);
        mFadeInAnimation.setDuration(750);
        mFadeInAnimation.setRepeatCount(2);
        mFadeInAnimation.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(final Animation arg0) {
            }

            public void onAnimationRepeat(final Animation arg0) {
            }

            public void onAnimationEnd(final Animation arg0) {
                if (mSwipeStatus != null) {
                    mSwipeStatus.setAnimation(null);
                    mSwipeStatus.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void showBadSwipe(final int progress) {
        final int totalTime = 1000;
        mCountdownTimer = new CountDownTimer(totalTime, 50) {
            int sProgress = 0;
            final int halfTime = totalTime / 2;

            @Override
            public void onTick(long millisUntilFinished) {
                if (mPowerMgr.isScreenOn()) {
                    if (millisUntilFinished < halfTime) {
                        sProgress = progress + (int) (millisUntilFinished) * 25
                                / halfTime;
                    } else {
                        sProgress = progress
                                + (int) (totalTime - millisUntilFinished) * 25
                                / halfTime;
                    }
                    mEnrollProgress.setSecondaryProgress(sProgress);
                }
            }

            @Override
            public void onFinish() {
                if (mPowerMgr.isScreenOn()) {
                    mEnrollProgress.setSecondaryProgress(0);
                }
            }
        }.start();
    }

    private void onEnrollmentSuccess() {

        String prompt = getString(R.string.enrollment_success);
        Log.i(TAG, "prompt:" + prompt);
        Toast.makeText(mContext, prompt, Toast.LENGTH_LONG).show();
        setStatusText(getString(R.string.enrollment_success));
        showAlertDialog(mContext, SweetAlertDialog.RENAME_TYPE);
        // finish();

        // //Change to next screen
        // mHandler.postDelayed(new Runnable() {
        // public void run() {
        // initViews(Stage.SetAlternateUnlock);
        // updateStage(Stage.SetAlternateUnlock);
        // }
        // }, 1500);
    }

    private void log(String message) {
        if (!DBG)
            return;
        Log.i(TAG, message);
    }

    private void initValidityLib() {
        if (mFingerprint == null) {
            // create instance of fingerprint wrapper
            mFingerprint = new FingerprintApiWrapper(mContext, this);
        }
    }

    private void loadValidityConfig() {
        Log.i(TAG, "loadValidityConfig()");
        if (mConfigData == null) {
            mConfigData = FingerprintConfigReader.getData();
        }

        if (mConfigData != null) {
            mHapticFeedbackEnable = mConfigData.hapticFeedback;
        }

        if (mConfigData != null && mConfigData.fingerPlaceOrSwipeLabel != null) {
            FINGER_PLACE_OR_SWIPE_LABEL = mConfigData.fingerPlaceOrSwipeLabel
                    + " to enroll";
        } else {
            FINGER_PLACE_OR_SWIPE_LABEL = getString(R.string.place_or_swipe_to_enroll);
        }

        if (mConfigData != null && mConfigData.fingerLiftLabel != null) {
            FINGER_LIFT_LABEL = mConfigData.fingerLiftLabel;
        } else {
            getString(R.string.lift_your_finger);
        }

        if (mConfigData != null && mConfigData.fingerActionGenericLabel != null) {
            FINGER_GENERIC_ACTION_LABEL = mConfigData.fingerActionGenericLabel;
        } else {
            FINGER_GENERIC_ACTION_LABEL = getString(R.string.please_place_or_swipe);
        }
    }

    private void initSensorView() {

        if (mConfigData == null) {
            Log.e(TAG, "initSensorView()::Configuration data is null");
            return;
        }

        if (mConfigData.showVideo) {
            showSwipeTechniqueVideo();
        }

        if (mHeaderText != null) {
            mHeaderText.bringToFront();
        }

    }

    private void showSwipeTechniqueVideo() {

        mVideoView = (VideoView) findViewById(R.id.video_view);
        if (mVideoView == null) {
            Log.e(TAG, "Video view not available");
            return;
        }

        mVideoView
                .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        mVideoView.start();
                        if (mUiStage == Stage.PracticeMode
                                && !mGetFpImagesStarted) {
                            mGetFpImagesStarted = true;
                            getFingerprintImage();
                        }
                    }
                });

        File file = new File(Environment.getDataDirectory(),
                "swipe_technique.mp4");
        if (file.exists()) {
            mVideoView.setVideoPath(file.getAbsolutePath());
        } else {
            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/"
                    + R.raw.swipe_technique);
            mVideoView.setVideoURI(uri);
        }

        // Hide static fingerprint image
        if (mFingerprintImg != null) {
            mFingerprintImg.setVisibility(View.INVISIBLE);
        }

        // playback video
        mVideoView.start();
        mVideoView.setVisibility(View.VISIBLE);

    }

    private void showImageQualityFeedback(final int quality) {
        String message = "";
        switch (quality) {
        case FingerprintApiWrapper.VCS_IMAGE_QUALITY_GOOD:
            message = getString(R.string.good_swipe);
            break;
        case FingerprintApiWrapper.VCS_IMAGE_QUALITY_TOO_FAST:
            message = getString(R.string.bad_swipe_too_fast);
            break;
        case FingerprintApiWrapper.VCS_IMAGE_QUALITY_TOO_SLOW:
            message = getString(R.string.bad_swipe_too_slow);
            break;
        default:
            message = getString(R.string.bad_swipe_default);
            break;
        }

        if (!message.equals("") && (mStatusText != null)) {
            mStatusText.setText(message);
        }
    }

    private void setStatusText(final String message) {

        if (mHeaderText != null) {

            mHeaderText.setText(message);
        }

    }

    /**
     * Check the enrolled finger list and show the option - "Remove fingerprint"
     * in menu if fingerprints are previosely enrolled.
     */
    public class MenuInitTask extends AsyncTask<Context, Void, Integer> {

        ProgressDialog mBusyDialog;
        Handler mMenuTaskHandler = new Handler();
        boolean mCancel = false;

        @Override
        protected void onPreExecute() {
            showBusyDialog(true);
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Context... params) {

            boolean screenLocked = false;

            // wait until phone is unlocked
            while (mKeygaurdMgr.inKeyguardRestrictedInputMode()) {
                screenLocked = true;

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Exit the thread if app goes to pause
                if (mCancel) {
                    log("Cancelling task");
                    return 1;
                }
            }

            // if resumed from lock screen, give some room to unlock sreen
            // cleanup
            if (screenLocked) {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                mLock.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            initValidityLib();

            int fpIndex = getNextFingerIndex();

            mLock.release();

            return fpIndex;
        }

        @Override
        protected void onCancelled() {
            log("onCancelled()");
            mCancel = true;
            mLock.release();
            showBusyDialog(false);
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Integer result) {
            log("onPostExecute()");
            mFpIndex = result;

            if (mUiStage != Stage.FingerprintMenu) {
                showBusyDialog(false);
                super.onPostExecute(result);
                return;
            }

            String promptMsg = "";
            if (mFpIndex > 1) {
                mFpMask = mFingerprint
                        .getEnrolledFingerList(ConstantUtils.USER_ID);
                mRemoveFingerPrintLayout.setVisibility(View.VISIBLE);
                refreshFingerListView();
            } else {
                mRemoveFingerPrintLayout.setVisibility(View.GONE);
            }

            showBusyDialog(false);
            super.onPostExecute(result);

            if (!promptMsg.equals("")) {
                popupMessage(promptMsg, null);
            }
        }

        private void showBusyDialog(boolean visible) {
            mMenuTaskHandler.removeCallbacksAndMessages(null);
            if (visible) {
                mBusyDialog = ProgressDialog.show(ChooseLockFingerprint.this,
                        "", getString(R.string.loading), true);
                mMenuTaskHandler.postDelayed(new Runnable() {
                    public void run() {
                        if (mBusyDialog.isShowing()) {
                            mBusyDialog.dismiss();
                        }
                    }
                }, 6000);
            } else {
                if (mBusyDialog.isShowing()) {
                    mBusyDialog.dismiss();
                }
            }
        }

        public void cancel() {
            log("MenuInitTask:: cancel()");
            showBusyDialog(false);
            super.cancel(true);
        }

    }

    private void refreshFingerListView() {
        if (mFpIndex >= 1) {
            if (mFpMask != -1) {
                HashMap<String, Object> hm;
                mFingerList.clear();
                int i = 1;
                for (i = 1; i <= 10; i++) {
                    if (((mFpMask >>> i) & 1) != 0) {
                        if (!mProviderHelper.isFingerIndexInProvider(String
                                .valueOf(i))) {
                            mProviderHelper.insertFingerIndexData(mContext,
                                    String.valueOf(i));
                        }
                        hm = new HashMap<String, Object>();
                        String name = mProviderHelper
                                .getFingerNameWithFingerIndex(i);
                        hm.put(ITEM_NAME,
                                name == null ? getString(R.string.finger, i)
                                        : name);
                        hm.put(ITEM_FINGER, Integer.toString(i));
                        if (mFingerList != null) {
                            mFingerList.add(hm);
                        }

                    }
                }
            }
        }
        if (mFingerList.size() > 0) {
            mListAdapter = new SimpleAdapter(mContext, mFingerList,
                    R.layout.fingerprint_list_item, new String[] { ITEM_NAME },
                    new int[] { R.id.text1 });
            mListView.setAdapter(mListAdapter);
            mListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1,
                        int position, long id) {
                    // TODO Auto-generated method stub
                    mDeleteFingerIndex = mFingerList.get(position)
                            .get(ITEM_FINGER).toString();
                    mPosition = position;
                    showAlertDialog(mContext, 1);
                }
            });
            mListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void showAlertDialog(Context context, int type) {
        // TODO Auto-generated method stub
        final SweetAlertDialog alert = new SweetAlertDialog(context, type);
        switch (type) {
        case SweetAlertDialog.NORMAL_TYPE:

            break;
        case SweetAlertDialog.ERROR_TYPE:

            alert.setTitleText(getResources().getString(
                    R.string.remove_fingerprints));
            alert.setConfirmText(getResources().getString(R.string.ok));
            alert.showCancelButton(true);
            alert.setCancelText(getResources().getString(R.string.cancel));
            alert.setConfirmText(getResources().getString(R.string.ok));
            alert.setConfirmClickListener(new OnSweetClickListener() {

                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    // TODO Auto-generated method stub
                    removeFingerprints(mDeleteFingerIndex);

                    mFingerList.remove(mPosition);
                    mListAdapter.notifyDataSetChanged();
                    if (mFingerList.size() < 1) {
                        mRemoveFingerPrintLayout.setVisibility(View.GONE);
                    }

                    alert.dismiss();
                }
            });
            alert.setCancelClickListener(new OnSweetClickListener() {

                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    // TODO Auto-generated method stub
                    alert.dismiss();
                }
            });
            alert.show();
            break;
        case SweetAlertDialog.RENAME_TYPE:
            alert.setTitleText(getResources().getString(R.string.name_title));
            alert.setConfirmText(getResources().getString(R.string.ok));
            alert.setConfirmText(getResources().getString(R.string.ok));
            alert.setConfirmClickListener(new OnSweetClickListener() {

                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    // TODO Auto-generated method stub
                    String name = alert.getRenameStr();
                    TextUtils.isEmpty(name);
                    if (TextUtils.isEmpty(name)) {
                        ToastUtils.show(mContext,
                                getResources().getString(R.string.name_empty));
                    } else {
                        mProviderHelper.insertFingerData(mContext, null, null,
                                String.valueOf(mFpIndex - 1), name);
                        // mProviderHelper.updateFingerData(mContext,
                        // String.valueOf(mFpIndex - 1), name);
                        alert.dismiss();
                        finish();
                    }

                }
            });
            alert.show();
            break;

        default:
            break;
        }

    }
}

package com.newschip.fingerprint.activity;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.widget.EditText;

import com.newschip.fingerprint.utils.PackageUtils;
import com.newschip.fingerprint.utils.PreferenceUtil;
import com.newschip.fingerprint.utils.VibratorUtils;
import com.silead.fp.utils.FpControllerNative;
import com.silead.fp.utils.FpControllerNative.SLFpsvcIndex;

public class ConfirmLockFingerprint extends PasswordActivity implements
        FpControllerNative.OnIdentifyRspCB {

    private static final String TAG = "ConfirmPasswordActivity";
    private static final int MODE_CHECK = 0;
    private static final int MODE_FIRST_INPUT = 1;
    private static final int MODE_CONFIRM_PWD = 2;
    private EditText mText;
    private int mMode = 0; // 0: check mode; 1: first input; 2: input confirm;
    private String mFirstPwd;
    private String mCheckedPackage;
    private FpControllerNative mFpControllerNative;
    private boolean mIdentifying = false;
    private KeyguardManager mKeyguardManager;
    private PowerManager mPowerManager;
    private Handler mHandler = new Handler();
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        int mode = 0;
        mFpControllerNative = FpControllerNative.getInstance();
        initFPSystem();
        mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mIntent = getIntent();

        mSPUtil = new PreferenceUtil(mContext);
        // changeMode(mode);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "$$$$ onResume $$$$ ");
        mFpControllerNative.registerIdentifyListener(this);
        identifyCredentialREQ(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "$$$$ onPause $$$$ ");
        mFpControllerNative.unregisterIdentifyListener(this);
        cancelOperation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "$$$$ onDestroy $$$$ ");
        mFpControllerNative.unregisterIdentifyListener(this);
        cancelOperation();
    }

    @Override
    public void onIdentifyRsp(int index, int result, int fingerid) {
        Log.d(TAG, "$$$$ onIdentifyRsp $$$$ index = " + index + "result:"
                + result + "fingerid:" + fingerid + "result" + result);
        mIdentifying = false;
        if (mKeyguardManager.inKeyguardRestrictedInputMode()) {
            Log.d(TAG, " onIdentifyRsp keyguard is showing return !!! ");
            return;
        }
        if (result == FpControllerNative.IDENTIFY_SUCCESS) {
            VibratorUtils.Vibrate(mContext);
            // mResultText.setText(getResources().getString(
            // R.string.verification_success));
            onIdentifySuccess();
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

    private void onIdentifySuccess() {
        setResult(RESULT_OK);
        String pkg = mIntent.getStringExtra("packageName");
        Log.d(TAG, "pkg = " + pkg);
        if (pkg != null) {
            PackageUtils.runApp(mContext, pkg);
        } else {
            startActivity(new Intent(mContext,
                    MainActivity.class));
        }
        finish();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    private int initFPSystem() {
        Log.d(TAG, " initFPSystem ");
        return mFpControllerNative.initFPSystem();
    }

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

    private int destroyFPSystem() {
        return mFpControllerNative.destroyFPSystem();
    }

    private void cancelOperation() {
        if (mIdentifying) {
            mFpControllerNative.FpCancelOperation();
            mIdentifying = false;
        }
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
        return hasEnabled;
    }

}

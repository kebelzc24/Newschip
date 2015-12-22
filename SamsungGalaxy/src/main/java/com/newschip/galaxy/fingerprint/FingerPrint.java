package com.newschip.galaxy.fingerprint;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;
import com.samsung.android.sdk.pass.SpassInvalidStateException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by LQ on 2015/12/11.
 */
public class FingerPrint implements IFingerPrint {
    private final String TAG = "newschip_fingerprint";
    private Context mContext;
    private SpassFingerprint mSpassFingerprint;
    private Spass mSpass;

    private ArrayList<Integer> mFingerIndexs = new ArrayList<>();
    private ArrayList<String> mFingerName = new ArrayList<>();

    public void setmOnIndentifyFinishListener(OnIndentifyFinishListener mOnIndentifyFinishListener) {
        this.mOnIndentifyFinishListener = mOnIndentifyFinishListener;
    }

    private OnIndentifyFinishListener mOnIndentifyFinishListener;


    public ArrayList<Integer> getFingerIndexs() {
        return mFingerIndexs;
    }


    public FingerPrint(Context mContext) {
        this.mContext = mContext;
        initSpass();
    }

    public interface OnIndentifyFinishListener {
        void onIdentifyReady();

        void onIdentifyStart();

        void onIdentifyFinish(int index);
    }

    @Override
    public void initSpass() {
        mSpass = new Spass();
        try {
            mSpass.initialize(mContext);
        } catch (SsdkUnsupportedException e) {
            Log.d(TAG, "Exception: " + e);
        } catch (UnsupportedOperationException e) {
            Log.d(TAG, "Fingerprint Service is not supported in the device");
        }

    }

    @Override
    public boolean hasRegisteredFinger() {
        if(mSpassFingerprint == null){
            mSpassFingerprint = new SpassFingerprint(mContext);
        }
        return mSpassFingerprint.hasRegisteredFinger();
    }

    //检查是否有录入指纹
    @Override
    public ArrayList<String> getRegisteredFingerprintName() {
        if (mSpassFingerprint == null) {
            mSpassFingerprint = new SpassFingerprint(mContext);
        }
        ArrayList<String> names = new ArrayList<>();
        try {
            SparseArray<String> mList = mSpassFingerprint.getRegisteredFingerprintName();
            if (mList == null) {
                Log.d(TAG, "Registered fingerprint is not existed.");
            } else {
                for (int i = 0; i < mList.size(); i++) {
                    mFingerIndexs.add(mList.keyAt(i));
                    mFingerName.add(mList.get(mList.keyAt(i)));
                }
            }
        } catch (UnsupportedOperationException e) {
            Log.d(TAG, "Fingerprint Service is not supported in the device.");
        }
        return names;
    }

    @Override
    public void startIdentify() {
        if (mSpassFingerprint == null) {
            mSpassFingerprint = new SpassFingerprint(mContext);
        }
        try {
            if (!mSpassFingerprint.hasRegisteredFinger()) {
                Log.d(TAG, "Please register finger first");
            } else {
                try {
                    mSpassFingerprint.startIdentify(mListener);
                } catch (SpassInvalidStateException ise) {
                    if (ise.getType() == SpassInvalidStateException.STATUS_OPERATION_DENIED) {
                        Log.d(TAG, "Exception: " + ise.getMessage());
                    }
                } catch (IllegalStateException e) {
                }

            }
        } catch (UnsupportedOperationException e) {
            Log.d(TAG, "Fingerprint Service is not supported in the device");
        } catch (Exception e) {

        }
    }

    private SpassFingerprint.IdentifyListener mListener = new SpassFingerprint.IdentifyListener() {

        @Override
        public void onFinished(int eventStatus) {
            Log.e("kebelzc245", "IdentifyListener onFinished...");
            int index = 0;
            try {
                index = mSpassFingerprint.getIdentifiedFingerprintIndex();
            } catch (IllegalStateException ise) {
            }
            if (mOnIndentifyFinishListener != null) {
                mOnIndentifyFinishListener.onIdentifyFinish(index);
            }

        }

        @Override
        public void onReady() {
            if (mOnIndentifyFinishListener != null) {
                mOnIndentifyFinishListener.onIdentifyReady();
            }
            Log.e("kebelzc245", "IdentifyListener onReady...");
        }

        @Override
        public void onStarted() {
            if (mOnIndentifyFinishListener != null) {
                mOnIndentifyFinishListener.onIdentifyStart();
            }
            Log.e("kebelzc245", "IdentifyListener onStarted...");
        }
    };

    @Override
    public void cancelIdentify() {
        try {
            mSpassFingerprint.cancelIdentify();
        } catch (IllegalStateException ise) {
        } catch (UnsupportedOperationException e) {
        } catch (Exception e) {

        }
    }

    @Override
    public boolean isDeviceSupport() {
        return mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);
    }
}

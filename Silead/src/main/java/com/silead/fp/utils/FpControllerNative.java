package com.silead.fp.utils;

import java.util.ArrayList;

import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.os.Message;

public class FpControllerNative {
    public static final String TAG = "FpControllerNative";

    public static final int CHECK_IMG_FAIL = -1;
    public static final int SL_TOUCH_TOO_FAST = -3;
    public static final int SL_RSP_SESSION_BUSY_IN_VERI = -4;
    public static final int SL_RSP_SESSION_BUSY_IN_ENRO = -5;
    public static final int SL_RSP_SESSION_BUSY_IN_WAKEUP_VERI = -6;
    
    public static final int ENROLL_SUCCESS = 0;
    public static final int ENROLL_CANCLED = -2;
    public static final int ENROLL_NOT_SUPPORT = -3;
    public static final int ENROLL_ERROR = -4;
    public static final int ENROLL_FAIL = -105;

    public static final int ENROLLING = 0;
    public static final int REENROLL = 5;
    public static final int ENROLL_INDEX = 1;
    public static final int ENROLL_CREDENTIAL_RSP = 1;
    /** for current single enroll failure due to image is not suitable*/
    public static final int SL_ENROLL_CURR_ENR_FAIL = -106;
    /** for image is not good when finger is putting on chip*/
    public static final int SL_ENROLL_CURR_IMG_BAD = -107;

    public static final int INIT_FP_FAIL = 0;
    public static final int INIT_FP_SUCCESS = 1;

    public static final int IDENTIFY = 2;
    public static final int IDENTIFY_SUCCESS = 0;
    public static final int IDENTIFY_TMEOUT = -1;
    public static final int IDENTIFY_CANCELED = -2;
    public static final int IDENTIFY_ERR_MATCH = -3;
    public static final int IDENTIFY_ERROR = -4;
    public static final int IDENTIFY_FAIL = -5;
    public static final int IDENTIFY_MAX = 5;

    public static final int IDENTIFY_INDEX = 2;
    public static final int IDENTIFY_CREDENTIAL_RSP = 0;
    public static final int IDENTIFY_WAKEUP_NOT_MATCHED = -206;
    /**success to wake by verify*/
    public static final int IDENTIFY_WAKEUP_MATCHED = -207;
    public static final int IDENTIFY_WAKEUP_BAD_IMG = -208;
    /** for image is not good when finger is put on chip*/
    public static final int IDENTIFY_CURR_IMG_BAD = -209;

    public static final int FP_GENERIC_CB = 3;
    public static final int FP_KEY_CB = 4;

    public static final int VK_STATE_ON = 1;
    public static final int VK_STATE_OFF = 0;
    private Handler mFPhandler;

    private ArrayList<OnIdentifyRspCB> mOnIdentifyRspCBs = new ArrayList<OnIdentifyRspCB>();

    private boolean mCanceling = false;

    private DefaultHandler mDefaultHandler = new DefaultHandler();

    private static FpControllerNative sFpControllerNative = null;// 声明一个Emperor类的引用

    public static class SLFpsvcFPInfo implements Parcelable {
        public int slot;// 0=false 1 = true
        public int enable;
        public String fingerName;
        public int enrollIndex;

        public SLFpsvcFPInfo() {
            // TODO Auto-generated constructor stub
        }

        public SLFpsvcFPInfo(int slot, int enable, String fingerName,
                int enrollIndex) {
            super();
            this.slot = slot;
            this.enable = enable;
            this.fingerName = fingerName;
            this.enrollIndex = enrollIndex;
        }

        public int getEnrollIndex() {
            return enrollIndex;
        }

        public void setEnrollIndex(int enrollIndex) {
            this.enrollIndex = enrollIndex;
        }

        public String getFingerName() {
            return fingerName;
        }

        public void setFingerName(String fingerName) {
            this.fingerName = fingerName;
        }

        public int getSlot() {
            return slot;
        }

        public void setSlot(int slot) {
            this.slot = slot;
        }

        public int getEnable() {
            return enable;
        }

        public void setEnable(int enable) {
            this.enable = enable;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        // 把数据写入到Parcel
        public void writeToParcel(Parcel dest, int flags) {
            // TODO Auto-generated method stub
            dest.writeString(fingerName);
            dest.writeInt(slot);
            dest.writeInt(enable);
        }

        // public static final Parcelable.Creator<SLFpsvcFPInfo> CREATOR = new
        // Parcelable.Creator<SLFpsvcFPInfo>() {
        // //创建对象 从Parcel中获取数据
        // public SLFpsvcFPInfo createFromParcel(Parcel in) {
        // SLFpsvcFPInfo fpInfo = new SLFpsvcFPInfo();
        // fpInfo.fingerName = in.readString();
        // fpInfo.slot = in.readInt();
        // fpInfo.enable = in.readInt();
        // return fpInfo;
        // }
        //
        // public SLFpsvcFPInfo[] newArray(int size) {
        // return new SLFpsvcFPInfo[size];
        // }
        // };
    }

    public class SLFpsvcIndex {
        public int total;
        public int max;
        public int wenable;
        public int frame_w;
        public int frame_h;
        public int usernow;
        public int userid;
        public SLFpsvcFPInfo[] FPInfo;

        public SLFpsvcFPInfo[] getFPInfo() {
            return FPInfo;
        }

        public void setFPInfo(SLFpsvcFPInfo[] fPInfo) {
            FPInfo = fPInfo;
        }
    }

    public interface OnIdentifyRspCB {
        public void onIdentifyRsp(int index, int result, int fingerid);
    }

    class DefaultHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int result = msg.what;
            Log.d(TAG, "handleMessage $$$$$$$ result = " + result);
            switch (result) {
            case IDENTIFY_CREDENTIAL_RSP:
                break;
            case ENROLL_CREDENTIAL_RSP:
                break;
            case FP_GENERIC_CB:
                break;
            case FP_KEY_CB:
                break;
            default:
                break;
            }
        }
    }

    private FpControllerNative() {
        mFPhandler = mDefaultHandler;
        Log.d(TAG, "enrollCredentialRSP $$$$$$$ handle = " + mFPhandler);
    }

    public static FpControllerNative getInstance() {// 实例化引用
        if (sFpControllerNative == null) {
            sFpControllerNative = new FpControllerNative();
            sFpControllerNative.nativeInit();
        }
        return sFpControllerNative;
    }

    public void setHandler(Handler handler) {
        mFPhandler = handler;
    }

    private native void nativeInit();

    // 初始化FP //InitFPService 初始化成功或者失败通过返回值判断
    private native int nativeInitFPSystem();

    // 关闭FP //DeinitFPService
    private native int nativeDestoryFPSystem();

    // 登记指纹
    private native int nativeEnrollCredentialREQ(int index);

    // 发出消息识别指纹
    private native int nativeIdentifyCredentialREQ(int index);

    // 从底层移除指纹
    private native int nativeRemoveCredential(int index);

    private native int nativeResetFPService();

    private native int nativeEnalbeCredential(int index, int enable);

    private native int nativeGetEnableCredential(int index);

    private native int nativeFpCancelOperation();

    private native int nativeSetFpInfo(SLFpsvcIndex fpindex);

    private native int nativeSwitchUser(int userid);

    private native int nativeGetVirtualKeyCode();

    private native int nativeSetVirtualKeyCode(int VirtualKeyCode);

    private native int nativeGetVirtualKeyState();

    private native int nativeSetVirtualKeyState(int VirtualKeyState);

    private native int nativeGetWakeUpState();

    private native int nativeSetWakeUpState(int WakeUpState);

    private native int nativeGetFingerPrintState();

    private native int nativeSetFingerPrintState(int FingerPrintState);

    private native SLFpsvcIndex nativeGetFpInfo();

    private native int nativeSetFPScreenStatus(int screenState);

    static {
        try {
            System.loadLibrary("slfpjni");
        } catch (UnsatisfiedLinkError e) {
            Log.i(TAG, "FpControllerNative loadLibrary fail, " + e);
        }
    }

    public int initFPSystem() {
        Log.d(TAG, "initFPSystem");
        return nativeInitFPSystem();
    }

    public int destroyFPSystem() {
        Log.d(TAG, "destroyFPSystem");
        return nativeDestoryFPSystem();
    }

    public int enrollCredentialREQ(int index) {
        Log.d(TAG, "enrollCredentialREQ");
        return nativeEnrollCredentialREQ(index);
    }

    public int identifyCredentialREQ(int index) {
        Log.d(TAG, "identifyCredentialREQ");
        return nativeIdentifyCredentialREQ(index);
    }

    public int removeCredential(int index) {
        Log.d(TAG, "removeCredential");
        return nativeRemoveCredential(index);
    }

    public int EnalbeCredential(int index, int enable) {
        Log.d(TAG, "EnalbeCredential");
        return nativeEnalbeCredential(index, enable);
    }

    public int GetEnableCredential(int index) {
        Log.d(TAG, "GetEnableCredential");
        return nativeGetEnableCredential(index);
    }

    public int FpCancelOperation() {
        // Log.d(TAG, "FpCancelOperation mCanceling = "+mCanceling);
        // if (mCanceling) {
        // Log.w(TAG, "FpCancelOperation canceling, return");
        // return -1;
        // }
        // mCanceling = true;
        int result = nativeFpCancelOperation();
        // if (result < 0) {
        // mCanceling = false;
        // }
        return result;
    }

    public int resetFPService() {
        Log.d(TAG, "resetFPService");
        return nativeResetFPService();
    }

    public SLFpsvcIndex GetFpInfo() {
        Log.d(TAG, "GetFpInfo");
        return nativeGetFpInfo();
    }

    public int SetFpInfo(SLFpsvcIndex fpindex) {
        Log.d(TAG, "SetFpInfo");
        return nativeSetFpInfo(fpindex);
    }

    public int SwitchUser(int userid) {
        Log.d(TAG, "SwitchUser");
        return nativeSwitchUser(userid);
    }

    public int GetVirtualKeyCode() {
        Log.d(TAG, "GetVirtualKeyCode");
        return nativeGetVirtualKeyCode();
    }

    public int SetVirtualKeyCode(int VirtualKeyCode) {
        Log.d(TAG, "SetVirtualKeyCode");
        return nativeSetVirtualKeyCode(VirtualKeyCode);
    }

    public int GetVirtualKeyState() {
        Log.d(TAG, "GetVirtualKeyState");
        return nativeGetVirtualKeyState();
    }

    public int SetVirtualKeyState(int virtualKeyState) {
        Log.d(TAG, "SetVirtualKeyState");
        return nativeSetVirtualKeyState(virtualKeyState);
    }

    public int GetFingerPrintState() {
        Log.d(TAG, "GetFingerPrintState");
        return nativeGetFingerPrintState();
    }

    public int SetFingerPrintState(int fingerPrintState) {
        Log.d(TAG, "SetFingerPrintState");
        return nativeSetFingerPrintState(fingerPrintState);
    }

    public int GetWakeUpState() {
        Log.d(TAG, "GetWakeUpState");
        return nativeGetWakeUpState();
    }

    public int SetWakeUpState(int wakeUpState) {
        Log.d(TAG, "SetWakeUpState");
        return nativeSetWakeUpState(wakeUpState);
    }

    public int SetScreenStatus(int screenState) {
        Log.d(TAG, "SetFPScreenStatus" + screenState);
        return nativeSetFPScreenStatus(screenState);
    }

    public void registerIdentifyListener(OnIdentifyRspCB callback) {
        Log.d(TAG, "registerIdentifyListener $$$$$$$ callback = " + callback);
        mOnIdentifyRspCBs.add(callback);
    }

    public void unregisterIdentifyListener(OnIdentifyRspCB callback) {
        Log.d(TAG, "unregisterIdentifyListener $$$$$$$ callback = " + callback);
        mOnIdentifyRspCBs.remove(callback);
    }

    // call from native
    private void enrollCredentialRSP(int index, int percent, int result,
            int area) { // percent
        Log.d(TAG, "enrollCredentialRSP $$$$$$$ index 222 = " + index + ":"
                + percent + ":" + result);
        Log.d(TAG, "enrollCredentialRSP $$$$$$$ handle = " + mFPhandler);
        if (result == ENROLL_CANCLED) {
            mCanceling = false;
        }
        int[] intArray = new int[4];
        intArray[0] = index;
        intArray[1] = percent;
        intArray[2] = result;
        intArray[3] = area;
        Message msg = mFPhandler.obtainMessage(ENROLL_CREDENTIAL_RSP, intArray);
        mFPhandler.sendMessage(msg);
    }

    // call from native
    private void identifyCredentialRSP(int index, int result, int fingerid) {
        final int len = mOnIdentifyRspCBs.size();
        Log.d(TAG, "identifyCredentialRSP $$$$$$$ index = " + index
                + "result :" + result + "fingerid :" + fingerid + ":" + len);
        for (int i = 0; i < len; i++) {
            if (mOnIdentifyRspCBs.get(i) != null) {
                mOnIdentifyRspCBs.get(i).onIdentifyRsp(index, result, fingerid);
            }
        }
        if (result == ENROLL_CANCLED) {
            mCanceling = false;
        }
        /*
         * int[] intArray = new int[2]; intArray[0] = index; Message msg =
         * mFPhandler.obtainMessage(IDENTIFY_CREDENTIAL_RSP, intArray);
         * mFPhandler.sendMessage(msg);
         */
    }

    // call from native
    private void fpGenericCB(int index, String event_name, int result,
            String event_data) {
        Log.d(TAG, "fpGenericCB $$$$$$$ index = " + index + ":" + event_name
                + ":" + result + ":" + event_data + ":" + mCanceling);
        if (result == ENROLL_CANCLED) {
            mCanceling = false;
        }
        Message msg = mFPhandler.obtainMessage(FP_GENERIC_CB);
        mFPhandler.sendMessage(msg);
    }

    private void slfpkeyRSP(int keyret) {
        // Message msg = mFPhandler.obtainMessage(FP_KEY_CB,keyret);
        // mFPhandler.sendMessage(msg);
    }

}

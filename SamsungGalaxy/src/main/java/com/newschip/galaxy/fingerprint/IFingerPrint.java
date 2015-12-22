package com.newschip.galaxy.fingerprint;

import java.util.ArrayList;

/**
 * Created by LQ on 2015/12/15.
 */
public interface IFingerPrint {
    void initSpass();

    boolean isDeviceSupport();

    boolean hasRegisteredFinger();

    ArrayList<String> getRegisteredFingerprintName();

    void startIdentify();

    void cancelIdentify();
}

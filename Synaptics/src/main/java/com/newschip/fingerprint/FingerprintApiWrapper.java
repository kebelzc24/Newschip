package com.newschip.fingerprint;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import dalvik.system.DexClassLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.InvalidParameterException;

/**
 * Wrapper class for Synaptics Sensor's fingerprint APIs.
 * It loads validity-sys.jar at runtime and makes the API calls.
 */
public class FingerprintApiWrapper {

    public static final String TAG = "FingerprintApiWrapper";

    public static final boolean DBG = false;

    /** Path of Synaptics API library */
    private static final String SYNA_JAR_PATH = "/system/framework/validity-sys.jar";

    /** Directory where optimized dex files should be written */
    private static String OPTIMIZED_DIRECTORY = "/data/data";

    /** Callback Interface **/
    public interface EventListener {
        public void onEvent(FingerprintEvent fpEvent);
    }

    /** Class for fingerprint callback event data */
    public class FingerprintEvent {
        public int      eventId;
        public Object   eventData;

        FingerprintEvent() { }

        FingerprintEvent(int id, Object extraData) {
            eventId     = id;
            eventData   = extraData;
        }

        public String logMessage() {
            return "FingerprintEvent:: Id:" + eventId;
        }
    }

    /** Class to hold event data for enroll progress event */
    public class EnrollCaptureStatus {

        public int imageQuality;
        public int templateResult;
        public int totalSwipes;
        public int badSwipes;
        public int progress;

        EnrollCaptureStatus(int imageQuality, int templateResult, int totalSwipes,
                            int badSwipes, int progress) {
            this.imageQuality   = imageQuality;
            this.templateResult = templateResult;
            this.totalSwipes    = totalSwipes;
            this.badSwipes      = badSwipes;
            this.progress       = progress;
        }
    }

    /** Class to hold Captured image data */
    public class FingerprintBitmap {
        public Bitmap   fingerprint;
        public int      quality;

        FingerprintBitmap(Bitmap fp, int qty) {
            fingerprint = fp;
            quality = qty;
        }
    }

    //vcs event Ids
    public static final int VCS_EVT_SENSOR_REMOVED                          = 1;
    public static final int VCS_EVT_SENSOR_DETECTED                         = 2;
    public static final int VCS_EVT_SENSOR_READY_FOR_USE                    = 3;
    public static final int VCS_EVT_SENSOR_FAILED_INITIALIZATION            = 4;
    public static final int VCS_EVT_SENSOR_FINGERPRINT_CAPTURE_COMPLETE     = 5;
    public static final int VCS_EVT_SENSOR_RAW_FINGERPRINT_CAPTURE_COMPLETE = 6;
    public static final int VCS_EVT_SENSOR_FINGERPRINT_CAPTURE_FAILED       = 7;
    public static final int VCS_EVT_SENSOR_FINGERPRINT_CAPTURE_START        = 8;
    public static final int VCS_EVT_ALL_SENSORS_INITIALIZED                 = 9;
    public static final int VCS_EVT_SENSOR_FINGERPRINT_FAILED_SWIPE_RETRY   = 10;
    public static final int VCS_EVT_FINGER_DETECTED                         = 11;
    public static final int VCS_EVT_SENSOR_NAVIGATION_REPORT                = 12;
    public static final int VCS_EVT_ENROLL_COMPLETED                        = 13;
    public static final int VCS_EVT_VERIFY_COMPLETED                        = 14;
    public static final int VCS_EVT_IDENTIFY_COMPLETED                      = 15;
    public static final int VCS_EVT_ENROLL_NEXT_CAPTURE_START               = 16;
    public static final int VCS_EVT_EIV_FINGERPRINT_CAPTURED                = 17;
    public static final int VCS_EVT_ENROLL_CAPTURE_STATUS                   = 32;
    public static final int VCS_EVT_FINGER_SETTLED                          = 33;
    public static final int VCS_EVT_ENROLL_SUCCESS                          = 421;
    public static final int VCS_EVT_VERIFY_SUCCESS                          = 422;
    public static final int VCS_EVT_IDENTIFY_SUCCESS                        = 423;
    public static final int VCS_EVT_ENROLL_FAILED                           = 424;
    public static final int VCS_EVT_VERIFY_FAILED                           = 425;
    public static final int VCS_EVT_IDENTIFY_FAILED                         = 426;

    // vcs operation result codes
    public static final int VCS_RESULT_FAILED                       = -1;
    public static final int VCS_RESULT_OK                           = 0;
    public static final int VCS_RESULT_USER_DOESNT_EXIST            = 65;
    public static final int VCS_RESULT_USER_FINGER_ALREADY_ENROLLED = 88;
    public static final int VCS_RESULT_TOO_MANY_BAD_SWIPES          = 515;
    public static final int VCS_RESULT_MATCHER_ADD_IMAGE_FAILED     = 306;
    public static final int VCS_RESULT_SERVICE_NOT_RUNNING          = 321;
    public static final int VCS_RESULT_OPERATION_CANCELED           = 361;
    public static final int VCS_RESULT_SENSOR_IS_REMOVED            = 360;
    public static final int VCS_RESULT_SENSOR_NOT_FOUND             = 19;
    public static final int VCS_RESULT_KEYDATA_NOT_FOUND            = 777;
    public static final int VCS_RESULT_MATCH_FAILED                 = 367;
    public static final int VCS_RESULT_DATA_STORE_FAILED            = 1001;
    public static final int VCS_RESULT_DATA_RETRIEVE_FAILED         = 1002;
    public static final int VCS_RESULT_DATA_REMOVE_FAILED           = 1003;
    public static final int VCS_RESULT_ALREADY_INPROGRESS           = 1004;

    //Image Quality Flags
    public static final int VCS_IMAGE_QUALITY_GOOD      = 0;
    public static final int VCS_IMAGE_QUALITY_TOO_FAST  = 0x00000002;
    public static final int VCS_IMAGE_QUALITY_TOO_SLOW  = 0x00000010;

    /**
     * Default enrollment mode. Enroll only if finger not previously enrolled.
     */
    public static final int VCS_ENROLL_MODE_DEFAULT = 1;

    /**
     * Re enrollment mode. Overwrite if finger already enrolled.
     */
    public static final int VCS_ENROLL_MODE_REENROLL = 2;


    //Class loader to load classes from validity-sys.jar
    static ClassLoader mClassLoader = null;

    //Classes of validity-sys
    Class<?> CLASS_FINGERPRINT  = null;
    Class<?> CLASS_VCSINT       = null;
    Class<?> CLASS_VCSDATA      = null;
    Class<?> CLASS_ENROLLUSER   = null;
    Class<?> CLASS_REMOVEENROLL = null;
    Class<?> INTERFACE_LISTENER = null;

    //APIs of fingerprint class
    Method METHOD_ENROLL;
    Method METHOD_IDENTIFY;
    Method METHOD_GETFINGERLIST;
    Method METHOD_GETFPIMAGE;
    Method METHOD_REMOVE;
    Method METHOD_CANCEL;
    Method METHOD_CLEANUP;

    //Needed fields from classes
    Field[] FIELDS_VCSDATA;
    Field FIELD_VCSINT;

    private static Constructor<?> FP_CTR    = null;
    private static Object mFingerprint      = null;
    private EventListener mListener         = null;

    /**
     * Constructor
     */
    public FingerprintApiWrapper(Context context, final EventListener listener) {

        if (context == null || listener == null) {
            throw new InvalidParameterException("Invalid arguments.");
        }

        try {

            OPTIMIZED_DIRECTORY = context.getFilesDir().getAbsolutePath();

            //Create a DexClassLoader that loads the validity-sys.jar.
            if (mClassLoader == null) {
                mClassLoader = new DexClassLoader(
                                    SYNA_JAR_PATH,
                                    OPTIMIZED_DIRECTORY,
                                    (String) null,
                                    FingerprintApiWrapper.class.getClassLoader());
            }

            //load classes from jar
            CLASS_FINGERPRINT = mClassLoader.loadClass("com.validity.fingerprint.Fingerprint");

            CLASS_VCSINT = mClassLoader.loadClass("com.validity.fingerprint.VcsInt");

            CLASS_ENROLLUSER = mClassLoader.loadClass("com.validity.fingerprint.EnrollUser");

            CLASS_REMOVEENROLL = mClassLoader.loadClass("com.validity.fingerprint.RemoveEnroll");

            CLASS_VCSDATA = mClassLoader.loadClass("com.validity.fingerprint.FingerprintEvent");

            //get listener interface from fingerprint class
            Class[] innerClasses = CLASS_FINGERPRINT.getSuperclass().getDeclaredClasses();
            for (Class<?> innerClass : innerClasses) {
                if (innerClass.isInterface()) {
                    INTERFACE_LISTENER = innerClass;
                    break;
                }
            }

            //get APIs of fingerprint class
            METHOD_IDENTIFY = CLASS_FINGERPRINT.getMethod("identify", String.class);

            METHOD_ENROLL = 
                    CLASS_FINGERPRINT.getMethod("enroll", CLASS_ENROLLUSER);

            METHOD_REMOVE = 
                    CLASS_FINGERPRINT.getMethod("removeEnrolledFinger", CLASS_REMOVEENROLL);

            METHOD_GETFINGERLIST =
                    CLASS_FINGERPRINT.getMethod("getEnrolledFingerList", String.class, CLASS_VCSINT);

            METHOD_GETFPIMAGE = CLASS_FINGERPRINT.getMethod("getFingerprintImage");
            
            METHOD_CANCEL = CLASS_FINGERPRINT.getMethod("cancel");

            METHOD_CLEANUP = CLASS_FINGERPRINT.getMethod("cleanUp");

            //get fields of FingerprintEvent class
            FIELDS_VCSDATA = CLASS_VCSDATA.getFields();

            FIELD_VCSINT = CLASS_VCSINT.getField("num");

            //Prepare listener object
            final InvocationHandler handler = new InvocationHandler() {
                public Object invoke( Object object, Method method, Object[] args ) {
                    if ( "onEvent".equals( method.getName() ) ) {
                        handleEvent(args[0]);
                    }
                    return args[0];
                }
            };

            /** Crate an instance of the dynamically built INTERFACE_LISTENER class
             *  for EventListener interface.
             *  Method invocations on the returned instance are forwarded
             *  to the above invocation handler.
             */
            Object proxy =
                    Proxy.newProxyInstance( INTERFACE_LISTENER.getClassLoader(),
                    new Class[]{ INTERFACE_LISTENER }, handler );

            //create fingerprint object
            FP_CTR = CLASS_FINGERPRINT.getDeclaredConstructor(Context.class, INTERFACE_LISTENER);
            mFingerprint = FP_CTR.newInstance(context, proxy);

            //save the listener reference
            mListener = listener;

            log("Fingerprint initialization " + (mFingerprint != null ? "success" : "failed"));

        } catch (Exception e) {
            Log.e(TAG, "FingerprintApiWrapper constuctor failed, Exception: ", e);
//            throw new ExceptionInInitializerError(e);
        }

      } //End of Method

    /**
     * Enroll finger
     */
    public int enroll(String userId, int fingerIndex, int mode) {

        log("enroll()");

        boolean newEnrollAPI = false;
        try {

            Object object = CLASS_ENROLLUSER.getConstructor().newInstance();

            Field field1 = CLASS_ENROLLUSER.getDeclaredField("userId");
            field1.setAccessible(true);
            field1.set(object, userId);
            
            Field field2 = CLASS_ENROLLUSER.getDeclaredField("fingerIndex");
            field2.setAccessible(true);
            field2.set(object, fingerIndex);

            Field field3 = CLASS_ENROLLUSER.getDeclaredField("mode");
            field3.setAccessible(true);
            field3.set(object, mode);

            Object ret = METHOD_ENROLL.invoke(mFingerprint, object);
            newEnrollAPI = true;
            return (Integer) ret;

        } catch(Exception e) {

            Log.e(TAG, "enroll() failed: " + e);
            e.printStackTrace();
        }


        if (!newEnrollAPI) {
            try {
                METHOD_ENROLL = 
                    CLASS_FINGERPRINT.getMethod("enroll", String.class, String.class, Integer.TYPE);
                Object ret = METHOD_ENROLL.invoke(mFingerprint, userId, "", fingerIndex);
                return (Integer) ret;
            } catch(Exception e) {
                Log.e(TAG, "enroll() failed: " + e);
                e.printStackTrace();
            }
        }

        return VCS_RESULT_FAILED;
    }

    /**
     * Identify a user
     */
    public int identify(String userId) {

        log("identify()");

        try {

            Object ret = METHOD_IDENTIFY.invoke(mFingerprint, userId);
            return (Integer) ret;

        } catch(Exception e) {

            Log.e(TAG, "identify() failed: " + e);
            e.printStackTrace();

        }

        return VCS_RESULT_FAILED;

    }

    /**
     * Get enrolled finger list of a user.
     */
    public int getEnrolledFingerList(String userId) {
        log("getEnrolledFingerList(" + userId + ")");
        try {
            Object vcsMask = CLASS_VCSINT.newInstance();
            Object ret = METHOD_GETFINGERLIST.invoke(mFingerprint, userId, vcsMask);
            log("getEnrolledFingerList(), fingermask: " + FIELD_VCSINT.getInt(vcsMask));
            return FIELD_VCSINT.getInt(vcsMask);
        } catch(Exception e) {
            Log.e(TAG, "getEnrolledFingerList() failed: " + e);
            e.printStackTrace();
        }
        return VCS_RESULT_FAILED;
    }

    /**
     * Remove fingerprint(s) of a user
     */
    public int removeEnrolledFinger(String userId, int fingerIndex) {

        log("removeEnrolledFinger()");
        boolean newEnrollAPI = false;
        try {

            Object object = CLASS_REMOVEENROLL.getConstructor().newInstance();

            Field field1 = CLASS_REMOVEENROLL.getDeclaredField("userId");
            field1.setAccessible(true);
            field1.set(object, userId);
            
            Field field2 = CLASS_REMOVEENROLL.getDeclaredField("fingerIndex");
            field2.setAccessible(true);
            field2.set(object, fingerIndex);

            Object ret = METHOD_REMOVE.invoke(mFingerprint, object);
            newEnrollAPI = true;
            return (Integer) ret;

        } catch(Exception e) {
            Log.e(TAG, "removeEnrolledFinger() failed: " + e);
        }

        if (!newEnrollAPI) {
            try {
                METHOD_REMOVE = 
                    CLASS_FINGERPRINT.getMethod("removeEnrolledFinger", String.class, Integer.TYPE);
                Object ret = METHOD_REMOVE.invoke(mFingerprint, userId, fingerIndex);
                return (Integer) ret;
            } catch(Exception e) {
                Log.e(TAG, "removeEnrolledFinger() failed: " + e);
            }
        }

        return VCS_RESULT_FAILED;

    }

    /**
     * Cancel fingerprint operation.
     */
    public int cancel(){

        log("cancel()");

        try {

            Object ret = METHOD_CANCEL.invoke(mFingerprint);
            return (Integer) ret;

        } catch(Exception e) {
            Log.e(TAG, "Cancel() failed: " + e);
            e.printStackTrace();
        }

        return VCS_RESULT_FAILED;
    }

    /**
     * Get fingerprint bitmap
     */
    public int getFingerprintImage() {

        log("getFingerprintImage()");

        try {

            Object ret = METHOD_GETFPIMAGE.invoke(mFingerprint);
            return (Integer) ret;

        } catch(Exception e) {
            Log.e(TAG, "getFingerprintImage() failed: ", e);
            e.printStackTrace();
        }

        return VCS_RESULT_FAILED;

    }

    /**
     * Cleanup
     */
    public void cleanUp() {

        log("cleanUp()");

        try {

            METHOD_CLEANUP.invoke(mFingerprint);

        } catch(Exception e) {
            Log.e(TAG, "cleanUp() failed: ", e);
            e.printStackTrace();
        }

        mFingerprint = null;
        mListener = null;
    }

    /**
     * Handle the fingerprint event and clone it to FingerprintEvent to send to the caller app.
     */
    private void handleEvent(Object obj) {

        try {

            Object event = CLASS_VCSDATA.cast(obj);
            if (event == null) {
                Log.e(TAG, "casting VCSDATA object failed");
                return;
            }

            int id = -1, status = -1, result = -1; Object data = null;

            for (Field field : FIELDS_VCSDATA) {
                if (field.getName().equalsIgnoreCase("eventId"))
                    id = field.getInt(event);
                else if (field.getName().equalsIgnoreCase("eventData"))
                    data = field.get(event);
            }

            FingerprintEvent eventdata = new FingerprintEvent(id, data);

            if (mListener != null) {
                mListener.onEvent(eventdata);
            }
            eventdata = null;

        } catch (Exception e) {
            Log.e(TAG, "handleEvent() failed: ", e);
            e.printStackTrace();
        }
    }

    /**
     * Convert generic Object to EnrollProgress object and return.
     */
    public EnrollCaptureStatus getEnrollStatus(Object eventData) {

        if (eventData == null) {
            Log.i(TAG, "getEnrollStatus()::Invalid event data");
            return null;
        }

        EnrollCaptureStatus enrollStatus = null;
        try {

            int quality = -1, templateResult = -1, totalSwipes = -1, badSwipes = -1, progress = -1;
            Class cls = eventData.getClass();
            Field[] fields = cls.getFields();
            for (Field field : fields) {
                if (field.getName().equalsIgnoreCase("imageQuality"))
                    quality = field.getInt(eventData);
                else if (field.getName().equalsIgnoreCase("templateResult"))
                    templateResult = field.getInt(eventData);
                else if (field.getName().equalsIgnoreCase("totalSwipes"))
                    totalSwipes = field.getInt(eventData);
                else if (field.getName().equalsIgnoreCase("badSwipes"))
                    badSwipes = field.getInt(eventData);
                else if (field.getName().equalsIgnoreCase("progress"))
                    progress = field.getInt(eventData);
            }
            enrollStatus = new EnrollCaptureStatus(quality, templateResult, totalSwipes, badSwipes,
                    progress);
        } catch (Exception e) {
            Log.i(TAG, "getEnrollStatus::Exception caught");
            e.printStackTrace();
        }
        return enrollStatus;
    }

    /**
     * Convert generic Object to CapturedImageData object and return.
     */
    public FingerprintBitmap getFingerprint(Object eventData) {

        FingerprintBitmap vcsFp = null;

        if (eventData == null) {
            Log.i(TAG, "getFingerprint()::Invalid event data");
            return vcsFp;
        }

        try {
            Bitmap fp = null;
            int quality = -1;
            Class cls = eventData.getClass();
            Field[] fields = cls.getFields();
            for (Field field : fields) {
                if (field.getName().equalsIgnoreCase("fingerprint"))
                    fp = (Bitmap) field.get(eventData);
                else if (field.getName().equalsIgnoreCase("quality"))
                    quality = field.getInt(eventData);
            }
            vcsFp = new FingerprintBitmap(fp, quality);
        } catch (Exception e) {
            Log.i(TAG, "getFingerprint()::Exception caught");
            e.printStackTrace();
        }
        return vcsFp;
    }

    private void log(String message) {
        if(DBG) Log.i (TAG, message);
    }
}

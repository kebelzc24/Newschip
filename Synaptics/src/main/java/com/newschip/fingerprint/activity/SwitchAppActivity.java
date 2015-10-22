package com.newschip.fingerprint.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.newschip.fingerprint.FingerprintApiWrapper;
import com.newschip.fingerprint.FingerprintApiWrapper.FingerprintEvent;
import com.newschip.fingerprint.R;
import com.newschip.fingerprint.dialog.SweetAlertDialog;
import com.newschip.fingerprint.dialog.SweetAlertDialog.OnSweetClickListener;
import com.newschip.fingerprint.provider.ProviderHelper;
import com.newschip.fingerprint.utils.ConstantUtils;
import com.newschip.fingerprint.utils.SystemBarTintManager;

public class SwitchAppActivity extends BaseActivity implements OnClickListener,
        OnSweetClickListener, FingerprintApiWrapper.EventListener {
    private String TAG = "kebelzc24_SwitchAppActivity";
    private RelativeLayout mRelativeLayout;
    private ImageView mImageView;
    private TextView mTextView;
    private ListView mListView;

    private FingerprintApiWrapper mFingerprint = null;
    private KeyguardManager mKeygaurdMgr;
    private Semaphore mLock = new Semaphore(1);
    private static int mFpIndex = 1;
    private static int mFpMask = 0;
    private ProviderHelper mProviderHelper;
    
    public final String TABLE_USAGE = "finger_useage";
    
    private ArrayList<HashMap<String, Object>> mListItems;
    private static final String ITEM_NAME = "itemName";
    private static final String ITEM_DESC = "itemDesc";
    private static final String ITEM_FINGER = "itemFinger";
    private SimpleAdapter mListAdapter;
    private MenuInitTask mMenuInitTask;

    private Toolbar mToolbar;
    @Override
    protected int getLayoutView() {
        return R.layout.activity_fast_switch_app;
    }
    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        initToolbar();
        mKeygaurdMgr = (KeyguardManager) mContext
                .getSystemService(Context.KEYGUARD_SERVICE);
        

        mProviderHelper = new ProviderHelper(mContext, TABLE_USAGE);
        initViews();
        mMenuInitTask = new MenuInitTask();
        mMenuInitTask.execute(mContext);
    }
    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        super.initToolbar(mToolbar);
        mToolbar.setTitle(R.string.switch_app);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void initViews() {
        //setCommonTitleBarTitle(R.string.switch_app);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.switch_layout);
        mRelativeLayout.setOnClickListener(this);
        mRelativeLayout.setClickable(false);
        mImageView = (ImageView) findViewById(R.id.imageView);

        mTextView = (TextView) findViewById(R.id.tv_switch);
        mListView = (ListView) findViewById(R.id.listView);
        mListItems = new ArrayList<HashMap<String, Object>>();
        if (mSwitchStateHelper.getSwitchState()) {
            mListView.setVisibility(View.VISIBLE);
            mImageView.setImageResource(R.mipmap.button_selected);
        } else {
            mListView.setVisibility(View.INVISIBLE);
            mImageView.setImageResource(R.mipmap.button_unselect);
        }

    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        if (view == mRelativeLayout) {
            if (mSwitchStateHelper.getSwitchState()) {
                mSwitchStateHelper.setSwitchState("0");
                mImageView.setImageResource(R.mipmap.button_unselect);
                mListView.setVisibility(View.INVISIBLE);
            } else {
                mSwitchStateHelper.setSwitchState("1");
                mImageView.setImageResource(R.mipmap.button_selected);
                refreshFingerListView();
                mListView.setVisibility(View.VISIBLE);
            }
            startOrStopWatchDogService();
        }

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (mSwitchStateHelper.getSwitchState()) {
            refreshFingerListView();
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (mMenuInitTask != null) {
            mMenuInitTask.cancel();
        }
    }

    private void clean(){
        if (mFingerprint != null) {
            mFingerprint.cleanUp();
            mFingerprint = null;
        }
    }
    protected void onDestroy() {
        super.onDestroy();

        try {
            mLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {

            clean();
        }
    }

    private void initValidityLib() {
        if (mFingerprint == null) {
            // create instance of fingerprint wrapper
            mFingerprint = new FingerprintApiWrapper(mContext, this);
        }
    }

    private int getNextFingerIndex() {
        int fpIndex = 1;
        final int fingermask = mFingerprint.getEnrolledFingerList(ConstantUtils.USER_ID);
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
        return fpIndex;
    }

    public class MenuInitTask extends AsyncTask<Context, Void, Integer> {

        ProgressDialog mBusyDialog;
        Handler mMenuTaskHandler = new Handler();
        boolean mCancel = false;

        @Override
        protected void onPreExecute() {
            if (mSwitchStateHelper.getSwitchState()) {
                showBusyDialog(true);
            }
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
            mCancel = true;
            mLock.release();
            showBusyDialog(false);
            clean();
            Log.d(TAG, "mFpIndex = " + mFpIndex);
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Integer result) {
            mFpIndex = result;
            if (mFpIndex > 1) {
                mRelativeLayout.setClickable(true);
                mFpMask = mFingerprint
                        .getEnrolledFingerList(ConstantUtils.USER_ID);
            } else {// no finger
                clean();
                mSwitchStateHelper.setSwitchState("0");
                showAlertDialog(mContext, NORMAL_TYPE);
            }

            if (mSwitchStateHelper.getSwitchState()) {
                refreshFingerListView();
            }

            clean();
            showBusyDialog(false);
            super.onPostExecute(result);

        }

        private void showBusyDialog(boolean visible) {
            mMenuTaskHandler.removeCallbacksAndMessages(null);
            if (visible) {
                mBusyDialog = ProgressDialog.show(mContext, "",
                        getString(R.string.loading), true);
                mMenuTaskHandler.postDelayed(new Runnable() {
                    public void run() {
                        if (mBusyDialog.isShowing()) {
                            mBusyDialog.dismiss();
                        }
                    }
                }, 6000);
            } else {
                if (mBusyDialog != null && mBusyDialog.isShowing()) {
                    mBusyDialog.dismiss();
                }
            }
        }

        public void cancel() {
            showBusyDialog(false);
            super.cancel(true);
        }

    }

    private void refreshFingerListView() {
        if (mFpIndex >= 1) {
            if (mFpMask != -1) {
                HashMap<String, Object> hm;
                mListItems.clear();
                int i = 1;
                for (i = 1; i <= 10; i++) {
                    if (((mFpMask >>> i) & 1) != 0) {
                        if(!mProviderHelper.isFingerIndexInProvider(String.valueOf(i))){
                            mProviderHelper.insertFingerIndexData(mContext, String.valueOf(i));
                        }
                        hm = new HashMap<String, Object>();
                        String name = mProviderHelper
                                .getFingerNameWithFingerIndex(i);
                        hm.put(ITEM_NAME,
                                name == null ? getString(R.string.finger, i)
                                        : name);
                        String tmp;
                        if (mProviderHelper.getPackageNameWithFingerIndex(i) != null) {
                            String pkg = mProviderHelper
                                    .getPackageNameWithFingerIndex(i);
                            tmp = getResources().getString(
                                    R.string.finger_usage_with_app,
                                    mPackageUtils.getLabelWithPackgeName(
                                            mContext, pkg));
                        } else {
                            tmp = getString(R.string.finger_usage);
                        }
                        hm.put(ITEM_DESC, tmp);
                        hm.put(ITEM_FINGER, Integer.toString(i));
                        if (mListItems != null) {
                            mListItems.add(hm);
                        }

//                        mProviderHelper.insertFingerData(mContext, null, null,
//                                Integer.toString(i),
//                                getString(R.string.finger, i));
                    }
                }
            }
        }
        if (mListItems.size() > 0) {
            mListAdapter = new SimpleAdapter(mContext, mListItems,
                    R.layout.choose_lock_fingerprint_list_item, new String[] {
                            ITEM_NAME, ITEM_DESC }, new int[] { R.id.text1,
                            R.id.text2 });
            mListView.setAdapter(mListAdapter);
            mListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1,
                        int position, long id) {
                    // TODO Auto-generated method stub
                    final String item = (String) mListItems.get(position).get(
                            ITEM_NAME);
                    final String index = (String) mListItems.get(position).get(
                            ITEM_FINGER);
                    Intent intent = new Intent(mContext, AppListActivity.class);
                    intent.putExtra("provider_table", "finger_useage");
                    intent.putExtra(ITEM_NAME, item);
                    intent.putExtra(ITEM_FINGER, index);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onEvent(FingerprintEvent fpEvent) {
        // TODO Auto-generated method stub

    }


    @Override
    public void showAlertDialog(Context context, int type) {
        // TODO Auto-generated method stub
        SweetAlertDialog alert = new SweetAlertDialog(context, type);
        alert.setTitleText(getResources()
                .getString(R.string.please_encroll));
        alert.setConfirmText(getResources().getString(R.string.ok));
        alert.setConfirmClickListener(this);
        alert.show();
    }

    @Override
    public void onClick(SweetAlertDialog sweetAlertDialog) {
        // TODO Auto-generated method stub
        startActivitySafely(ConstantUtils.ACTION_CHOOSELOCKFINGERPRINT);
        finish();
//        sweetAlertDialog.dismiss();
    }

}

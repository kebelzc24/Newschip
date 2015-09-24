package com.newschip.fingerprint.activity;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.newschip.fingerprint.R;
import com.newschip.fingerprint.dialog.SweetAlertDialog;
import com.newschip.fingerprint.dialog.SweetAlertDialog.OnSweetClickListener;
import com.newschip.fingerprint.provider.ProviderHelper;
import com.silead.fp.utils.FpControllerNative;
import com.silead.fp.utils.FpControllerNative.SLFpsvcFPInfo;
import com.silead.fp.utils.FpControllerNative.SLFpsvcIndex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class FastDialActivity extends BaseActivity implements OnClickListener,
        OnSweetClickListener {
    private String TAG = "kebelzc24_SwitchAppActivity";
    private RelativeLayout mRelativeLayout;
    private ImageView mImageView;
    private TextView mTextView;
    private ListView mListView;

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
	 private ArrayList<SLFpsvcFPInfo> mFingerList;
    private FpControllerNative mControllerNative;
    private SLFpsvcIndex fpsvcIndex;

    private Toolbar mToolbar;

    @Override
    protected int getLayoutView() {
        return R.layout.activity_fast_dail;
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
		mControllerNative = FpControllerNative.getInstance();
        mControllerNative.initFPSystem();
        mFingerList = new ArrayList<SLFpsvcFPInfo>();
        Log.d(TAG, "mFingerList size $$$$$$$ " + mFingerList.size());

        fpsvcIndex = mControllerNative.GetFpInfo();
        for (int i = 0; i < fpsvcIndex.max; i++) {
            if (fpsvcIndex.FPInfo[i].slot == 1) {
                mFingerList.add(fpsvcIndex.FPInfo[i]);
            }
        }
        if(mFingerList.size() == 0){
            mImageView.setImageResource(R.mipmap.button_unselect);
            mListView.setVisibility(View.INVISIBLE);
            showAlertDialog(mContext, SweetAlertDialog.NORMAL_TYPE);
        } else {
            mRelativeLayout.setClickable(true);
            getFingerList();
        }
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        super.initToolbar(mToolbar);
        mToolbar.setTitle(R.string.switch_app);
        mToolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initViews() {
        mRelativeLayout = (RelativeLayout) findViewById(R.id.switch_layout);
        mRelativeLayout.setOnClickListener(this);
        mRelativeLayout.setClickable(false);
        mImageView = (ImageView) findViewById(R.id.imageView);
//        mImageView.setOnClickListener(this);

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
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        mControllerNative.destroyFPSystem();

    }
    private void getFingerList(){
        HashMap<String, Object> hm;
        for(int j=0;j<mFingerList.size();j++){
            {
                int i = mFingerList.get(j).enrollIndex;
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

                // mProviderHelper.insertFingerData(mContext, null,
                // null,
                // Integer.toString(i),
                // getString(R.string.finger, i));
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
    private void refreshFingerList() {
        mListItems.clear();
        for (int i = 0; i < mFingerList.size(); i++) {
            if (!mProviderHelper.isFingerIndexInProvider(String.valueOf(i))) {
                mProviderHelper.insertFingerIndexData(mContext,
                        String.valueOf(i));
            }
            HashMap<String, Object> hm = new HashMap<String, Object>();
            String name = mFingerList.get(i).getFingerName();
            hm.put(ITEM_NAME, name == null ? getString(R.string.finger, i)
                    : name);
            String tmp;
            if (mProviderHelper.getPackageNameWithFingerIndex(i) != null) {
                String pkg = mProviderHelper.getPackageNameWithFingerIndex(i);
                tmp = getResources().getString(R.string.finger_usage_with_app,
                        mPackageUtils.getLabelWithPackgeName(mContext, pkg));
            } else {
                tmp = getString(R.string.finger_usage);
            }
            hm.put(ITEM_DESC, tmp);
            hm.put(ITEM_FINGER, Integer.toString(i));
            if (mListItems != null) {
                mListItems.add(hm);
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

    private void refreshFingerListView() {
        if (mFpIndex >= 1) {
            if (mFpMask != -1) {
                HashMap<String, Object> hm;
                mListItems.clear();
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

                        // mProviderHelper.insertFingerData(mContext, null,
                        // null,
                        // Integer.toString(i),
                        // getString(R.string.finger, i));
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
    public void showAlertDialog(Context context, int type) {
        // TODO Auto-generated method stub
        SweetAlertDialog alert = new SweetAlertDialog(context, type);
        alert.setTitleText(getResources().getString(R.string.please_encroll));
        alert.setConfirmText(getResources().getString(R.string.ok));
        alert.setConfirmClickListener(this);
        alert.show();
    }

    @Override
    public void onClick(SweetAlertDialog sweetAlertDialog) {
        // TODO Auto-generated method stub
        startActivity(new Intent(mContext,ManagerFingerPrint.class));
        finish();
        // sweetAlertDialog.dismiss();
    }

}

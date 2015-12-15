package com.newschip.galaxy.activity;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.newschip.galaxy.R;
import com.newschip.galaxy.fingerprint.FingerPrint;
import com.newschip.galaxy.provider.ProviderHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class SwitchActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "kebelzc24_SwitchAppActivity";
    private RelativeLayout mNoFingerprintLayout;
    private ImageView mStateBtn;
    private TextView mTextView;
    private ListView mListView;

    private ArrayList<HashMap<String, Object>> mListItems;
    private static final String ITEM_NAME = "itemName";
    private static final String ITEM_DESC = "itemDesc";
    private static final String ITEM_FINGER = "itemFinger";
    private SimpleAdapter mListAdapter;


    @Override
    public int getLayoutView() {
        return R.layout.activity_switch;
    }

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        mFingerPrint = new FingerPrint(mContext);
        mNoFingerprintLayout = (RelativeLayout) findViewById(R.id.rl_no_fingerprint);
        mStateBtn = (ImageView) findViewById(R.id.iv_imageView);
        mStateBtn.setOnClickListener(this);

        mTextView = (TextView) findViewById(R.id.tv_switch);
        mListView = (ListView) findViewById(R.id.lv_listView);
        mListItems = new ArrayList<HashMap<String, Object>>();
        if (ProviderHelper.isEnableSwitchState(mContext)) {
            mListView.setVisibility(View.VISIBLE);
            mStateBtn.setImageResource(R.mipmap.button_selected);
        } else {
            mListView.setVisibility(View.INVISIBLE);
            mStateBtn.setImageResource(R.mipmap.button_unselect);
        }
        if (!mFingerPrint.hasRegisteredFinger()){
            mNoFingerprintLayout.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }
    }



    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        if (view == mStateBtn) {
            if (ProviderHelper.isEnableSwitchState(mContext)) {
                ProviderHelper.enableSwitchState(mContext,false);
                mStateBtn.setImageResource(R.mipmap.button_unselect);
                mListView.setVisibility(View.INVISIBLE);
            } else {
                ProviderHelper.enableSwitchState(mContext, true);
                mStateBtn.setImageResource(R.mipmap.button_selected);
                mListView.setVisibility(View.VISIBLE);
            }
            startOrStopWatchDogService();
        }

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        getFingerList();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();

    }
    private void getFingerList(){
        mListItems.clear();
        HashMap<String, Object> hm;
        ArrayList<Integer> fingerIndex = mFingerPrint.getFingerIndexs();
        for(int j=0;j<fingerIndex.size();j++){
            {
                int i = fingerIndex.get(j);
                if(!ProviderHelper.isFingerIndexExit(mContext, i)){
                    //先将指纹信息保存到数据库
                    ProviderHelper.insertFingerIndex(mContext, i);
                }
                hm = new HashMap<String, Object>();
                hm.put(ITEM_NAME,
                        getString(R.string.finger, i));
                String tmp;
                String pkg = ProviderHelper.getPackageWithFingerIndex(mContext,i);
                if(pkg!=null){
                    tmp = getString(R.string.finger_usage_with_app,ProviderHelper.getAppNameWithFingerIndex(mContext,i));
                }else{
                    tmp = getString(R.string.finger_usage);
                }
                hm.put(ITEM_DESC, tmp);
                hm.put(ITEM_FINGER, i);
                if (mListItems != null) {
                    mListItems.add(hm);
                }

            }
        }
        if (mListItems.size() > 0) {
            mListAdapter = new SimpleAdapter(mContext, mListItems,
                    R.layout.fingerprint_list_item, new String[] {
                    ITEM_NAME, ITEM_DESC }, new int[] { R.id.text1,
                    R.id.text2 });
            mListView.setAdapter(mListAdapter);
            mListAdapter.notifyDataSetChanged();
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1,
                                        int position, long id) {
                    // TODO Auto-generated method stub
                    final String item = (String) mListItems.get(position).get(
                            ITEM_NAME);
                    final int index = (Integer) mListItems.get(position).get(
                            ITEM_FINGER);
                    Intent intent = new Intent(mContext, AppListActivity.class);
                    intent.putExtra(AppListActivity.EXTRA_TYPE, AppListActivity.TYPE_SWITCH);
                    intent.putExtra(AppListActivity.EXTRA_FINGER_INDEX, index);
                    startActivity(intent);
                }
            });
        }

    }




}

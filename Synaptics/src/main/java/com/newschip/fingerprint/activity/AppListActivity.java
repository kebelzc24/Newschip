package com.newschip.fingerprint.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.newschip.fingerprint.R;
import com.newschip.fingerprint.provider.ProviderHelper;
import com.newschip.fingerprint.view.CommonTitleLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AppListActivity extends BaseActivity implements OnClickListener {
    AppListAdapter listItemAdapter;
    ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
    private String PROVIDER_TABLE;
    private String FINGER_NAME;
    private String ITEM_FINGER;
    private final String TABLE_PROTECT = "app_protect";
    private final String TABLE_USAGE = "finger_useage";
    private ProviderHelper mProviderHelper;

    private RelativeLayout mRelativeLayout;
    private ImageView mImageView;

    private ListView mListView;
    private Toolbar mToolbar;
    @Override
    protected int getLayoutView() {
        return R.layout.activity_app_list;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PROVIDER_TABLE = getIntent().getStringExtra("provider_table");
        FINGER_NAME = getIntent().getStringExtra("itemName");
        ITEM_FINGER = getIntent().getStringExtra("itemFinger");
        if (PROVIDER_TABLE == null) {
            PROVIDER_TABLE = TABLE_PROTECT;
        }
        mProviderHelper = new ProviderHelper(this, PROVIDER_TABLE);

        initToolbar();

        mRelativeLayout = (RelativeLayout) findViewById(R.id.switch_layout);
        mRelativeLayout.setOnClickListener(this);
        mImageView = (ImageView) findViewById(R.id.imageView);
        

        mListView = (ListView) findViewById(R.id.list);

       
        // 采用ResolveInfo过滤应用
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> ResolveInfos = pm.queryIntentActivities(intent, 0);

        HashMap<String, Object> map = new HashMap<String, Object>();
        for (ResolveInfo ri : ResolveInfos) {
            if (ri.activityInfo.packageName.equals(getPackageName()))
                continue;
            if (PROVIDER_TABLE.equals(TABLE_PROTECT)) {
                map = new HashMap<String, Object>();
                map.put("appIcon", ri.loadIcon(pm));
                map.put("appName", ri.loadLabel(pm).toString());
                map.put("packageName", ri.activityInfo.packageName);
                map.put("arg0", ITEM_FINGER);
                items.add(map);
            } else if (PROVIDER_TABLE.equals(TABLE_USAGE)) {
                if (!(mProviderHelper
                        .isPkgRelateFinger(ri.activityInfo.packageName) && (!FINGER_NAME
                        .equals(mProviderHelper
                                .getFingerNameWithPackageName(ri.activityInfo.packageName))))) {
                    map = new HashMap<String, Object>();
                    map.put("appIcon", ri.loadIcon(pm));
                    map.put("appName", ri.loadLabel(pm).toString());
                    map.put("packageName", ri.activityInfo.packageName);
                    map.put("arg0", ITEM_FINGER);
                    items.add(map);
                }
            }

        }

        listItemAdapter = new AppListAdapter(this, items, PROVIDER_TABLE,
                FINGER_NAME);
        mListView.setAdapter(listItemAdapter);
        if (PROVIDER_TABLE.equals(TABLE_USAGE)) {
            mRelativeLayout.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        } else {
            if (mProtectStateHelper.getProtectState()) {
                mListView.setVisibility(View.VISIBLE);
                mImageView.setImageResource(R.mipmap.button_selected);
            } else {
                mListView.setVisibility(View.INVISIBLE);
                mImageView.setImageResource(R.mipmap.button_unselect);
            }
        }
        
    }

    private void initToolbar(){
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        super.initToolbar(mToolbar);
        mToolbar.setTitle(R.string.choose_app);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        if (view == mRelativeLayout) {
            if (mProtectStateHelper.getProtectState()) {
                mProtectStateHelper.setProtectState("0");
                mImageView.setImageResource(R.mipmap.button_unselect);
                mListView.setVisibility(View.INVISIBLE);
            } else {
                mProtectStateHelper.setProtectState("1");
                mImageView.setImageResource(R.mipmap.button_selected);
                mListView.setVisibility(View.VISIBLE);
            }
            startOrStopWatchDogService();
        }
    }

}
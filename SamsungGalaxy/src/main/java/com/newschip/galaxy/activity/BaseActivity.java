package com.newschip.galaxy.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.newschip.galaxy.R;
import com.newschip.galaxy.fingerprint.FingerPrint;
import com.newschip.galaxy.provider.ProviderHelper;
import com.newschip.galaxy.service.WatchDogService;

/**
 * Created by LQ on 2015/12/10.
 */
public abstract class BaseActivity extends AppCompatActivity {
    public Context mContext;
    public Toolbar mToolbar;
    public FingerPrint mFingerPrint;

    public abstract int getLayoutView();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutView());
        mContext = this;
        initToolbar();
    }

    public void initToolbar(){
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void startOrStopWatchDogService() {
        if (ProviderHelper.isEnableSwitchState(mContext) || ProviderHelper.isEnableProtectState(mContext)) {
            startService(new Intent(mContext, WatchDogService.class));
        } else {
            stopService(new Intent(mContext, WatchDogService.class));
        }
    }
}

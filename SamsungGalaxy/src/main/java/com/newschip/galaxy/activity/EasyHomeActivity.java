package com.newschip.galaxy.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.newschip.galaxy.R;
import com.newschip.galaxy.provider.ProviderHelper;
import com.newschip.galaxy.utils.PackageUtils;
import com.newschip.galaxy.utils.ToastUtils;

public class EasyHomeActivity extends BaseActivity implements View.OnClickListener{
    private RelativeLayout mRelativeLayout;
    private ImageView mImageView;
    @Override
    public int getLayoutView() {
        return R.layout.activity_easy_home;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getString(R.string.easy_home));
        mRelativeLayout = (RelativeLayout) findViewById(R.id.switch_layout);
        mRelativeLayout.setOnClickListener(this);
        mImageView = (ImageView) findViewById(R.id.imageView);
        if (ProviderHelper.isEnableEasyHomeState(mContext)) {
            mImageView.setImageResource(R.mipmap.button_selected);
        } else {
            mImageView.setImageResource(R.mipmap.button_unselect);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mRelativeLayout) {
            if(PackageUtils.hasPermission(mContext)){
                if (ProviderHelper.isEnableEasyHomeState(mContext)) {
                    ProviderHelper.enableEasyHomeState(mContext, false);
                    mImageView.setImageResource(R.mipmap.button_unselect);
                } else {
                    ProviderHelper.enableEasyHomeState(mContext, true);
                    mImageView.setImageResource(R.mipmap.button_selected);
                }
                startOrStopWatchDogService();
            } else {
                ToastUtils.show(mContext, "请勾选" + getString(R.string.app_name));
                PackageUtils.startUsageSetting(mContext);
            }

        }
    }
}

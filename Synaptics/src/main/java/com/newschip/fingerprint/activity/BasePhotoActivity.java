package com.newschip.fingerprint.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.newschip.fingerprint.R;
import com.newschip.fingerprint.gallery.ImageDownLoader;
import com.newschip.fingerprint.hide.ProviderHelper;
import com.newschip.fingerprint.utils.SystemBarTintManager;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BasePhotoActivity extends Activity {

    public ProviderHelper mProviderHelper;
    public TextView mCenterTitleText;
    public Button mSelectAllBtn;
    public Button mHideBtn;
    public Button mShowHideBtn;
    protected ImageLoader mImageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
       // requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(!(getClassName().equals("AlbumeSetActivity")||getClassName().equals("VideoPageActivity"))){
            setImmerseSystemUI();
        }
        mProviderHelper = new ProviderHelper(this);
        mImageLoader = ImageDownLoader.getImageLoader();
    }
    /**
     * 获取子类的类名
     */
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    public void initTitleView(){
        mCenterTitleText = (TextView) findViewById(R.id.tv_center_title);
        mSelectAllBtn = (Button) findViewById(R.id.btn_select_all);
        mHideBtn = (Button) findViewById(R.id.btn_hide);
        //mShowHideBtn = (Button) findViewById(R.id.btn_load_hide);
    }
    private void setImmerseSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            winParams.flags |= bits;
            win.setAttributes(winParams);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.title_bar_bg);// 通知栏所需颜色
        }
    }
}

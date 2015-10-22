package com.newschip.fingerprint.gallery;

import android.app.ActivityGroup;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.newschip.fingerprint.R;
import com.newschip.fingerprint.materialmenu.MaterialMenuDrawable;
import com.newschip.fingerprint.materialmenu.MaterialMenuDrawable.Stroke;
import com.newschip.fingerprint.materialmenu.MaterialMenuIcon;
import com.newschip.fingerprint.utils.SystemBarTintManager;
import com.newschip.fingerprint.video.VideoHideListActivity;
import com.newschip.fingerprint.video.VideoPageActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @项目名: NFingers_Sync
 * @包名: com.newschip.fingerprint.gallery
 * @类名: AlbumeNewSetActivity
 * @创建者: 邱英健
 * @创建时间: 2015-8-14 上午9:13:43
 * @描述: 隐藏文件主页面
 */

public class AlbumeNewSetActivity extends ActivityGroup {

    /** DrawerLayout */
    private DrawerLayout mDrawerLayout;
    /** 左边栏菜单 */
    private ListView mMenuListView;
    /** 右边栏 */
    private RelativeLayout right_drawer;
    /** 菜单列表 */
    private String[] mMenuTitles;
    /** Material Design风格 */
    private MaterialMenuIcon mMaterialMenuIcon;
    /** 菜单打开/关闭状态 */
    private boolean isDirection_left = false;
    /** 右边栏打开/关闭状态 */
    private boolean isDirection_right = false;
    private View showView;

    /** 主界面布局 */
    private FrameLayout bodyView;

    /** 主界面显示内容 */
    private LinearLayout photo, video;
    private Context mContext;

    // 获取当前所在的页面
    private int localPosition = 0;

    private SimpleAdapter mAdapter;
    private List<HashMap<String, Object>> mHashMaps;
    private HashMap<String, Object> map;
    private int flags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albume_new_set);
        setSystemBarImmerse();

        mContext = this;
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mMenuListView = (ListView) findViewById(R.id.left_drawer);
        this.showView = mMenuListView;

        // 初始化菜单列表
        mMenuTitles = getResources().getStringArray(R.array.menu_array);

        mAdapter = new SimpleAdapter(this, getData(), R.layout.menu_array,
                new String[] { "image", "text" }, new int[] { R.id.image,
                R.id.text });
        mMenuListView.setAdapter(mAdapter);

        mMenuListView.setOnItemClickListener(new DrawerItemClickListener());

        // 设置抽屉打开时，主要内容区被自定义阴影覆盖
        mDrawerLayout.setDrawerShadow(R.mipmap.drawer_shadow,
                GravityCompat.START);
        // 设置ActionBar可见，并且切换菜单和内容视图
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mMaterialMenuIcon = new MaterialMenuIcon(this, Color.WHITE, Stroke.THIN);
        mDrawerLayout.setDrawerListener(new DrawerLayoutStateListener());
        initMainView();

        // 默认显示界面
        if (savedInstanceState == null) {
            selectItem(0);
        }

    }

    private List<HashMap<String, Object>> getData() {

        mHashMaps = new ArrayList<HashMap<String, Object>>();
        map = new HashMap<String, Object>();
        map.put("image", R.mipmap.picture);
        map.put("text", "照片");
        mHashMaps.add(map);

        map = new HashMap<String, Object>();
        map.put("image", R.mipmap.video);
        map.put("text", "视频");
        mHashMaps.add(map);

        return mHashMaps;
    }

    /*
     * 初始化主界面
     */
    public void initMainView() {
        bodyView = (FrameLayout) findViewById(R.id.content_frame);
        photo = (LinearLayout) findViewById(R.id.album_gridview);
        video = (LinearLayout) findViewById(R.id.video_gridview);

    }

    /**
     * ListView上的Item点击事件
     *
     */
    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            selectItem(position);
        }
    }

    /**
     * DrawerLayout状态变化监听
     */
    private class DrawerLayoutStateListener extends
            DrawerLayout.SimpleDrawerListener {
        /**
         * 当导航菜单滑动的时候被执行
         */
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            showView = drawerView;
            if (drawerView == mMenuListView) {// 根据isDirection_left决定执行动画
                mMaterialMenuIcon.setTransformationOffset(
                        MaterialMenuDrawable.AnimationState.BURGER_ARROW,
                        isDirection_left ? 2 - slideOffset : slideOffset);
            }
        }

        /**
         * 当导航菜单打开时执行
         */
        @Override
        public void onDrawerOpened(android.view.View drawerView) {
            if (drawerView == mMenuListView) {
                isDirection_left = true;
            } else if (drawerView == right_drawer) {
                isDirection_right = true;
            }
        }

        /**
         * 当导航菜单关闭时执行
         */
        @Override
        public void onDrawerClosed(android.view.View drawerView) {
            if (drawerView == mMenuListView) {
                isDirection_left = false;
            } else if (drawerView == right_drawer) {
                isDirection_right = false;
                showView = mMenuListView;
            }
        }
    }

    /**
     * 切换主视图区域的Fragment
     *
     * @param position
     */
    private void selectItem(int position) {
        switch (position) {
            case 0:
                localPosition = 0;
                bodyView.removeAllViews();
                View v = getLocalActivityManager().startActivity(
                        "photo",
                        new Intent(AlbumeNewSetActivity.this,
                                AlbumeSetActivity.class)).getDecorView();
                bodyView.addView(v);
                break;
            case 1:
                localPosition = 1;
                bodyView.removeAllViews();
                bodyView.addView(getLocalActivityManager().startActivity(
                        "video",
                        new Intent(AlbumeNewSetActivity.this,
                                VideoPageActivity.class)).getDecorView());
                break;
            default:
                break;
        }

        // 更新选择后的item和title，然后关闭菜单
        mMenuListView.setItemChecked(position, true);
        setTitle(mMenuTitles[position]);
        mDrawerLayout.closeDrawer(mMenuListView);
    }

    /**
     * 点击ActionBar上菜单
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                if (showView == mMenuListView) {
                    if (!isDirection_left) { // 左边栏菜单关闭时，打开
                        mDrawerLayout.openDrawer(mMenuListView);
                    } else {// 左边栏菜单打开时，关闭
                        mDrawerLayout.closeDrawer(mMenuListView);
                    }
                }
                break;
            case R.id.action_lock:
                if (localPosition == 0) {
                    Toast.makeText(mContext, "正在加载隐藏相册", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AlbumeNewSetActivity.this,
                            FileHideListActivity.class);
                    intent.putExtra("show_hide", true);
                    startActivityForResult(intent, 0);
                } else {
                    Toast.makeText(mContext, "正在加载隐藏视频", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AlbumeNewSetActivity.this,
                            VideoHideListActivity.class);
                    intent.putExtra("show_hide", true);
                    startActivityForResult(intent, 0);

                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 根据onPostCreate回调的状态，还原对应的icon state
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mMaterialMenuIcon.syncState(savedInstanceState);
    }

    /**
     * 根据onSaveInstanceState回调的状态，保存当前icon state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mMaterialMenuIcon.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    /**
     * 加载菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        // MediaUtils.syncMediaData(mContext,
        // Environment.getExternalStorageDirectory().getAbsolutePath()+"/2");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return getCurrentActivity().onKeyDown(keyCode, event);
    }
    public void setSystemBarImmerse() {
        // TODO Auto-generated method stub
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            flags |= bits;
            win.setAttributes(winParams);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.title_bar_bg);// 通知栏所需颜色
        }

    }
}

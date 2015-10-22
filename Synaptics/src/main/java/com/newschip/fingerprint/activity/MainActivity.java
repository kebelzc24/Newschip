package com.newschip.fingerprint.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.newschip.fingerprint.R;
import com.newschip.fingerprint.animation.RecyleViewItemAnimator;
import com.newschip.fingerprint.splash.SplashActivity;
import com.newschip.fingerprint.utils.ConstantUtils;
import com.newschip.fingerprint.utils.ToastUtils;

import net.youmi.android.AdManager;
import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;
import net.youmi.android.banner.AdViewListener;
import net.youmi.android.spot.SpotManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity implements OnClickListener {

    private final String TAG = ConstantUtils.TAG + "MainActivity";

    private Toolbar mToolbar;


    private RecyclerView mRecyclerView;
    private RecyleViewAdapter mRecyleViewAdapter;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerMenuListView;
    private ActionBarDrawerToggle mDrawerToggle;

    private View mDrawerRootView;
    private int mCurrentPosition = 0;
    private List<String> mDrawerContent;
    private List<RecyleViewItem> mRecyleViewItems = new ArrayList<RecyleViewItem>();


    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);

        mDrawerContent = Arrays.asList(getResources().getStringArray(R.array.drawer_content));
        initView();
//        initAD();
//        startService(new Intent(this, WatchDogService.class));

    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_main_new;
    }

    private void initAD() {
        AdManager.getInstance(this).init("fe7d2b2768c9d3e9",
                "8f2c2eb1cf1e9bd5", false);
//        AdManager.getInstance(this).init("85aa56a59eac8b3d",
//                 "a14006f66f58d5d7", false);
        SpotManager.getInstance(this).loadSpotAds();
        // 插屏出现动画效果，0:ANIM_NONE为无动画，1:ANIM_SIMPLE为简单动画效果，2:ANIM_ADVANCE为高级动画效果
        SpotManager.getInstance(this)
                .setAnimationType(SpotManager.ANIM_ADVANCE);
        // 设置插屏动画的横竖屏展示方式，如果设置了横屏，则在有广告资源的情况下会是优先使用横屏图。
        SpotManager.getInstance(this).setSpotOrientation(
                SpotManager.ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
//        showBanner();
    }

    private void showBanner() {

        // 广告条接口调用（适用于应用）
        // 将广告条adView添加到需要展示的layout控件中
        // LinearLayout adLayout = (LinearLayout) findViewById(R.id.adLayout);
        // AdView adView = new AdView(this, AdSize.FIT_SCREEN);
        // adLayout.addView(adView);

        // 广告条接口调用（适用于游戏）

        // 实例化LayoutParams(重要)
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        // 设置广告条的悬浮位置
        layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT; // 这里示例为右下角
        // 实例化广告条
        AdView adView = new AdView(this, AdSize.FIT_SCREEN);
        // 调用Activity的addContentView函数

        // 监听广告条接口
        adView.setAdListener(new AdViewListener() {

            @Override
            public void onSwitchedAd(AdView arg0) {
                Log.i("YoumiAdDemo", "广告条切换");
            }

            @Override
            public void onReceivedAd(AdView arg0) {
                Log.i("YoumiAdDemo", "请求广告成功");

            }

            @Override
            public void onFailedToReceivedAd(AdView arg0) {
                Log.i("YoumiAdDemo", "请求广告失败");
            }
        });
        this.addContentView(adView, layoutParams);
    }


    private void initView() {

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerMenuListView = (ListView) findViewById(R.id.left_drawer_listview);
        mDrawerRootView = (View) findViewById(R.id.left_drawer);
        initToolbar();
        initDrawerView();
        initRecyleView();
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        super.initToolbar(mToolbar);
        mToolbar.setTitle(R.string.app_name);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map;
        for (int i = 0; i < mDrawerContent.size(); i++) {
            map = new HashMap<String, Object>();
            map.put("title", mDrawerContent.get(i));
            list.add(map);
        }
        return list;
    }

    private void initDrawerListView() {
        SimpleAdapter adapter = new SimpleAdapter(this, getData(), R.layout.drawer_list_item_layout, new String[]{"title"}, new int[]{R.id.textView});
        mDrawerMenuListView.setAdapter(adapter);
        mDrawerMenuListView.setItemChecked(mCurrentPosition, true);
        mToolbar.setTitle(mDrawerContent.get(mCurrentPosition));
    }

    private void initDrawerView() {
        initDrawerListView();
        mDrawerMenuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDrawerMenuListView.setItemChecked(position, true);
                openOrCloseDrawer();
                mCurrentPosition = position;
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        startActivity(new Intent(mContext, AboutActivity.class));
                        break;
                    default:
                        break;
                }
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 0, 0) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                mToolbar.setTitle(R.string.app_name);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
                mToolbar.setTitle(mDrawerContent.get(mCurrentPosition));
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setScrimColor(getColors(R.color.drawer_scrim_color));

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mDrawerLayout.isDrawerOpen(mDrawerRootView)) {
            mDrawerLayout.closeDrawer(mDrawerRootView);
            return true;
        }
        moveTaskToBack(true);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
        if (mToolbar != null) {
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openOrCloseDrawer();
                }
            });
        }
    }

    private void initRecyleView() {
        mRecyleViewItems.clear();
        RecyleViewItem item1 = new RecyleViewItem(R.mipmap.finger, getStringRes(R.string.manager_finger_print), getStringRes(R.string.enroll_or_remove_finger_print));
        RecyleViewItem item2 = new RecyleViewItem(R.mipmap.app_lock, getStringRes(R.string.app_lock), getStringRes(R.string.app_may_be_locked));
        RecyleViewItem item3 = new RecyleViewItem(R.mipmap.app_switch, getStringRes(R.string.switch_app), getStringRes(R.string.switch_app_content));
        RecyleViewItem item4 = new RecyleViewItem(R.mipmap.file_hide, getStringRes(R.string.hidden_file), getStringRes(R.string.hidden_file_content));
        RecyleViewItem item5 = new RecyleViewItem(R.mipmap.finger, getStringRes(R.string.fast_dial), getStringRes(R.string.fast_dial_content));
        mRecyleViewItems.add(item1);
        mRecyleViewItems.add(item2);
        mRecyleViewItems.add(item3);
        mRecyleViewItems.add(item4);
        mRecyleViewItems.add(item5);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new RecyleViewItemAnimator());

        mRecyleViewAdapter = new RecyleViewAdapter(mRecyleViewItems);
        mRecyclerView.setAdapter(mRecyleViewAdapter);
        mRecyleViewAdapter.notifyItemRangeInserted(0, mRecyleViewItems.size() - 1);
    }

    private void initItemLayout() {
//        if (preferenceUtils.getBooleanParam(getString(R.string.card_note_item_layout_key), true)){
//            cardLayout = true;
//            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
//        }else {
//            cardLayout = false;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
//        }
    }

    private void openOrCloseDrawer() {
        if (mDrawerLayout.isDrawerOpen(mDrawerRootView)) {
            mDrawerLayout.closeDrawer(mDrawerRootView);
        } else {
            mDrawerLayout.openDrawer(mDrawerRootView);
        }
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
//        if (mFingerLayout == view) {
//            startActivitySafely(ConstantUtils.ACTION_CHOOSELOCKFINGERPRINT);
//        } else if (mAppLockLayout == view) {
//            startActivitySafely(ConstantUtils.ACTION_APPLISTACTIVITY);
//        } else if (mSwitchAppLayout == view) {
//            startActivitySafely(ConstantUtils.ACTION_FASTSWITCHAPPACTIVITY);
//        } else if (mHiddenFileLayout == view) {
//            startActivitySafely(ConstantUtils.ACTION_MHIDDENFILELAYOUT);
//        }

    }


    @Override
    protected void onStop() {
        // 如果不调用此方法，则按home键的时候会出现图标无法显示的情况。
//        SpotManager.getInstance(this).onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
//        SpotManager.getInstance(this).onDestroy();
        super.onDestroy();
    }

    private class RecyleViewItem implements Serializable {
        private int mIconId;
        private String mTitle;
        private String mContent;

        public RecyleViewItem(int mIconId, String mTitle, String mContent) {
            this.mIconId = mIconId;
            this.mTitle = mTitle;
            this.mContent = mContent;
        }

        public int getmIconId() {
            return mIconId;
        }

        public void setmIconId(int mIconId) {
            this.mIconId = mIconId;
        }

        public String getmTitle() {
            return mTitle;
        }

        public void setmTitle(String mTitle) {
            this.mTitle = mTitle;
        }

        public String getmContent() {
            return mContent;
        }

        public void setmContent(String mContent) {
            this.mContent = mContent;
        }

    }

    private class RecyleViewAdapter extends RecyclerView.Adapter<RecyleViewAdapter.ViewHolder> {

        private List<RecyleViewItem> mItems;

        public RecyleViewAdapter(List<RecyleViewItem> items) {
            this.mItems = items;
        }


        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_main, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
            RecyleViewItem item = mItems.get(i);
            viewHolder.title.setText(item.getmTitle());
            viewHolder.content.setText(item.getmContent());
            viewHolder.icon.setImageDrawable(getResources().getDrawable(item.getmIconId()));

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (i) {
                        case 0:
                            startActivitySafely(ConstantUtils.ACTION_CHOOSELOCKFINGERPRINT);
                            break;
                        case 1:
                            startActivitySafely(ConstantUtils.ACTION_APPLISTACTIVITY);
                            break;
                        case 2:
                            startActivitySafely(ConstantUtils.ACTION_FASTSWITCHAPPACTIVITY);
                            break;
                        case 3:
                            startActivitySafely(ConstantUtils.ACTION_MHIDDENFILELAYOUT);
                            break;
                        case 4:
                            startActivity(new Intent(mContext, ContactsActivity.class));
                            break;
                        case 5:
                            startActivity(new Intent(mContext, AboutActivity.class));
                            break;
                        default:
                            break;

                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mItems == null ? 0 : mItems.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView title;
            public TextView content;
            public ImageView icon;

            public ViewHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.tv_title);
                content = (TextView) itemView.findViewById(R.id.tv_content);
                icon = (ImageView) itemView.findViewById(R.id.iv_icon);
            }

        }
    }
}


package com.newschip.galaxy.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.newschip.galaxy.R;
import com.newschip.galaxy.provider.ProviderHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AppListActivity extends BaseActivity implements OnClickListener {
    private ListView mListView;
    AppListAdapter mAdapter;
    ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
//    private ProviderHelper mProviderHelper;

    private RelativeLayout mRelativeLayout;
    private ImageView mImageView;


    public static final String EXTRA_TYPE = "type";
    public static final String EXTRA_FINGER_NAME = "finger_name";
    public static final String EXTRA_FINGER_INDEX = "finger_index";
    //应用锁
    public static final String TYPE_PROTECT = "type_protect";
    //快速切换
    public static final String TYPE_SWITCH = "type_switch";
    private String mType;//mType = TYPE_PROTECT || mType = TYPE_SWITCH;
    private int mFingerIndex;

    @Override
    public int getLayoutView() {
        return R.layout.activity_app_list;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getResources().getString(R.string.choose_app));
        mType = getIntent().getStringExtra(EXTRA_TYPE);
        mFingerIndex = getIntent().getIntExtra(EXTRA_FINGER_INDEX,-1);
        if(mType ==null){
            mType = TYPE_PROTECT;
            getSupportActionBar().setTitle(getResources().getString(R.string.app_lock));
        }

        mRelativeLayout = (RelativeLayout) findViewById(R.id.switch_layout);
        mRelativeLayout.setOnClickListener(this);
        mImageView = (ImageView) findViewById(R.id.imageView);


        mListView = (ListView) findViewById(R.id.lv_listView);


        // 采用ResolveInfo过滤应用
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> ResolveInfos = pm.queryIntentActivities(intent, 0);

        HashMap<String, Object> map = new HashMap<String, Object>();
        for (ResolveInfo ri : ResolveInfos) {
            if (ri.activityInfo.packageName.equals(getPackageName()))
                continue;
            if (mType != null && mType.equals(TYPE_SWITCH)) {
                if (!ProviderHelper.isAppRelateFinger(mContext, ri.activityInfo.packageName)
                        || mFingerIndex==(ProviderHelper.getFingerIndexWithPackage(mContext, ri.activityInfo.packageName))) {
                    map = new HashMap<String, Object>();
                    map.put("appIcon", ri.loadIcon(pm));
                    map.put("appName", ri.loadLabel(pm).toString());
                    map.put("packageName", ri.activityInfo.packageName);
                    map.put("arg0", mFingerIndex);
                    items.add(map);
                }
            } else {
                map = new HashMap<String, Object>();
                map.put("appIcon", ri.loadIcon(pm));
                map.put("appName", ri.loadLabel(pm).toString());
                map.put("packageName", ri.activityInfo.packageName);
                map.put("arg0", mFingerIndex);
                items.add(map);
            }

        }

        mAdapter = new AppListAdapter(mContext, items);
        mListView.setAdapter(mAdapter);
        if (mType != null && mType.equals(TYPE_SWITCH)) {
            mRelativeLayout.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        } else {
            if (ProviderHelper.isEnableProtectState(mContext)) {
                mListView.setVisibility(View.VISIBLE);
                mImageView.setImageResource(R.mipmap.button_selected);
            } else {
                mListView.setVisibility(View.INVISIBLE);
                mImageView.setImageResource(R.mipmap.button_unselect);
            }
        }

    }


    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        if (view == mRelativeLayout) {
            if (ProviderHelper.isEnableProtectState(mContext)) {
                ProviderHelper.enableProtectState(mContext, false);
                mImageView.setImageResource(R.mipmap.button_unselect);
                mListView.setVisibility(View.INVISIBLE);
            } else {
                ProviderHelper.enableProtectState(mContext, true);
                mImageView.setImageResource(R.mipmap.button_selected);
                mListView.setVisibility(View.VISIBLE);
            }
            startOrStopWatchDogService();
        }
    }

    public class AppListAdapter extends BaseAdapter {

        private Context mContext;
        private ArrayList<HashMap<String, Object>> listData;


        public AppListAdapter(Context context,
                              ArrayList<HashMap<String, Object>> listData) {
            this.mContext = context;
            this.listData = listData;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return listData.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return listData.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub

            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.app_list_item, null);
                holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
                holder.TxtLabel = (TextView) convertView.findViewById(R.id.appName);
                holder.TxtPkg = (TextView) convertView.findViewById(R.id.packageName);
                holder.imageCheck = (ImageView) convertView.findViewById(R.id.imageCheck);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Drawable drawable = (Drawable) listData.get(position).get("appIcon");
            holder.imageView.setImageDrawable(drawable);
            final String label = (String) listData.get(position).get("appName");
            holder.TxtLabel.setText(label);
            final String pkg = (String) listData.get(position).get("packageName");
            holder.TxtPkg.setText(pkg);
            Resources res = mContext.getResources();
            if (mType != null && mType.equals(TYPE_PROTECT)) {
                if (ProviderHelper.isAppProtected(mContext, pkg)) {
                    holder.imageCheck.setImageDrawable(res.getDrawable(R.mipmap.av_checkbox_checked));
                } else {
                    holder.imageCheck.setImageDrawable(res.getDrawable(R.mipmap.av_checkbox_unchecked));
                }
            } else if (mType != null && mType.equals(TYPE_SWITCH)) {
                if (ProviderHelper.isAppRelateFinger(mContext, pkg)) {
                    holder.TxtPkg.setText(res.getString(R.string.finger_relate, mFingerIndex));
                    holder.imageCheck.setImageDrawable(res.getDrawable(R.mipmap.av_checkbox_checked));
                } else {
                    holder.imageCheck.setImageDrawable(res.getDrawable(R.mipmap.av_checkbox_unchecked));
                }
            }

            convertView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    // TODO Auto-generated method stub
                    if (mType != null && mType.equals(TYPE_PROTECT)) {
                        if (ProviderHelper.isAppProtected(mContext, pkg)) {
                            ProviderHelper.removeProtectedApp(mContext, pkg);
                        } else {
                            ProviderHelper.setAppProtected(mContext, pkg, label);
                        }
                    } else if (mType.equals(TYPE_SWITCH)) {
                        if (ProviderHelper.isAppRelateFinger(mContext, pkg)) {
                            ProviderHelper.updateFingerData(mContext, mFingerIndex);
                        } else {
                            ProviderHelper.updateFingerData(mContext, pkg, label, mFingerIndex);
                        }
                    }

                    notifyDataSetChanged();
                }
            });
            return convertView;
        }

        public class ViewHolder {
            ImageView imageView;
            TextView TxtLabel;
            TextView TxtPkg;
            ImageView imageCheck;
        }
    }

}
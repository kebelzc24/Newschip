package com.newschip.fingerprint.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.newschip.fingerprint.R;
import com.newschip.fingerprint.provider.ProviderHelper;

public class AppListAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<HashMap<String, Object>> listData;

    private String PROVIDER_TABLE;
    private String FINGER_NAME;

    private ProviderHelper mProviderHelper;
    
    public AppListAdapter(Context context,
            ArrayList<HashMap<String, Object>> listData, String tab, String name) {
        this.mContext = context;
        this.listData = listData;
        this.PROVIDER_TABLE = tab;
        this.FINGER_NAME = name;
        mProviderHelper = new ProviderHelper(context,PROVIDER_TABLE);
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
        if(convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.app_list_item, null);
            holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
            holder.TxtLabel = (TextView) convertView.findViewById(R.id.appName);
            holder.TxtPkg = (TextView) convertView.findViewById(R.id.packageName);
            holder.imageCheck = (ImageView) convertView.findViewById(R.id.imageCheck);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        Drawable drawable = (Drawable) listData.get(position).get("appIcon");
        holder.imageView.setImageDrawable(drawable);
        final String label = (String) listData.get(position).get("appName");
        holder.TxtLabel.setText(label);
        final String pkg = (String) listData.get(position).get("packageName");
        holder.TxtPkg.setText(pkg);
        final String arg0 = (String) listData.get(position).get("arg0");
        Resources res = mContext.getResources();
        if (PROVIDER_TABLE.equals(mProviderHelper.TABLE_PROTECT)){
            if (mProviderHelper.isPkgProtecet(pkg)) {
                holder.imageCheck.setImageDrawable(res.getDrawable(R.mipmap.av_checkbox_checked));
            } else {
                holder.imageCheck.setImageDrawable(res.getDrawable(R.mipmap.av_checkbox_unchecked));
            }
        } else if(PROVIDER_TABLE.equals(mProviderHelper.TABLE_USAGE)){
            if(mProviderHelper.isFingerRelatePkg(pkg, FINGER_NAME)){
                holder.TxtPkg.setText(res.getString(R.string.finger_relate,FINGER_NAME));
                holder.imageCheck.setImageDrawable(res.getDrawable(R.mipmap.av_checkbox_checked));
            } else {
                holder.imageCheck.setImageDrawable(res.getDrawable(R.mipmap.av_checkbox_unchecked));
            }
        }
        
        convertView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                if (PROVIDER_TABLE.equals(mProviderHelper.TABLE_PROTECT)){
                    if(mProviderHelper.isPkgProtecet(pkg)){
                        mProviderHelper.deletePackageData(mContext, pkg);
                    } else {
                        mProviderHelper.insertPackageData(mContext, pkg, label, arg0);
                    }
                } else if(PROVIDER_TABLE.equals(mProviderHelper.TABLE_USAGE)){
                    if(mProviderHelper.isPkgRelateFinger(pkg)){
                        mProviderHelper.updateFingerData(mContext, arg0, FINGER_NAME);
                    } else {
                        mProviderHelper.updateFingerData(mContext, pkg, label, arg0, FINGER_NAME);
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
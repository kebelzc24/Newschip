package com.newschip.fingerprint.gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.newschip.fingerprint.R;

public class GridViewAdapter extends BaseAdapter {
    private String names[];

    private int icons[];
    private Context mContext;
    private TextView name_tv;
    private ImageView img;
    private View deleteView;
    private boolean isShowDelete;// 根据这个变量来判断是否显示删除图标，true是显示，false是不显示

    public GridViewAdapter(Context mContext, int icons[]) {
        this.mContext = mContext;
        this.icons = icons;
    }

    public void setIsShowDelete(boolean isShowDelete) {
        this.isShowDelete = isShowDelete;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {

        return icons.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return icons[position];
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_item,
                null);
        img = (ImageView) convertView.findViewById(R.id.img);
        name_tv = (TextView) convertView.findViewById(R.id.name_tv);
        deleteView = convertView.findViewById(R.id.delete_markView);
        deleteView.setVisibility(isShowDelete ? View.VISIBLE : View.GONE);// 设置删除按钮是否显示
        img.setBackgroundResource(icons[position]);
//        name_tv.setText(names[position]);
        return convertView;
    }
}

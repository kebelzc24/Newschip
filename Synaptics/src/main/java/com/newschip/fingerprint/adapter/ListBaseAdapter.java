/**
 * 
 */
/**
 * @author LQ
 *
 */
package com.newschip.fingerprint.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.newschip.fingerprint.activity.AppListAdapter.ViewHolder;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListBaseAdapter extends BaseAdapter{
    private ArrayList<HashMap<String, Object>> listData;
    public ListBaseAdapter(ArrayList<HashMap<String, Object>> data){
        this.listData = data;
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
        return null;
    }

    public class ViewHolder {
    }
    
}
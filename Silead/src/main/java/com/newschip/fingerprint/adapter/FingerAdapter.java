package com.newschip.fingerprint.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.newschip.fingerprint.R;
import com.silead.fp.utils.FpControllerNative;
import com.silead.fp.utils.FpControllerNative.SLFpsvcFPInfo;

import java.util.ArrayList;
import java.util.HashMap;

public class FingerAdapter extends BaseAdapter {

    private final static String TAG = "FingerAdapter";
    private ArrayList<SLFpsvcFPInfo> mfingerlist;
    @SuppressLint("UseSparseArrays")
    private static HashMap<Integer, Boolean> check = new HashMap<Integer, Boolean>();
    @SuppressWarnings("unused")
    private Context mContext;
    private LayoutInflater mInflater;
    public static String BACKGROUND_COLOR_ENABLED = "#FFFFFF";
    public static String BACKGROUND_COLOR_DISABLED = "#CCCCCC";
    public static String BACKGROUND_COLOR_ONTOUCH = "#CCCCCC";
    // public static boolean isTouch = true;
    private FpControllerNative fpControllerNative;
    private ListView mSwipeListView ;
    private SwipListViewOnClick mSwipListViewOnClick;

    public interface SwipListViewOnClick {
        public void onRenameClick(int pos);

        public void onDeleteClick(int pos);
    }
    public FingerAdapter(Context context, ArrayList<SLFpsvcFPInfo> mFingerList, ListView slv, SwipListViewOnClick click) {
        mContext = context;
        mfingerlist = (ArrayList<SLFpsvcFPInfo>) mFingerList;
        mInflater = LayoutInflater.from(context);
        this.mSwipeListView = slv;
        this.mSwipListViewOnClick = click;
    }

    public void setFingerList(ArrayList<SLFpsvcFPInfo> mlist) {
        mfingerlist = mlist;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mfingerlist.size();
    }

    @Override
    public Object getItem(int pos) {
        // TODO Auto-generated method stub
        return mfingerlist.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        // TODO Auto-generated method stub
        return pos;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.package_row, parent, false);
            holder = new ViewHolder();
            holder.mFrontText = (TextView) convertView.findViewById(R.id.example_row_tv_title);
            holder.mBtnRename = (Button) convertView.findViewById(R.id.btn_rename);
            holder.mBtnDelete = (Button) convertView.findViewById(R.id.btn_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mBtnRename.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwipListViewOnClick.onRenameClick(position);
//                mSwipeListView.closeAnimate(position);
//                mSwipeListView.dismiss(position);
            }
        });
        holder.mBtnDelete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                mSwipListViewOnClick.onDeleteClick(position);
            }
        });
        String item = mfingerlist.get(position).getFingerName();
        holder.mFrontText.setText(item);
        fpControllerNative = FpControllerNative.getInstance();
        int enrollIndex = mfingerlist.get(position).enrollIndex;
        mfingerlist.get(position).setEnable(1);
        fpControllerNative.EnalbeCredential(enrollIndex, 1);
        check.put(enrollIndex, true);
        return convertView;

    }

    class ViewHolder {
        TextView mFrontText;
        Button mBtnRename, mBtnDelete;
    }

    public static HashMap<Integer, Boolean> getCheck() {
        return check;
    }

    public static void setCheck(HashMap<Integer, Boolean> check) {
        FingerAdapter.check = check;
    }

}

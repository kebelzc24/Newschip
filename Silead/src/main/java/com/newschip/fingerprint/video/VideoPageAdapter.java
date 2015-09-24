package com.newschip.fingerprint.video;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.newschip.fingerprint.tools.LocalImageView;
import com.newschip.fingerprint.R;

@SuppressWarnings("unused")
@SuppressLint("UseSparseArrays")
public class VideoPageAdapter extends BaseAdapter {
    private Point mPoint = new Point(0, 0);// 用来封装ImageView的宽和高的对象
    /**
     * 用来存储图片的选中情况
     */
    private ArrayList<VideoBean> mVideoBeans;
    private Context mContext;
    protected LayoutInflater mInflater;
    private OnVideoSelectListener mCallback = null;
    private ArrayList<VideoBean> mSelectVideo;

    private boolean mMultiChoiseMode = false;

    public void setMultiChoiseMode(boolean choise) {
        mMultiChoiseMode = choise;
    }

    public boolean getMultiChoiseMode() {
        return mMultiChoiseMode;
    }

    public void clearSelect() {
        mSelectVideo.clear();
    }

    public void selectAll() {
        mSelectVideo.clear();
        for (int i = 0; i < mVideoBeans.size(); i++) {
            mSelectVideo.add(mVideoBeans.get(i));
        }
    }

    public VideoPageAdapter(Context context, ArrayList<VideoBean> beans) {
        this.mVideoBeans = beans;
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        mSelectVideo = new ArrayList<VideoBean>();
    }

    @Override
    public int getCount() {
        return mVideoBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return mVideoBeans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        final ViewHolder viewHolder;
        final VideoBean bean = mVideoBeans.get(position);
        final String path = bean.getPath();
        final String name = bean.getName();
        final long size = bean.getSize();
        final long duration = bean.getDuration();

        View rootView = convertView;
        if (rootView == null) {

            viewHolder = new ViewHolder();
            rootView = mInflater.inflate(R.layout.video_grid_child_item, null);
            viewHolder.mOneImageView = (ImageView) rootView
                    .findViewById(R.id.imageview_item_one);
            viewHolder.mThumbnail = (LocalImageView) rootView
                    .findViewById(R.id.vedio_thumbnail);
            viewHolder.mCheckBox = (ImageView) rootView
                    .findViewById(R.id.child_checkbox);
            viewHolder.view = rootView.findViewById(R.id.view_ImageView_up);

            viewHolder.mVideoName = (TextView) rootView
                    .findViewById(R.id.tv_video_name);
            viewHolder.mVedioSize = (TextView) rootView
                    .findViewById(R.id.tv_size_duration);

            rootView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rootView.getTag();
        }
        viewHolder.mThumbnail.setImageBitmap(bean.getThumbnail());
        viewHolder.mVideoName.setText(name);
        viewHolder.mVedioSize.setText(mContext.getResources().getString(
                R.string.size_duration, getVedioSize(size),
                getVideoDuration(duration)));

        viewHolder.mOneImageView.setVisibility(View.GONE);
        viewHolder.mThumbnail.setVisibility(View.VISIBLE);
        viewHolder.mCheckBox.setVisibility(View.VISIBLE);

        if (mMultiChoiseMode) {
            // //解决checkbox选择乱序
            viewHolder.view.setVisibility(View.VISIBLE);
            viewHolder.mCheckBox.setVisibility(View.VISIBLE);
            if (mSelectVideo.contains(bean)) {
                viewHolder.mCheckBox
                        .setImageResource(R.mipmap.av_checkbox_checked);
                // viewHolder.view.setVisibility(View.VISIBLE);
            } else {
                viewHolder.mCheckBox
                        .setImageResource(R.mipmap.av_checkbox_unchecked);
                // viewHolder.view.setVisibility(View.GONE);
            }
        } else {
            viewHolder.mCheckBox.setVisibility(View.GONE);
            viewHolder.view.setVisibility(View.GONE);
        }

        viewHolder.view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mSelectVideo.contains(bean)) {
                    viewHolder.mCheckBox
                            .setImageResource(R.mipmap.av_checkbox_unchecked);
                    mSelectVideo.remove(bean);
                } else {
                    viewHolder.mCheckBox
                            .setImageResource(R.mipmap.av_checkbox_checked);
                    mSelectVideo.add(bean);
                }
            }
        });
        return rootView;
    }

    public static class ViewHolder {
        public ImageView mCheckBox, mOneImageView;
        public LocalImageView mThumbnail;
        private View view;
        private TextView mVideoName;
        private TextView mVedioSize;

    }

    public interface TextCallback {
        public void onListen(int count);
    }

    public interface OnVideoSelectListener {
        public void onVideoSelect(ArrayList<VideoBean> bean);
    }

    public void setCallback(OnVideoSelectListener callback) {
        this.mCallback = callback;
    }

    public void getSelectData() {
        mCallback.onVideoSelect(mSelectVideo);
        mSelectVideo.clear();
    }

    private String getVedioSize(long size) {
        return Formatter.formatFileSize(mContext, size);
    }

    private String getVideoDuration(long duration) {
        String tmp = "";
        int second = (int) (duration / 1000) % 60;
        int minute = (int) (duration / 1000 / 60) % 60;
        int hour = (int) (duration / 1000 / 60 / 60);
        if (hour == 0) {
            tmp = "00:";
        } else if (hour > 0 && hour < 10) {
            tmp = "0" + hour + ":";
        } else if (hour >= 10) {
            tmp = hour + ":";
        }
        if (minute == 0) {
            tmp = tmp + "00:";
        } else if (minute > 0 && minute < 10) {
            tmp = tmp + "0" + minute + ":";
        } else if (minute >= 10) {
            tmp = tmp + minute + ":";
        }
        if (second == 0) {
            tmp = tmp + "00";
        } else if (second > 0 && second < 10) {
            tmp = tmp + "0" + second;
        } else if (second >= 10) {
            tmp = tmp + second;
        }

        return tmp;

    }
}

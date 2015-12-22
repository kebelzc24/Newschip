package com.newschip.galaxy.media;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.newschip.galaxy.R;
import com.newschip.galaxy.widget.LocalImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.ArrayList;
import java.util.List;

public class MediaAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<MediaBean> mMediaBean;
    protected LayoutInflater mInflater;
    private OnMediaSelectListener mCallback = null;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mDisplayImageOptions;
    private ArrayList<MediaBean> mSelectMedia = new ArrayList<>();


    private boolean mMultiChoiseMode = false;

    public void setMultiChoiseMode(boolean choise) {
        mMultiChoiseMode = choise;
    }

    public boolean getMultiChoiseMode() {
        return mMultiChoiseMode;
    }

    public void clearSelect() {
        mSelectMedia.clear();
    }

    public void selectAll() {
        mSelectMedia.clear();
        for (MediaBean bean:mMediaBean){
            mSelectMedia.add(bean);
        }
    }

    public ArrayList<MediaBean> getList() {
        return mMediaBean;
    }

    public void setList(ArrayList<MediaBean> list) {
        this.mMediaBean = list;
    }

    public MediaAdapter(Context context, ArrayList<MediaBean> list, ImageLoader loader) {
        this.mMediaBean = list;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mImageLoader = loader;
        mDisplayImageOptions = ImageDownLoader.getDisplayImageOptions(R.mipmap.default_photo);
    }

    @Override
    public int getCount() {
        return mMediaBean.size();
    }

    @Override
    public Object getItem(int position) {
        return mMediaBean.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        final ViewHolder viewHolder;
        final MediaBean bean = mMediaBean.get(position);
        View rootView = convertView;
        if (rootView == null) {

            viewHolder = new ViewHolder();
            rootView = mInflater.inflate(R.layout.grid_child_item, null);
            viewHolder.mOneImageView = (ImageView) rootView
                    .findViewById(R.id.imageview_item_one);
            viewHolder.mThumbnail = (LocalImageView) rootView
                    .findViewById(R.id.iv_thumbnail);
            viewHolder.mCheckBox = (ImageView) rootView
                    .findViewById(R.id.child_checkbox);
            viewHolder.frontView = rootView.findViewById(R.id.view_ImageView_up);

            viewHolder.mName = (TextView) rootView
                    .findViewById(R.id.tv_name);
            viewHolder.mCountOrDuration = (TextView) rootView
                    .findViewById(R.id.tv_count_or_duration);
            viewHolder.mVideoPlayView = (RelativeLayout)rootView.findViewById(R.id.rl_videoplay);
            if(bean.getmType() == MediaBean.TYPE_IMAGE){
                viewHolder.mVideoPlayView.setVisibility(View.GONE);
                viewHolder.mName.setText(bean.getFolderName());
                viewHolder.mCountOrDuration
                        .setText(String.valueOf(bean.getImageCounts()));
                final String path = bean.getTopImagePath();
                ImageAware imageAware = new ImageViewAware(viewHolder.mThumbnail, false);
                mImageLoader.displayImage(Scheme.FILE.wrap(path), imageAware, mDisplayImageOptions);

            } else {
                viewHolder.mVideoPlayView.setVisibility(View.VISIBLE);
                if(bean.getmVideoThumbnail() == null){
                    viewHolder.mOneImageView.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.mThumbnail.setImageBitmap(bean.getmVideoThumbnail());
                }
                viewHolder.mName.setText(bean.getmVideoName());
                viewHolder.mCountOrDuration.setText(mContext.getResources().getString(
                        R.string.size_duration, getVideoSize(bean.getmVideoSize()),
                        getVideoDuration(bean.getmVideoDuration())));
            }

            rootView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rootView.getTag();
        }



        viewHolder.mThumbnail.setVisibility(View.VISIBLE);
        viewHolder.mCheckBox.setVisibility(View.VISIBLE);

        if (mMultiChoiseMode) {
            viewHolder.frontView.setVisibility(View.VISIBLE);
            viewHolder.mCheckBox.setVisibility(View.VISIBLE);
            if (mSelectMedia.contains(bean)) {
                viewHolder.mCheckBox
                        .setImageResource(R.mipmap.av_checkbox_checked);
            } else {
                viewHolder.mCheckBox
                        .setImageResource(R.mipmap.av_checkbox_unchecked);
            }
        } else {
            viewHolder.mCheckBox.setVisibility(View.GONE);
            viewHolder.frontView.setVisibility(View.GONE);
        }

        viewHolder.frontView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mSelectMedia.contains(bean)) {
                    viewHolder.mCheckBox
                            .setImageResource(R.mipmap.av_checkbox_unchecked);
                    mSelectMedia.remove(bean);

                } else {
                    viewHolder.mCheckBox
                            .setImageResource(R.mipmap.av_checkbox_checked);
                    mSelectMedia.add(bean);
                }

            }
        });
        return rootView;
    }

    public static class ViewHolder {
        public ImageView mCheckBox, mOneImageView;
        public LocalImageView mThumbnail;
        private View frontView;
        private TextView mName;
        private TextView mCountOrDuration;
        private RelativeLayout mVideoPlayView;

    }

    public interface OnMediaSelectListener {
        public void onMediaSelect(ArrayList<MediaBean> beans);
    }

    public void setOnMediaSelectListener(OnMediaSelectListener callback) {
        this.mCallback = callback;
    }

    public void getSelectMedia() {
        mCallback.onMediaSelect(mSelectMedia);
    }

    private String getVideoSize(long size) {
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

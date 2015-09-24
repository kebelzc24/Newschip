package com.newschip.fingerprint.gallery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.newschip.fingerprint.tools.LocalImageView;
import com.newschip.fingerprint.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

public class AlbumeSetAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<ImageBean> list;
    protected LayoutInflater mInflater;
    private OnAlbumeSetSelectListener mCallback = null;
    private List<String> mPathList;
    private List<String> mFolderList;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mDisplayImageOptions;


    private boolean mMultiChoiseMode = false;

    public void setMultiChoiseMode(boolean choise) {
        mMultiChoiseMode = choise;
        notifyDataSetChanged();
    }

    public boolean getMultiChoiseMode() {
        return mMultiChoiseMode;
    }

    public void clearSelect() {
        mPathList.clear();
        mFolderList.clear();
    }

    public void selectAll() {
        mPathList.clear();
        mFolderList.clear();
        for (int i = 0; i < list.size(); i++) {
            ImageBean bean = list.get(i);
            final String path = bean.getTopImagePath();
            final String folderName = bean.getFolderName();
            mPathList.add(path);
            mFolderList.add(folderName);
        }
    }

    public ArrayList<ImageBean> getList() {
        return list;
    }

    public void setList(ArrayList<ImageBean> list) {
        this.list = list;
    }

    public AlbumeSetAdapter(Context context, ArrayList<ImageBean> list, ImageLoader loader) {
        this.list = list;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mPathList = new ArrayList<String>();
        mFolderList = new ArrayList<String>();
        mImageLoader = loader;
        mDisplayImageOptions = ImageDownLoader.getDisplayImageOptions(R.mipmap.default_photo);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        final ViewHolder viewHolder;
        final ImageBean bean = list.get(position);
        final String path = bean.getTopImagePath();
        final String parentFolder = bean.getParentFolderPath();
        final String folderName = bean.getFolderName();
        View rootView = convertView;
        if (rootView == null) {

            viewHolder = new ViewHolder();
            rootView = mInflater.inflate(R.layout.albume_grid_child_item, null);
            viewHolder.mOneImageView = (ImageView) rootView
                    .findViewById(R.id.imageview_item_one);
            viewHolder.mImageView = (LocalImageView) rootView
                    .findViewById(R.id.child_image);
            viewHolder.mCheckBox = (ImageView) rootView
                    .findViewById(R.id.child_checkbox);
            viewHolder.view = rootView.findViewById(R.id.view_ImageView_up);

            viewHolder.mAlbumeSetName = (TextView) rootView
                    .findViewById(R.id.tv_albume_name);
            viewHolder.mAlbumeSetCount = (TextView) rootView
                    .findViewById(R.id.tv_albume_count);

            ImageAware imageAware = new ImageViewAware(viewHolder.mImageView, false);
            mImageLoader.displayImage(Scheme.FILE.wrap(path), imageAware, mDisplayImageOptions);
//            mImageLoader.displayImage(Scheme.FILE.wrap(path), viewHolder.mImageView);
            rootView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rootView.getTag();
        }
        viewHolder.mAlbumeSetName.setText(bean.getFolderName());
        viewHolder.mAlbumeSetCount
                .setText(String.valueOf(bean.getImageCounts()));

        viewHolder.mOneImageView.setVisibility(View.GONE);
        viewHolder.mImageView.setVisibility(View.VISIBLE);
        viewHolder.mCheckBox.setVisibility(View.VISIBLE);

        if (mMultiChoiseMode) {
            // //解决checkbox选择乱序
            viewHolder.view.setVisibility(View.VISIBLE);
            viewHolder.mCheckBox.setVisibility(View.VISIBLE);
            if (mPathList.contains(path)) {
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
                if (mPathList.contains(path)) {
                    viewHolder.mCheckBox
                            .setImageResource(R.mipmap.av_checkbox_unchecked);
                    mPathList.remove(path);
                    mFolderList.remove(folderName);
                } else {
                    viewHolder.mCheckBox
                            .setImageResource(R.mipmap.av_checkbox_checked);
                    mPathList.add(path);
                    mFolderList.add(folderName);
                }

            }
        });
//        viewHolder.mImageView.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.default_photo));
//        if(!mMultiChoiseMode){
//            ImageDownLoader.showLocationImage(path, viewHolder.mImageView,
//                    R.mipmap.default_photo);
//        }
        return rootView;
    }

    public static class ViewHolder {
        public ImageView mCheckBox, mOneImageView;
        public LocalImageView mImageView;
        private View view;
        private TextView mAlbumeSetName;
        private TextView mAlbumeSetCount;

    }

    public interface OnAlbumeSetSelectListener {
        public void onAlbumeSetSelect(List<String> path, List<String> folder);
    }

    public void setCallback(OnAlbumeSetSelectListener callback) {
        this.mCallback = callback;
    }

    public void getSelectData() {
        mCallback.onAlbumeSetSelect(mPathList, mFolderList);
        mPathList.clear();
        mFolderList.clear();
    }
    

}

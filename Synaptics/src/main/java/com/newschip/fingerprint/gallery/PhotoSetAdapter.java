package com.newschip.fingerprint.gallery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.newschip.fingerprint.tools.LocalImageView;
import com.newschip.fingerprint.tools.LocalImageView.OnMeasureListener;
import com.newschip.fingerprint.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

@SuppressWarnings("unused")
@SuppressLint("UseSparseArrays")
public class PhotoSetAdapter extends BaseAdapter {
    private GridView mGridView;
    private ArrayList<String> list;
    private Context context;
    protected LayoutInflater mInflater;
    private TextCallback textcallback = null;
    private ListCallback mListCallback = null;
    private List<String> mPathList;

    private HashMap<Integer, View> mHashMap = new HashMap<Integer, View>();

    private ImageLoader mImageLoader;
    private DisplayImageOptions mDisplayImageOptions;

    private boolean mMultiChoiseMode = false;

    public void setMultiChoiseMode(boolean choise) {
        mMultiChoiseMode = choise;
    }

    public boolean getMultiChoiseMode() {
        return mMultiChoiseMode;
    }

    public void clearSelect() {
        mPathList.clear();
    }

    public void selectAll() {
        mPathList.clear();
        for (int i = 0; i < list.size(); i++) {
            final String path = list.get(i);
            mPathList.add(path);
        }

    }

    public ArrayList<String> getList() {
        return list;
    }

    public void setList(ArrayList<String> list) {
        this.list = list;
    }

    public PhotoSetAdapter(Context context, ArrayList<String> list,
            GridView mGridView, ImageLoader loader) {
        this.list = list;
        this.mGridView = mGridView;
        this.context = context;
        mInflater = LayoutInflater.from(context);
        mPathList = new ArrayList<String>();
        mImageLoader = loader;
        mDisplayImageOptions = ImageDownLoader
                .getDisplayImageOptions(R.mipmap.default_photo);
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
        final String path = list.get(position);
        View rootView = convertView;

        if (rootView == null) {

            viewHolder = new ViewHolder();
            rootView = mInflater.inflate(R.layout.grid_child_item, null);
            // mHashMap.put(path, rootView);
            viewHolder.mOneImageView = (ImageView) rootView
                    .findViewById(R.id.imageview_item_one);
            viewHolder.mImageView = (LocalImageView) rootView
                    .findViewById(R.id.child_image);
            viewHolder.mCheckBox = (ImageView) rootView
                    .findViewById(R.id.child_checkbox);
            viewHolder.view = rootView.findViewById(R.id.view_ImageView_up);
            rootView.findViewById(R.id.rl_name_count).setVisibility(View.GONE);
            viewHolder.mImageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.default_photo));
            ImageLoader.getInstance().displayImage("file://" + path,
                    viewHolder.mImageView, mDisplayImageOptions);
            rootView.setTag(viewHolder);
        } else {
            // rootView = mHashMap.get(path);
            viewHolder = (ViewHolder) rootView.getTag();
        }

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
            } else {
                viewHolder.mCheckBox
                        .setImageResource(R.mipmap.av_checkbox_unchecked);
            }
        } else {
            viewHolder.mCheckBox.setVisibility(View.GONE);
            viewHolder.view.setVisibility(View.GONE);
        }

        viewHolder.view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mPathList.contains(path)) {
                    viewHolder.mCheckBox
                            .setImageResource(R.mipmap.av_checkbox_unchecked);
                    mPathList.remove(path);
                } else {
                    viewHolder.mCheckBox
                            .setImageResource(R.mipmap.av_checkbox_checked);
                    mPathList.add(path);
                }
            }
        });

        // if(!mMultiChoiseMode){
        // ImageDownLoader.showLocationImage(path, viewHolder.mImageView,
        // R.mipmap.default_photo);
        // }
        return rootView;
    }

    /*
     * private void addListener(ViewHolder viewHolder, final int position,final
     * String path) { // TODO Auto-generated method stub
     * 
     * }
     */

    /**
     * 给CheckBox加点击动画，利用开源库nineoldandroids设置动画
     * 
     * @param view
     */
    @SuppressLint("NewApi")
    /*
     * private void addAnimation(View view){ float [] vaules = new float[]{0.5f,
     * 0.6f, 0.7f, 0.8f, 0.9f, 1.0f, 1.1f, 1.2f, 1.3f, 1.25f, 1.2f, 1.15f, 1.1f,
     * 1.0f}; AnimatorSet set = new AnimatorSet();
     * set.playTogether(ObjectAnimator.ofFloat(view, "scaleX", vaules),
     * ObjectAnimator.ofFloat(view, "scaleY", vaules)); set.setDuration(150);
     * set.start(); }
     */
    /**
     * 获取选中的Item的position
     * @return
     */
    /*
     * public List<Integer> getSelectItems(){ List<Integer> list = new
     * ArrayList<Integer>(); for(Iterator<Map.Entry<Integer, Boolean>> it =
     * Bimp.mHashMap.entrySet().iterator(); it.hasNext();){ Map.Entry<Integer,
     * Boolean> entry = it.next(); if(entry.getValue()){
     * list.add(entry.getKey()); } }
     * 
     * return list; }
     */
    public static class ViewHolder {
        public ImageView mCheckBox, mOneImageView;
        public LocalImageView mImageView;
        private View view;
    }

    public interface TextCallback {
        public void onListen(int count);
    }

    public void setTextCallback(TextCallback listener) {
        textcallback = listener;
    }

    public interface ListCallback {
        public void onListener(List<String> path, String pathStr);
    }

    public void setListCallback(ListCallback callback) {
        this.mListCallback = callback;
    }

    private OnPhotoSelectListener mOnPhotoSelectListener;

    public void setOnPhotoSelectListener(OnPhotoSelectListener listener) {
        mOnPhotoSelectListener = listener;
    }

    public interface OnPhotoSelectListener {
        public void onPhotoSelect(List<String> paths);
    }

    public void getSelectData() {
        mOnPhotoSelectListener.onPhotoSelect(mPathList);
        mPathList.clear();
    }

}

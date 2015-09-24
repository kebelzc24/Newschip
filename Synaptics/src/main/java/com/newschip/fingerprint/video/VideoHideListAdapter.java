package com.newschip.fingerprint.video;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.newschip.fingerprint.gallery.ImageDownLoader;
import com.newschip.fingerprint.hide.FileObject;
import com.newschip.fingerprint.R;

public class VideoHideListAdapter extends BaseAdapter {

    private Context mContext;
    private List<FileObject> mFileObject;
    private ArrayList<String> mFileList;
    private List<FileObject> mSelectObjects;

    public VideoHideListAdapter(Context context, List<FileObject> objects) {
        this.mFileObject = objects;
        this.mContext = context;
        mFileList = new ArrayList<String>();
        mSelectObjects = new ArrayList<FileObject>();
        for (int i = 0; i < mFileObject.size(); i++) {
            mFileList.add(mFileObject.get(i).getPath());
        }

    }

    public void selectAll() {
        mSelectObjects.clear();
        for (int i = 0; i < mFileObject.size(); i++) {
            mSelectObjects.add(mFileObject.get(i));
        }
        // mSelectObjects = mFileObject;
    }

    public void clearSelect() {
        mSelectObjects.clear();
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mFileObject.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mFileObject.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.hide_list_item, null);
            holder.mTypeIcon = (ImageView) convertView
                    .findViewById(R.id.iv_type_icon);
            holder.mFileName = (TextView) convertView
                    .findViewById(R.id.tv_file_name);
            holder.mCheckImage = (ImageView) convertView
                    .findViewById(R.id.iv_checked);
            holder.mRelativeLayout = (RelativeLayout) convertView
                    .findViewById(R.id.rl_check);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final FileObject fo = mFileObject.get(position);
        if (fo.isImage()) {
            ImageDownLoader.showLocationImage(fo.getPath(), holder.mTypeIcon,
                    R.mipmap.default_photo);
        } else {
            holder.mTypeIcon.setImageDrawable(mContext.getResources()
                    .getDrawable(R.mipmap.video));
        }
        Log.d("liuzhicang",
                "mSelectObjects.contains(fo) = " + mSelectObjects.contains(fo));
        if (mSelectObjects.contains(fo)) {

            holder.mCheckImage.setImageDrawable(mContext.getResources()
                    .getDrawable(R.mipmap.av_checkbox_checked));
        } else {
            holder.mCheckImage.setImageDrawable(mContext.getResources()
                    .getDrawable(R.mipmap.av_checkbox_unchecked));
        }
        holder.mFileName.setText(fo.getName());
        convertView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(fo.getPath()); 
                intent.setDataAndType(uri , "video/mp4"); 
                mContext.startActivity(intent);
            }
        });
        holder.mRelativeLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (mSelectObjects.contains(fo)) {
                    holder.mCheckImage
                            .setImageResource(R.mipmap.av_checkbox_unchecked);
                    mSelectObjects.remove(fo);
                } else {
                    holder.mCheckImage
                            .setImageResource(R.mipmap.av_checkbox_checked);
                    mSelectObjects.add(fo);
                }
            }
        });
        return convertView;
    }

    private class ViewHolder {
        ImageView mTypeIcon;
        TextView mFileName;
        ImageView mCheckImage;
        RelativeLayout mRelativeLayout;
    }

    private OnPhotoSelectListener mOnPhotoSelectListener;

    public void setOnPhotoSelectListener(OnPhotoSelectListener listener) {
        mOnPhotoSelectListener = listener;
    }

    public interface OnPhotoSelectListener {
        public void onPhotoSelect(List<FileObject> obj);
    }

    public void getSelectData() {
        mOnPhotoSelectListener.onPhotoSelect(mSelectObjects);
        mSelectObjects.clear();
    }

}

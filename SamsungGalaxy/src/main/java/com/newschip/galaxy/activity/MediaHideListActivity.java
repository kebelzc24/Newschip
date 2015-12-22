package com.newschip.galaxy.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.newschip.galaxy.R;
import com.newschip.galaxy.dialog.DialogHelper;
import com.newschip.galaxy.media.FileObject;
import com.newschip.galaxy.media.ImageDownLoader;
import com.newschip.galaxy.media.MediaBean;
import com.newschip.galaxy.media.MediaUtils;
import com.newschip.galaxy.provider.ProviderHelper;
import com.newschip.galaxy.utils.FileUtils;
import com.newschip.galaxy.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MediaHideListActivity extends BaseActivity implements
        OnClickListener {

    private ArrayList<FileObject> mImageObject = new ArrayList<>();
    private ArrayList<FileObject> mVideoObject = new ArrayList<>();
    private TextView mNoMediaText;

    private final int RESULT_SUCCESS = 0;
    private final int RESULT_FAIL = 1;

    private ListView mImageListView, mVideoListView;
    private RelativeLayout mImageLayout, mVideoLayout;
    private MediaAdapter mImageAdapter, mVideoAdapter;

    private boolean isImageReady, isVideoReady;
    private LinearLayout mButtonsLayout;
    private Button mBtnSelectAll;
    private Button mBtnHide;

    private Dialog mDialog;
    private boolean needRefrsh;


    @Override
    public int getLayoutView() {
        return R.layout.activity_file_hide;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(getString(R.string.hide_file));
        mImageLayout = (RelativeLayout) findViewById(R.id.rl_image);
        mVideoLayout = (RelativeLayout) findViewById(R.id.rl_video);
        mImageListView = (ListView) findViewById(R.id.lv_image);
        mVideoListView = (ListView) findViewById(R.id.lv_video);
        mImageLayout.setOnClickListener(this);
        mVideoLayout.setOnClickListener(this);
        mNoMediaText = (TextView) findViewById(R.id.tv_no_hide_file);
        mButtonsLayout = (LinearLayout) findViewById(R.id.ll_buttons);
        mBtnSelectAll = (Button) findViewById(R.id.btn_select_all);
        mBtnHide = (Button) findViewById(R.id.btn_hide);
        mBtnSelectAll.setOnClickListener(this);
        mBtnHide.setOnClickListener(this);
        new LoadImageTask().execute(0);
        new LoadVideoTask().execute(0);
    }

    private boolean isReady() {
        return isImageReady && isVideoReady;
    }

    private class LoadImageTask extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mImageObject.clear();
            mDialog = DialogHelper.createLoadingDialog(mContext, "正在加载图片和视频");
            mDialog.show();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            File f = new File(FileUtils.getFileHidePath());
            if (f.exists()) {
                File[] files = f.listFiles();// 列出所有文件
                if (files != null) {
                    int count = files.length;// 文件个数
                    for (int i = 0; i < count; i++) {
                        File file = files[i];
                        FileObject fo = new FileObject(FileObject.TYPE_IMAGE);
                        fo.setName(file.getName());
                        fo.setPath(file.getAbsolutePath());
                        mImageObject.add(fo);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            isImageReady = true;
            if (isReady()) {
                initListView();
            }
        }
    }

    private class LoadVideoTask extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mVideoObject.clear();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            File f = new File(FileUtils.getVideoHidePath());
            if (f.exists()) {
                File[] files = f.listFiles();// 列出所有文件
                if (files != null) {
                    int count = files.length;// 文件个数
                    for (int i = 0; i < count; i++) {
                        File file = files[i];
                        FileObject fo = new FileObject(FileObject.TYPE_VIDEO);
                        fo.setName(file.getName());
                        fo.setPath(file.getAbsolutePath());
                        mVideoObject.add(fo);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            isVideoReady = true;
            if (isReady()) {
                initListView();
            }
        }
    }

    private void setViewVisibility() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        if (mImageObject.size() < 1 && mVideoObject.size() < 1) {
            mNoMediaText.setVisibility(View.VISIBLE);
            mButtonsLayout.setVisibility(View.GONE);
        }
        if (mImageObject.size() < 1) {
            mImageLayout.setVisibility(View.GONE);
            mImageListView.setVisibility(View.GONE);
        }
        if (mVideoObject.size() < 1) {
            mVideoLayout.setVisibility(View.GONE);
            mVideoListView.setVisibility(View.GONE);
        }
    }

    private void initListView() {
        setViewVisibility();
        mImageAdapter = new MediaAdapter(mImageObject);
        mImageListView.setAdapter(mImageAdapter);
        mVideoAdapter = new MediaAdapter(mVideoObject);
        mVideoListView.setAdapter(mVideoAdapter);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (needRefrsh) {
            setResult(RESULT_OK, new Intent());
        }
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        int id = view.getId();
        switch (id) {
            case R.id.btn_select_all:
                if (mBtnSelectAll.getText().equals(getString(R.string.select_all))) {
                    mBtnSelectAll.setText(getString(R.string.select_none));
                    mImageAdapter.selectAll();
                    mImageAdapter.notifyDataSetChanged();
                    mVideoAdapter.selectAll();
                    mVideoAdapter.notifyDataSetChanged();
                } else {
                    mBtnSelectAll.setText(getString(R.string.select_all));
                    mImageAdapter.clearSelect();
                    mImageAdapter.notifyDataSetChanged();
                    mVideoAdapter.clearSelect();
                    mVideoAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.btn_hide:
                if (mImageAdapter.getSelectData().size() < 1 && mVideoAdapter.getSelectData().size() < 1) {
                    ToastUtils.show(mContext, "没有选择项");
                } else {
                    needRefrsh = true;
                    ArrayList<FileObject> hideObjects = new ArrayList<>();
                    for (FileObject object : mImageAdapter.getSelectData()) {
                        hideObjects.add(object);
                    }
                    for (FileObject object : mVideoAdapter.getSelectData()) {
                        hideObjects.add(object);
                    }
                    new HideMediaTask(hideObjects).execute(0);
                }

                break;
            case R.id.rl_image:
                if (mImageListView.getVisibility() == View.VISIBLE) {
                    ((ImageView) findViewById(R.id.iv_image_state)).setImageDrawable(getResources().getDrawable(R.mipmap.arrow_collapse));
                    mImageListView.setVisibility(View.GONE);
                } else {
                    ((ImageView) findViewById(R.id.iv_image_state)).setImageDrawable(getResources().getDrawable(R.mipmap.arrow_expand));
                    mImageListView.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.rl_video:
                if (mVideoListView.getVisibility() == View.VISIBLE) {
                    ((ImageView) findViewById(R.id.iv_video_state)).setImageDrawable(getResources().getDrawable(R.mipmap.arrow_collapse));
                    mVideoListView.setVisibility(View.GONE);
                } else {
                    ((ImageView) findViewById(R.id.iv_video_state)).setImageDrawable(getResources().getDrawable(R.mipmap.arrow_expand));
                    mVideoListView.setVisibility(View.VISIBLE);
                }
                break;

            default:
                break;
        }
    }


    private class HideMediaTask extends AsyncTask<Integer, Integer, Integer> {
        List<FileObject> mHideBeans = Collections
                .synchronizedList(new ArrayList<FileObject>());

        public HideMediaTask(ArrayList<FileObject> beans) {
            for (FileObject bean : beans) {
                this.mHideBeans.add(bean);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = DialogHelper.createLoadingDialog(mContext, "正在操作，请稍后");
            mDialog.show();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            for (FileObject fo : mHideBeans) {
                String path = fo.getPath();
                String oldPath = ProviderHelper.getOldPath(mContext, path);
                String dest;
                if (TextUtils.isEmpty(oldPath)) {
                    oldPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/favor";
                    dest = oldPath;
                } else {
                    dest = new File(oldPath).getParent();
                }
                if (!FileUtils.moveFile(path, dest)) {
                    return RESULT_FAIL;
                }
                ProviderHelper.deleteHidePath(mContext, path);
                MediaUtils.syncMediaData(mContext, oldPath);
                if (fo.getmType() == FileObject.TYPE_IMAGE) {
                    mImageObject.remove(fo);
                    publishProgress(0);
                } else {
                    mVideoObject.remove(fo);
                    publishProgress(1);
                }
            }

            return RESULT_SUCCESS;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
//            if(values[0] == 0){
//                mImageAdapter.notifyDataSetChanged();
//            } else {
//                mVideoAdapter.notifyDataSetChanged();
//            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
//            new LoadImageTask().execute(0);
//            new LoadVideoTask().execute(0);

            if (mDialog != null) {
                mDialog.dismiss();
            }
            mImageAdapter.clearSelect();
            mImageAdapter.notifyDataSetChanged();
            mVideoAdapter.clearSelect();
            mVideoAdapter.notifyDataSetChanged();
            if (integer == RESULT_SUCCESS) {
                ToastUtils.show(mContext, "操作成功");
                setViewVisibility();
            } else {
                ToastUtils.show(mContext, "部分文件操作失败，请重试");
            }
        }
    }

    public class MediaAdapter extends BaseAdapter {

        private List<FileObject> mFileObject;
        private ArrayList<String> mFileList = new ArrayList<>();
        private List<FileObject> mSelectObjects = new ArrayList<>();

        public MediaAdapter(List<FileObject> mFileObject) {
            this.mFileObject = mFileObject;
            for(FileObject object:mFileObject){
                if(object.getmType()==FileObject.TYPE_IMAGE){
                    mFileList.add(object.getPath());
                }
            }
        }

        public void selectAll() {
            mSelectObjects.clear();
            for (int i = 0; i < mFileObject.size(); i++) {
                mSelectObjects.add(mFileObject.get(i));
            }
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
                    if(mFileObject.get(position).getmType()==FileObject.TYPE_IMAGE){
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("list", mFileList);
                        bundle.putInt("position", position - 1);
                        Intent intent = new Intent(mContext, ViewPhotoActivity.class);
                        intent.putExtras(bundle);
                        mContext.startActivity(intent);
                    } else {

                    }


                }
            });
            holder.mRelativeLayout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    if (mButtonsLayout.getVisibility() != View.VISIBLE) {
                        Animation animation = AnimationUtils.loadAnimation(
                                mContext, R.anim.slide_in_anim);
                        mButtonsLayout.setVisibility(View.VISIBLE);
                        mButtonsLayout.startAnimation(animation);
                    }
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


        public List<FileObject> getSelectData() {
            return mSelectObjects;
        }

    }
}

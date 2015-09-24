package com.newschip.fingerprint.video;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.newschip.fingerprint.activity.BasePhotoActivity;
import com.newschip.fingerprint.dialog.DialogHelper;
import com.newschip.fingerprint.gallery.AlbumeSetAdapter;
import com.newschip.fingerprint.hide.FileUtils;
import com.newschip.fingerprint.hide.MediaUtils;
import com.newschip.fingerprint.video.VideoPageAdapter.OnVideoSelectListener;
import com.newschip.fingerprint.R;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

public class VideoPageActivity extends BasePhotoActivity implements
        OnItemClickListener, OnItemLongClickListener, OnClickListener,
        OnVideoSelectListener {

    private Context mContext;
    private ArrayList<VideoBean> mVideoBeans = new ArrayList<VideoBean>();

    private VideoPageAdapter mAdapter;
    private GridView mGridView;
    private TextView mNoVideoText;
    private Dialog mHidingDialog;
    private final static int HIDE_OK = 1;
    private final static int HIDE_FAIL = 2;
    private final static int SCAN_OK = 3;

    final static String TAG = "LOGCAT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_video_set);
        // LoadVideoAsyncTask task = new LoadVideoAsyncTask();
        // task.execute(0);
    }

    private void initViews() {
        initTitleView();
        mHideBtn.setOnClickListener(this);
        mSelectAllBtn.setOnClickListener(this);
        // mShowHideBtn.setOnClickListener(this);
        // mCenterTitleText.setText("视频");

        initTileBar(false);
    }

    private void initAlbumePage() {
        mNoVideoText = (TextView) findViewById(R.id.tv_no_video);
        mGridView = (GridView) findViewById(R.id.video_gridview);
        mGridView.setVisibility(View.VISIBLE);
        mGridView.setOnItemLongClickListener(this);
        mGridView.setOnItemClickListener(this);
        // Toast.makeText(mContext, "size1:"+mVideoBeans.size(),
        // Toast.LENGTH_SHORT).show();
        if (mVideoBeans.size() < 1) {
            mNoVideoText.setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.GONE);
            // Toast.makeText(mContext, "size2:"+mVideoBeans.size(),
            // Toast.LENGTH_SHORT).show();
        } else {
            mNoVideoText.setVisibility(View.GONE);
            mAdapter = new VideoPageAdapter(mContext, mVideoBeans);
            mAdapter.setCallback(this);
            mGridView.setAdapter(mAdapter);
            mGridView.setVisibility(View.VISIBLE);
            // Toast.makeText(mContext, "size3:"+mVideoBeans.size(),
            // Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        LoadVideoAsyncTask task = new LoadVideoAsyncTask();
        task.execute(0);

    }

    public class LoadVideoAsyncTask extends AsyncTask<Integer, Void, Integer> {

        public LoadVideoAsyncTask() {
            // TODO Auto-generated constructor stub
            mVideoBeans.clear();
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Integer... arg0) {
            // TODO Auto-generated method stub
            Uri mUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            ContentResolver mContentResolver = mContext.getContentResolver();
            Cursor mCursor = mContentResolver.query(mUri, null, null, null,
                    MediaStore.Video.Media.DEFAULT_SORT_ORDER);
            while (mCursor.moveToNext()) {
                long id = mCursor.getLong(mCursor
                        .getColumnIndex(MediaColumns._ID));
                String path = mCursor.getString(mCursor
                        .getColumnIndex(MediaColumns.DATA));

                String name = mCursor.getString(mCursor
                        .getColumnIndex(MediaColumns.DISPLAY_NAME));
                long size = mCursor.getLong(mCursor
                        .getColumnIndex(MediaColumns.SIZE));
                long duration = mCursor.getLong(mCursor
                        .getColumnIndex(AudioColumns.DURATION));
                String mimeType = mCursor.getString(mCursor
                        .getColumnIndex(MediaColumns.MIME_TYPE));
                VideoBean bean = new VideoBean(path, name, size, duration);
                bean.setMimeType(mimeType);
                mVideoBeans.add(bean);
                // 通知Handler扫描图片完成
                // mHandler.sendEmptyMessage(SCAN_OK);
            }
            // mHandler.sendEmptyMessage(SCAN_OK);
            mCursor.close();
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            initViews();
            initAlbumePage();
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
            new LoadThumbnailTask().execute();
        }

    }

    public class LoadThumbnailTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            // TODO Auto-generated method stub
            Uri mUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            ContentResolver mContentResolver = mContext.getContentResolver();
            Cursor mCursor = mContentResolver.query(mUri, null, null, null,
                    MediaStore.Video.Media.DEFAULT_SORT_ORDER);
            int i = 0;
            while (mCursor.moveToNext()) {
                long id = mCursor.getLong(mCursor
                        .getColumnIndex(MediaColumns._ID));
                mVideoBeans.get(i).setThumbnail(getThumbnail(id));
                publishProgress(i);
                i++;
            }
            mCursor.close();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private Bitmap getThumbnail(long id) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inScaled = true;
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        ContentResolver cr = mContext.getContentResolver();
        bitmap = MediaStore.Video.Thumbnails.getThumbnail(cr, id,
                Images.Thumbnails.MINI_KIND, null);
        return bitmap;
    }

    private boolean checkSD() {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
            int position, long arg3) {
        // TODO Auto-generated method stub
        initTileBar(true);
        mAdapter.setMultiChoiseMode(true);
        mAdapter.notifyDataSetChanged();
        return true;
    }

    private void initTileBar(boolean hide) {
        if (hide) {
            // mShowHideBtn.setVisibility(View.GONE);
            mSelectAllBtn.setVisibility(View.VISIBLE);
            mHideBtn.setVisibility(View.VISIBLE);
        } else {
            // mShowHideBtn.setVisibility(View.VISIBLE);
            mSelectAllBtn.setVisibility(View.GONE);
            mHideBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
        case R.id.btn_load_hide:
            Toast.makeText(mContext, "正在加载隐藏视频", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(VideoPageActivity.this,
                    VideoHideListActivity.class);
            intent.putExtra("show_hide", true);
            startActivityForResult(intent, 0);

            break;
        case R.id.btn_hide:
            // 确定按钮的事件
            mAdapter.getSelectData();
            break;
        case R.id.btn_select_all:
            if (mSelectAllBtn.getText().equals("全选")) {
                mSelectAllBtn.setText("全不选");
                mAdapter.selectAll();
            } else {
                mSelectAllBtn.setText("全选");
                mAdapter.clearSelect();
            }
            mAdapter.notifyDataSetChanged();
            break;

        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position,
            long arg3) {
        // TODO Auto-generated method stub
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Uri uri = Uri.parse(mVideoBeans.get(position).getPath());
        intent.setDataAndType(uri, mVideoBeans.get(position).getMimeType());
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mAdapter != null && mAdapter.getMultiChoiseMode()) {
                mAdapter.setMultiChoiseMode(false);
                initTileBar(false);
                mAdapter.clearSelect();
                mAdapter.notifyDataSetChanged();
                return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onVideoSelect(ArrayList<VideoBean> beans) {
        // TODO Auto-generated method stub
        if (beans.size() < 1) {
            Toast.makeText(mContext, "请至少选择一个视频", Toast.LENGTH_SHORT).show();
        } else {
            mHidingDialog = DialogHelper.createLoadingDialog(mContext,
                    "正在隐藏中，请稍后...");
            mHidingDialog.show();
            initTileBar(false);
            // mHandler.sendMessage(Message.obtain(mHandler, DO_HIDE));
            new HideThread(beans).start();
        }
    }

    public class HideThread extends Thread {

        // ArrayList 不是线程安全的，需要使用Collections类使其变成同步的
        List<VideoBean> beans = Collections
                .synchronizedList(new ArrayList<VideoBean>());;

        public HideThread(List<VideoBean> beans) {
            // TODO Auto-generated constructor stub
            // this.paths = paths;
            // this.folder = folder;
            // 不能使用上面的方法赋值
            for (int i = 0; i < beans.size(); i++) {
                this.beans.add(beans.get(i));
            }

        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            for (int i = 0; i < beans.size(); i++) {
                String oldPath = beans.get(i).getPath();
                String newPath = FileUtils.getVideoHidePath();
                String name = beans.get(i).getName();
                if (!FileUtils.moveFile(oldPath, newPath)) {
                    mHandler.sendMessage(Message.obtain(mHandler, HIDE_FAIL));
                    break;
                }
                mProviderHelper.insertHidePath(name, newPath + name, oldPath);
                MediaUtils.deleteVideoData(mContext, new File(oldPath));
                MediaUtils.syncMediaData(mContext, oldPath);
            }
            mHandler.sendMessageDelayed(Message.obtain(mHandler, HIDE_OK), 100);

        }
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case HIDE_OK:
                if (mHidingDialog != null) {
                    mHidingDialog.dismiss();
                }
                LoadVideoAsyncTask task = new LoadVideoAsyncTask();
                task.execute(0);
                break;
            case HIDE_FAIL:
                Toast.makeText(mContext, "隐藏失败，请稍后重试!", 1000).show();
                break;
            case SCAN_OK:
                initAlbumePage();
                break;
            }
        }

    };

    protected void onActivityResult(int requestCode, int resultCode,
            Intent intent) {

        if (requestCode == 0 && intent != null
                && intent.getBooleanExtra("refresh", false)) {
            LoadVideoAsyncTask task = new LoadVideoAsyncTask();
            task.execute(0);
        }
    };

}
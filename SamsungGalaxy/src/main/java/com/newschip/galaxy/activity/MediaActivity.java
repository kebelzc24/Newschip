package com.newschip.galaxy.activity;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.newschip.galaxy.R;
import com.newschip.galaxy.dialog.DialogHelper;
import com.newschip.galaxy.media.ImageSort;
import com.newschip.galaxy.media.MediaAdapter;
import com.newschip.galaxy.media.MediaBean;
import com.newschip.galaxy.media.MediaUtils;
import com.newschip.galaxy.provider.ProviderHelper;
import com.newschip.galaxy.utils.FileUtils;
import com.newschip.galaxy.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MediaActivity extends BaseActivity implements MediaAdapter.OnMediaSelectListener, View.OnClickListener {

    private HashMap<String, ArrayList<String>> mImageGroup = new HashMap<String, ArrayList<String>>();
    private ArrayList<String> mChildList = new ArrayList<>();
    private ArrayList<String> mSubImages = new ArrayList<>();
    private ArrayList<MediaBean> mImageBean = new ArrayList<>();
    private ArrayList<MediaBean> mVideoBean = new ArrayList<>();
    private ArrayList<MediaBean> mMediaBean = new ArrayList<>();
    private boolean isImageReady = false;
    private boolean isVideoReady = false;

    private GridView mGridView;
    private MediaAdapter mAdapter;
    private LinearLayout mButtonsLayout;
    private Button mBtnSelectAll;
    private Button mBtnHide;
    private TextView mNoMediaText;
    private Dialog mDialog;

    public static final String EXTRA_ALBUME = "albume";
    public static final String EXTRA_PHOTO_LIST = "photo";
    private final int RESULT_SUCCESS = 0;
    private final int RESULT_FAIL = 1;

    private LoadImageTask mLoadImageTask;
    private LoadVideoTask mLoadVideoTask;
    private LoadVideoThumbnailTask mLoadVideoThumbnailTask;


    @Override
    public int getLayoutView() {
        return R.layout.activity_media;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle("图片和视频");
        mGridView = (GridView) findViewById(R.id.gv_gridview);
        mButtonsLayout = (LinearLayout) findViewById(R.id.ll_buttons);
        mBtnSelectAll = (Button) findViewById(R.id.btn_select_all);
        mBtnHide = (Button) findViewById(R.id.btn_hide);
        mBtnSelectAll.setOnClickListener(this);
        mBtnHide.setOnClickListener(this);
        mNoMediaText = (TextView) findViewById(R.id.tv_no_media);
        loadMedia();
        ToastUtils.show(mContext,"长按图标可选择需要隐藏的文件", Toast.LENGTH_SHORT);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void loadMedia() {
        if (mLoadImageTask == null) {
            mLoadImageTask = new LoadImageTask();
        }
        mLoadImageTask.execute(0);

        if (mLoadVideoTask == null) {
            mLoadVideoTask = new LoadVideoTask();
        }
        mLoadVideoTask.execute(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_select_all:
                if (mBtnSelectAll.getText().equals(getString(R.string.select_all))) {
                    mBtnSelectAll.setText(getString(R.string.select_none));
                    mAdapter.selectAll();
                    mAdapter.notifyDataSetChanged();
                } else {
                    mBtnSelectAll.setText(getString(R.string.select_all));
                    mAdapter.clearSelect();
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.btn_hide:
                mAdapter.getSelectMedia();
                break;
        }
    }

    private boolean isReady() {
        return isImageReady && isVideoReady;
    }

    private void setGridView() {
        mLoadImageTask = null;
        mLoadVideoTask = null;
        if (mDialog != null) {
            mDialog.dismiss();
        }
        mMediaBean.clear();
        if (mImageBean == null && mVideoBean.size() < 1) {
            mNoMediaText.setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.GONE);
        } else {
            mNoMediaText.setVisibility(View.GONE);
            mGridView.setVisibility(View.VISIBLE);
            if (mImageBean != null) {
                for (MediaBean bean : mImageBean) {
                    mMediaBean.add(bean);
                }
            }
            if (mVideoBean != null) {
                for (MediaBean bean : mVideoBean) {
                    mMediaBean.add(bean);
                }
            }

            mAdapter = new MediaAdapter(mContext, mMediaBean, mImageLoader);
            mAdapter.setMultiChoiseMode(false);
            mAdapter.setOnMediaSelectListener(this);
            mGridView.setAdapter(mAdapter);
            mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Animation animation = AnimationUtils.loadAnimation(
                            mContext, R.anim.slide_in_anim);
                    mButtonsLayout.setVisibility(View.VISIBLE);
                    mButtonsLayout.startAnimation(animation);
                    mAdapter.setMultiChoiseMode(true);
                    mAdapter.notifyDataSetChanged();
                    return false;
                }
            });
            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MediaBean bean = mMediaBean.get(position);
                    if (!mAdapter.getMultiChoiseMode() && mMediaBean != null) {
                        if (bean.getmType() == MediaBean.TYPE_IMAGE) {
                            String fileName = bean.getFolderName();
                            mChildList = mImageGroup.get(fileName);
                            Intent intent = new Intent(mContext,
                                    PhotoSetActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(EXTRA_PHOTO_LIST, mChildList);
                            intent.putExtras(bundle);
                            intent.putExtra(EXTRA_ALBUME, fileName);
                            startActivityForResult(intent, 1);
                        } else {

                            playVideo(Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, ""
                                    + bean.getmVideoId()));
                        }

                    }
                }
            });
        }
    }

    private class LoadImageTask extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            mDialog = DialogHelper.createLoadingDialog(mContext, "正在加载图片和视频");
//            mDialog.show();
            isImageReady = false;
            if (mImageBean != null) {
                mImageBean.clear();
            }

        }

        @Override
        protected Integer doInBackground(Integer... params) {
            Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentResolver mContentResolver = mContext.getContentResolver();
            Cursor mCursor = mContentResolver.query(mImageUri, null, null,
                    null, MediaStore.MediaColumns.DATE_MODIFIED);
            mImageGroup.clear();

            while (mCursor.moveToNext()) {
                // 获取图片的路径
                String path = mCursor.getString(mCursor
                        .getColumnIndex(MediaStore.MediaColumns.DATA));
                // 获取该图片的父路径名
                String parentName = new File(path).getParentFile().getName();
                if (!mImageGroup.containsKey(parentName)) {
                    mSubImages = new ArrayList<String>();
                    mSubImages.add(path);
                    mImageGroup.put(parentName, mSubImages);
                } else {
                    if (!mSubImages.contains(path)) {
                        if (mImageGroup != null) {
                            mImageGroup.get(parentName).add(path);
                        }

                    }
                }
            }

            mCursor.close();
            mImageGroup.keySet();
            Iterator<Map.Entry<String, ArrayList<String>>> it = mImageGroup
                    .entrySet().iterator();
            List<ImageSort> sortList = new ArrayList<ImageSort>();
            List<String> pathArray = new ArrayList<String>();
            while (it.hasNext()) {
                Map.Entry<String, ArrayList<String>> entry = it.next();
                String key = entry.getKey();
                List<String> value = entry.getValue();
                if ("Camera".equals(key)) {

                    for (String path : value) {
                        File fileImage = new File(path);
                        ImageSort sort = new ImageSort();
                        sort.path = fileImage.getAbsolutePath();
                        sort.lastModified = fileImage.lastModified();
                        sortList.add(sort);
                    }
                    Collections.sort(sortList, new ImageSort());
                    for (int i = 0; i < sortList.size(); i++) {
                        pathArray.add(sortList.get(i).path);
                    }
                    mChildList.addAll(0, pathArray);
                } else {
                    mChildList.addAll(value);
                }
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            mImageBean = subGroupOfImage(mImageGroup);
            isImageReady = true;
            if (isReady()) {
                setGridView();
            }
        }
    }

    private class LoadVideoTask extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isVideoReady = false;
            mVideoBean.clear();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            Uri mUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            ContentResolver mContentResolver = mContext.getContentResolver();
            Cursor mCursor = mContentResolver.query(mUri, null, null, null,
                    MediaStore.Video.Media.DEFAULT_SORT_ORDER);
            while (mCursor.moveToNext()) {
                long id = mCursor.getLong(mCursor
                        .getColumnIndex(MediaStore.MediaColumns._ID));
                String path = mCursor.getString(mCursor
                        .getColumnIndex(MediaStore.MediaColumns.DATA));

                String name = mCursor.getString(mCursor
                        .getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                long size = mCursor.getLong(mCursor
                        .getColumnIndex(MediaStore.MediaColumns.SIZE));
                long duration = mCursor.getLong(mCursor
                        .getColumnIndex(MediaStore.Audio.AudioColumns.DURATION));
                String mimeType = mCursor.getString(mCursor
                        .getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
                MediaBean bean = new MediaBean(MediaBean.TYPE_VIDEO);
                bean.setmVideoId(id);
                bean.setmVideoPath(path);
                bean.setmVideoName(name);
                bean.setmVideoSize(size);
                bean.setmVideoDuration(duration);
                bean.setmVideoMimeType(mimeType);
                mVideoBean.add(bean);
            }
            mCursor.close();
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            isVideoReady = true;
            if (isReady()) {
                setGridView();
            }
            if (mVideoBean != null) {
                if (mLoadVideoThumbnailTask == null) {
                    mLoadVideoThumbnailTask = new LoadVideoThumbnailTask();
                }
                mLoadVideoThumbnailTask.execute();
            }

        }
    }

    public class LoadVideoThumbnailTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            // TODO Auto-generated method stub
            for (MediaBean bean : mVideoBean) {
                bean.setmVideoThumbnail(getThumbnail(bean.getmVideoId()));
                publishProgress(0);

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            mLoadVideoThumbnailTask = null;
        }
    }

    private Bitmap getThumbnail(long id) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        ContentResolver cr = mContext.getContentResolver();
        bitmap = MediaStore.Video.Thumbnails.getThumbnail(cr, id,
                MediaStore.Images.Thumbnails.MINI_KIND, options);
        return bitmap;
    }

    /**
     * 组装分组界面GridView的数据源，因为我们扫描手机的时候将图片信息放在HashMap中 所以需要遍历HashMap将数据组装成List
     *
     * @param mGruopMap
     * @return
     */
    private ArrayList<MediaBean> subGroupOfImage(
            HashMap<String, ArrayList<String>> mGruopMap) {
        if (mGruopMap.size() == 0) {
            return null;
        }
        ArrayList<MediaBean> listLink = new ArrayList<MediaBean>();
        mGruopMap.keySet();
        Iterator<Map.Entry<String, ArrayList<String>>> it = mGruopMap.entrySet()
                .iterator();

        while (it.hasNext()) {
            Map.Entry<String, ArrayList<String>> entry = it.next();
            MediaBean mMediaBean = new MediaBean(MediaBean.TYPE_IMAGE);
            String key = entry.getKey();
            List<String> value = entry.getValue();

            mMediaBean.setFolderName(key);
            mMediaBean.setImageCounts(value.size());
            mMediaBean.setTopImagePath(value.get(0));// 获取该组的第一张图片
            listLink.add(mMediaBean);
        }

        return listLink;

    }

    @Override
    public void onMediaSelect(ArrayList<MediaBean> beans) {

        if (beans.size() < 1) {
            ToastUtils.show(mContext, "没有选择项");
        } else {
            mButtonsLayout.setVisibility(View.GONE);
            new HideMediaTask(beans).execute(0);
        }
    }

    private class HideMediaTask extends AsyncTask<Integer, Integer, Integer> {
        List<MediaBean> mHideBeans = Collections
                .synchronizedList(new ArrayList<MediaBean>());

        public HideMediaTask(ArrayList<MediaBean> beans) {
            for (MediaBean bean : beans) {
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
            for (MediaBean bean : mHideBeans) {
                if (bean.getmType() == MediaBean.TYPE_VIDEO) {
                    String oldPath = bean.getPath();
                    String newPath = FileUtils.getVideoHidePath();
                    String name = bean.getmVideoName();
                    if (!FileUtils.moveFile(oldPath, newPath)) {
                        return RESULT_FAIL;
                    }
                    ProviderHelper.insertHideMediaInfo(mContext, name, newPath + name, oldPath);
                    MediaUtils.deleteVideoData(mContext, new File(oldPath));
                    MediaUtils.syncMediaData(mContext, oldPath);
                } else {
                    ArrayList<String> imagePaths = mImageGroup.get(bean.getFolderName());
                    for (String path : imagePaths) {
                        String newPath = FileUtils.getFileHidePath();
                        String oldPath = path;
                        String name = new File(oldPath).getName();

                        if (!FileUtils.moveFile(oldPath, newPath)) {
                            return RESULT_FAIL;
                        }
                        ProviderHelper.insertHideMediaInfo(mContext, name, newPath + name, oldPath);

                        MediaUtils.deleteMediaData(mContext, new File(path));
                        MediaUtils.syncMediaData(mContext, oldPath);
                    }
                }
                mMediaBean.remove(bean);
            }

            return RESULT_SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            if (mDialog != null) {
                mDialog.dismiss();
            }
            if (integer == RESULT_SUCCESS) {
                ToastUtils.show(mContext, "操作成功");
            } else {
                ToastUtils.show(mContext, "部分文件操作失败，请重试");
            }
            new LoadImageTask().execute(0);
            new LoadVideoTask().execute(0);
        }
    }

    @Override
    public void onBackPressed() {
        if (mAdapter != null && mAdapter.getMultiChoiseMode()) {
            mButtonsLayout.setVisibility(View.GONE);
            mAdapter.setMultiChoiseMode(false);
            mAdapter.notifyDataSetChanged();
            return;
        }
        super.onBackPressed();

    }

    public void playVideo(Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "video/*");
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            ToastUtils.show(mContext, "未找到视频播放器");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(getString(R.string.show_hide));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            startActivityForResult(new Intent(mContext, MediaHideListActivity.class),1);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLoadImageTask!=null){
            mLoadImageTask.cancel(false);
            mLoadImageTask=null;
        }
        if(mLoadVideoTask!=null){
            mLoadVideoTask.cancel(false);
            mLoadVideoTask=null;
        }
        if(mLoadVideoThumbnailTask!=null){
            mLoadVideoThumbnailTask.cancel(false);
            mLoadVideoThumbnailTask=null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            boolean refresh = data.getBooleanExtra("refresh", false);
            if (refresh) {
                loadMedia();
            }
        }
    }
}

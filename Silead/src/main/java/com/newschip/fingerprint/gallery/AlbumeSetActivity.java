package com.newschip.fingerprint.gallery;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
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
import com.newschip.fingerprint.gallery.AlbumeSetAdapter.OnAlbumeSetSelectListener;
import com.newschip.fingerprint.hide.FileUtils;
import com.newschip.fingerprint.hide.MediaUtils;
import com.newschip.fingerprint.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

public class AlbumeSetActivity extends BasePhotoActivity implements
        OnItemClickListener, OnItemLongClickListener, OnClickListener,
        OnAlbumeSetSelectListener {

    private String TAG = "kebelzc24";
    private ArrayList<String> list, chileList;
    private ArrayList<ImageBean> mBeenList;
    private HashMap<String, ArrayList<String>> mGruopMap = new HashMap<String, ArrayList<String>>();
    private final static int SCAN_OK = 1;
    private final static int HIDE_OK = 2;
    private final static int HIDE_FAIL = 3;
    private Context mContext;
    private GridView mGridView;
    private AlbumeSetAdapter mAdapter;
    private TextView mNoPhotoText;

    private Thread mRefreshThread;

    private Dialog mHidingDialog;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case SCAN_OK:
                mRefreshThread = null;
                mBeenList = null;
                mBeenList = subGroupOfImage(mGruopMap);
                initAlbumePage();
                break;
            case HIDE_OK:
                getImages();
                if (mHidingDialog != null) {
                    mHidingDialog.dismiss();
                }
                mAdapter.setMultiChoiseMode(false);
                mAdapter.notifyDataSetChanged();
                break;
            case HIDE_FAIL:
                Toast.makeText(mContext, "隐藏失败，请稍后重试!", 1000).show();
                break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_albume_set);
        if (mImageLoader != null) {
            mImageLoader.clearMemoryCache();
            System.gc();
        }

        initViews();
    }

    private void initViews() {
        // TODO Auto-generated method stub
        initTitleView();
        mHideBtn.setOnClickListener(this);
        mSelectAllBtn.setOnClickListener(this);
        //mShowHideBtn.setOnClickListener(this);
        //mCenterTitleText.setText("相册");
        list = new ArrayList<String>();
        initTileBar(false);
    }

    private void initAlbumePage() {
        mNoPhotoText = (TextView) findViewById(R.id.tv_no_photo);
        mNoPhotoText.setVisibility(View.GONE);
        mGridView = (GridView) findViewById(R.id.album_gridview);
        mGridView.setVisibility(View.VISIBLE);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnItemLongClickListener(this);
        mGridView.setOnScrollListener(new PauseOnScrollListener(mImageLoader, true, true));//两个分别表示拖动下拉条和滑动过程中暂停加载
        if (mGruopMap != null && mGruopMap.size() > 0) {
            // Log.d("TAG", "size: "+mBeenList.size());
            mNoPhotoText.setVisibility(View.GONE);
            mAdapter = new AlbumeSetAdapter(mContext, mBeenList,mImageLoader);
            mAdapter.setCallback(this);
            mGridView.setAdapter(mAdapter);
        } else {
            mNoPhotoText.setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.GONE);
            Log.e("", "image is null");
        }
    }

    /*
     * . 点击事件
     */

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
        case R.id.btn_hide:
            // 确定按钮的事件
            Log.d(TAG, "hide click");
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
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        ImageLoader.getInstance().clearMemoryCache();
        System.gc();
    }

    /**
     * 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中
     */
    private void getImages() {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            // showShortToast("暂无外部存储");
            // return;
        }

        // 显示进度条

        if (mRefreshThread == null) {
            mRefreshThread = new Thread(mRefreshRunnable);
            mRefreshThread.start();
        }

    }

    private Runnable mRefreshRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub

            Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentResolver mContentResolver = mContext.getContentResolver();
            Cursor mCursor = mContentResolver.query(mImageUri, null, null,
                    null, MediaColumns.DATE_MODIFIED);
            mGruopMap.clear();

            while (mCursor.moveToNext()) {
                // 获取图片的路径
                String path = mCursor.getString(mCursor
                        .getColumnIndex(MediaColumns.DATA));
                // 获取该图片的父路径名
                String parentName = new File(path).getParentFile().getName();
                if (!mGruopMap.containsKey(parentName)) {
                    chileList = new ArrayList<String>();
                    chileList.add(path);
                    mGruopMap.put(parentName, chileList);
                } else {
                    if (!chileList.contains(path)) {
                        if (mGruopMap != null) {
                            mGruopMap.get(parentName).add(path);
                        }

                    }
                }
            }

            mCursor.close();
            mGruopMap.keySet();
            Iterator<Entry<String, ArrayList<String>>> it = mGruopMap
                    .entrySet().iterator();
            List<ImageSort> sortList = new ArrayList<ImageSort>();
            List<String> pathArray = new ArrayList<String>();
            while (it.hasNext()) {
                Entry<String, ArrayList<String>> entry = it.next();
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
                    list.addAll(0, pathArray);
                } else {
                    list.addAll(value);
                }
            }

            // 通知Handler扫描图片完成
            mHandler.sendEmptyMessage(SCAN_OK);

        }
    };

    /**
     * 组装分组界面GridView的数据源，因为我们扫描手机的时候将图片信息放在HashMap中 所以需要遍历HashMap将数据组装成List
     * 
     * @param mGruopMap
     * @return
     */
    private ArrayList<ImageBean> subGroupOfImage(
            HashMap<String, ArrayList<String>> mGruopMap) {
        if (mGruopMap.size() == 0) {
            return null;
        }
        ArrayList<ImageBean> listLink = new ArrayList<ImageBean>();
        mGruopMap.keySet();
        // ImageBean bean = new ImageBean(list.get(0), "全部图片", 0);//这里是全部图片设置
        // listLink.add(bean);
        Iterator<Entry<String, ArrayList<String>>> it = mGruopMap.entrySet()
                .iterator();

        while (it.hasNext()) {
            Entry<String, ArrayList<String>> entry = it.next();
            ImageBean mImageBean = new ImageBean();
            String key = entry.getKey();
            List<String> value = entry.getValue();

            mImageBean.setFolderName(key);
            mImageBean.setImageCounts(value.size());
            mImageBean.setTopImagePath(value.get(0));// 获取该组的第一张图片
            listLink.add(mImageBean);
        }

        return listLink;

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        ImageLoader.getInstance().clearMemoryCache();
        System.gc();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        if (mImageLoader != null) {
            mImageLoader.clearMemoryCache();
        }
        System.gc();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
            int position, long arg3) {
        // TODO Auto-generated method stub

        initTileBar(true);
        mAdapter.setMultiChoiseMode(true);
        return true;
    }

    private void initTileBar(boolean hide) {
        if (hide) {
            //mShowHideBtn.setVisibility(View.GONE);
            mSelectAllBtn.setVisibility(View.VISIBLE);
            mHideBtn.setVisibility(View.VISIBLE);
        } else {
            //mShowHideBtn.setVisibility(View.VISIBLE);
            mSelectAllBtn.setVisibility(View.GONE);
            mHideBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
        if (!mAdapter.getMultiChoiseMode() && mBeenList != null) {
            String fileName = mBeenList.get(arg2).getFolderName();
            list = mGruopMap.get(fileName);
            Intent intent = new Intent(AlbumeSetActivity.this,
                    PhotoSetActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("photo", list);
            intent.putExtras(bundle);
            intent.putExtra("albume_name", fileName);
            startActivityForResult(intent, 0);
        }

    }

    @Override
    public void onAlbumeSetSelect(List<String> paths, List<String> folder) {
        // TODO Auto-generated method stub
        Log.d(TAG, "i am callback");
        if (folder.size() < 1) {
            Toast.makeText(mContext, "请至少选择一个相册", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "dialog show");
            mHidingDialog = DialogHelper.createLoadingDialog(mContext,
                    "正在隐藏中，请稍后...");
            mHidingDialog.show();
            Log.d(TAG, "dialog showing");
            initTileBar(false);
            // mHandler.sendMessage(Message.obtain(mHandler, DO_HIDE));
            new HideThread(paths, folder).start();
        }
    }

    public class HideThread extends Thread {

        // ArrayList 不是线程安全的，需要使用Collections类使其变成同步的
        List<String> paths = Collections
                .synchronizedList(new ArrayList<String>());;
        List<String> folder = Collections
                .synchronizedList(new ArrayList<String>());;

        public HideThread() {
            // TODO Auto-generated constructor stub
        }

        public HideThread(List<String> paths, List<String> folder) {
            // TODO Auto-generated constructor stub
            // this.paths = paths;
            // this.folder = folder;
            // 不能使用上面的方法赋值
            for (int i = 0; i < paths.size(); i++) {
                this.paths.add(paths.get(i));
                this.folder.add(folder.get(i));
            }

        }

        @Override
        public void run() {
            // TODO Auto-generated method stub

            // TODO Auto-generated method stub
            for (int i = 0; i < folder.size(); i++) {
                ArrayList<String> path = mGruopMap.get(folder.get(i));
                for (int j = 0; j < path.size(); j++) {
                    String newPath = FileUtils.getFileHidePath();
                    String oldPath = path.get(j);
                    String name = new File(oldPath).getName();

                    if (!FileUtils.moveFile(oldPath, newPath)) {
                        mHandler.sendMessage(Message
                                .obtain(mHandler, HIDE_FAIL));
                        break;
                    }
                    mProviderHelper.insertHidePath(name, newPath + name,
                            oldPath);
                    MediaUtils.deleteMediaData(mContext, new File(path.get(j)));
                    MediaUtils.syncMediaData(mContext, oldPath);
                }

            }
            mHandler.sendMessageDelayed(Message.obtain(mHandler, HIDE_OK), 100);

        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        getImages();
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
    protected void onActivityResult(int requestCode, int resultCode,
            Intent intent) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, intent);
        if (intent != null && intent.getBooleanExtra("refresh", true)) {
            getImages();
        }
    }

}

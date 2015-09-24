package com.newschip.fingerprint.gallery;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.newschip.fingerprint.activity.BasePhotoActivity;
import com.newschip.fingerprint.dialog.DialogHelper;
import com.newschip.fingerprint.gallery.PhotoSetAdapter.OnPhotoSelectListener;
import com.newschip.fingerprint.hide.FileUtils;
import com.newschip.fingerprint.hide.MediaUtils;
import com.newschip.fingerprint.R;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

public class PhotoSetActivity extends BasePhotoActivity implements
        OnItemLongClickListener, OnItemClickListener, OnClickListener,
        OnPhotoSelectListener {
    private Context mContext;
    private ArrayList<String> mArrayList;
    private GridView mGridView;
    private PhotoSetAdapter mAdapter;
    private Dialog mHidingDialog;
    private final static int HIDE_OK = 1;
    private final static int HIDE_FAIL = 2;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case HIDE_OK:
                setRefresh();
                if (mHidingDialog != null) {
                    mHidingDialog.dismiss();
                }
                if (mArrayList.size() == 0) {
                    finish();
                } else {
                    mAdapter.setMultiChoiseMode(false);
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case HIDE_FAIL:
                Toast.makeText(mContext, "隐藏失败，请稍后重试!", 1000).show();
                break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_set);
        mContext = this;
        mArrayList = (ArrayList<String>) getIntent().getExtras().get("photo");
        initView();
    }


    private void initView() {
        initTitleView();
        mHideBtn.setOnClickListener(this);
        mSelectAllBtn.setOnClickListener(this);
        //mShowHideBtn.setVisibility(View.GONE);
        mCenterTitleText.setText(getIntent().getStringExtra("albume_name"));
        mGridView = (GridView) findViewById(R.id.gridview);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnItemLongClickListener(this);
        mGridView.setOnScrollListener(new PauseOnScrollListener(mImageLoader, true, true));//两个分别表示拖动下拉条和滑动过程中暂停加载
        mAdapter = new PhotoSetAdapter(mContext, mArrayList, mGridView,mImageLoader);
        mGridView.setAdapter(mAdapter);
        mAdapter.setOnPhotoSelectListener(this);
        initTileBar(false);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
        Bundle bundle = new Bundle();
        bundle.putSerializable("list", mArrayList);
        bundle.putInt("position", arg2 - 1);
        Intent intent = new Intent(mContext, ViewPagerActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void initTileBar(boolean hide) {
        if (hide) {
            mSelectAllBtn.setVisibility(View.VISIBLE);
            mHideBtn.setVisibility(View.VISIBLE);
        } else {
            mSelectAllBtn.setVisibility(View.GONE);
            mHideBtn.setVisibility(View.GONE);
        }
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

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        int id = view.getId();
        switch (id) {
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
        case R.id.btn_hide:
            mAdapter.getSelectData();
            break;
        default:
            break;
        }
    }

    @Override
    public void onPhotoSelect(List<String> paths) {
        // TODO Auto-generated method stub
        if (paths.size() < 1) {
            Toast.makeText(mContext, "请至少选择一个图片", Toast.LENGTH_SHORT).show();
        } else {
            mHidingDialog = DialogHelper.createLoadingDialog(mContext,
                    "正在隐藏，请稍后!");
            mHidingDialog.show();
            initTileBar(false);
            new HideThread(paths).start();
        }
    }

    private void setRefresh() {
        Intent mIntent = new Intent();
        mIntent.putExtra("refresh", true);
        this.setResult(0, mIntent);
    }

    public class HideThread extends Thread {

        // ArrayList 不是线程安全的，需要使用Collections类使其变成同步的
        List<String> paths = Collections
                .synchronizedList(new ArrayList<String>());;

        public HideThread(List<String> paths) {
            // TODO Auto-generated constructor stub
            // this.paths = paths;
            // this.folder = folder;
            // 不能使用上面的方法赋值
            for (int i = 0; i < paths.size(); i++) {
                this.paths.add(paths.get(i));
            }

        }

        @Override
        public void run() {
            for (int i = 0; i < paths.size(); i++) {
                String name = new File(paths.get(i)).getName();
                String newPath = FileUtils.getFileHidePath();
                String oldPath = paths.get(i);
                if (!FileUtils.moveFile(oldPath, newPath)) {
                    mHandler.sendMessage(Message.obtain(mHandler, HIDE_FAIL));
                    break;
                }
                mProviderHelper.insertHidePath(name, newPath + name, oldPath);
                MediaUtils.deleteMediaData(mContext, new File(paths.get(i)));
                MediaUtils.syncMediaData(mContext, oldPath);
                mArrayList.remove(paths.get(i));
            }
            mHandler.sendMessageDelayed(Message.obtain(mHandler, HIDE_OK), 100);
        }
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

}

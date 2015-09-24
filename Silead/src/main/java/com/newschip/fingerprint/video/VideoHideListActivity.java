package com.newschip.fingerprint.video;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.newschip.fingerprint.activity.BasePhotoActivity;
import com.newschip.fingerprint.dialog.DialogHelper;
import com.newschip.fingerprint.hide.FileObject;
import com.newschip.fingerprint.hide.FileUtils;
import com.newschip.fingerprint.hide.MediaUtils;
import com.newschip.fingerprint.video.VideoHideListAdapter.OnPhotoSelectListener;
import com.newschip.fingerprint.R;

public class VideoHideListActivity extends BasePhotoActivity implements
        OnClickListener, OnPhotoSelectListener{

    private ArrayList<FileObject> mFileObject = null;// 存放名称
    private ListView mFileListView;
    private VideoHideListAdapter mAdapter;
    private TextView mNoFileText;
    private Context mContext;
    private Dialog mDeHidingDialog;

    private final int DEHIDE_OK = 1;
    private final int DEHIDE_FAIL = 2;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case DEHIDE_OK:
                if (mDeHidingDialog != null) {
                    mDeHidingDialog.dismiss();
                }
                getFileObjects(FileUtils.getVideoHidePath());
                initViews();
                setRefresh();
                break;
            case DEHIDE_FAIL:
                Toast.makeText(mContext, "恢复失败，请稍后重试！", 1000).show();
                break;
            default:
                break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_hide);
        mContext = this;
        getFileObjects(FileUtils.getVideoHidePath());
        initViews();
    }

    private void initViews() {
        initTitleView();
        mHideBtn.setOnClickListener(this);
        mSelectAllBtn.setVisibility(View.VISIBLE);
        mSelectAllBtn.setOnClickListener(this);
        //mShowHideBtn.setVisibility(View.GONE);
        mCenterTitleText.setText("视频(" + mFileObject.size() + ")");
        mHideBtn.setText("恢复");
        mHideBtn.setOnClickListener(this);
        mFileListView = (ListView) findViewById(R.id.listView);
        mAdapter = new VideoHideListAdapter(this, mFileObject);
         mAdapter.setOnPhotoSelectListener(VideoHideListActivity.this);
        mFileListView.setAdapter(mAdapter);
        mNoFileText = (TextView) findViewById(R.id.tv_no_hide_file);
        if (mFileObject.size() < 1) {
            ((TextView)findViewById(R.id.tv_no_hide_file)).setText("没有隐藏的视频");
            mSelectAllBtn.setVisibility(View.GONE);
            mFileListView.setVisibility(View.GONE);
            mNoFileText.setVisibility(View.VISIBLE);
            mHideBtn.setVisibility(View.GONE);
        } else {
            mSelectAllBtn.setVisibility(View.VISIBLE);
            mFileListView.setVisibility(View.VISIBLE);
            mNoFileText.setVisibility(View.GONE);
            mHideBtn.setVisibility(View.VISIBLE);
        }
    }

    public void getFileObjects(String filePath) {
        mFileObject = new ArrayList<FileObject>();
        mFileObject.clear();
        File f = new File(filePath);
        if (f.exists()) {
            File[] files = f.listFiles();// 列出所有文件
            // 将所有文件存入list中
            if (files != null) {
                int count = files.length;// 文件个数
                for (int i = 0; i < count; i++) {
                    File file = files[i];
                    FileObject fo = new FileObject();
                    fo.setName(file.getName());
                    fo.setPath(file.getAbsolutePath());
                    mFileObject.add(fo);
                }
            }
        }

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
    public void onPhotoSelect(List<FileObject> objs) {
        // TODO Auto-generated method stub
        int size = objs.size();
        if (size < 1) {
            Toast.makeText(mContext, "请至少选择一个文件", Toast.LENGTH_SHORT).show();
            return;
        }
        mDeHidingDialog = DialogHelper.createLoadingDialog(mContext,
                "正在恢复中，请稍后...");
        mDeHidingDialog.show();
        new HideThread(objs).start();

        // mAdapter.notifyDataSetChanged();

    }

    private void setRefresh(){
        Intent intent = new Intent(mContext,VideoPageActivity.class);
        intent.putExtra("refresh", true);
        this.setResult(0,intent);
    }
    public class HideThread extends Thread {
        List<FileObject> objs = Collections
                .synchronizedList(new ArrayList<FileObject>());;

        public HideThread(List<FileObject> fb) {
            // TODO Auto-generated constructor stub
            for (int i = 0; i < fb.size(); i++) {
                this.objs.add(fb.get(i));
            }
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            int size = objs.size();
            for (int i = 0; i < size; i++) {
                FileObject fo = objs.get(i);
                String path = fo.getPath();
                String oldPath = mProviderHelper.getOldPath(path);
                if(TextUtils.isEmpty(oldPath)){
                    oldPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/favor";
                    if (!FileUtils.moveFile(path, oldPath)) {
                        mHandler.sendMessageDelayed(
                                Message.obtain(mHandler, DEHIDE_FAIL), 0);
                        break;
                    }
                } else {
                    if (!FileUtils.moveFile(path, new File(oldPath).getParent())) {
                        mHandler.sendMessageDelayed(
                                Message.obtain(mHandler, DEHIDE_FAIL), 0);
                        break;
                    }
                }

                
                mProviderHelper.deleteHidePath(path);
                MediaUtils.syncMediaData(mContext, oldPath);
            }
            mHandler.sendMessageDelayed(Message.obtain(mHandler, DEHIDE_OK),
                    100);
        }
    }

}

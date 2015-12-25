package com.newschip.galaxy.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.newschip.galaxy.R;
import com.newschip.galaxy.dialog.DialogHelper;
import com.newschip.galaxy.media.ImageDownLoader;
import com.newschip.galaxy.media.MediaUtils;
import com.newschip.galaxy.provider.ProviderHelper;
import com.newschip.galaxy.utils.FileUtils;
import com.newschip.galaxy.utils.ToastUtils;
import com.newschip.galaxy.widget.LocalImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PhotoSetActivity extends BaseActivity implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener,View.OnClickListener{
    private ArrayList<String> mImageList;
    private GridView mGridView;
    private PhotoSetAdapter mAdapter;
    private LinearLayout mButtonsLayout;
    private Button mBtnSelectAll;
    private Button mBtnHide;
    private Dialog mDialog;
    private final int RESULT_SUCCESS = 0;
    private final int RESULT_FAIL = 1;

    private boolean refresh;



    @Override
    public int getLayoutView() {
        return R.layout.activity_photo_set;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mBtnSelectAll = (Button) findViewById(R.id.btn_select_all);
        mBtnHide = (Button) findViewById(R.id.btn_hide);
        mBtnSelectAll.setOnClickListener(this);
        mBtnHide.setOnClickListener(this);
        mButtonsLayout = (LinearLayout) findViewById(R.id.ll_buttons);
        mImageList = (ArrayList<String>) getIntent().getExtras().get(MediaActivity.EXTRA_PHOTO_LIST);
        initView();
    }


    private void initView() {
        getSupportActionBar().setTitle(getIntent().getStringExtra(MediaActivity.EXTRA_ALBUME));
        mGridView = (GridView) findViewById(R.id.gridview);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnItemLongClickListener(this);
        mGridView.setOnScrollListener(new PauseOnScrollListener(mImageLoader, true, true));//两个分别表示拖动下拉条和滑动过程中暂停加载
        mAdapter = new PhotoSetAdapter(mContext, mImageList, mGridView, mImageLoader);
        mGridView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
        Bundle bundle = new Bundle();
        bundle.putSerializable("list", mImageList);
        bundle.putInt("position", arg2 - 1);
        Intent intent = new Intent(mContext, ViewPhotoActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                   int position, long arg3) {
        // TODO Auto-generated method stub
        Animation animation = AnimationUtils.loadAnimation(
                mContext, R.anim.slide_in_anim);
        mButtonsLayout.setVisibility(View.VISIBLE);
        mButtonsLayout.startAnimation(animation);
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
                if (mAdapter.getmSelectList().size() < 1) {
                    ToastUtils.show(mContext, "没有选择项");
                } else {
                    refresh = true;
                    new HideMediaTask(mAdapter.getmSelectList()).execute(0);
                }
                break;
            default:
                break;
        }
    }

    private class HideMediaTask extends AsyncTask<Integer, Integer, Integer> {
        List<String> mHideBeans = Collections
                .synchronizedList(new ArrayList<String>());

        public HideMediaTask(ArrayList<String> beans) {
            for (String bean : beans) {
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
            for (String bean:mHideBeans) {
                String name = new File(bean).getName();
                String newPath = FileUtils.getFileHidePath();
                String oldPath = bean;
                if (!FileUtils.moveFile(oldPath, newPath)) {
                    return RESULT_FAIL;
                }
                ProviderHelper.insertHideMediaInfo(mContext,name, newPath + name, oldPath);
                MediaUtils.deleteMediaData(mContext, new File(bean));
                MediaUtils.syncMediaData(mContext, oldPath);
                mImageList.remove(bean);
            }

            return RESULT_SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            mAdapter.notifyDataSetChanged();
            if (mDialog != null) {
                mDialog.dismiss();
            }
            if (integer == RESULT_SUCCESS) {
                ToastUtils.show(mContext, "操作成功");
            } else {
                ToastUtils.show(mContext, "部分文件操作失败，请重试");
            }
            if(mImageList.size()<1){
                onBackPressed();
            }
        }
    }


    @Override
    public void onBackPressed() {
        if (mAdapter.getMultiChoiseMode()) {
            mButtonsLayout.setVisibility(View.GONE);
            mAdapter.setMultiChoiseMode(false);
            mAdapter.notifyDataSetChanged();
            return;
        }
        if(refresh){
            Intent data = new Intent();
            data.putExtra("refresh", true);
            setResult(RESULT_OK, data);
        }
        super.onBackPressed();

    }

    public class PhotoSetAdapter extends BaseAdapter {
        private GridView mGridView;
        private ArrayList<String> list;
        private Context context;
        protected LayoutInflater mInflater;



        private ArrayList<String> mSelectList;

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
            mSelectList.clear();
        }
        public ArrayList<String> getmSelectList() {
            return mSelectList;
        }
        public void selectAll() {
            mSelectList.clear();
            for (int i = 0; i < list.size(); i++) {
                final String path = list.get(i);
                mSelectList.add(path);
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
            mSelectList = new ArrayList<String>();
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
                        .findViewById(R.id.iv_thumbnail);
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
                if (mSelectList.contains(path)) {
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

            viewHolder.view.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (mSelectList.contains(path)) {
                        viewHolder.mCheckBox
                                .setImageResource(R.mipmap.av_checkbox_unchecked);
                        mSelectList.remove(path);
                    } else {
                        viewHolder.mCheckBox
                                .setImageResource(R.mipmap.av_checkbox_checked);
                        mSelectList.add(path);
                    }
                }
            });

            // if(!mMultiChoiseMode){
            // ImageDownLoader.showLocationImage(path, viewHolder.mImageView,
            // R.mipmap.default_photo);
            // }
            return rootView;
        }


        public class ViewHolder {
            public ImageView mCheckBox, mOneImageView;
            public LocalImageView mImageView;
            private View view;
        }

    }

}

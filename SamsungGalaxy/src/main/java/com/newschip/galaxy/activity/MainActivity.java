package com.newschip.galaxy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.newschip.galaxy.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private RecycleViewAdapter mRecycleViewAdapter;
    private List<RecycleViewItem> mRecycleViewItems = new ArrayList<RecycleViewItem>();

    @Override
    public int getLayoutView() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRecycleView();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    private void initRecycleView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_recyclerView);
        RecycleViewItem item;
        item = new RecycleViewItem(R.mipmap.app_lock, getResources().getString(R.string.app_lock), getResources().getString(R.string.app_may_be_locked));
        mRecycleViewItems.add(item);
        item = new RecycleViewItem(R.mipmap.app_switch, getResources().getString(R.string.switch_app), getResources().getString(R.string.switch_app_content));
        mRecycleViewItems.add(item);
        item = new RecycleViewItem(R.mipmap.file_hide, getResources().getString(R.string.hidden_file), getResources().getString(R.string.hidden_file_content));
        mRecycleViewItems.add(item);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecycleViewAdapter = new RecycleViewAdapter(mRecycleViewItems);
        mRecyclerView.setAdapter(mRecycleViewAdapter);
        mRecycleViewAdapter.notifyItemRangeInserted(0, mRecycleViewItems.size() - 1);
    }

    private class RecycleViewItem {
        private int mIconId;
        private String mTitle;
        private String mContent;

        public RecycleViewItem(int mIconId, String mTitle, String mContent) {
            this.mIconId = mIconId;
            this.mTitle = mTitle;
            this.mContent = mContent;
        }

        public int getmIconId() {
            return mIconId;
        }

        public void setmIconId(int mIconId) {
            this.mIconId = mIconId;
        }

        public String getmTitle() {
            return mTitle;
        }

        public void setmTitle(String mTitle) {
            this.mTitle = mTitle;
        }

        public String getmContent() {
            return mContent;
        }

        public void setmContent(String mContent) {
            this.mContent = mContent;
        }

    }

    private class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {

        private List<RecycleViewItem> mItems;

        public RecycleViewAdapter(List<RecycleViewItem> items) {
            this.mItems = items;
        }


        @Override
        public ViewHolder onCreateViewHolder(final ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_main, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
            RecycleViewItem item = mItems.get(i);
            viewHolder.title.setText(item.getmTitle());
            viewHolder.content.setText(item.getmContent());
            viewHolder.icon.setImageDrawable(getResources().getDrawable(item.getmIconId()));

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (i) {
                        case 0:
                            startActivity(new Intent(MainActivity.this, AppListActivity.class));
                            break;
                        case 1:
                            startActivity(new Intent(MainActivity.this, SwitchActivity.class));
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                        case 4:
                            break;
                        case 5:
                            break;
                        default:
                            break;

                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mItems == null ? 0 : mItems.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView title;
            public TextView content;
            public ImageView icon;

            public ViewHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.tv_title);
                content = (TextView) itemView.findViewById(R.id.tv_content);
                icon = (ImageView) itemView.findViewById(R.id.iv_icon);
            }

        }
    }
}

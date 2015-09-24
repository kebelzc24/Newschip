package com.newschip.fingerprint.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.newschip.fingerprint.R;

import java.io.Serializable;
import java.util.ArrayList;

import static com.newschip.fingerprint.R.id.rl_people;
import static com.newschip.fingerprint.R.layout.item_people;

/**
 * Created by LQ on 2015/9/24.
 */
public class ContactsActivity extends BaseActivity {

    private Toolbar mToolbar;
    private ListView mListView;
    private DataAdapter mAdapter = new DataAdapter();

    private ArrayList<ContantsPeople> mContantsPeople = new ArrayList<ContantsPeople>();
    @Override
    protected int getLayoutView() {
        return R.layout.activity_contacts;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initToolbar();
        mListView = (ListView) findViewById(R.id.listview);
        mListView.setAdapter(mAdapter);
        new LoadContantsTask().execute(0);
    }
    private void initToolbar(){
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        super.initToolbar(mToolbar);
        mToolbar.setTitle(R.string.fast_dail);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private class LoadContantsTask extends
            AsyncTask<Integer, Integer, ArrayList<ContantsPeople>> {

        @Override
        protected ArrayList<ContantsPeople> doInBackground(Integer... params) {
            // TODO Auto-generated method stub
            ArrayList<ContantsPeople> sPeople = new ArrayList<ContantsPeople>();
            ContentResolver mResolver = mContext.getContentResolver();
            Cursor mCursor = mResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
                    null, null);
            if (mCursor != null) {
                while (mCursor.moveToNext()) {
                    ContantsPeople people = new ContantsPeople();
                    people.setName(mCursor.getString(mCursor
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                    people.setPhoneNumber(mCursor.getString(mCursor
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    sPeople.add(people);
                }
            }
            mCursor.close();
            return sPeople;
        }

        @Override
        protected void onPostExecute(ArrayList<ContantsPeople> result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            mContantsPeople = result;
            mAdapter.notifyDataSetChanged();
        }

    }

    private class DataAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mContantsPeople.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mContantsPeople.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(
                        item_people, null);
                holder.mPeopleLayout = (RelativeLayout) convertView
                        .findViewById(rl_people);
                holder.mNumber = (TextView) convertView
                        .findViewById(R.id.tv_num);
                holder.mPeople = (TextView) convertView
                        .findViewById(R.id.tv_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final ContantsPeople people = mContantsPeople.get(position);
            holder.mNumber.setText(people.getPhoneNumber());
            holder.mPeople.setText(people.getName());
            holder.mPeopleLayout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri
                            .parse("tel:" + people.getPhoneNumber()));
                    startActivity(dialIntent);
                }
            });
            return convertView;
        }

        private class ViewHolder {
            private RelativeLayout mPeopleLayout;
            private TextView mPeople;
            private TextView mNumber;
        }

    }

    public class ContantsPeople implements Serializable {
        private static final long serialVersionUID = 1L;
        private String mPhoneNumber;
        private String mName;
        public String getPhoneNumber() {
            return mPhoneNumber;
        }
        public void setPhoneNumber(String mPhoneNumber) {
            this.mPhoneNumber = mPhoneNumber;
        }
        public String getName() {
            return mName;
        }
        public void setName(String mName) {
            this.mName = mName;
        }

    }
}

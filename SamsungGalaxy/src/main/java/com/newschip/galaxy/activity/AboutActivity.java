package com.newschip.galaxy.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.newschip.galaxy.R;


public class AboutActivity extends BaseActivity implements View.OnClickListener {
    Toolbar toolbar;
    TextView versionTextView;
    Button blogButton;
    Button projectHomeButton;

    private final String NEWSCHIP_URL = "http://www.newschip.cn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.about);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        versionTextView = (TextView) findViewById(R.id.version_text);
        blogButton = (Button) findViewById(R.id.blog_btn);
        projectHomeButton = (Button) findViewById(R.id.project_home_btn);
        initVersionText();
        blogButton.setOnClickListener(this);
        projectHomeButton.setOnClickListener(this);
    }


    @Override
    public int getLayoutView() {
        return R.layout.activity_about;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.blog_btn:
                startViewAction(NEWSCHIP_URL);
                break;
            case R.id.project_home_btn:
//                startViewAction(BuildConfig.PROJECT_URL);
                break;
            default:
                break;
        }
    }


    private void initVersionText() {
        versionTextView.setText("v" + getVersion(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getVersion(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "1.0.0";
    }

    private void startViewAction(String uriStr) {
        try {
            Uri uri = Uri.parse(uriStr);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
}

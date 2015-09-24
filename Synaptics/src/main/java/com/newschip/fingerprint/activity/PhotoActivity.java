package com.newschip.fingerprint.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.newschip.fingerprint.gallery.AlbumeSetActivity;
import com.newschip.fingerprint.video.VideoPageActivity;
import com.newschip.fingerprint.R;

public class PhotoActivity extends BasePhotoActivity implements OnClickListener {

    private RelativeLayout mImageLayout;
    private RelativeLayout mVideoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_photo);
        mImageLayout = (RelativeLayout) findViewById(R.id.rl_image);
        mVideoLayout = (RelativeLayout) findViewById(R.id.rl_video);
        mImageLayout.setOnClickListener(this);
        mVideoLayout.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        int id = view.getId();
        switch (id) {
        case R.id.rl_image:
            startActivity(new Intent(PhotoActivity.this, AlbumeSetActivity.class));
            break;
        case R.id.rl_video:
            startActivity(new Intent(PhotoActivity.this, VideoPageActivity.class));
            break;

        default:
            break;
        }
    }
}

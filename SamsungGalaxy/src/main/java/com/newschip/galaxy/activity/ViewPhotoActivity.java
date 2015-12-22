package com.newschip.galaxy.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;


import com.newschip.galaxy.R;
import com.newschip.galaxy.media.FileObject;
import com.newschip.galaxy.media.ImageDownLoader;
import com.newschip.galaxy.widget.HackyViewPager;
import com.newschip.galaxy.widget.PhotoView;

import java.util.List;

public class ViewPhotoActivity extends Activity {

    private ViewPager mViewPager;
    private Intent intent;
    private List<String> list;
    private Context mContext;
    private int position;
    private FileObject mFileObject;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        findViewById();
        mContext = this;
        initView();
    }

    private void findViewById() {
        // TODO Auto-generated method stub
        mViewPager = new HackyViewPager(this);
        mViewPager.setBackgroundColor(Color.BLACK);
        setContentView(mViewPager);
    }

    protected void initView() {
        // TODO Auto-generated method stub
        intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }

        list = (List<String>) bundle.getSerializable("list");
        position = bundle.getInt("position", -100);
        if (list != null && list.size() > 0 && "".equals(list.get(0))) {
            list.remove(0);
        } else if (position == -100) {
            position = 0;
        } else {
            position = position + 1;
        }

        mViewPager.setAdapter(new SamplePagerAdapter());
        mViewPager.setCurrentItem(position);
    }

    class SamplePagerAdapter extends PagerAdapter {

        /*
         * private static int[] sDrawables = { R.mipmap.wallpaper,
         * R.mipmap.a1, R.mipmap.a2, R.mipmap.a3, R.mipmap.wallpaper,
         * R.mipmap.wallpaper };
         */

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());
            // photoView.setImageResource(sDrawables[position]);
            /**
             * 使用xutis加载图片
             */
            // BitmapUtils bitmapUtils = new BitmapUtils(mContext);
            // bitmapUtils.configDefaultLoadFailedImage(R.mipmap.friends_sends_pictures_no);
            //
            // // 加载本地图片(路径以/开头， 绝对路径)
            // bitmapUtils.display(photoView, list.get(position));

            // 显示图片的配置
            // DisplayImageOptions options = new DisplayImageOptions.Builder()
            // .showImageOnLoading(R.mipmap.default_1)
            // .showImageOnFail(R.mipmap.default_1)
            // .cacheInMemory(false)
            // .cacheOnDisk(false)
            // .considerExifParams(true)
            // .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)//设置图片以如何的编码方式显示
            // .bitmapConfig(Bitmap.Config.RGB_565)
            // .build();

            // String imageUrl = Scheme.FILE.wrap(list.get(position));
            // String imageUrl =
            // "http://img.my.csdn.net/uploads/201309/01/1378037235_7476.jpg";
            ImageDownLoader.showLocationImage(list.get(position), photoView,
                    R.mipmap.default_photo);
            // imageLoader.loadImage(list.get(position), photoView);
            // Now just add PhotoView to ViewPager and return it
            container.addView(photoView, LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);

            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

}

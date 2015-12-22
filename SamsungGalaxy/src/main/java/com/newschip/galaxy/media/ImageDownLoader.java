package com.newschip.galaxy.media;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;

public class ImageDownLoader {
    public static ImageLoader getImageLoader(){
        ImageLoader loader = ImageLoader.getInstance();
        return loader;
    }
    
    public static DisplayImageOptions getDisplayImageOptions(int defaultImage){
        DisplayImageOptions options = new DisplayImageOptions.Builder()
        .showImageOnLoading(defaultImage)
        .showImageForEmptyUri(defaultImage).showImageOnFail(defaultImage)
        .cacheInMemory(true).cacheOnDisk(true)
        .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
        .imageScaleType(ImageScaleType.NONE)
        // .considerExifParams(true)
        .imageScaleType(ImageScaleType.IN_SAMPLE_INT)// 设置图片以如何的编码方式显示
        // //
        // .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)//设置图片以如何的编码方式显示
        .bitmapConfig(Bitmap.Config.RGB_565)// 设置 图片的解码类型//
        .resetViewBeforeLoading(true)
        // .imageScaleType(ImageScaleType.IN_SAMPLE_INT)//设置图片以如何的编码方式显示
        // .resetViewBeforeLoading(true)
        // 设置图片在下载前是否重置，复位
        .build();
        return options;
    }

    public static void showLocationImage(String path,
            final ImageView imageView, final int loadingImg) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(loadingImg)
                .showImageForEmptyUri(loadingImg).showImageOnFail(loadingImg)
                .cacheInMemory(true).cacheOnDisk(true)
                .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
                .imageScaleType(ImageScaleType.NONE)
                // .considerExifParams(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)// 设置图片以如何的编码方式显示
                // //
                // .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)//设置图片以如何的编码方式显示
                .bitmapConfig(Bitmap.Config.RGB_565)// 设置 图片的解码类型//
                .resetViewBeforeLoading(true)
                // .imageScaleType(ImageScaleType.IN_SAMPLE_INT)//设置图片以如何的编码方式显示
                // .resetViewBeforeLoading(true)
                // 设置图片在下载前是否重置，复位
                .build();
        //
        String imageUrl = Scheme.FILE.wrap(path);
        // // String imageUrl =
        // "http://img.my.csdn.net/uploads/201309/01/1378037235_7476.jpg";
        ImageLoader mImageLoader = ImageLoader.getInstance();
        ImageLoader.getInstance().displayImage(imageUrl, imageView, options);

    }
    
    
}
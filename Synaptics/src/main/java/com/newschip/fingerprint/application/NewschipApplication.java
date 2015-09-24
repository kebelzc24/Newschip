package com.newschip.fingerprint.application;

import java.io.File;

import android.app.Application;
import android.content.Context;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class NewschipApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initImageLoader(getApplicationContext());
    }

    public static void initImageLoader(Context context) {
        // 获取缓存图片目录
        File cacheDir = StorageUtils.getOwnCacheDirectory(context,
                "imageloader/Cache");
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context)
                .threadPriority(Thread.MAX_PRIORITY - 1)
                .diskCache(new UnlimitedDiskCache(cacheDir))//自定义缓存路径
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                // 设置图片下载和显示的工作队列排序
                .denyCacheImageMultipleSizesInMemory()
                 .memoryCacheExtraOptions(400, 800)
                .threadPoolSize(4)//线程池内加载的数量 
                .diskCacheSize(10 * 1024 * 1024)
                .memoryCache(new UsingFreqLimitedMemoryCache(5*1024*1024))//add 设置内存缓存
                .memoryCacheSize(5 * 1024 * 1024)//add
                .discCacheFileCount(100) //缓存的文件数量 
                .discCache(new UnlimitedDiskCache(cacheDir)) 
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())//add
                // .memoryCache(new FIFOLimitedMemoryCache())
                // .memoryCacheSize(3 * 1024 * 1024)
                // .memoryCacheExtraOptions(480, 800) // default = device screen
                // dimensions
//                .discCacheExtraOptions(480, 800, CompressFormat.JPEG, 75, null) // Can slow ImageLoader, use it carefully (Better don't use it)/设置缓存的详细信息，最好不要设置这个
                .imageDownloader(
                        new BaseImageDownloader(context, 5 * 1000, 30 * 1000))// connectTimeout (10 s), readTimeout (30 s)超时时间  
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

}

package com.newschip.galaxy.media;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;

import java.io.File;

/**
 * Created by LQ on 2015/12/16.
 */
public class MediaBean {
    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_VIDEO = 1;
    private int mType;

    //图片相关
    //文件夹的第一张图片路径
    private String mImagePath;
    //文件夹名
    private String mImageName;
    // 文件夹中的图片数
    private int mImageCounts;


    //视频相关
    private long mVideoId;// _id
    private String mVideoPath;// _data
    private String mVideoName;// _display_name
    private long mVideoSize;// _size
    private String mVideoMimeType;// _mime_type
    private String mVideoTitle;// title
    private long mVideoDuration;// duration
    private Bitmap mVideoThumbnail;

    public MediaBean(int mType) {
        this.mType = mType;
    }

    public String getPath() {
        if (mType == TYPE_IMAGE) {
            return mImagePath;
        } else {
            return mVideoPath;
        }
    }

    public void setPath(String path) {
        if (mType == TYPE_IMAGE) {
            mImagePath = path;
        } else {
            mVideoPath = path;
        }
    }

    public void setName(String name) {
        if (mType == TYPE_IMAGE) {
            mImageName = name;
        } else {
            mVideoName = name;
        }
    }

    public String getName() {
        if (mType == TYPE_IMAGE) {
            return mImageName;
        } else {
            return mVideoName;
        }
    }

    public int getmType() {
        return mType;
    }


    public String getTopImagePath() {
        return mImagePath;
    }

    public void setTopImagePath(String topImagePath) {
        this.mImagePath = topImagePath;
    }

    public String getFolderName() {
        return mImageName;
    }

    public void setFolderName(String folderName) {
        this.mImageName = folderName;
    }

    public int getImageCounts() {
        return mImageCounts;
    }

    public void setImageCounts(int imageCounts) {
        this.mImageCounts = imageCounts;
    }


    public long getmVideoId() {
        return mVideoId;
    }

    public void setmVideoId(long mVideoId) {
        this.mVideoId = mVideoId;
    }

    public String getmVideoPath() {
        return mVideoPath;
    }

    public void setmVideoPath(String mVideoPath) {
        this.mVideoPath = mVideoPath;
    }

    public String getmVideoName() {
        return mVideoName;
    }

    public void setmVideoName(String mVideoName) {
        this.mVideoName = mVideoName;
    }

    public long getmVideoSize() {
        return mVideoSize;
    }

    public void setmVideoSize(long mVideoSize) {
        this.mVideoSize = mVideoSize;
    }

    public String getmVideoMimeType() {
        return mVideoMimeType;
    }

    public void setmVideoMimeType(String mVideoMimeType) {
        this.mVideoMimeType = mVideoMimeType;
    }

    public String getmVideoTitle() {
        return mVideoTitle;
    }

    public void setmVideoTitle(String mVideoTitle) {
        this.mVideoTitle = mVideoTitle;
    }

    public long getmVideoDuration() {
        return mVideoDuration;
    }

    public void setmVideoDuration(long mVideoDuration) {
        this.mVideoDuration = mVideoDuration;
    }

    public Bitmap getmVideoThumbnail() {
        return mVideoThumbnail;
    }

    public void setmVideoThumbnail(Bitmap mVideoThumbnail) {
        this.mVideoThumbnail = mVideoThumbnail;
    }
}

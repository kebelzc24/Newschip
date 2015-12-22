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
    private String mTopImagePath;
    //文件夹名
    private String mFolderName;
    // 文件夹中的图片数
    private int mImageCounts;
    private String mParentFolderPath;


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

    public String getPath(){
        if(mType == TYPE_IMAGE){
            return mTopImagePath;
        } else {
            return mVideoPath;
        }
    }
    public int getmType() {
        return mType;
    }
    public String getParentFolderPath() {
        mParentFolderPath = new File(mTopImagePath).getParentFile()
                .getAbsolutePath();
        return mParentFolderPath;
    }

    public String getTopImagePath() {
        return mTopImagePath;
    }

    public void setTopImagePath(String topImagePath) {
        this.mTopImagePath = topImagePath;
    }

    public String getFolderName() {
        return mFolderName;
    }

    public void setFolderName(String folderName) {
        this.mFolderName = folderName;
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

    private Bitmap getVideoThumbnail(String videoPath, int width, int height,
                                     int kind) {
        Bitmap bitmap = null;
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }


}

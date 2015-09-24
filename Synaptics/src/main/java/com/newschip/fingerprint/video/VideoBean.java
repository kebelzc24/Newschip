package com.newschip.fingerprint.video;

import java.io.Serializable;

import com.newschip.fingerprint.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

public class VideoBean implements Serializable{
    private static final long serialVersionUID = -7605274499678828857L;
    private long mId;// _id
    private String mPath;// _data
    private String mName;// _display_name
    private long mSize;// _size
    private String mMimeType;// _mime_type
    private String mTitle;// title
    private long mDuration;// duration
    private Bitmap mThumbnail;

    public VideoBean(String path, String name, long size, long duration) {
        this.mPath = path;
        this.mName = name;
        this.mSize = size;
        this.mDuration = duration;
    }

    public long getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String mPath) {
        this.mPath = mPath;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long mSize) {
        this.mSize = mSize;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public void setMimeType(String mMimeType) {
        this.mMimeType = mMimeType;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long mDuration) {
        this.mDuration = mDuration;
    }

    public Bitmap getThumbnail() {
        return mThumbnail;
    }
    
    public void setThumbnail(Bitmap bitmap){
        this.mThumbnail = bitmap;
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

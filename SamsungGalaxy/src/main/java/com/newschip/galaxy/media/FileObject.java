package com.newschip.galaxy.media;

import com.newschip.galaxy.utils.FileUtils;

import java.io.File;
import java.io.Serializable;
import java.security.PublicKey;

public class FileObject implements Serializable{

    private File mFile;
    private String mName;
    private String mPath;
    private int mType;//image,vedio,or...
    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_VIDEO = 1;

    public FileObject(int mType) {
        this.mType = mType;
    }

    public int getmType() {
        return mType;
    }

    public void setmType(int mType) {
        this.mType = mType;
    }

    public File getFile(){
        return new File(mPath);
    }
    public boolean isImage(){
       return FileUtils.isImageFile(mName);
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String mPath) {
        this.mPath = mPath;
    }

}

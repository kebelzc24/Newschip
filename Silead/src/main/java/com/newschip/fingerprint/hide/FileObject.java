package com.newschip.fingerprint.hide;

import java.io.File;
import java.io.Serializable;

public class FileObject implements Serializable{

    private File mFile;
    private String mName;
    private String mPath;
    private String mType;//image,vedio,or...
    
    public FileObject(){
        
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

    public String getType() {
        return mType;
    }

    public void setType(String mType) {
        this.mType = mType;
    }
}

package com.newschip.galaxy.media;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;


import java.io.File;
import java.util.ArrayList;

public class MediaUtils {

    public static void syncMediaData(Context context, final File file) {
        MediaScanner scanner = new MediaScanner(context);
        scanner.setOnMediaScanListener(new MediaScanner.OnMediaScanListener() {

            @Override
            public void OnFinish() {
                // TODO Auto-generated method stub
            }
        });
        scanner.scanfile(file);
    }

    public static void syncMediaData(Context context,String path) {
        File file = new File(path);
        syncMediaData(context,file);
    }

    public static void syncMediaData(Context context,ArrayList<String> paths) {
        for (int i = 0; i < paths.size(); i++) {
            syncMediaData(context,paths.get(i));
        }
    }
    
    public static void deleteMediaData(Context context,File filePath) {
        if (filePath.isDirectory()) {
            File[] files = filePath.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    deleteMediaData(context,files[i]);
                }
            }
        } else {
            Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentResolver mContentResolver = context.getContentResolver();
            mContentResolver.delete(mImageUri, MediaColumns.DATA + "=?",
                    new String[] { filePath.getAbsolutePath() });
        }
    }
    public static void deleteVideoData(Context context,File filePath) {
        if (filePath.isDirectory()) {
            File[] files = filePath.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    deleteMediaData(context,files[i]);
                }
            }
        } else {
            Uri mImageUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            ContentResolver mContentResolver = context.getContentResolver();
            mContentResolver.delete(mImageUri, MediaColumns.DATA + "=?",
                    new String[] { filePath.getAbsolutePath() });
        }
    }

    public static void updateMediaData(Context context,File file, boolean hide) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    updateMediaData(context,files[i], hide);
                }
            }
        } else {
            ContentResolver cr = context.getContentResolver();
            Uri uri = MediaStore.Files.getContentUri("external");

            // every column, although that is huge waste, you probably need
            // BaseColumns.DATA (the path) only.
            ContentValues mValues = new ContentValues();
            int type;
            if (hide) {
                type = MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;
            } else {
                type = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
            }
            mValues.put(MediaStore.Files.FileColumns.MEDIA_TYPE, type);
            cr.update(uri, mValues, MediaStore.Files.FileColumns._ID + "=?",
                    new String[] { file.getAbsolutePath() });
        }

    }
}

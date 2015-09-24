package com.newschip.fingerprint.hide;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

public class MediaScanner {

    private MediaScannerConnection mediaScanConn = null;

    private MusicSannerClient client = null;

    private File filePath = null;

    private String fileType = null;

    private OnMediaScanListener listener;

    /**
     * 然后调用MediaScanner.scanFile("/sdcard/2.mp3");
     * */
    public MediaScanner(Context context) {
        // 创建MusicSannerClient
        if (client == null) {

            client = new MusicSannerClient();
        }

        if (mediaScanConn == null) {

            mediaScanConn = new MediaScannerConnection(context, client);
        }
    }

    public interface OnMediaScanListener {
        public void OnFinish();
    }

    public void setOnMediaScanListener(OnMediaScanListener listener) {
        this.listener = listener;
    }

    class MusicSannerClient implements
            MediaScannerConnection.MediaScannerConnectionClient {

        public void onMediaScannerConnected() {

            if (filePath != null) {

                if (filePath.isDirectory()) {
                    File[] files = filePath.listFiles();
                    if (files != null) {
                        for (int i = 0; i < files.length; i++) {
                            if (files[i].isDirectory())
                                scanfile(files[i]);
                            else {
                                mediaScanConn.scanFile(
                                        files[i].getAbsolutePath(), fileType);
                            }
                        }
                    }
                } else {
                    mediaScanConn.scanFile(
                            filePath.getAbsolutePath(), fileType);
                }
            }

            filePath = null;

        }

        public void onScanCompleted(String path, Uri uri) {
            // TODO Auto-generated method stub
            Log.d("liuzhicang", "scanner path = "+ path);
            mediaScanConn.disconnect();
//            listener.OnFinish();
        }

    }

    public void scanfile(File f) {
        this.filePath = f;
        mediaScanConn.connect();
    }

    public void scanfile(ArrayList<File> files) {
        int size = files.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                scanfile(files.get(i));
            }
        }
    }

}
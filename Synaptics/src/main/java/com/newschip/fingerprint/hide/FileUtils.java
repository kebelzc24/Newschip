package com.newschip.fingerprint.hide;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;

public class FileUtils {

    private final static String NOMEDIA = ".nomedia";

    public static boolean isSDCardMounted() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    public static String getFileDeHidePath() {
        return getSDDirectory() + "favor" + File.separator;

    }

    public static String getFileHidePath() {
        return getSDDirectory() + "Android" + File.separator + "data"
                + File.separator + "com.newschip.gallery" + "/.hide"
                + File.separator+"File"+ File.separator;

    }
    public static String getImageHidePath() {
        return getSDDirectory() + "Android" + File.separator + "data"
                + File.separator + "com.newschip.gallery" + "/.hide"
                + File.separator+"Image"+ File.separator;
        
    }
    public static String getVideoHidePath() {
        return getSDDirectory() + "Android" + File.separator + "data"
                + File.separator + "com.newschip.gallery" + "/.hide"
                + File.separator+"Video"+ File.separator;
        
    }

    public static void mkdir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static String getSDDirectory() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator;
    }

    public static String getPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "2";
    }

    public static void createNomediaFile(String path) {
        File nomedia = new File(path + File.separator + ".nomedia");
        if (!nomedia.exists())
            try {
                nomedia.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public static void removeNomediaFile(String path) {
        File nomedia = new File(path + File.separator + ".nomedia");
        if (nomedia.exists())
            nomedia.delete();
    }

    /**
     * 复制单个文件
     * 
     * @param oldPath
     *            String 原文件路径 如：c:/fqf.txt
     * @param newPath
     *            String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static void copyFile(String oldPath, String newPath) {
        try {
            (new File(newPath).getParentFile()).mkdirs();
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (!oldfile.exists() || !oldfile.isFile() || !oldfile.canRead()
                    || !isImageFile(oldfile)) {
                return;
            }
            // 文件存在时
            InputStream inStream = new FileInputStream(oldPath); // 读入原文件
            FileOutputStream fs = new FileOutputStream(newPath);
            byte[] buffer = new byte[1444];
            while ((byteread = inStream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteread);
            }
            inStream.close();
            fs.close();

        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        } finally {
        }

    }

    /**
     * 复制整个文件夹内容
     * 
     * @param oldPath
     *            String 原文件路径 如：c:/fqf
     * @param newPath
     *            String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    public static void copyFolder(String oldPath, String newPath) {

        try {
            (new File(newPath)).mkdirs(); // 如果文件夹不存在 则建立新文件夹
            File a = new File(oldPath);
            String[] file = a.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }

                // 只复制图片
                if (temp.isFile() && isImageFile(temp)) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath
                            + "/" + (temp.getName()).toString());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {// 如果是子文件夹
                    copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
                }
            }
        } catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错");
            e.printStackTrace();

        }

    }

    public static void deleteFile(String path) {
        File file = new File(path);
        deleteFile(file);
    }

    public static void deleteFile(File file) {
        // 只删除图片文件
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                deleteFile(f);
            }
            if (file.list().length < 1) {
                // 没有其他文件了，删除目录，否则保留
                file.delete();
            }

        } else if (isImageFile(file)) {
            file.delete();
        }
    }

    public static String getFileName(String path) {
        if (path != null) {
            int typeIndex = path.lastIndexOf(File.separator);
            if (typeIndex != -1) {
                String name = path.substring(typeIndex + 1).toLowerCase();
                return name;
            }
        }
        return "";
    }

    public static String getFileType(String fileName) {
        if (fileName != null) {
            int typeIndex = fileName.lastIndexOf(".");
            if (typeIndex != -1) {
                String fileType = fileName.substring(typeIndex + 1)
                        .toLowerCase();
                return fileType;
            }
        }
        return "";
    }

    public static boolean isImageFile(String name) {
        String type = getFileType(name);
        if (type != null
                && (type.equals("jpg") || type.equals("png") || type
                        .equals("jpeg"))) {
            return true;
        }
        return false;

    }

    public static boolean isImageFile(File file) {
        String name = file.getName();
        String type = getFileType(name);
        if (type != null
                && (type.equals("jpg") || type.equals("png") || type
                        .equals("jpeg"))) {
            return true;
        }
        return false;

    }

    /**
     * 移动文件
     * 
     * @param srcFileName
     *            源文件完整路径
     * @param destDirName
     *            目的目录完整路径
     * @return 文件移动成功返回true，否则返回false
     */
    public static boolean moveFile(String srcFileName, String destDirName) {

        File srcFile = new File(srcFileName);
        if (!srcFile.exists() || !srcFile.isFile())
            return false;

        File destDir = new File(destDirName);
        if (!destDir.exists())
            destDir.mkdirs();

        return srcFile.renameTo(new File(destDirName + File.separator
                + srcFile.getName()));
    }
}

package cn.iclass.webapp.qudu.util;

import android.os.Environment;

import java.io.File;


public class FileUtil {

    public static boolean checkSdCard() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    public static String getFilenameFromUrl(String url) {
        int endPos = url.indexOf('?');
        endPos = endPos == -1 ? url.length() : endPos;
        return url.substring(url.lastIndexOf('/') + 1, endPos);
    }


    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else {
                String[] filePaths = file.list();
                for (String path : filePaths) {
                    deleteFile(filePath + File.separator + path);
                }
                file.delete();
            }
        }
    }

}

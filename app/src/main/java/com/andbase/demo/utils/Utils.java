package com.andbase.demo.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

/**
 * Created by Administrator on 2015/11/30.
 */
public class Utils {
    public static void install(Context context, String path) {
        if (context == null) {
            throw new RuntimeException("context==null");
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(path)),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    public static boolean deleteFileSafely(File file) {
        if (file != null) {
            String tmpPath = file.getParent() + File.separator + System.currentTimeMillis();
            File tmp = new File(tmpPath);
            file.renameTo(tmp);
            return tmp.delete();
        }
        return false;
    }

    public static void createDirIfNotExists(String fileDir) {
        File dir = new File(fileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

}

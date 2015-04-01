package com.miscell.glasswiping.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by chenjishi on 15/2/28.
 */
public class DirectoryUtils {

    public static void init(Context context) {
        mkDirs(getCacheDirectory(context));
    }

    public static void mkDirs(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) file.mkdirs();
    }

    public static String getTempCacheDir() {
        return getSDCardDirectory() + "/beauty/";
    }

    public static String getSDCardDirectory() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return Environment.getExternalStorageDirectory().getPath();
        }
        return null;
    }

    public static String getCacheDirectory(Context context) {
        return getRootDirectory(context) + "/cache/";
    }

    public static String getRootDirectory(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            final String cacheDir = "/Android/data/" + context.getPackageName();
            return Environment.getExternalStorageDirectory() + cacheDir;
        } else {
            String path = null;
            File cacheDir = context.getCacheDir();
            if (cacheDir.exists()) path = cacheDir.getAbsolutePath();
            return path;
        }
    }
}

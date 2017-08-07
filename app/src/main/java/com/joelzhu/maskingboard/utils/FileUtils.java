package com.joelzhu.maskingboard.utils;

import android.os.Environment;

import java.io.File;

public final class FileUtils {
    /**
     * 文件根目录
     *
     * @return 文件目录
     */
    public static String getFileDir() {
        return Environment.getExternalStorageDirectory().getPath() + File.separator + "masking";
    }
}
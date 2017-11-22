package com.joelzhu.maskingboard.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;

/**
 * 文件处理工具类
 *
 * @author JoelZhu
 */
public final class FileUtils {
    /**
     * 文件根目录
     *
     * @return 文件目录
     */
    public static String getFileDir() {
        return Environment.getExternalStorageDirectory().getPath() + File.separator + "masking";
    }

    /**
     * 根据Uri获取图片路径
     *
     * @param context Context
     * @param uri     Uri
     * @return 图片路径
     */
    public static String getFilePathFromUri(Context context, Uri uri) {
        // 异常判断
        if (context == null || uri == null)
            return "";

        final String scheme = uri.getScheme();
        String data = null;
        // Scheme为空
        if (scheme == null) {
            data = uri.getPath();
        }
        // Scheme以"file:"开头
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        }
        // Scheme以"content:"开头
        else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }
}
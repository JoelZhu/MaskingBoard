package com.joelzhu.maskingboard.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

public final class JZDeviceUtils {
    /**
     * 判断应用是否获取相机权限
     *
     * @param activity 宿主Activity
     * @return true - 获取到权限，false - 未获取到权限
     */
    public static boolean isGrantedCameraPermission(Activity activity) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 判断应用是否获取读写权限
     *
     * @param activity 宿主Activity
     * @return true - 获取到权限，false - 未获取到权限
     */
    public static boolean isGrantedStoragePermission(Activity activity) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
}
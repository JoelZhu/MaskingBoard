package com.joelzhu.maskingboard.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public final class JZDeviceUtils {
    /**
     * 检查单个权限
     *
     * @param context    Context
     * @param permission 权限
     * @return true - 已经获取到权限；false - 没有获取到权限
     */
    public static boolean checkPermission(Context context, String permission) {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 检查多个权限
     *
     * @param context     Context
     * @param permissions 权限数组
     * @return 未获取到的权限数组
     */
    public static String[] checkPermissions(Context context, String[] permissions) {
        // 初始化未获取到的权限数组
        List<String> requirePermissions = new ArrayList<>();

        // 遍历需要检查的权限
        for (String permission : permissions) {
            // 该权限未获取到
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                // 加入结果数组中
                requirePermissions.add(permission);
            }
        }

        // 返回
        return requirePermissions.toArray(new String[]{});
    }

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
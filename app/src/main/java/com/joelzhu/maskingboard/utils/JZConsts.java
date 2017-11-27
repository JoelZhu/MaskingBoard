package com.joelzhu.maskingboard.utils;

/**
 * 常量类
 *
 * @author JoelZhu
 */
public final class JZConsts {
    // Log标签
    public static final String LogTag = "JoelZhu";

    // 相册请求码
    public static final int GALLERY_REQUEST_CODE = 101;

    // 应用首次权限请求码
    public static final int INIT_PERMISSION = 0;
    // 相机权限请求码
    public static final int CAMERA_PERMISSION = 1;
    // 读写权限请求码
    public static final int GALLERY_PERMISSION = 2;

    // 裁剪图片最大尺寸
    public static final int ORIGIN_MAX = 4000;

    // Intent参数key值
    public static final String ExtraPictureUri = "ExtraPictureUri";
    public static final String ExtraRotateDegree = "ExtraRotateDegree";
    public static final String ExtraIsFromCamera = "ExtraIsFromCamera";
}
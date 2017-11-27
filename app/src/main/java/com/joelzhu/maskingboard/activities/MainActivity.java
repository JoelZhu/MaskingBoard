package com.joelzhu.maskingboard.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.joelzhu.maskingboard.R;
import com.joelzhu.maskingboard.adapters.PictureAdapter;
import com.joelzhu.maskingboard.models.LayoutAttrs;
import com.joelzhu.maskingboard.utils.JZConsts;
import com.joelzhu.maskingboard.utils.JZDeviceUtils;
import com.joelzhu.maskingboard.utils.JZFileUtils;
import com.joelzhu.maskingboard.utils.JZSharedPerferenceUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 主界面
 *
 * @author JoelZhu
 */
public class MainActivity extends BaseActivity implements PictureAdapter.OnButtonClickListener, AdapterView.OnItemClickListener {
    // 适配器
    private PictureAdapter adapter;

    // 图片文件数组
    private List<Uri> uris;

    @Override
    protected LayoutAttrs setLayoutAttributes() {
        return new LayoutAttrs.Builder()
                .layout(R.layout.activity_main)
                .title(R.string.app_name)
                .hasToolbar(true)
                .create();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化数组
        uris = new ArrayList<>();

        // 配置GridView
        GridView gridView = (GridView) findViewById(R.id.main_gridView);
        adapter = new PictureAdapter(this, uris);
        gridView.setAdapter(adapter);
        // 注册GridView点击事件
        gridView.setOnItemClickListener(this);
        // 注册按钮点击事件
        adapter.setOnButtonClickListener(this);

        // 判断应用是否是首次运行
        if (JZSharedPerferenceUtils.getAppFirstLaunch(this)) {
            // 初始化申请权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, JZConsts.INIT_PERMISSION);
            }

            // 设置应用为非首次运行
            JZSharedPerferenceUtils.setAppFirstLaunch(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 更新文件数组
        uris.clear();
        File file = new File(JZFileUtils.getFileDir());
        if (file.exists() && file.listFiles() != null && file.listFiles().length > 0) {
            for (File childFile : file.listFiles()) {
                uris.add(Uri.fromFile(childFile));
            }
        }
        // 通知适配器数据修改
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case JZConsts.GALLERY_REQUEST_CODE:
                    // 跳转涂鸦页面
                    goToMaskingActivity(JZFileUtils.getFilePathFromUri(MainActivity.this, data.getData()));
                    break;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        // 跳转涂鸦页面
        goToMaskingActivity(JZFileUtils.getFilePathFromUri(MainActivity.this, uris.get(position)));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 根据请求码判断
        switch (requestCode) {
            case JZConsts.INIT_PERMISSION:
                // 初始化申请权限
                for (int i = 0; i < permissions.length; i++) {
                    if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[i]) &&
                            grantResults[i] == PackageManager.PERMISSION_DENIED)
                        Toast.makeText(this, "读写权限被拒绝，将无法正常使用", Toast.LENGTH_SHORT).show();
                }
                break;

            case JZConsts.CAMERA_PERMISSION:
                // 相机申请权限
                // 是否获取到所有权限标志位
                boolean canGoToCamera = true;
                // 用户是否点击了不再提示标志位
                boolean shouldShowHint = false;
                for (String permission : permissions) {
                    // 没有获取到该权限
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        canGoToCamera = false;
                    } else {
                        // 用户点击了不再提示
                        if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                            canGoToCamera = false;
                            shouldShowHint = true;
                        }
                    }
                }

                if (canGoToCamera)
                    // 跳转拍照页面
                    goToCameraActivity();
                else if (shouldShowHint)
                    // 用户勾选不再提示
                    Toast.makeText(this, "User Set Never Ask Again", Toast.LENGTH_SHORT).show();
                else
                    // 用户拒绝提供权限
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                break;

            case JZConsts.GALLERY_PERMISSION:
                // 图库申请权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                } else {
                    if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
                        // 跳转系统相册
                        goToGalleryActivity();
                    } else {
                        Toast.makeText(this, "User Set Never Ask Again", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    public void onCameraClick() {
        // 检查是否拥有相机和读写权限
        if (JZDeviceUtils.isGrantedCameraPermission(this) && !JZDeviceUtils.isGrantedStoragePermission(this))
            // 缺少读写权限
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, JZConsts.CAMERA_PERMISSION);
        else if (!JZDeviceUtils.isGrantedCameraPermission(this) && JZDeviceUtils.isGrantedStoragePermission(this))
            // 缺少相机权限
            requestPermissions(new String[]{Manifest.permission.CAMERA}, JZConsts.CAMERA_PERMISSION);
        else if (!JZDeviceUtils.isGrantedCameraPermission(this) && !JZDeviceUtils.isGrantedStoragePermission(this))
            // 缺少相机和读写权限
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, JZConsts.CAMERA_PERMISSION);
        else
            // 跳转拍照页面
            goToCameraActivity();
    }

    @Override
    public void onGalleryClick() {
        // 检查是否拥有相机权限
        if (!JZDeviceUtils.isGrantedStoragePermission(this))
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, JZConsts.GALLERY_PERMISSION);
        else
            // 跳转系统相册
            goToGalleryActivity();
    }

    /**
     * 跳转拍照页面
     */
    private void goToCameraActivity() {
        // 跳转拍照页面
        Intent cameraIntent = new Intent(this, CameraActivity.class);
        startActivity(cameraIntent);
    }

    /**
     * 跳转系统相册
     */
    private void goToGalleryActivity() {
        // 跳转系统相册
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(galleryIntent, JZConsts.GALLERY_REQUEST_CODE);
    }

    /**
     * 跳转涂鸦页面
     *
     * @param filePath 文件路径
     */
    private void goToMaskingActivity(String filePath) {
        Intent intent = new Intent(this, MaskingActivity.class);
        intent.putExtra(JZConsts.ExtraPictureUri, filePath);
        intent.putExtra(JZConsts.ExtraRotateDegree, 0);
        startActivity(intent);
    }
}
package com.joelzhu.maskingboard.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.joelzhu.maskingboard.R;
import com.joelzhu.maskingboard.adapters.PictureAdapter;
import com.joelzhu.maskingboard.common.JZPermissionHelper;
import com.joelzhu.maskingboard.models.LayoutAttrs;
import com.joelzhu.maskingboard.utils.JZConsts;
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
public class MainActivity extends PermissionBaseActivity implements PictureAdapter.OnButtonClickListener,
        AdapterView.OnItemClickListener {
    // 适配器
    private PictureAdapter adapter;

    // 图片文件数组
    private List<Uri> uris;

    // 没有存储权限警告框
    private AlertDialog storageRequireDialog;
    // 权限不足警告框
    private AlertDialog permissionInsufficientDialog;

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
            new JZPermissionHelper.Builder(this)
                    .onUserDenied(new OnUserDenied() {
                        @Override
                        public void onUserDenied(String[] deniedPermissions) {
                            for (String permission : deniedPermissions) {
                                if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
                                    storageRequireDialog.show();
                                    break;
                                }
                            }
                        }
                    })
                    .requestCode(JZConsts.INIT_PERMISSION)
                    .create()
                    .requestPermission(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});

            // 设置应用为非首次运行
            JZSharedPerferenceUtils.setAppFirstLaunch(this);
        }

        // 初始化权限不足警告框
        storageRequireDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(getString(R.string.permission_storage))
                .setPositiveButton(getString(R.string.permission_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        storageRequireDialog.dismiss();
                    }
                })
                .create();
        // 初始化存储权限警告框
        permissionInsufficientDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(getString(R.string.permission_require))
                .setPositiveButton(getString(R.string.permission_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        permissionInsufficientDialog.dismiss();
                    }
                })
                .create();
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
        final Uri tempUri = uris.get(position);
        if (tempUri != Uri.EMPTY)
            // 跳转涂鸦页面
            goToMaskingActivity(JZFileUtils.getFilePathFromUri(MainActivity.this, uris.get(position)));
    }

    @Override
    public void onCameraClick() {
        new JZPermissionHelper.Builder(this)
                .onUserAllowed(new OnUserAllowed() {
                    @Override
                    public void onUserAllowed() {
                        // 跳转拍照页面
                        goToCameraActivity();
                    }
                })
                .onUserSetNeverAsk(new OnUserSetNeverAsk() {
                    @Override
                    public void onUserSetNeverAsk(String[] neverAskPermissions) {
                        permissionInsufficientDialog.show();
                    }
                })
                .onPermissionsGrantedAlready(new OnPermissionsGrantedAlready() {
                    @Override
                    public void onPermissionsGrantedAlready() {
                        // 跳转拍照页面
                        goToCameraActivity();
                    }
                })
                .requestCode(JZConsts.CAMERA_PERMISSION)
                .create()
                .requestPermission(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
    }

    @Override
    public void onGalleryClick() {
        new JZPermissionHelper.Builder(this)
                .onUserAllowed(new OnUserAllowed() {
                    @Override
                    public void onUserAllowed() {
                        // 跳转系统相册
                        goToGalleryActivity();
                    }
                })
                .onUserSetNeverAsk(new OnUserSetNeverAsk() {
                    @Override
                    public void onUserSetNeverAsk(String[] neverAskPermissions) {
                        permissionInsufficientDialog.show();
                    }
                })
                .onPermissionsGrantedAlready(new OnPermissionsGrantedAlready() {
                    @Override
                    public void onPermissionsGrantedAlready() {
                        // 跳转系统相册
                        goToGalleryActivity();
                    }
                })
                .requestCode(JZConsts.GALLERY_PERMISSION)
                .create()
                .requestPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
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
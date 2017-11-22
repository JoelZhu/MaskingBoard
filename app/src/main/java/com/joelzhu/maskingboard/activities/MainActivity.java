package com.joelzhu.maskingboard.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import com.joelzhu.maskingboard.R;
import com.joelzhu.maskingboard.models.LayoutAttrs;
import com.joelzhu.maskingboard.utils.Consts;
import com.joelzhu.maskingboard.utils.FileUtils;

/**
 * 主界面
 *
 * @author JoelZhu
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {
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

        // 添加按钮点击事件
        findViewById(R.id.main_fromCamera).setOnClickListener(this);
        findViewById(R.id.main_fromGallery).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_fromCamera:
                // 相机
                Intent cameraIntent = new Intent(this, CameraActivity.class);
                startActivity(cameraIntent);
                break;

            case R.id.main_fromGallery:
                // 相册
                Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(galleryIntent, Consts.GALLERY_REQUEST_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case Consts.GALLERY_REQUEST_CODE:
                    // 相册
                    String filePath = FileUtils.getFilePathFromUri(MainActivity.this, data.getData());
                    Intent intent = new Intent(this, MaskingActivity.class);
                    intent.putExtra(Consts.ExtraPictureUri, filePath);
                    startActivity(intent);
                    break;
            }
        }
    }
}
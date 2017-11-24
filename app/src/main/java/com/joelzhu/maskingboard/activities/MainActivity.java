package com.joelzhu.maskingboard.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;

import com.joelzhu.maskingboard.R;
import com.joelzhu.maskingboard.models.LayoutAttrs;
import com.joelzhu.maskingboard.utils.JZConsts;
import com.joelzhu.maskingboard.utils.JZFileUtils;
import com.joelzhu.maskingboard.views.JZAddButton;

/**
 * 主界面
 *
 * @author JoelZhu
 */
public class MainActivity extends BaseActivity implements JZAddButton.OnButtonClickListener {
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

        ((JZAddButton)findViewById(R.id.main_addButton)).setOnButtonClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case JZConsts.GALLERY_REQUEST_CODE:
                    // 相册
                    String filePath = JZFileUtils.getFilePathFromUri(MainActivity.this, data.getData());
                    Intent intent = new Intent(this, MaskingActivity.class);
                    intent.putExtra(JZConsts.ExtraPictureUri, filePath);
                    startActivity(intent);
                    break;
            }
        }
    }

    @Override
    public void onCameraClick() {
        Intent cameraIntent = new Intent(this, CameraActivity.class);
        startActivity(cameraIntent);
    }

    @Override
    public void onGalleryClick() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(galleryIntent, JZConsts.GALLERY_REQUEST_CODE);
    }
}
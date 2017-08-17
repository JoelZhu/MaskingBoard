package com.joelzhu.maskingboard.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import com.joelzhu.maskingboard.R;
import com.joelzhu.maskingboard.utils.Consts;

public class MainActivity extends Activity implements View.OnClickListener {
    private final static int GALLERY_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        findViewById(R.id.Main_FromCamera).setOnClickListener(this);
        findViewById(R.id.Main_FromGallery).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.Main_FromCamera:
                intent = new Intent(this, CameraActivity.class);
                startActivity(intent);
                break;

            case R.id.Main_FromGallery:
                intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, GALLERY_REQUEST_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GALLERY_REQUEST_CODE:
                    Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        String imagePath = cursor.getString(cursor.getColumnIndex("_data"));
                        cursor.close();

                        Intent intent = new Intent(this, MaskingActivity.class);
                        intent.putExtra(Consts.ExtraPictureUri, imagePath);
                    }
                    break;
            }
        }
    }
}
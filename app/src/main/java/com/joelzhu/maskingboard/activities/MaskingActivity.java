package com.joelzhu.maskingboard.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.joelzhu.maskingboard.R;
import com.joelzhu.maskingboard.models.LayoutAttrs;
import com.joelzhu.maskingboard.utils.Consts;
import com.joelzhu.maskingboard.utils.DisplayUtils;
import com.joelzhu.maskingboard.utils.FileUtils;
import com.joelzhu.maskingboard.views.MaskingView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;

public class MaskingActivity extends BaseActivity implements View.OnClickListener, MaskingView
        .OnPathCountChangeListener {
    // 原图最大长宽
    private final static int ORIGIN_MAX = 2000;
    // 按钮图标长宽(单位：dp)
    private final static int BUTTON_ICON_WIDTH = 39;
    private final static int BUTTON_ICON_HEIGHT = 30;

    private MaskingView maskingView;
    private Bitmap bitmap;
    private ProgressBar progressBar;

    private Uri uri;

    private boolean isProcessing = false;

    // 按钮
    private Button goBackButton, maskingButton, dragButton;

    @Override
    protected LayoutAttrs setLayoutAttributes() {
        return new LayoutAttrs.Builder()
                .layout(R.layout.activity_masking)
                .title(R.string.title_masking)
                .hasToolbar(true)
                .create();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化控件
        initWidget();

        uri = Uri.parse(getIntent().getStringExtra(Consts.ExtraPictureUri));
        int rotateDegree = getIntent().getIntExtra(Consts.ExtraRotateDegree, 90);

        //        if (!"content".equals(uri.getScheme()))
        //        {
        bitmap = BitmapFactory.decodeFile(uri.getPath());
        //        }
        //        else
        //        {
        //            bitmap = BitmapFactory.decodeFile(FileUtil.GetPath(this, uri));
        //        }

        if (bitmap != null) {
            Matrix matrix = new Matrix();
            if (rotateDegree != 0) {
                matrix.setRotate(rotateDegree);
            }

            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            Bitmap destBitmap;
            if (bitmapWidth > ORIGIN_MAX || bitmapHeight > ORIGIN_MAX) {
                float scale;

                if (bitmapWidth > bitmapHeight) {
                    scale = (float) ORIGIN_MAX / bitmap.getWidth();
                    bitmapWidth = ORIGIN_MAX;
                    bitmapHeight = (int) (scale * bitmap.getHeight());
                } else {
                    scale = (float) ORIGIN_MAX / bitmap.getHeight();
                    bitmapWidth = (int) (scale * bitmap.getWidth());
                    bitmapHeight = ORIGIN_MAX;
                }

                matrix.setScale(scale, scale);
            }

            destBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
            maskingView.setImageBitmap(destBitmap);
        } else {
            Log.d(Consts.LogTag, "Masking OnCreate bitmap is null");
        }

        View maskingHint = findViewById(R.id.masking_maskingHint);
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        maskingHint.measure(w, h);
        int height = maskingHint.getMeasuredHeight();
        maskingView.setOffsetHeight(height);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    @Override
    public void onClick(View view) {
        // 如果现在系统正在处理中，忽略点击事件
        if (isProcessing) {
            return;
        }

        switch (view.getId()) {
            // 回退
            case R.id.masking_goBack:
                maskingView.goBack();
                break;

            // 涂鸦
            case R.id.masking_masking:
                maskingView.setMaskingMode(true);
                maskingButton.setSelected(true);
                dragButton.setSelected(false);
                break;

            // 拖拽
            case R.id.masking_drag:
                maskingView.setMaskingMode(false);
                maskingButton.setSelected(false);
                dragButton.setSelected(true);
                break;

            // 旋转
            case R.id.masking_rotate:
                rotateMaskingView();
                break;

            // 保存
            case R.id.masking_finish:
                saveBitmapToFile();
                break;
        }
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        maskingView = (MaskingView) findViewById(R.id.masking_maskingView);
        progressBar = (ProgressBar) findViewById(R.id.base_progressBar);

        goBackButton = (Button) findViewById(R.id.masking_goBack);
        maskingButton = (Button) findViewById(R.id.masking_masking);
        dragButton = (Button) findViewById(R.id.masking_drag);
        Button rotateButton = (Button) findViewById(R.id.masking_rotate);

        // 计算按钮图标的长宽
        final int iconWidth = DisplayUtils.dp2Px(this, BUTTON_ICON_WIDTH);
        final int iconHeight = DisplayUtils.dp2Px(this, BUTTON_ICON_HEIGHT);
        // 设置回退按钮图标大小
        Drawable goBackDrawable = getResources().getDrawable(R.drawable.back_icon_selector);
        goBackDrawable.setBounds(0, 0, iconWidth, iconHeight);
        goBackButton.setCompoundDrawables(null, goBackDrawable, null, null);
        // 设置涂鸦按钮图标大小
        Drawable maskingDrawable = getResources().getDrawable(R.drawable.masking_icon_selector);
        maskingDrawable.setBounds(0, 0, iconWidth, iconHeight);
        maskingButton.setCompoundDrawables(null, maskingDrawable, null, null);
        // 设置拖拽按钮图标大小
        Drawable dragDrawable = getResources().getDrawable(R.drawable.drag_icon_selector);
        dragDrawable.setBounds(0, 0, iconWidth, iconHeight);
        dragButton.setCompoundDrawables(null, dragDrawable, null, null);
        // 设置旋转按钮图标大小
        Drawable rotateDrawable = getResources().getDrawable(R.drawable.rotate_icon_selector);
        rotateDrawable.setBounds(0, 0, iconWidth, iconHeight);
        rotateButton.setCompoundDrawables(null, rotateDrawable, null, null);

        goBackButton.setOnClickListener(this);
        maskingButton.setOnClickListener(this);
        dragButton.setOnClickListener(this);
        rotateButton.setOnClickListener(this);
        findViewById(R.id.masking_finish).setOnClickListener(this);

        goBackButton.setEnabled(false);
        dragButton.setSelected(true);

        maskingView.setOnPathCountChangeListener(this);
        maskingView.setMaskingMode(false);
    }

    @Override
    public void onPathCountChange() {
        if (maskingView.canGoBack()) {
            goBackButton.setEnabled(true);
        } else {
            goBackButton.setEnabled(false);
        }
    }

    /**
     * 旋转
     */
    private void rotateMaskingView() {
        new AsyncTask<String, Integer, Boolean>() {
            List<Path> tempPath;
            float scale;
            Bitmap tempBitmap;

            @Override
            protected void onPreExecute() {
                isProcessing = true;

                tempPath = maskingView.getAndRemoveAllPath();
                scale = maskingView.getImageViewScale();
                maskingView.resetMatrix();
                progressBar.setVisibility(View.VISIBLE);
                tempBitmap = maskingView.getImageBitmap();
            }

            @Override
            protected Boolean doInBackground(String... params) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                if (tempBitmap != null && !tempBitmap.isRecycled()) {
                    final Bitmap destBitmap = Bitmap.createBitmap(tempBitmap, 0, 0,
                            tempBitmap.getWidth(), tempBitmap.getHeight(), matrix, false);
                    tempBitmap.recycle();
                    tempBitmap = null;
                    if (destBitmap != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                BitmapDrawable drawable = (BitmapDrawable) maskingView.getDrawable();
                                if (drawable != null) {
                                    Bitmap temp = drawable.getBitmap();
                                    if (temp != null) {
                                        temp.recycle();
                                        temp = null;
                                    }
                                }
                                maskingView.setImageBitmap(destBitmap);
                                maskingView.rotatePaths(tempPath, scale);
                            }
                        });
                    }
                } else {
                    Log.d(Consts.LogTag, "Get Bitmap From ImageView Failed or Bitmap is Recycled...");
                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                isProcessing = false;
                progressBar.setVisibility(View.GONE);
            }
        }.execute();
    }

    private void saveBitmapToFile() {
        new AsyncTask<String, Integer, Boolean>() {
            @Override
            protected void onPreExecute() {
                maskingView.resetMatrix();

                progressBar.setVisibility(View.VISIBLE);
                isProcessing = true;
            }

            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    saveToFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                progressBar.setVisibility(View.GONE);
                isProcessing = false;

                Intent intent = new Intent(MaskingActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }.execute();
    }

    private void saveToFile() throws Exception {
        bitmap = maskingView.getViewBitmap();

        if (bitmap == null) {
            Log.d(Consts.LogTag, "Masking GetFile bitmap is null");
        }

        // 生成输出流
        File deleteFile = new File(FileUtils.getFilePathFromUri(this, uri));
        if (deleteFile.exists())
            deleteFile.delete();

        File file = new File(FileUtils.getFileDir() + File.separator + new Date().getTime() + ".png");
        // 文件如果不存在，创建文件
        if (!file.exists())
            file.getParentFile().mkdirs();

        FileOutputStream out = new FileOutputStream(file);
        // 将Bitmap绘制到文件中
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
        // 释放Bitmap资源
        bitmap.recycle();
        // 关闭流
        out.close();
    }
}
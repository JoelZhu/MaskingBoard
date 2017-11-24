package com.joelzhu.maskingboard.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.joelzhu.maskingboard.R;
import com.joelzhu.maskingboard.models.LayoutAttrs;
import com.joelzhu.maskingboard.tasks.PictureRotateAsyncTask;
import com.joelzhu.maskingboard.tasks.PictureSaveAsyncTask;
import com.joelzhu.maskingboard.utils.JZConsts;
import com.joelzhu.maskingboard.utils.JZDisplayUtils;
import com.joelzhu.maskingboard.views.JZMaskingView;

public class MaskingActivity extends BaseActivity implements View.OnClickListener, JZMaskingView
        .OnPathCountChangeListener {
    // 按钮图标长宽(单位：dp)
    private static final int BUTTON_ICON_WIDTH = 39;
    private static final int BUTTON_ICON_HEIGHT = 30;

    private JZMaskingView jzMaskingView;
    private Bitmap bitmap;
    private ProgressBar progressBar;

    private Uri uri;

    public boolean isProcessing = false;

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

        uri = Uri.parse(getIntent().getStringExtra(JZConsts.ExtraPictureUri));
        int rotateDegree = getIntent().getIntExtra(JZConsts.ExtraRotateDegree, 90);

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
            if (bitmapWidth > JZConsts.ORIGIN_MAX || bitmapHeight > JZConsts.ORIGIN_MAX) {
                float scale;

                if (bitmapWidth > bitmapHeight) {
                    scale = (float) JZConsts.ORIGIN_MAX / bitmap.getWidth();
                    bitmapWidth = JZConsts.ORIGIN_MAX;
                    bitmapHeight = (int) (scale * bitmap.getHeight());
                } else {
                    scale = (float) JZConsts.ORIGIN_MAX / bitmap.getHeight();
                    bitmapWidth = (int) (scale * bitmap.getWidth());
                    bitmapHeight = JZConsts.ORIGIN_MAX;
                }

                matrix.setScale(scale, scale);
            }

            destBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
            jzMaskingView.setImageBitmap(destBitmap);
        } else {
            Log.d(JZConsts.LogTag, "Masking OnCreate bitmap is null");
        }

        View maskingHint = findViewById(R.id.masking_maskingHint);
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        maskingHint.measure(w, h);
        int height = maskingHint.getMeasuredHeight();
        jzMaskingView.setOffsetHeight(height);
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
                jzMaskingView.goBack();
                break;

            // 涂鸦
            case R.id.masking_masking:
                jzMaskingView.setMaskingMode(true);
                maskingButton.setSelected(true);
                dragButton.setSelected(false);
                break;

            // 拖拽
            case R.id.masking_drag:
                jzMaskingView.setMaskingMode(false);
                maskingButton.setSelected(false);
                dragButton.setSelected(true);
                break;

            // 旋转
            case R.id.masking_rotate:
                new PictureRotateAsyncTask(this, jzMaskingView, progressBar).execute();
                break;

            // 保存
            case R.id.masking_finish:
                new PictureSaveAsyncTask(this, jzMaskingView, progressBar).execute(uri);
                break;
        }
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        jzMaskingView = (JZMaskingView) findViewById(R.id.masking_maskingView);
        progressBar = (ProgressBar) findViewById(R.id.base_progressBar);

        goBackButton = (Button) findViewById(R.id.masking_goBack);
        maskingButton = (Button) findViewById(R.id.masking_masking);
        dragButton = (Button) findViewById(R.id.masking_drag);
        Button rotateButton = (Button) findViewById(R.id.masking_rotate);

        // 计算按钮图标的长宽
        final int iconWidth = JZDisplayUtils.dp2Px(this, BUTTON_ICON_WIDTH);
        final int iconHeight = JZDisplayUtils.dp2Px(this, BUTTON_ICON_HEIGHT);
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

        jzMaskingView.setOnPathCountChangeListener(this);
        jzMaskingView.setMaskingMode(false);
    }

    @Override
    public void onPathCountChange() {
        if (jzMaskingView.canGoBack()) {
            goBackButton.setEnabled(true);
        } else {
            goBackButton.setEnabled(false);
        }
    }
}
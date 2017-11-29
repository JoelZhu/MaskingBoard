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
import com.joelzhu.maskingboard.utils.JZFileUtils;
import com.joelzhu.maskingboard.views.JZMaskingView;

public class MaskingActivity extends BaseActivity implements View.OnClickListener, JZMaskingView
        .OnPathCountChangeListener {
    // 按钮图标长宽(单位：dp)
    private static final int BUTTON_ICON_WIDTH = 39;
    private static final int BUTTON_ICON_HEIGHT = 30;

    // 控件
    private JZMaskingView jzMaskingView;

    // Bitmap对象
    private Bitmap bitmap;

    // 等待框
    private ProgressBar progressBar;

    // 是否在处理中
    public boolean isProcessing = false;

    // 文件Uri(接收从本应用文件夹中选取到图片)
    private Uri uri;

    // 按钮
    private Button undoButton, maskingButton, dragButton;

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

        // 是否是相机拍摄的照片
        boolean isFromCamera = getIntent().getBooleanExtra(JZConsts.ExtraIsFromCamera, false);
        // 旋转角度
        int rotateDegree = getIntent().getIntExtra(JZConsts.ExtraRotateDegree, 90);

        // 如果是相机拍摄而来，读取全局Bitmap
        if (isFromCamera)
            bitmap = JZFileUtils.tempBitmap;
            // 如果是相册而来，读取文件
        else {
            uri = Uri.parse(getIntent().getStringExtra(JZConsts.ExtraPictureUri));
            bitmap = BitmapFactory.decodeFile(uri.getPath());
        }

        // 加载图片到控件上
        loadPictureToWidget(rotateDegree);

        // 计算出提示框到高度
        View maskingHint = findViewById(R.id.masking_maskingHint);
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        maskingHint.measure(w, h);
        int height = maskingHint.getMeasuredHeight();
        jzMaskingView.setOffsetHeight(height);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 回收Bitmap
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
            case R.id.masking_undo:
                jzMaskingView.undo();
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
            case R.id.masking_done:
                new PictureSaveAsyncTask(this, jzMaskingView, progressBar).execute(uri);
                break;
        }
    }

    @Override
    public void onPathCountChange() {
        if (jzMaskingView.canUndo()) {
            undoButton.setEnabled(true);
        } else {
            undoButton.setEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {
        // 点击回退键，删除全局Bitmap
        JZFileUtils.destroyBitmap();

        super.onBackPressed();
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        jzMaskingView = (JZMaskingView) findViewById(R.id.masking_maskingView);
        progressBar = (ProgressBar) findViewById(R.id.base_progressBar);

        undoButton = (Button) findViewById(R.id.masking_undo);
        maskingButton = (Button) findViewById(R.id.masking_masking);
        dragButton = (Button) findViewById(R.id.masking_drag);
        Button rotateButton = (Button) findViewById(R.id.masking_rotate);

        // 计算按钮图标的长宽
        final int iconWidth = JZDisplayUtils.dp2Px(this, BUTTON_ICON_WIDTH);
        final int iconHeight = JZDisplayUtils.dp2Px(this, BUTTON_ICON_HEIGHT);
        // 设置回退按钮图标大小
        Drawable undoDrawable = getResources().getDrawable(R.drawable.undo_icon_selector);
        undoDrawable.setBounds(0, 0, iconWidth, iconHeight);
        undoButton.setCompoundDrawables(null, undoDrawable, null, null);
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

        undoButton.setOnClickListener(this);
        maskingButton.setOnClickListener(this);
        dragButton.setOnClickListener(this);
        rotateButton.setOnClickListener(this);
        findViewById(R.id.masking_done).setOnClickListener(this);

        undoButton.setEnabled(false);
        dragButton.setSelected(true);

        jzMaskingView.setOnPathCountChangeListener(this);
        jzMaskingView.setMaskingMode(false);
    }

    /**
     * 加载图片到控件上
     *
     * @param rotateDegree 旋转角度
     */
    private void loadPictureToWidget(int rotateDegree) {
        if (bitmap != null) {
            Matrix matrix = new Matrix();
            if (rotateDegree != 0) {
                matrix.setRotate(rotateDegree);
            }

            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            Bitmap destBitmap;

            // 如果图片大于最大尺寸，进行剪裁
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
    }
}
package com.joelzhu.maskingboard.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.joelzhu.maskingboard.R;
import com.joelzhu.maskingboard.utils.Consts;
import com.joelzhu.maskingboard.utils.FileUtils;
import com.joelzhu.maskingboard.views.MaskingView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;

public class MaskingActivity extends Activity implements View.OnClickListener, MaskingView.OnPathCountChangeListener {
    private final static int originMax = 2000;

    private MaskingView maskingView;
    private Bitmap bitmap;
    private Uri uri;
    private ProgressBar progressBar;

    private boolean isProcessing = false;

    private Button clearButton, maskingButton, draftButton, finishButton, rotateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_masking);

        InitWidgetOnTheScreen();

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

        if (bitmap != null)
        {
            Matrix matrix = new Matrix();
            if (rotateDegree != 0)
            {
                matrix.setRotate(rotateDegree);
            }

            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            Bitmap destBitmap;
            if (bitmapWidth > originMax || bitmapHeight > originMax)
            {
                float scale;

                if (bitmapWidth > bitmapHeight)
                {
                    scale = (float)originMax / bitmap.getWidth();
                    bitmapWidth = originMax;
                    bitmapHeight = (int)(scale * bitmap.getHeight());
                }
                else
                {
                    scale = (float)originMax / bitmap.getHeight();
                    bitmapWidth = (int)(scale * bitmap.getWidth());
                    bitmapHeight = originMax;
                }

                matrix.setScale(scale, scale);
                destBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
            else
            {
                destBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
            }

            maskingView.setImageBitmap(destBitmap);
        }
        else
        {
            Log.d(Consts.LogTag, "Masking OnCreate bitmap is null");
        }

        View maskingHint = findViewById(R.id.kenshin_maskingHint);
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        maskingHint.measure(w, h);
        int height = maskingHint.getMeasuredHeight();
        maskingView.setOffsetHeight(height);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (bitmap != null)
        {
            bitmap.recycle();
            bitmap = null;
        }
    }

    @Override
    public void onClick(View view)
    {
        // if we're saving or rotating bitmap, do nothing when user click any button on the screen
        if (isProcessing)
        {
            return;
        }

        switch (view.getId())
        {
            // remove last path
            case R.id.kenshin_clear:
                maskingView.goBack();
                break;

            // select masking mode
            case R.id.kenshin_masking:
                maskingView.setMaskingMode(true);
                maskingButton.setSelected(true);
                draftButton.setSelected(false);
                break;

            // select drafting mode
            case R.id.kenshin_draft:
                maskingView.setMaskingMode(false);
                maskingButton.setSelected(false);
                draftButton.setSelected(true);
                break;

            // rotate view
            case R.id.kenshin_rotate:
                RotateMaskingView();
                break;

            // save bitmap
            case R.id.kenshin_finish:
                SaveBitmapToFile();
                break;
        }
    }

    private void InitWidgetOnTheScreen()
    {
        maskingView = (MaskingView)findViewById(R.id.maskingView);
        progressBar = (ProgressBar)findViewById(R.id.kenshin_progressbar);

        clearButton = (Button)findViewById(R.id.kenshin_clear);
        maskingButton = (Button)findViewById(R.id.kenshin_masking);
        draftButton = (Button)findViewById(R.id.kenshin_draft);
        finishButton = (Button)findViewById(R.id.kenshin_finish);
        rotateButton = (Button)findViewById(R.id.kenshin_rotate);
        clearButton.setOnClickListener(this);
        maskingButton.setOnClickListener(this);
        draftButton.setOnClickListener(this);
        finishButton.setOnClickListener(this);
        rotateButton.setOnClickListener(this);

        clearButton.setEnabled(false);
        draftButton.setSelected(true);

        maskingView.setOnPathCountChangeListener(this);
        maskingView.setMaskingMode(false);
    }

    /// <summary>
    /// run when paths sum changed
    /// </summary>
    public void OnPathCountChange()
    {
        if (maskingView.canGoBack())
        {
            clearButton.setEnabled(true);
        }
        else
        {
            clearButton.setEnabled(false);
        }
    }

    private void RotateMaskingView()
    {
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
                // rotate the bitmap get from imageview
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                if (tempBitmap != null && !tempBitmap.isRecycled())
                {
                    final Bitmap destBitmap = Bitmap.createBitmap(tempBitmap, 0, 0,
                            tempBitmap.getWidth(), tempBitmap.getHeight(), matrix, false);
                    tempBitmap.recycle();
                    tempBitmap = null;
                    if (destBitmap != null)
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                BitmapDrawable drawable = (BitmapDrawable)maskingView.getDrawable();
                                if (drawable != null)
                                {
                                    Bitmap temp = drawable.getBitmap();
                                    if (temp != null)
                                    {
                                        temp.recycle();
                                        temp = null;
                                    }
                                }
                                maskingView.setImageBitmap(destBitmap);
                                maskingView.rotatePaths(tempPath, scale);
                            }
                        });
                    }
                }
                else
                {
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

    private void SaveBitmapToFile()
    {
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
                    SaveToFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                progressBar.setVisibility(View.GONE);
                isProcessing = false;

//                Intent intent = new Intent(this, KenshinCollectionActivity);
//                intent.putExtra(Consts.ExtraPictureUri, uri.toString());
//                startActivity(intent);

                finish();
            }
        }.execute();
    }

    private void SaveToFile() throws Exception
    {
        bitmap = maskingView.getViewBitmap();

        if (bitmap == null)
        {
            Log.d(Consts.LogTag, "Masking GetFile bitmap is null");
        }

        // 生成输出流
        File file = new File(FileUtils.getFileDir() + File.separator + new Date().getTime() + ".png");
        // 文件如果不存在，创建文件
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }

        FileOutputStream out = new FileOutputStream(file);
        // 将Bitmap绘制到文件中
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
        // 释放Bitmap资源
        bitmap.recycle();
        // 关闭流
        out.close();
    }
}
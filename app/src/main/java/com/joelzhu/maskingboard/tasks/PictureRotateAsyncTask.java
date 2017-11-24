package com.joelzhu.maskingboard.tasks;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.joelzhu.maskingboard.activities.MaskingActivity;
import com.joelzhu.maskingboard.utils.JZConsts;
import com.joelzhu.maskingboard.views.JZMaskingView;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 旋转图片异步线程
 *
 * @author JoelZhu
 */
public final class PictureRotateAsyncTask extends AsyncTask<String, Integer, Boolean> {
    private List<Path> tempPath;
    private float scale;
    private Bitmap tempBitmap;

    // 弱引用防止内存泄漏
    private WeakReference<MaskingActivity> activityWeakReference;
    private WeakReference<JZMaskingView> jzMaskingViewWeakReference;
    private WeakReference<ProgressBar> progressBarWeakReference;

    /**
     * 私有无参构造函数
     */
    private PictureRotateAsyncTask() {
    }

    /**
     * 公有构造函数
     *
     * @param activity 宿主Activity
     * @param jzMaskingView 自定义涂鸦控件
     * @param progressBar ProgressBar
     */
    public PictureRotateAsyncTask(MaskingActivity activity, JZMaskingView jzMaskingView, ProgressBar progressBar) {
        activityWeakReference = new WeakReference<>(activity);
        jzMaskingViewWeakReference = new WeakReference<>(jzMaskingView);
        progressBarWeakReference = new WeakReference<>(progressBar);
    }

    @Override
    protected void onPreExecute() {
        activityWeakReference.get().isProcessing = true;

        tempPath = jzMaskingViewWeakReference.get().getAndRemoveAllPath();
        scale = jzMaskingViewWeakReference.get().getImageViewScale();
        jzMaskingViewWeakReference.get().resetMatrix();
        progressBarWeakReference.get().setVisibility(View.VISIBLE);
        tempBitmap = jzMaskingViewWeakReference.get().getImageBitmap();
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        if (tempBitmap != null && !tempBitmap.isRecycled()) {
            final Bitmap destBitmap = Bitmap.createBitmap(tempBitmap, 0, 0,
                    tempBitmap.getWidth(), tempBitmap.getHeight(), matrix, false);
            tempBitmap.recycle();
            tempBitmap = null;
            if (destBitmap != null) {
                activityWeakReference.get().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BitmapDrawable drawable = (BitmapDrawable) jzMaskingViewWeakReference.get().getDrawable();
                        if (drawable != null) {
                            Bitmap temp = drawable.getBitmap();
                            if (temp != null) {
                                temp.recycle();
                                temp = null;
                            }
                        }
                        jzMaskingViewWeakReference.get().setImageBitmap(destBitmap);
                        jzMaskingViewWeakReference.get().rotatePaths(tempPath, scale);
                    }
                });
            }
        } else {
            Log.d(JZConsts.LogTag, "Get Bitmap From ImageView Failed or Bitmap is Recycled...");
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        activityWeakReference.get().isProcessing = false;
        progressBarWeakReference.get().setVisibility(View.GONE);
    }
}
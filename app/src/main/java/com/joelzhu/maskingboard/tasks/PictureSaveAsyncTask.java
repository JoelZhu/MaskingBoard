package com.joelzhu.maskingboard.tasks;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.joelzhu.maskingboard.activities.MainActivity;
import com.joelzhu.maskingboard.activities.MaskingActivity;
import com.joelzhu.maskingboard.utils.JZConsts;
import com.joelzhu.maskingboard.utils.JZFileUtils;
import com.joelzhu.maskingboard.views.JZMaskingView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Date;

/**
 * 保存图片异步线程
 *
 * @author JoelZhu
 */
public final class PictureSaveAsyncTask extends AsyncTask<Uri, Integer, Boolean> {
    // 弱引用防止内存泄漏
    private WeakReference<MaskingActivity> activityWeakReference;
    private WeakReference<JZMaskingView> jzMaskingViewWeakReference;
    private WeakReference<ProgressBar> progressBarWeakReference;

    /**
     * 私有无参构造函数
     */
    private PictureSaveAsyncTask() {
    }

    /**
     * 公有构造函数
     *
     * @param activity      宿主Activity
     * @param jzMaskingView 自定义涂鸦控件
     * @param progressBar   ProgressBar
     */
    public PictureSaveAsyncTask(MaskingActivity activity, JZMaskingView jzMaskingView, ProgressBar progressBar) {
        activityWeakReference = new WeakReference<>(activity);
        jzMaskingViewWeakReference = new WeakReference<>(jzMaskingView);
        progressBarWeakReference = new WeakReference<>(progressBar);
    }

    @Override
    protected void onPreExecute() {
        jzMaskingViewWeakReference.get().resetMatrix();

        progressBarWeakReference.get().setVisibility(View.VISIBLE);
        activityWeakReference.get().isProcessing = true;
    }

    @Override
    protected Boolean doInBackground(Uri... uris) {
        try {
            if (uris == null || uris.length < 1)
                saveToFile(null);
            else
                saveToFile(uris[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        progressBarWeakReference.get().setVisibility(View.GONE);
        activityWeakReference.get().isProcessing = false;

        Intent intent = new Intent(activityWeakReference.get(), MainActivity.class);
        activityWeakReference.get().startActivity(intent);

        JZFileUtils.destroyBitmap();
    }

    /**
     * 保存为图片
     *
     * @param uri 文件Uri
     */
    private void saveToFile(Uri uri) {
        Bitmap bitmap = jzMaskingViewWeakReference.get().getViewBitmap();

        if (bitmap == null) {
            Log.d(JZConsts.LogTag, "Masking GetFile bitmap is null");
            return;
        }

        // 生成输出流
        File file;
        if (uri == null)
            file = new File(JZFileUtils.getFileDir() + File.separator + new Date().getTime() + ".png");
        else {
            file = new File(JZFileUtils.getFilePathFromUri(activityWeakReference.get(), uri));
            file.delete();
        }

        // 文件如果不存在，创建文件
        if (!file.exists())
            file.getParentFile().mkdirs();

        try {
            FileOutputStream out = new FileOutputStream(file);
            // 将Bitmap绘制到文件中
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            // 释放Bitmap资源
            bitmap.recycle();
            // 关闭流
            out.close();
        } catch (IOException e) {
            // do nothing
        }
    }
}
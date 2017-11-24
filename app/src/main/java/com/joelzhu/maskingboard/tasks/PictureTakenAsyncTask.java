package com.joelzhu.maskingboard.tasks;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.joelzhu.maskingboard.activities.MaskingActivity;
import com.joelzhu.maskingboard.utils.JZConsts;
import com.joelzhu.maskingboard.utils.JZFileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.Date;

/**
 * 拍摄照片异步线程
 *
 * @author JoelZhu
 */

public final class PictureTakenAsyncTask extends AsyncTask<Integer, Integer, Uri> {
//public final class PictureTakenAsyncTask extends AsyncTask<Integer, Integer, Boolean> {
    // 图片旋转角度
    private int ori;

    private byte[] data;

    private int textureWidth, textureHeight;

    // 弱引用防止内存泄漏
    private WeakReference<Activity> activityWeakReference;
    private WeakReference<ProgressBar> progressBarWeakReference;

    /**
     * 私有无参构造函数
     */
    private PictureTakenAsyncTask() {}

    /**
     * 公有构造函数
     *
     * @param activity 宿主Activity
     * @param progressBar ProgressBar
     * @param data 相机返回二进制数组
     * @param textureWidth 预览框宽度
     * @param textureHeight 预览框高度
     */
    public PictureTakenAsyncTask(Activity activity, ProgressBar progressBar, byte[] data,
                                 int textureWidth, int textureHeight) {
        this.activityWeakReference = new WeakReference<>(activity);
        this.progressBarWeakReference = new WeakReference<>(progressBar);
        this.data = data;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    protected Uri doInBackground(Integer... params) {
//    protected Boolean doInBackground(Integer... params) {
        ori = params[0];

        Log.d(JZConsts.LogTag, "Start to create file");
        // 生成输出流
        File file = new File(JZFileUtils.getFileDir() + File.separator + new Date().getTime() + ".png");
        // 文件如果不存在，创建文件
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        Log.d(JZConsts.LogTag, "Create file succeeded");

        Log.d(JZConsts.LogTag, "Start to create bitmap");
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        float widthRate = (float) bitmap.getWidth() / textureHeight;
        float heightRate = (float) bitmap.getHeight() / textureWidth;
        float bitmapRate = widthRate < heightRate ? widthRate : heightRate;

        final Bitmap tempBitmap = Bitmap.createBitmap(bitmap,
                (int) ((bitmap.getWidth() - textureHeight * bitmapRate) / 2),
                (int) ((bitmap.getHeight() - textureWidth * bitmapRate) / 2),
                (int) (textureHeight * bitmapRate),
                (int) (textureWidth * bitmapRate));

        // 控制图片的大小
        int destWidth, destHeight;
        if (tempBitmap != null) {
            destWidth = tempBitmap.getWidth();
            destHeight = tempBitmap.getHeight();
            if (destWidth > JZConsts.ORIGIN_MAX || destHeight > JZConsts.ORIGIN_MAX) {
                if (destWidth > destHeight) {
                    destWidth = JZConsts.ORIGIN_MAX;
                    destHeight = (int) (((float) JZConsts.ORIGIN_MAX / tempBitmap.getWidth()) * tempBitmap.getHeight());
                } else {
                    destWidth = (int) (((float) JZConsts.ORIGIN_MAX / tempBitmap.getHeight()) * tempBitmap.getWidth());
                    destHeight = JZConsts.ORIGIN_MAX;
                }
            }
            Log.d(JZConsts.LogTag, "Create bitmap succeeded");

            JZFileUtils.tempBitmap = Bitmap.createScaledBitmap(tempBitmap, destWidth, destHeight, false);

            Log.d(JZConsts.LogTag, "Start to write to file");
            try {
                Bitmap destBitmap = Bitmap.createScaledBitmap(tempBitmap, destWidth, destHeight, false);

                FileOutputStream out = new FileOutputStream(file);
                // 将Bitmap绘制到文件中
                destBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                // 释放Bitmap资源
                destBitmap.recycle();
                // 关闭流
                out.close();
            } catch (Exception e) {
                // do nothing
            }
            Log.d(JZConsts.LogTag, "Write file succeeded");
        }

        bitmap.recycle();
        bitmap = null;

        return Uri.fromFile(file);
//        return true;
    }

    @Override
    protected void onPostExecute(Uri uri) {
        // 跳转涂鸦页面
        Intent intent = new Intent(activityWeakReference.get(), MaskingActivity.class);
        intent.putExtra(JZConsts.ExtraPictureUri, uri.toString());
        intent.putExtra(JZConsts.ExtraRotateDegree, ori);
        activityWeakReference.get().startActivity(intent);

        progressBarWeakReference.get().setVisibility(View.GONE);
    }

//    @Override
//    protected void onPostExecute(Boolean aBoolean) {
//        // 跳转涂鸦页面
//        Intent intent = new Intent(activityWeakReference.get(), MaskingActivity.class);
//        intent.putExtra(JZConsts.ExtraRotateDegree, ori);
//        activityWeakReference.get().startActivity(intent);
//
//        progressBarWeakReference.get().setVisibility(View.GONE);
//    }
}
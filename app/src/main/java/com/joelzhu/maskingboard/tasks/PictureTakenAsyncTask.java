package com.joelzhu.maskingboard.tasks;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.joelzhu.maskingboard.activities.MaskingActivity;
import com.joelzhu.maskingboard.utils.Consts;
import com.joelzhu.maskingboard.utils.FileUtils;

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
    // 图片旋转角度
    private int ori;

    private byte[] data;

    private int textureWidth, textureHeight;

    // 弱引用防止内存泄漏
    private WeakReference<ProgressBar> progressBarWeakReference;
    private WeakReference<Activity> activityWeakReference;

    /**
     * 私有无参构造函数
     */
    private PictureTakenAsyncTask() {}

    /**
     * 公有构造函数
     *
     * @param activity
     * @param progressBar
     * @param data
     * @param textureWidth
     * @param textureHeight
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
        ori = params[0];

        // 生成输出流
        File file = new File(FileUtils.getFileDir() + File.separator + new Date().getTime() + ".png");
        // 文件如果不存在，创建文件
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }

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
            if (destWidth > Consts.ORIGIN_MAX || destHeight > Consts.ORIGIN_MAX) {
                if (destWidth > destHeight) {
                    destWidth = Consts.ORIGIN_MAX;
                    destHeight = (int) (((float) Consts.ORIGIN_MAX / tempBitmap.getWidth()) * tempBitmap.getHeight());
                } else {
                    destWidth = (int) (((float) Consts.ORIGIN_MAX / tempBitmap.getHeight()) * tempBitmap.getWidth());
                    destHeight = Consts.ORIGIN_MAX;
                }
            }

            try {
                Bitmap destBitmap = Bitmap.createScaledBitmap(tempBitmap, destWidth, destHeight, false);

                FileOutputStream out = new FileOutputStream(file);
                // 将Bitmap绘制到文件中
                destBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                // 释放Bitmap资源
                destBitmap.recycle();
                // 关闭流
                out.close();
            } catch (Exception e) {

            }
        }

        bitmap.recycle();
        bitmap = null;

        return Uri.fromFile(file);
    }

    @Override
    protected void onPostExecute(Uri uri) {
        // 跳转涂鸦页面
        Intent intent = new Intent(activityWeakReference.get(), MaskingActivity.class);
        intent.putExtra(Consts.ExtraPictureUri, uri.toString());
        intent.putExtra(Consts.ExtraRotateDegree, ori);
        activityWeakReference.get().startActivity(intent);

        progressBarWeakReference.get().setVisibility(View.GONE);
    }
}
package com.joelzhu.maskingboard.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.joelzhu.maskingboard.R;
import com.joelzhu.maskingboard.models.LayoutAttrs;
import com.joelzhu.maskingboard.utils.Consts;
import com.joelzhu.maskingboard.utils.FileUtils;
import com.joelzhu.maskingboard.views.RoundButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class CameraActivity extends BaseActivity implements Camera.PictureCallback, TextureView.SurfaceTextureListener, SensorEventListener {
    private final static int CAMERA_PERMISSION_REQUEST = 1;
    private final static int ORIGIN_MAX = 2000;

    private RoundButton takePicture;

    private int previewWidth, previewHeight, textureWidth, textureHeight;
    private float rate = 1;
    private int cameraOri;

    private float gravityX, gravityY;

    private SensorManager sensorManager;
    private Camera camera;
    private TextureView textureView;
    private ProgressBar progressBar;

    private boolean isFirst = true;

    @Override
    protected LayoutAttrs setLayoutAttributes() {
        return new LayoutAttrs.Builder()
                .layout(R.layout.activity_camera)
                .title(R.string.title_camera)
                .hasToolbar(true)
                .create();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        takePicture = (RoundButton) findViewById(R.id.camera_takePicture);
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture.setEnabled(false);
                camera.takePicture(null, null, CameraActivity.this);
            }
        });

        textureView = (TextureView) findViewById(R.id.camera_texture);
        textureView.setSurfaceTextureListener(this);

        progressBar = (ProgressBar) findViewById(R.id.base_progressBar);
    }

    @Override
    protected void onResume() {
        super.onResume();

        takePicture.setEnabled(true);

//        StartAutoFocus();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorManager.unregisterListener(this);
    }

    @Override
    public void onPictureTaken(final byte[] data, Camera camera) {
        progressBar.setVisibility(View.VISIBLE);

        if (camera != null) {
            camera.stopPreview();

            int ori = cameraOri;
            if (Math.abs(gravityX) > Math.abs(gravityY)) {
                if (gravityX > 0) {
                    ori -= 90;
                }
                if (gravityX < 0) {
                    ori += 90;
                }
            } else {
                if (gravityY < 0) {
                    ori -= 180;
                }
            }

            // 启动线程保存拍摄图片
            new AsyncTask<Integer, Integer, Uri>() {
                int ori;

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

                    Bitmap tempBitmap = Bitmap.createBitmap(bitmap,
                            (int) ((bitmap.getWidth() - textureHeight * bitmapRate) / 2),
                            (int) ((bitmap.getHeight() - textureWidth * bitmapRate) / 2),
                            (int) (textureHeight * bitmapRate),
                            (int) (textureWidth * bitmapRate));

                    // 控制图片的大小
                    int destWidth, destHeight;
                    if (tempBitmap != null) {
                        destWidth = tempBitmap.getWidth();
                        destHeight = tempBitmap.getHeight();
                        if (destWidth > ORIGIN_MAX || destHeight > ORIGIN_MAX) {
                            if (destWidth > destHeight) {
                                destWidth = ORIGIN_MAX;
                                destHeight = (int) (((float) ORIGIN_MAX / tempBitmap.getWidth()) * tempBitmap.getHeight());
                            } else {
                                destWidth = (int) (((float) ORIGIN_MAX / tempBitmap.getHeight()) * tempBitmap.getWidth());
                                destHeight = ORIGIN_MAX;
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
                    Intent intent = new Intent(CameraActivity.this, MaskingActivity.class);
                    intent.putExtra(Consts.ExtraPictureUri, uri.toString());
                    intent.putExtra(Consts.ExtraRotateDegree, ori);
                    startActivity(intent);

                    progressBar.setVisibility(View.GONE);
                }
            }.execute(ori);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        if (e.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravityX = e.values[0];
            gravityY = e.values[1];
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        initCamera(surface);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }

        return true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // do nothing
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // do nothing
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            //if (!DeviceUtil.IsGrantedCameraPermission(this))
            //{
            //	var intent = new Intent(this, typeof(CaradaWebViewActivity));
            //	StartActivity(intent);
            //	OverridePendingTransition(Resource.Animation.in_right, Resource.Animation.out_left);
            //}
            //else
            //{
            //	Finish();
            //	StartActivity(Intent);
            //	OverridePendingTransition(Resource.Animation.in_right, Resource.Animation.out_left);
            //}
            // TODO never granted permission
        }
    }

    private void initCamera(SurfaceTexture surface) {
        // TODO deal with permission
        //if (!DeviceUtil.IsGrantedCameraPermission(this))
        //{
        //	if (!ActivityCompat.ShouldShowRequestPermissionRationale(this, Manifest.Permission.Camera))
        //	{
        //		ShowPermissionDialog();
        //		return;
        //	}

        //	ActivityCompat.RequestPermissions(this, new string[] { Manifest.Permission.Camera }, CAMERA_PERMISSION_REQUEST);
        //}
        //else
        //{
        if (Camera.getNumberOfCameras() == 0) {
//            System.Diagnostics.Debug.WriteLine("Cemera not supported.");
            return;
        }
        if (camera == null) {
            camera = Camera.open();
            if (camera == null)
                camera = Camera.open(0);
        }

        setCameraDisplayOrientation(0);

        if (isFirst) {
            textureWidth = textureView.getWidth();
            textureHeight = textureView.getHeight();
            isFirst = false;
        }

        Camera.Parameters param = camera.getParameters();
        Camera.Size previewSize = param.getPreviewSize();
        if (previewSize.width >= textureView.getHeight() && previewSize.height >= textureView.getWidth()) {
            previewWidth = previewSize.width;
            previewHeight = previewSize.height;

            textureView.setLayoutParams(new FrameLayout.LayoutParams(previewHeight, previewWidth, Gravity.CENTER));
        } else {
            List<Camera.Size> previewSizes = param.getSupportedPreviewSizes();
            Collections.sort(previewSizes, new Comparator<Camera.Size>() {
                @Override
                public int compare(Camera.Size o1, Camera.Size o2) {
                    return Integer.compare(o1.height * o1.width, o2.height * o2.width);
                }
            });
            Camera.Size maxSize = previewSizes.get(0);

            if (maxSize.width >= textureView.getHeight() && maxSize.height >= textureView.getWidth()) {
                for (Camera.Size size : previewSizes) {
                    if (size.width >= textureView.getHeight() && size.height >= textureView.getWidth()) {
                        previewWidth = size.width;
                        previewHeight = size.height;
                    } else {
                        break;
                    }
                }

                param.setPreviewSize(previewWidth, previewHeight);
                camera.setParameters(param);
                textureView.setLayoutParams(new FrameLayout.LayoutParams(previewHeight, previewWidth, Gravity.CENTER));
            } else {
                previewWidth = maxSize.width;
                previewHeight = maxSize.height;

                float matchestWidth, matchestHeight;

                if ((float) maxSize.height / textureView.getWidth() > (float) maxSize.width / textureView.getHeight()) {
                    matchestHeight = textureView.getHeight();
                    rate = (float) textureView.getHeight() / maxSize.width;
                    matchestWidth = rate * maxSize.height;
                } else {
                    matchestWidth = textureView.getWidth();
                    rate = (float) textureView.getWidth() / maxSize.height;
                    matchestHeight = rate * maxSize.width;
                }

                param.setPreviewSize(previewWidth, previewHeight);
                camera.setParameters(param);
                textureView.setLayoutParams(new FrameLayout.LayoutParams((int) matchestWidth, (int) matchestHeight, Gravity.CENTER));
            }
        }

        List<Camera.Size> pictureSizes = param.getSupportedPictureSizes();
        Collections.sort(pictureSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size o1, Camera.Size o2) {
                return Integer.compare(o1.height * o1.width, o2.height * o2.width);
            }
        });
        param.setPictureSize(pictureSizes.get(pictureSizes.size() - 1).width, pictureSizes.get(pictureSizes.size() - 1).height);
        for (Camera.Size size : pictureSizes) {
            if (size.width > ORIGIN_MAX && size.height > ORIGIN_MAX) {
                param.setPictureSize(size.width, size.height);
                break;
            }
        }
        camera.setParameters(param);

        try {
            setCamFocusMode();
            camera.setPreviewTexture(surface);
            camera.startPreview();
        } catch (IOException ex) {
//            Console.WriteLine(ex.Message);
        }
        //}
    }

    private void setCameraDisplayOrientation(int cameraId) {
        // 获取相机信息对象
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        // ディスプレイの向き取得
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        // プレビューの向き計算
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            cameraOri = (cameraInfo.orientation + degrees) % 360;
            cameraOri = (360 - cameraOri) % 360; // compensate the mirror
        } else { // back-facing
            cameraOri = (cameraInfo.orientation - degrees + 360) % 360;
        }
        // ディスプレイの向き設定
        camera.setDisplayOrientation(cameraOri);
    }

    /// <summary>
    /// プレビューのぼやけを解消する
    /// </summary>
    private void setCamFocusMode() {
        if (null == camera) {
            return;
        }

        // Set Auto focus
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);

        camera.setParameters(parameters);
    }
}
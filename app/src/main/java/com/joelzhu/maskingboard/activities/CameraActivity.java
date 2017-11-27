package com.joelzhu.maskingboard.activities;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.joelzhu.maskingboard.R;
import com.joelzhu.maskingboard.models.LayoutAttrs;
import com.joelzhu.maskingboard.tasks.PictureTakenAsyncTask;
import com.joelzhu.maskingboard.utils.JZConsts;
import com.joelzhu.maskingboard.views.JZRoundButton;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 相机页面
 *
 * @author JoelZhu
 */
public class CameraActivity extends BaseActivity implements Camera.PictureCallback, TextureView.SurfaceTextureListener,
        SensorEventListener {
    // 拍照按钮
    private JZRoundButton takePicture;

    // 相机预览尺寸和控件尺寸的长宽
    private int previewWidth, previewHeight, textureWidth, textureHeight;

    // 相机的角度
    private int cameraOri;

    // 手机X轴和Y轴的重力加速度
    private float gravityX, gravityY;

    // 传感器帮助类
    private SensorManager sensorManager;

    // 相机对象
    private Camera camera;

    // 预览控件
    private TextureView textureView;

    // 等待框
    private ProgressBar progressBar;

    // 是否是从其他Activity回退回来
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

        // 获取拍照按钮控件
        takePicture = (JZRoundButton) findViewById(R.id.camera_takePicture);
        // 添加点击事件
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(JZConsts.LogTag, "Click button succeeded");

                // 注销重力加速度传感器
                sensorManager.unregisterListener(CameraActivity.this);

                takePicture.setEnabled(false);
                camera.takePicture(null, null, CameraActivity.this);
            }
        });

        // 获取预览控件
        textureView = (TextureView) findViewById(R.id.camera_texture);
        textureView.setSurfaceTextureListener(this);

        // 等待框
        progressBar = (ProgressBar) findViewById(R.id.base_progressBar);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 拍照按钮可用
        takePicture.setEnabled(true);

        //        StartAutoFocus();

        // 添加重力加速度传感器
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null)
            // 如果传感器不为空，注册传感器
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 注销重力加速度传感器
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onPictureTaken(final byte[] data, Camera camera) {
        // 显示等待框
        progressBar.setVisibility(View.VISIBLE);

        if (camera != null) {
            // 停止预览
            camera.stopPreview();

            // 计算手机重力加速度，用于判断照片方向
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

            // 处理拍摄的图片
            new PictureTakenAsyncTask(this, progressBar, data, textureWidth, textureHeight).execute(ori);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        if (e.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // 通过回调不断修正传感器的值
            gravityX = e.values[0];
            gravityY = e.values[1];
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        // 初始化相机
        initCamera(surface);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (camera != null) {
            // 销毁相机
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

    /**
     * 初始化相机
     *
     * @param surface
     */
    private void initCamera(SurfaceTexture surface) {
        // 如果可用相机为空，跳过下面操作
        if (Camera.getNumberOfCameras() == 0) {
            // TODO
            return;
        }

        if (camera == null) {
            camera = Camera.open();
            if (camera == null)
                camera = Camera.open(0);
        }

        setCameraDisplayOrientation();

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
                    return Integer.compare(o2.height * o2.width, o1.height * o1.width);
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

                float exceptWidth, exceptHeight, rate;
                if ((float) maxSize.height / textureView.getWidth() > (float) maxSize.width / textureView.getHeight()) {
                    exceptHeight = textureView.getHeight();
                    rate = (float) textureView.getHeight() / maxSize.width;
                    exceptWidth = rate * maxSize.height;
                } else {
                    exceptWidth = textureView.getWidth();
                    rate = (float) textureView.getWidth() / maxSize.height;
                    exceptHeight = rate * maxSize.width;
                }

                param.setPreviewSize(previewWidth, previewHeight);
                camera.setParameters(param);
                textureView.setLayoutParams(new FrameLayout.LayoutParams((int) exceptWidth, (int) exceptHeight,
                        Gravity.CENTER));
            }
        }

        List<Camera.Size> pictureSizes = param.getSupportedPictureSizes();
        Collections.sort(pictureSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size o1, Camera.Size o2) {
                return Integer.compare(o1.height * o1.width, o2.height * o2.width);
            }
        });
        param.setPictureSize(pictureSizes.get(pictureSizes.size() - 1).width, pictureSizes.get(pictureSizes.size() -
                1).height);
        for (Camera.Size size : pictureSizes) {
            if (size.width > JZConsts.ORIGIN_MAX && size.height > JZConsts.ORIGIN_MAX) {
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
    }

    /**
     * 设置相机展示方向
     */
    private void setCameraDisplayOrientation() {
        // 获取相机信息对象
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(0, cameraInfo);

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

        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            cameraOri = (cameraInfo.orientation + degrees) % 360;
            cameraOri = (360 - cameraOri) % 360;
        } else {
            cameraOri = (cameraInfo.orientation - degrees + 360) % 360;
        }

        camera.setDisplayOrientation(cameraOri);
    }

    /**
     * 设置相机自动对焦
     */
    private void setCamFocusMode() {
        if (null == camera) {
            return;
        }

        Camera.Parameters parameters = camera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);

        camera.setParameters(parameters);
    }
}
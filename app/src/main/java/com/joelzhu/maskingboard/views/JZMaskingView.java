package com.joelzhu.maskingboard.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义控件 - 涂鸦板
 *
 * @author JoelZhu
 */
public class JZMaskingView extends AppCompatImageView {
    // 控件边框冗余像素常量
    private static final int OFFSET_PADDING = 100;

    // 涂鸦线数量变化监听器
    private OnPathCountChangeListener listener;

    // 画笔对象
    private Paint paint;
    // 当前涂鸦线对象
    private Path path;
    // 涂鸦线数组
    private List<Path> paths;
    // 涂鸦线颜色数组
    private List<Integer> pathColors;
    // 当前画笔颜色
    private int currentPaintColor;

    // 涂鸦模式
    private static final int MODE_MASKING = 0;
    // 拉拽模式
    private static final int MODE_DRAG = 1;
    // 缩放模式
    private static final int MODE_ZOOM = 2;
    // 控件当前模式
    private int currentMode = MODE_MASKING;

    private PointF startPoint = new PointF();
    private Matrix matrix = new Matrix();
    private Matrix currentMatrix = new Matrix();

    private float startDis;
    private PointF midPoint;

    private PointF offsetPoint = new PointF();
    private float zoomScale = 1;
    private float minScale;

    // 原图长宽
    private int originWidth;
    private int originHeight;
    // 窗口长宽
    private int windowWidth;
    private int windowHeight;
    private int offsetHeight;

    private boolean isMasking = true;
    private boolean isFirst = true;

    /**
     * 构造函数
     *
     * @param context 上下文对象
     */
    public JZMaskingView(Context context) {
        super(context);

        // 初始化控件
        initWidget();
    }

    /**
     * 构造函数
     *
     * @param context 上下文对象
     * @param attrs   属性对象
     */
    public JZMaskingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 初始化控件
        initWidget();
    }

    /**
     * 构造函数
     *
     * @param context      上下文对象
     * @param attrs        属性对象
     * @param defStyleAttr 样式
     */
    public JZMaskingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 初始化控件
        initWidget();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            windowWidth = widthSize;
        } else {
            windowWidth = 100;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            windowHeight = heightSize;
        } else {
            windowHeight = 100;
        }

        setMeasuredDimension(windowWidth, windowHeight);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);

        // 获取Bitmap长宽
        if (bm != null) {
            originWidth = bm.getWidth();
            originHeight = bm.getHeight();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 判断是否时第一次初始化
        if (isFirst) {
            initWidgetPosition();

            minScale = zoomScale;
            isFirst = false;
        }

        // 绘制所有涂鸦线
        canvas.concat(matrix);

        final int size = paths.size();
        for (int i = 0; i < size; i++) {
            // 设置画笔颜色
            paint.setColor(pathColors.get(i));
            // 绘制涂鸦线
            canvas.drawPath(paths.get(i), paint);
        }
        // 当前Path不为空，绘制该Path
        if (!path.isEmpty()) {
            paint.setColor(currentPaintColor);
            canvas.drawPath(path, paint);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // 当前处于涂鸦模式中
        if (isMasking) {
            switch (e.getAction()) {
                // 按下事件
                case MotionEvent.ACTION_DOWN:
                    // 绘制涂鸦线起点
                    path.moveTo((e.getX() - offsetPoint.x) / zoomScale, (e.getY() - offsetPoint.y) / zoomScale);
                    invalidate();
                    return true;

                // 移动事件
                case MotionEvent.ACTION_MOVE:
                    // 绘制涂鸦线中折线
                    path.lineTo((e.getX() - offsetPoint.x) / zoomScale, (e.getY() - offsetPoint.y) / zoomScale);
                    invalidate();
                    return true;

                // 松开事件
                case MotionEvent.ACTION_UP:
                    // 绘制涂鸦线终点
                    path.lineTo((e.getX() - offsetPoint.x) / zoomScale, (e.getY() - offsetPoint.y) / zoomScale);
                    paths.add(path);
                    path = new Path();
                    pathColors.add(currentPaintColor);
                    invalidate();
                    break;
            }
            if (listener != null) {
                // 通知监听者涂鸦线数量变化
                listener.onPathCountChange();
            }

            return super.onTouchEvent(e);
        }
        // 当前处于拖拽/缩放模式
        else {
            switch (e.getAction() & MotionEvent.ACTION_MASK) {
                // 单指按下事件
                case MotionEvent.ACTION_DOWN:
                    // 设置拖拽模式，记录拖拽起点
                    currentMode = MODE_DRAG;
                    currentMatrix.set(getImageMatrix());
                    startPoint.set(e.getX(), e.getY());
                    break;

                // 移动事件
                case MotionEvent.ACTION_MOVE:
                    // 拖拽模式
                    if (currentMode == MODE_DRAG) {
                        // 当前缩放倍数大于等于最小缩放倍数
                        if (zoomScale >= minScale) {
                            float dx = e.getX() - startPoint.x;
                            float dy = e.getY() - startPoint.y;
                            matrix.set(currentMatrix);
                            matrix.postTranslate(dx, dy);
                        }
                    }
                    // 缩放模式
                    else if (currentMode == MODE_ZOOM) {
                        // 计算两指间距离
                        float endDis = getDistance(e);
                        // 距离大于10像素
                        if (endDis > 10f) {
                            // 计算缩放倍数
                            float scaleTemp = endDis / startDis;
                            matrix.set(currentMatrix);
                            matrix.postScale(scaleTemp, scaleTemp, midPoint.x, midPoint.y);
                        }
                    }
                    break;

                // 松开事件
                case MotionEvent.ACTION_UP:
                    // 当前缩放倍数小于最小缩放倍数
                    if (zoomScale < minScale) {
                        // 初始化图像位置
                        initWidgetPosition();
                    }
                    // 缩放倍数大于1（即当前图片大小大于原图大小）
                    if (zoomScale > 1) {
                        // 纠正图片缩放倍数
                        correctWidgetScale();
                    }
                    // 拖拽模式
                    else if (currentMode == MODE_DRAG) {
                        // 计算拖拽移动距离
                        float dx = e.getX() - startPoint.x;
                        float dy = e.getY() - startPoint.y;
                        correctWidgetPosition(dx, dy);
                        currentMode = MODE_MASKING;
                    }
                    break;

                // 单指按下事件
                case MotionEvent.ACTION_POINTER_DOWN:
                    // 纠正图片位置
                    correctWidgetPosition(e.getX() - startPoint.x, e.getY() - startPoint.y);

                    currentMode = MODE_ZOOM;
                    startDis = getDistance(e);
                    // 如果两指间距离大于10像素
                    if (startDis > 10f) {
                        midPoint = getMidPoint(e);
                        currentMatrix.set(getImageMatrix());
                    }
                    break;

                // 单指松开事件
                case MotionEvent.ACTION_POINTER_UP:
                    // 缩放模式
                    if (currentMode == MODE_ZOOM) {
                        // 获取此时两指间的距离
                        float endDis = getDistance(e);
                        // 计算用户缩放倍数
                        float scaleTemp = endDis / startDis;
                        offsetPoint.set((midPoint.x - offsetPoint.x) * (1 - scaleTemp) + offsetPoint.x,
                                (midPoint.y - offsetPoint.y) * (1 - scaleTemp) + offsetPoint.y);
                        zoomScale = zoomScale * scaleTemp;
                        currentMode = MODE_MASKING;
                    }
                    break;
            }

            setImageMatrix(matrix);
            return true;
        }
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        setWillNotDraw(false);

        paint = new Paint();
        path = new Path();
        paths = new ArrayList<>();
        pathColors = new ArrayList<>();
        currentPaintColor = Color.BLACK;

        // 设置画笔对象
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(50);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);

        setScaleType(ScaleType.MATRIX);
        // 开启软件绘制，硬件绘制在某些机子上可能会有问题
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    /**
     * 撤销（如果可以的话）
     */
    public void undo() {
        path.reset();
        if (canUndo()) {
            paths.remove(paths.size() - 1);
            pathColors.remove(pathColors.size() - 1);
            invalidate();

            if (listener != null) {
                // 涂鸦线数量发生变更
                listener.onPathCountChange();
            }
        }
    }

    /**
     * 判断是否可以撤销
     *
     * @return true - 可以回退; false - 不能回退
     */
    public boolean canUndo() {
        return paths.size() > 0;
    }

    /**
     * 设置是否是涂鸦模式
     *
     * @param isMasking 是否是涂鸦模式
     */
    public void setMaskingMode(boolean isMasking) {
        // 当前缩放倍数大于1或者小于最小值
        if (zoomScale < minScale || zoomScale > 1) {
            // 初始化图片位置
            initWidgetPosition();
        }
        this.isMasking = isMasking;
    }

    /**
     * 初始化图片位置
     */
    private void initWidgetPosition() {
        // 计算出x和y方向上刚好缩放到控件大小的最适放大倍数
        float scaleX = (float) windowWidth / originWidth;
        float scaleY = (float) windowHeight / originHeight;

        // x和y方向上放大倍数均大于1（即图片实际长宽均小于控件大小）
        if (scaleX >= 1 && scaleY >= 1) {
            // 设置当前缩放倍数为1
            zoomScale = 1;
            matrix.reset();

            // 无需缩放，将图片至于控件中间
            float offsetX = (windowWidth - originWidth * zoomScale) / 2;
            float offsetY = (windowHeight - originHeight * zoomScale) / 2;
            offsetPoint.set(offsetX, offsetY);
            matrix.postScale(zoomScale, zoomScale, 0, 0);
            matrix.postTranslate(offsetX, offsetY);
            setImageMatrix(matrix);
        }
        // 如果在x方向上所需的放大倍数比y方向上的小（即x方向上的缩小倍数比y方向上的大）
        else if (scaleX < scaleY) {
            // 将x方向上的缩放倍数作为当前缩放倍数
            zoomScale = scaleX;
            matrix.reset();

            // 缩放之后，将图片至于控件中间
            float offsetY = (windowHeight - originHeight * zoomScale) / 2;
            offsetPoint.set(0, offsetY);
            matrix.postScale(scaleX, scaleX, 0, 0);
            matrix.postTranslate(0, offsetY);
            setImageMatrix(matrix);
        }
        // 如果在x方向上所需的放大倍数比y方向上的大（即x方向上的缩小倍数比y方向上的小）
        else {
            // 将y方向上的缩放倍数作为当前缩放倍数
            zoomScale = scaleY;
            matrix.reset();

            // 缩放之后，将图片至于控件中间
            float offsetX = (windowWidth - originWidth * zoomScale) / 2;
            offsetPoint.set(offsetX, 0);
            matrix.postScale(scaleY, scaleY, 0, 0);
            matrix.postTranslate(offsetX, 0);
            setImageMatrix(matrix);
        }
    }

    /**
     * 纠正控件位置
     *
     * @param movingX x方向上的移动距离
     * @param movingY y方向上的移动距离
     */
    private void correctWidgetPosition(float movingX, float movingY) {
        float offsetX;
        float offsetY;
        boolean isNeedReset = false;

        // 如果当前宽大于控件宽
        if (originWidth * zoomScale > windowWidth - OFFSET_PADDING) {
            // 移动之后，左侧边距大于边框冗余值
            if (offsetPoint.x + movingX > OFFSET_PADDING) {
                // 强制设置左边边距为边框冗余值
                offsetX = OFFSET_PADDING;
                // 需要纠正图片位置
                isNeedReset = true;
            }
            // 移动之后，右侧边距大于边框冗余值
            else if (offsetPoint.x + movingX < -originWidth * zoomScale + windowWidth - OFFSET_PADDING) {
                // 强制设置右边边距为边框冗余值
                offsetX = -originWidth * zoomScale + windowWidth - OFFSET_PADDING;
                // 需要纠正图片位置
                isNeedReset = true;
            }
            // 移动之后，左右均没有超过边框冗余值
            else {
                offsetX = movingX + offsetPoint.x;
            }
        }
        // 当前宽不大于控件宽
        else {
            // 需要纠正图片位置，直接将图片宽至于控件中间
            offsetX = (windowWidth - originWidth * zoomScale) / 2;
            isNeedReset = true;
        }

        // 如果当前高大于控件高
        if (originHeight * zoomScale > windowHeight - OFFSET_PADDING) {
            // 移动之后，上侧边距大于边框冗余值
            if (offsetPoint.y + movingY > OFFSET_PADDING + offsetHeight) {
                // 强制设置上边边距为边框冗余值
                offsetY = OFFSET_PADDING + offsetHeight;
                // 需要纠正图片位置
                isNeedReset = true;
            }
            // 移动之后，下侧边距大于边框冗余值
            else if (offsetPoint.y + movingY < -originHeight * zoomScale + windowHeight - OFFSET_PADDING) {
                // 强制设置下边边距为边框冗余值
                offsetY = -originHeight * zoomScale + windowHeight - OFFSET_PADDING;
                // 需要纠正图片位置
                isNeedReset = true;
            }
            // 移动之后，上下均没有超过边框冗余值
            else {
                offsetY = movingY + offsetPoint.y;
            }
        }
        // 当前高不大于控件高
        else {
            // 需要纠正图片位置，直接将图片高至于控件中间
            offsetY = (windowHeight - originHeight * zoomScale) / 2;
            isNeedReset = true;
        }

        // 如果需要进行图片位置纠正
        if (isNeedReset) {
            matrix.reset();
            matrix.postScale(zoomScale, zoomScale);
            matrix.postTranslate(offsetX, offsetY);
            setImageMatrix(matrix);
        }

        offsetPoint.set(offsetX, offsetY);
    }

    /**
     * 纠正图片缩放倍数
     */
    private void correctWidgetScale() {
        matrix.postScale(1 / zoomScale, 1 / zoomScale, midPoint.x, midPoint.y);
        offsetPoint.set((midPoint.x - offsetPoint.x) * (1 - 1 / zoomScale) + offsetPoint.x,
                (midPoint.y - offsetPoint.y) * (1 - 1 / zoomScale) + offsetPoint.y);
        zoomScale = 1;
        setImageMatrix(matrix);
    }

    /**
     * 获取两指间的距离
     *
     * @param e 事件对象
     * @return 两指间的距离
     */
    private float getDistance(MotionEvent e) {
        float dx = e.getX(1) - e.getX(0);
        float dy = e.getY(1) - e.getY(0);

        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * 获取两指间的中间点
     *
     * @param e 事件对象
     * @return 两指间的中间点
     */
    private PointF getMidPoint(MotionEvent e) {
        float midX = (e.getX(1) + e.getX(0)) / 2;
        float midY = (e.getY(1) + e.getY(0)) / 2;
        return new PointF(midX, midY);
    }

    /**
     * 设置高度冗余值
     *
     * @param offsetHeight 高度冗余值
     */
    public void setOffsetHeight(int offsetHeight) {
        this.offsetHeight = offsetHeight;
    }

    /**
     * 重置图片
     */
    public void resetMatrix() {
        matrix.reset();
        setImageMatrix(matrix);
    }

    /**
     * 获取当前涂鸦线数组，并且清空控件中的数组
     *
     * @return 当前涂鸦线数组
     */
    public List<Path> getAndRemoveAllPath() {
        List<Path> tempPath = new ArrayList<>(paths);
        paths.clear();
        return tempPath;
    }

    /**
     * 获取当前缩放倍数
     *
     * @return 当前缩放倍数
     */
    public float getImageViewScale() {
        return zoomScale;
    }

    /**
     * 顺时针旋转涂鸦线90度
     *
     * @param paths 原涂鸦线
     * @param scale 原缩放倍数
     */
    public void rotatePaths(List<Path> paths, float scale) {
        isFirst = true;

        Matrix rotateMatrix = new Matrix();
        // 此时高度和宽度需要调换位置
        rotateMatrix.postTranslate(-originHeight / 2, -originWidth / 2);
        rotateMatrix.postScale(1 / scale, 1 / scale);
        rotateMatrix.postRotate(90);
        rotateMatrix.postScale(scale, scale);
        rotateMatrix.postTranslate(originWidth / 2, originHeight / 2);
        for (Path tempPath : paths) {
            tempPath.transform(rotateMatrix);
        }

        this.paths.addAll(paths);
    }

    /**
     * 获取图片Bitmap对象（不包含任何涂鸦线）
     *
     * @return Bitmap对象
     */
    public Bitmap getImageBitmap() {
        BitmapDrawable drawable = (BitmapDrawable) getDrawable();
        if (drawable != null) {
            Bitmap temp = drawable.getBitmap();
            if (temp != null) {
                return temp;
            }
        }
        return null;
    }

    /**
     * 获取图片Bitmap对象（包含涂鸦线）
     *
     * @return Bitmap对象
     */
    public Bitmap getViewBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(originWidth, originHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return bitmap;
    }

    /**
     * 设置画笔颜色
     *
     * @param red   R
     * @param green G
     * @param blue  B
     */
    public void setPaintColor(int red, int green, int blue) {
        this.currentPaintColor = Color.rgb(red, green, blue);
    }

    /**
     * 设置涂鸦线数量变化监听器
     *
     * @param listener 涂鸦线数量变化监听器
     */
    public void setOnPathCountChangeListener(OnPathCountChangeListener listener) {
        this.listener = listener;
    }

    /**
     * 涂鸦线数量变化监听器
     */
    public interface OnPathCountChangeListener {
        void onPathCountChange();
    }
}
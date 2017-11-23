package com.joelzhu.maskingboard.views;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.joelzhu.maskingboard.R;
import com.joelzhu.maskingboard.utils.DisplayUtils;

public class JZAddButton extends View {
    // 是否需要展开按钮
    private WidgetState widgetState = WidgetState.Origin;

    private OnButtonClickListener listener;

    // 按钮区域
    private Region addArea, cameraArea, galleryArea;

    // 控件展开值
    private float expendingValue = 1;

    // 控件长宽
    private int widgetWidth, widgetHeight;

    // 半径
    private float radius;

    private Paint paint;

    /**
     * 控件状态枚举类
     */
    private enum WidgetState {
        // 原始
        Origin,
        // 展开中
        Expending,
        // 展开完成
        Expended
    }

    public JZAddButton(Context context) {
        super(context);

        initWidget();
    }

    public JZAddButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initWidget();
    }

    public JZAddButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initWidget();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            widgetWidth = widthSize;
        } else {
            widgetWidth = DisplayUtils.dp2Px(getContext(), 70);
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            widgetHeight = heightSize;
        } else {
            widgetHeight = DisplayUtils.dp2Px(getContext(), 70);
        }

        if (widgetWidth <= widgetHeight) {
            radius = (float) widgetWidth / 8f;
        } else {
            radius = (float) widgetHeight / 8f;
        }

        // 初始化三个按钮区域
        initRegions();

        setMeasuredDimension(widgetWidth, widgetHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch (widgetState) {
            case Origin:
                drawOriginWidget(canvas);
                break;

            case Expending:
                drawExpendingWidget(canvas);
                break;

            case Expended:
                drawExpendedWidget(canvas);
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            switch (widgetState) {
                case Origin:
                    if (addArea.contains((int) event.getX(), (int) event.getY())) {
                        // 设置为展开中状态
                        widgetState = WidgetState.Expending;
                        // 启动展开动画
                        startExpendingAnimation();
                    }
                    return true;

                case Expending:
                    return true;

                case Expended:
                    // 点击两个按钮
                    if (listener != null) {
                        // 点击相机按钮
                        if (cameraArea.contains((int) event.getX(), (int) event.getY())) {
                            listener.onCameraClick();
                        }
                        // 点击相册按钮
                        else if (galleryArea.contains((int) event.getX(), (int) event.getY())) {
                            listener.onGalleryClick();
                        }
                    }

                    // 设置为展开中状态
                    widgetState = WidgetState.Expending;
                    // 启动收缩动画
                    startContractingAnimation();

                    return true;
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        paint = new Paint();
    }

    /**
     * 初始化三个按钮区域
     */
    private void initRegions() {
        if (addArea == null) {
            addArea = new Region();
            final Path path = new Path();
            path.addCircle((float) widgetWidth / 2f, (float) widgetHeight / 2f, radius, Path.Direction.CCW);
            addArea.setPath(path, new Region(0, 0, widgetWidth, widgetHeight));
        }

        if (cameraArea == null) {
            cameraArea = new Region();
            final Path path = new Path();
            path.addCircle((float) widgetWidth / 2f - radius * 2, (float) widgetHeight / 2f, radius, Path.Direction.CCW);
            cameraArea.setPath(path, new Region(0, 0, widgetWidth, widgetHeight));
        }

        if (galleryArea == null) {
            galleryArea = new Region();
            final Path path = new Path();
            path.addCircle((float) widgetWidth / 2f + radius * 2, (float) widgetHeight / 2f, radius, Path.Direction.CCW);
            galleryArea.setPath(path, new Region(0, 0, widgetWidth, widgetHeight));
        }
    }

    /**
     * 绘制原始状态的控件
     *
     * @param canvas
     */
    private void drawOriginWidget(Canvas canvas) {
        paint.reset();
        paint.setColor(getResources().getColor(R.color.colorPrimary));
        paint.setAntiAlias(true);

        canvas.drawCircle((float) widgetWidth / 2f, (float) widgetHeight / 2f, radius, paint);
    }

    /**
     * 绘制展开中状态的控件
     *
     * @param canvas
     */
    private void drawExpendingWidget(Canvas canvas) {
        // 绘制添加按钮
        paint.reset();
        paint.setColor(getResources().getColor(R.color.colorPrimary));
        paint.setAntiAlias(true);
        paint.setAlpha((int) (255 * expendingValue));

        canvas.drawCircle((float) widgetWidth / 2f, (float) widgetHeight / 2f,
                radius * expendingValue, paint);

        // 绘制拍照和图库按钮
        paint.reset();
        paint.setColor(getResources().getColor(R.color.colorPrimary));
        paint.setAntiAlias(true);
        paint.setAlpha((int) (255 * (1 - expendingValue)));

        canvas.drawCircle((float) widgetWidth / 2f - radius * 2 * (1 - expendingValue),
                (float) widgetHeight / 2f, radius, paint);
        canvas.drawCircle((float) widgetWidth / 2f + radius * 2 * (1 - expendingValue),
                (float) widgetHeight / 2f, radius, paint);
    }

    /**
     * 绘制展开完成状态的控件
     *
     * @param canvas
     */
    private void drawExpendedWidget(Canvas canvas) {
        paint.reset();
        paint.setColor(getResources().getColor(R.color.colorPrimary));
        paint.setAntiAlias(true);

        canvas.drawCircle((float) widgetWidth / 2f - radius * 2, (float) widgetHeight / 2f, radius, paint);
        canvas.drawCircle((float) widgetWidth / 2f + radius * 2, (float) widgetHeight / 2f, radius, paint);
    }

    /**
     * 启动展开动画
     */
    private void startExpendingAnimation() {
        final ValueAnimator animator = ValueAnimator.ofFloat(1, 0);
        animator.setDuration(750);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                expendingValue = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                widgetState = WidgetState.Expended;
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
    }

    /**
     * 启动收缩动画
     */
    private void startContractingAnimation() {
        final ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(750);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                expendingValue = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                widgetState = WidgetState.Origin;
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
    }

    public interface OnButtonClickListener {
        void onCameraClick();

        void onGalleryClick();
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.listener = listener;
    }
}
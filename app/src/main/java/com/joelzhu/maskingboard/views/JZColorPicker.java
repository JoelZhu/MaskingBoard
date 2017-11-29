package com.joelzhu.maskingboard.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JZColorPicker extends View {
    // 颜色变化监听器
    private OnColorChangeListener listener;

    // Paint
    private Paint paint;
    // 线性渐变
    private LinearGradient lGradient;

    // 单位颜色高度
    private float unitWidth;

    // 颜色数组
    private final int[] colors = new int[]{
            Color.rgb(255, 255, 255),
            Color.rgb(0, 255, 255),
            Color.rgb(0, 255, 0),
            Color.rgb(255, 255, 0),
            Color.rgb(255, 0, 0),
            Color.rgb(255, 0, 255),
            Color.rgb(0, 0, 255),
            Color.rgb(0, 0, 0)
    };

    // 控件长宽
    private int widgetWidth;
    private int widgetHeight;

    public JZColorPicker(Context context) {
        super(context);

        initWidget();
    }

    public JZColorPicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initWidget();
    }

    public JZColorPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initWidget();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            widgetWidth = widthSize;
        } else {
            widgetWidth = 100;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            widgetHeight = heightSize;
        } else {
            widgetHeight = 100;
        }

        setMeasuredDimension(widgetWidth, widgetHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (lGradient == null) {
            if (unitWidth == 0 && widgetWidth != 0)
                unitWidth = (float) widgetHeight / 7f;

            lGradient = new LinearGradient(0, 0, 0, widgetHeight, colors, null, Shader.TileMode.CLAMP);
        }
        paint.setShader(lGradient);
        canvas.drawRect(0, 0, widgetWidth, widgetHeight, paint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (listener != null) {
            final int[] colors = caculateColor(event);

            listener.onColorChange(colors[0], colors[1], colors[2]);
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void initWidget() {
        paint = new Paint();
    }

    /**
     * 根据事件计算颜色
     *
     * @param event 事件
     * @return 颜色
     */
    private int[] caculateColor(MotionEvent event) {
        int level = (int) (event.getY() / unitWidth);

        int red = 0, green = 0, blue = 0;
        float deltaWidth = event.getY() - unitWidth * level;

        switch (level) {
            case 0:
                red = 255 - (int) (deltaWidth / unitWidth * 255);
                green = blue = 255;
                break;

            case 1:
                red = 0;
                green = 255;
                blue = 255 - (int) (deltaWidth / unitWidth * 255);
                break;

            case 2:
                red = (int) (deltaWidth / unitWidth * 255);
                green = 255;
                blue = 0;
                break;

            case 3:
                red = 255;
                green = 255 - (int) (deltaWidth / unitWidth * 255);
                blue = 0;
                break;

            case 4:
                red = 255;
                green = 0;
                blue = (int) (deltaWidth / unitWidth * 255);
                break;

            case 5:
                red = 255 - (int) (deltaWidth / unitWidth * 255);
                green = 0;
                blue = 255;
                break;

            case 6:
                red = 0;
                green = 0;
                blue = 255 - (int) (deltaWidth / unitWidth * 255);
                break;
        }

        // 修正颜色值
        if (red > 255)
            red = 255;
        else if (red < 0)
            red = 0;

        if (green > 255)
            green = 255;
        else if (green < 0)
            green = 0;

        if (blue > 255)
            blue = 255;
        else if (blue < 0)
            blue = 0;

        return new int[]{red, green, blue};
    }

    public interface OnColorChangeListener {
        void onColorChange(int red, int green, int blue);
    }

    public void setOnColorChangeListener(OnColorChangeListener listener) {
        this.listener = listener;
    }
}
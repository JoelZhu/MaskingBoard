package com.joelzhu.maskingboard.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.joelzhu.maskingboard.utils.DisplayUtils;

/**
 * 自定义控件 - 圆形按钮
 *
 * @author JoelZhu
 */
public class RoundButton extends View {
    // 画笔对象
    private Paint paint;

    // 半径
    private int diameter;

    /**
     * 构造函数
     *
     * @param context 上下文对象
     */
    public RoundButton(Context context) {
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
    public RoundButton(Context context, AttributeSet attrs) {
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
    public RoundButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 初始化控件
        initWidget();
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 绘制按钮
        drawInitButton(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = DisplayUtils.dp2Px(getContext(), 70);
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = DisplayUtils.dp2Px(getContext(), 70);
        }

        if (width <= height) {
            diameter = width;
        } else {
            diameter = height;
        }

        setMeasuredDimension(diameter, diameter);
    }

    /**
     * 绘制按钮
     *
     * @param canvas Canvas对象
     */
    private void drawInitButton(Canvas canvas) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(Color.argb(255, 128, 128, 128));
        canvas.drawCircle(diameter / 2, diameter / 2, diameter / 2, paint);

        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(Color.argb(255, 255, 255, 255));
        canvas.drawCircle(diameter / 2, diameter / 2, diameter / 2 * 6 / 7, paint);

        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(Color.argb(255, 128, 128, 128));
        canvas.drawCircle(diameter / 2, diameter / 2, diameter / 2 * 6 / 7 - DisplayUtils.dp2Px(getContext(), 3), paint);
    }
}
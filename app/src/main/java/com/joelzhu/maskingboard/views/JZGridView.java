package com.joelzhu.maskingboard.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * 自定义控件 - 嵌套用GridView
 *
 * @author JoelZhu
 */
public class JZGridView extends GridView {
    public JZGridView(Context context) {
        super(context);
    }

    public JZGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public JZGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
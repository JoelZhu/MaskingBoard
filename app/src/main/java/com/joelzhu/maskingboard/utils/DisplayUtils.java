package com.joelzhu.maskingboard.utils;

import android.content.Context;

/**
 * 单位计算工具类
 *
 * @author JoelZhu
 */
public final class DisplayUtils {
    public static int dp2Px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int sp2Px(Context context, float spValue) {
        float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * scale + 0.5f);
    }
}
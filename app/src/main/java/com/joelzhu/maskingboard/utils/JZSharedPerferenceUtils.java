package com.joelzhu.maskingboard.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class JZSharedPerferenceUtils {
    private static final String Name_AppFirstLaunch = "Name_AppFirstLaunch";
    private static final String Key_AppFirstLaunch = "Key_AppFirstLaunch";

    /**
     * 保存我的账户ID
     *
     * @param context Context对象
     */
    public static void setAppFirstLaunch(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Name_AppFirstLaunch,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Key_AppFirstLaunch, false);
        editor.apply();
    }

    /**
     * 获取我的账户ID
     *
     * @param context Context对象
     * @return 账户ID
     */
    public static boolean getAppFirstLaunch(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Name_AppFirstLaunch,
                Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(Key_AppFirstLaunch, true);
    }
}
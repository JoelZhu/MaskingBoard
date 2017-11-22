package com.joelzhu.maskingboard.activities;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

import com.joelzhu.maskingboard.R;
import com.joelzhu.maskingboard.models.LayoutAttrs;

/**
 * Activity父类
 *
 * @author JoelZhu
 */
public abstract class BaseActivity extends Activity {
    // 设置页面属性
    protected abstract LayoutAttrs setLayoutAttributes();

    // Toolbar
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sdk >= 21，设置页面跳转样式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setEnterTransition(new Slide());
            getWindow().setExitTransition(new Slide());
        }

        setContentView(R.layout.activity_base);

        // 页面初始化
        initLayout();
    }

    /**
     * 页面初始化
     */
    private void initLayout() {
        // 获取页面属性对象
        LayoutAttrs lAttrs = setLayoutAttributes();

        // 设置页面Layout
        if (lAttrs.getLayoutId() != 0) {
            ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(
                    lAttrs.getLayoutId(), (FrameLayout) findViewById(R.id.base_content));
        } else {
            // 没有Layout页面
            toolbar.setVisibility(View.GONE);
            // 停止后续处理
            return;
        }

        // 设置页面Toolbar
        if (lAttrs.isHasToolbar()) {
            // 获取Toolbar对象
            toolbar = (Toolbar) findViewById(R.id.base_toolbar);

            // 设置页面标题
            if (lAttrs.getTitleId() != 0) {
                toolbar.setTitle(lAttrs.getTitleId());
            } else {
                // 默认标题
                toolbar.setTitle(R.string.app_name);
            }
        } else {
            // 隐藏Toolbar
            findViewById(R.id.base_toolbar).setVisibility(View.GONE);
        }
    }
}
package com.joelzhu.maskingboard.models;

/**
 * 页面配置信息类
 *
 * @author JoelZhu
 */
public class LayoutAttrs {
    // 是否含有标题栏
    private boolean hasToolbar;

    // 布局文件Id
    private int layoutId;

    // 页面标题Id
    private int titleId;

    /**
     * 私有无参构造函数
     */
    private LayoutAttrs() {
    }

    public boolean isHasToolbar() {
        return hasToolbar;
    }

    public int getLayoutId() {
        return layoutId;
    }

    public int getTitleId() {
        return titleId;
    }

    public static class Builder {
        private LayoutAttrs attrs = new LayoutAttrs();

        public Builder layout(int layoutId) {
            attrs.layoutId = layoutId;
            return this;
        }

        public Builder title(int titleId) {
            attrs.titleId = titleId;
            return this;
        }

        public Builder hasToolbar(boolean hasToolBar) {
            attrs.hasToolbar = hasToolBar;
            return this;
        }

        public LayoutAttrs create() {
            return attrs;
        }
    }
}
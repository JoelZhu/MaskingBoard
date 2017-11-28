package com.joelzhu.maskingboard.common;

import android.os.Build;

import com.joelzhu.maskingboard.activities.PermissionBaseActivity;
import com.joelzhu.maskingboard.activities.PermissionBaseActivity.OnPermissionsGrantedAlready;
import com.joelzhu.maskingboard.activities.PermissionBaseActivity.OnUserAllowed;
import com.joelzhu.maskingboard.activities.PermissionBaseActivity.OnUserDenied;
import com.joelzhu.maskingboard.activities.PermissionBaseActivity.OnUserSetNeverAsk;
import com.joelzhu.maskingboard.utils.JZDeviceUtils;

import java.lang.ref.WeakReference;

/**
 * 权限申请帮助类
 */
public final class JZPermissionHelper {
    private WeakReference<PermissionBaseActivity> aWR;

    public int requestCode = 101;

    public OnUserAllowed onUserAllowed;
    public OnUserDenied onUserDenied;
    public OnUserSetNeverAsk onUserSetNeverAsk;
    private OnPermissionsGrantedAlready onPermissionsGrantedAlready;

    /**
     * 私有化构造函数
     */
    private JZPermissionHelper() {

    }

    /**
     * 申请权限
     */
    public void requestPermission(String[] permissions) {
        // 系统版本大于等于6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查尚未获取到到权限
            final String[] requirePermissions = JZDeviceUtils.checkPermissions(aWR.get(), permissions);

            // 没有未获取到的权限
            if (requirePermissions.length == 0 && onPermissionsGrantedAlready != null)
                // 已经拥有所有权限回调
                onPermissionsGrantedAlready.onPermissionsGrantedAlready();
            else
                // 申请未拥有权限
                aWR.get().requestPermissions(requirePermissions, requestCode);
        }
        // 系统版本小于6.0
        else if (onPermissionsGrantedAlready != null) {
            // 不需要运行时权限
            onPermissionsGrantedAlready.onPermissionsGrantedAlready();
        }
    }

    /**
     * PermissionHelper中的Builder类
     */
    public static class Builder {
        private JZPermissionHelper JZPermissionHelper = new JZPermissionHelper();

        /**
         * 建造者类构造函数
         *
         * @param activity Context
         */
        public Builder(PermissionBaseActivity activity) {
            JZPermissionHelper.aWR = new WeakReference<>(activity);
        }

        public Builder requestCode(int requestCode) {
            JZPermissionHelper.requestCode = requestCode;
            return this;
        }

        /**
         * 用户许可回调
         *
         * @param onUserAllowed 用户许可回调
         * @return Builder类
         */
        public Builder onUserAllowed(OnUserAllowed onUserAllowed) {
            JZPermissionHelper.onUserAllowed = onUserAllowed;
            return this;
        }

        /**
         * 用户拒绝回调
         *
         * @param onUserDenied 用户拒绝回调
         * @return Builder类
         */
        public Builder onUserDenied(OnUserDenied onUserDenied) {
            JZPermissionHelper.onUserDenied = onUserDenied;
            return this;
        }

        /**
         * 用户设置不再提示回调
         *
         * @param onUserSetNeverAsk 用户设置不再提示回调
         * @return Builder类
         */
        public Builder onUserSetNeverAsk(OnUserSetNeverAsk onUserSetNeverAsk) {
            JZPermissionHelper.onUserSetNeverAsk = onUserSetNeverAsk;
            return this;
        }


        /**
         * 当所有权限都已经拥有都回调
         *
         * @param onPermissionsGrantedAlready 当所有权限都已经拥有都回调
         * @return Builder类
         */
        public Builder onPermissionsGrantedAlready(OnPermissionsGrantedAlready onPermissionsGrantedAlready) {
            JZPermissionHelper.onPermissionsGrantedAlready = onPermissionsGrantedAlready;
            return this;
        }

        /**
         * 创建帮助类对象
         *
         * @return 帮助类对象
         */
        public JZPermissionHelper create() {
            JZPermissionHelper.aWR.get().setJZPermissionHelper(JZPermissionHelper);
            return JZPermissionHelper;
        }
    }
}
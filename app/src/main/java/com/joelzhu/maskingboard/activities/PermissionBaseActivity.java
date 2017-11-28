package com.joelzhu.maskingboard.activities;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.joelzhu.maskingboard.common.JZPermissionHelper;
import com.joelzhu.maskingboard.utils.JZDeviceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限申请帮助类
 */
public abstract class PermissionBaseActivity extends BaseActivity {
    private JZPermissionHelper JZPermissionHelper;

    public void setJZPermissionHelper(JZPermissionHelper JZPermissionHelper) {
        this.JZPermissionHelper = JZPermissionHelper;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (JZPermissionHelper == null)
            return;

        if (requestCode == JZPermissionHelper.requestCode) {
            // 未授权权限数组(包含不再提示)
            List<String> deniedPermissions = new ArrayList<>();
            // 不再提示权限数组
            List<String> neverAskPermissions = new ArrayList<>();

            for (String permission : permissions) {
                // 没有获取到该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                    deniedPermissions.add(permission);
                else
                    // 用户点击了不再提示
                    if (!JZDeviceUtils.checkPermission(this, permission)) {
                        neverAskPermissions.add(permission);
                        deniedPermissions.add(permission);
                    }
            }

            // 获取到全部权限
            if (deniedPermissions.size() == 0) {
                if (JZPermissionHelper.onUserAllowed != null)
                    JZPermissionHelper.onUserAllowed.onUserAllowed();
            }
            // 用户勾选不再提示
            else if (neverAskPermissions.size() != 0) {
                if (JZPermissionHelper.onUserSetNeverAsk != null)
                    JZPermissionHelper.onUserSetNeverAsk.onUserSetNeverAsk(neverAskPermissions.toArray(new String[]{}));
            }
            // 用户拒绝提供权限
            else {
                if (JZPermissionHelper.onUserDenied != null)
                    JZPermissionHelper.onUserDenied.onUserDenied(deniedPermissions.toArray(new String[]{}));
            }
        }
    }

    public interface OnUserAllowed {
        void onUserAllowed();
    }

    public interface OnUserDenied {
        void onUserDenied(String[] deniedPermissions);
    }

    public interface OnUserSetNeverAsk {
        void onUserSetNeverAsk(String[] neverAskPermissions);
    }

    public interface OnPermissionsGrantedAlready {
        void onPermissionsGrantedAlready();
    }
}
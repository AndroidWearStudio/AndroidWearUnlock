package net.npike.android.util;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;

import net.npike.android.wearunlock.WearUnlockApp;
import net.npike.android.wearunlock.receiver.WearUnlockDeviceAdminReceiver;

/**
 * Created by npike on 7/16/14.
 */
public class DevicePasswordManager {

    private final DevicePolicyManager mDevicePolicyManager;
    private final ComponentName mDeviceAdminReceiver;

    public DevicePasswordManager(Context context) {
        mDevicePolicyManager = (DevicePolicyManager) context.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mDeviceAdminReceiver = new ComponentName(context,
                WearUnlockDeviceAdminReceiver.class);
    }

    public boolean isDeviceEncrypted() {
        return mDevicePolicyManager.getStorageEncryptionStatus() == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE;
    }


    public boolean onLockDevice() {
        LogWrap.l();

        if (!TextUtils.isEmpty(WearUnlockApp.getInstance().getPassword())) {
            boolean result = resetPassword(WearUnlockApp.getInstance().getPassword());
            if (result && WearUnlockApp.getInstance().shouldLockDeviceImmediately()) {
                mDevicePolicyManager.lockNow();
            }
            return result;
        } else {
            return false;
        }

    }

    public boolean onUnlockDevice() {
        LogWrap.l();
        return resetPassword("");
    }


    protected boolean resetPassword(String newPassword) {
        LogWrap.l();
        if (mDevicePolicyManager.isAdminActive(mDeviceAdminReceiver)) {
            boolean result = mDevicePolicyManager.resetPassword(newPassword,
                    DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);

            LogWrap.l(result ? "Password changed." : "Password not changed.");

            return result;
        } else {
            LogWrap.l("Wear Unlock is not a device admin.");

            return false;
        }
    }
}

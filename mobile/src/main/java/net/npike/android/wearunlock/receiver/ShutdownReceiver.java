package net.npike.android.wearunlock.receiver;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import net.npike.android.util.DevicePasswordManager;
import net.npike.android.util.LogWrap;
import net.npike.android.wearunlock.WearUnlockApp;
import net.npike.android.wearunlock.service.WearUnlockService;


public class ShutdownReceiver extends BroadcastReceiver {

	private static final String TAG = "StartupReceiver";
    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mDeviceAdminReceiver;

    @Override
	public void onReceive(Context context, Intent intent) {
		LogWrap.l("Shuting down...");

        if (WearUnlockApp.getInstance().isEnabled()) {
            DevicePasswordManager manager = new DevicePasswordManager(context);
            manager.onLockDevice();
        }
	}

}
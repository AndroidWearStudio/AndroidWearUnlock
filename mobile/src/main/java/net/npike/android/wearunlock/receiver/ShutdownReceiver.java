package net.npike.android.wearunlock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.npike.android.util.LogWrap;
import net.npike.android.wearunlock.WearUnlockApp;
import net.npike.android.wearunlock.service.WearUnlockService;


public class ShutdownReceiver extends BroadcastReceiver {

	private static final String TAG = "StartupReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		LogWrap.l("starting up");

        if (WearUnlockApp.getInstance().isEnabled()) {
            WearUnlockService.startService(context);
        }
	}

}
package net.npike.android.wearunlock.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;
import com.squareup.otto.Subscribe;

import net.npike.android.util.BusProvider;
import net.npike.android.util.LogWrap;
import net.npike.android.wearunlock.R;
import net.npike.android.wearunlock.WearUnlockApp;
import net.npike.android.wearunlock.activity.PrefActivity;
import net.npike.android.wearunlock.event.WearNode;
import net.npike.android.wearunlock.provider.LogContract;
import net.npike.android.wearunlock.receiver.WearUnlockDeviceAdminReceiver;
import net.npike.android.wearunlock.wearutil.DiscoveryHelper;

/**
 * Created by npike on 6/30/14.
 */
public class WearUnlockService extends WearableListenerService {

    public static final int NOTIFICATION_ID = 1;

    private Notification mNotification;
    private NotificationManager mNotificationManager;
    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mDeviceAdminReceiver;
    private boolean mLastResult;

    private enum WearState {
        UNKNOWN, CONNECTED, DISCONNECTED, ERROR
    }


    public static void startService(Context context) {
        Intent intent = new Intent(context, WearUnlockService.class);
        context.startService(intent);
    }

    public static void stopService(Context context) {
        LogWrap.l();
        Intent intent = new Intent(context, WearUnlockService.class);
        context.stopService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogWrap.l();

        BusProvider.getInstance().register(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mDevicePolicyManager = (DevicePolicyManager) getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mDeviceAdminReceiver = new ComponentName(this,
                WearUnlockDeviceAdminReceiver.class);

        setupAsForeground();


        if (WearUnlockApp.getInstance().isEnabled()) {
            LogWrap.l("Existing Android Wear id: ;" + WearUnlockApp.getInstance().getPairedPebbleAddress() + ";");

            DiscoveryHelper.getInstance().startDiscovery(this);
        }

        logMessage(true, "Service started.");
    }

    @Subscribe
    public void onNodeEvent(final WearNode event) {
        LogWrap.l();

        // found a connected device.  Does this match the one we paired with during onboarding?
        LogWrap.l(";" + event.getId() + ";");
        if (TextUtils.equals(event.getId(), WearUnlockApp.getInstance().getPairedPebbleAddress())) {
            if (WearUnlockApp.getInstance().isEnabled()) {
                onRequestLockDevice(WearState.CONNECTED);
            }
        }
    }

    @Override
    public void onDestroy() {
        LogWrap.l();
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        LogWrap.l();
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
        LogWrap.l();

        if (WearUnlockApp.getInstance().isEnabled()) {
            onRequestLockDevice(WearState.CONNECTED);
        }
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);
        LogWrap.l();

        if (WearUnlockApp.getInstance().isEnabled()) {
            onRequestLockDevice(WearState.DISCONNECTED);
        }
    }

    private void setupAsForeground() {
        LogWrap.l();
        buildNotification(WearState.UNKNOWN);

        startForeground(NOTIFICATION_ID, mNotification);
    }

    private void onRequestLockDevice(WearState state) {
        LogWrap.l(state.toString());

        boolean lockResult = false;

        switch (state) {
            case CONNECTED:
                // TODO unlock device if we are connected
                logMessage(true, "Device connected.");
                lockResult =  onUnlockDevice();
                break;
            case UNKNOWN:
                // TODO lock device if we aren't sure
            case DISCONNECTED:
            default:
                // TODO lock device
                logMessage(false, "Device disconnected.");
                lockResult =  onLockDevice();
                break;
        }

        if (!lockResult) {
            state = WearState.ERROR;
        }

        updateNotification(state);
    }

    private boolean onLockDevice() {
        LogWrap.l();

        if (!TextUtils.isEmpty(WearUnlockApp.getInstance().getPassword())) {
            return resetPassword(WearUnlockApp.getInstance().getPassword());
        } else {
            return false;
        }

    }

    private boolean onUnlockDevice() {
        LogWrap.l();
        return resetPassword("");
    }

    private void updateNotification(WearState state) {
        LogWrap.l();
        buildNotification(state);

        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    private void buildNotification(WearState state) {
        LogWrap.l();
        String notificationText = getString(R.string.service_waiting_for_wear);
        int notificationDrawable = R.drawable.ic_locked;

        switch (state) {
            case CONNECTED:
                notificationText = getString(R.string.notification_watch_connected_text);
                notificationDrawable = R.drawable.ic_unlocked;
                break;
            case DISCONNECTED:
                // TODO externalize
                notificationText = getString(R.string.notification_disconnected);
                notificationDrawable = R.drawable.ic_locked;
                break;
            case ERROR:
                notificationText = "Unable to change device lock.";
                notificationDrawable = R.drawable.ic_unlocked;
                break;
            case UNKNOWN:
            default:
                notificationText = getString(R.string.service_waiting_for_wear);
                notificationDrawable = R.drawable.ic_locked;
                break;
        }

        Intent notificationIntent = new Intent(this, PrefActivity.class);
        notificationIntent.setAction(System.currentTimeMillis() + "");

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        long when = System.currentTimeMillis();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(
                        notificationText)
                .setWhen(when).setContentIntent(pendingIntent)
                .setSmallIcon(notificationDrawable);

        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_MIN);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(getString(R.string.app_name));
        bigTextStyle.bigText(notificationText);
        builder.setStyle(bigTextStyle);

        mNotification = builder.build();
    }

    protected boolean resetPassword(String newPassword) {
        LogWrap.l();
        if (mDevicePolicyManager.isAdminActive(mDeviceAdminReceiver)) {
            mLastResult = mDevicePolicyManager.resetPassword(newPassword,
                    DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);

            LogWrap.l(mLastResult ? "Password changed." : "Password not changed.");

            return mLastResult;
        } else {
            LogWrap.l("Wear Unlock is not a device admin.");

            return false;
        }
    }


    private void logMessage(boolean isConnected, String message) {
        ContentValues cv = new ContentValues();
        cv.put(LogContract.ConnectionEvent.COLUMN_NAME_CONNECTED,
                isConnected ? 1 : 0);
        cv.put(LogContract.ConnectionEvent.COLUMN_NAME_TIME,
                System.currentTimeMillis());
        if (!TextUtils.isEmpty(message)) {
            cv.put(LogContract.ConnectionEvent.COLUMN_NAME_MESSAGE, message);
        }

        getContentResolver().insert(LogContract.ConnectionEvent.CONTENT_URI, cv);
    }
}


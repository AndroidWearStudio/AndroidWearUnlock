package net.npike.android.wearunlock.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import net.npike.android.util.DevicePasswordManager;
import net.npike.android.util.LogWrap;
import net.npike.android.wearunlock.R;
import net.npike.android.wearunlock.WearUnlockApp;
import net.npike.android.wearunlock.activity.PrefActivity;
import net.npike.android.wearunlock.event.WearNode;
import net.npike.android.wearunlock.provider.LogContract;
import net.npike.android.wearunlock.wearutil.DiscoveryHelper;

/**
 * Created by npike on 6/30/14.
 */
public class WearUnlockService extends WearableListenerService {

    public static final int NOTIFICATION_ID = 1;
    public static final String ACTION_REBOOT = "reboot";

    private Notification mNotification;
    private NotificationManager mNotificationManager;
    private boolean mLastResult;
    private Intent mStartingIntent;
    private DevicePasswordManager mDevicePasswordManager;

    @Deprecated
    public static void startService(Context context) {
        Intent intent = new Intent(context, WearUnlockService.class);
        context.startService(intent);
    }

    @Deprecated
    public static void startServiceBoot(Context context) {
        Intent intent = new Intent(context, WearUnlockService.class);
        intent.setAction(ACTION_REBOOT);
        context.startService(intent);
    }

    public static void stopService(Context context) {
        LogWrap.l();
        Intent intent = new Intent(context, WearUnlockService.class);
        context.stopService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mStartingIntent = intent;

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogWrap.l();

        BusProvider.getInstance().register(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mDevicePasswordManager = new DevicePasswordManager(this);

        setupAsForeground();


        if (WearUnlockApp.getInstance().isEnabled()) {
            LogWrap.l("Existing Android Wear id: ;" + WearUnlockApp.getInstance().getPairdAndroidWearId() + ";");


            DiscoveryHelper.getInstance().startDiscovery(this);
        }

        logMessage(true, "Service started.");
    }

    @Subscribe
    public void onNodeEvent(final WearNode event) {
        LogWrap.l();

        // found a connected device.  Does this match the one we paired with during onboarding?
        LogWrap.l(";" + event.getId() + ";");
        if (TextUtils.equals(event.getId(), WearUnlockApp.getInstance().getPairdAndroidWearId())) {
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
        if (WearUnlockApp.getInstance().shouldShowNotification()) {
            LogWrap.l();
            buildNotification(WearState.UNKNOWN);

            startForeground(NOTIFICATION_ID, mNotification);
        } else {
            LogWrap.l("Not going into foreground because notification setting is disabled.");
        }
    }

    private void onRequestLockDevice(WearState state) {
        LogWrap.l(state.toString());

        boolean lockResult = false;

        switch (state) {
            case CONNECTED:
                logMessage(true, "Device connected.");
                lockResult = mDevicePasswordManager.onUnlockDevice();
                break;
            case UNKNOWN:
                // Lock device if we aren't sure
            case DISCONNECTED:
            default:
                logMessage(false, "Device disconnected.");
                lockResult = mDevicePasswordManager.onLockDevice();
                break;
        }

        if (!lockResult) {
            state = WearState.ERROR;
        }

        updateNotification(state);
    }


    private void updateNotification(WearState state) {
        LogWrap.l();
        if (WearUnlockApp.getInstance().shouldShowNotification()) {
            buildNotification(state);

            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        } else {
            LogWrap.l("Not updating notification because notification is disabled.");
        }
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


    private enum WearState {
        UNKNOWN, CONNECTED, DISCONNECTED, ERROR
    }
}


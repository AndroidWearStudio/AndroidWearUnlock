package net.npike.android.wearunlock;

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
import net.npike.android.util.LogWrap;
import net.npike.android.wearunlock.activity.PrefActivity;
import net.npike.android.wearunlock.event.WearNode;
import net.npike.android.wearunlock.provider.LogContract;
import net.npike.android.wearunlock.wearutil.DiscoveryHelper;

/**
 * Created by npike on 6/30/14.
 */
public class WearUnlockService extends WearableListenerService {

    public static final int NOTIFICATION_ID = 1;

    private Notification mNotification;
    private NotificationManager mNotificationManager;

    private enum WearState {
        UNKNOWN, CONNECTED, DISCONNECTED
    }


    public static void startService(Context context) {
        Intent intent = new Intent(context, WearUnlockService.class);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogWrap.l();

        BusProvider.getInstance().register(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // TODO should see if there is an already connected device when starting service.
        setupAsForeground();


        if (WearUnlockApp.getInstance().isEnabled()) {
            LogWrap.l("Existing Android Wear id: ;" + WearUnlockApp.getInstance().getPairedPebbleAddress() + ";");

            DiscoveryHelper.getInstance().startDiscovery(this);
        }

        logMessage(this, true, "Service started.");
    }

    @Subscribe
    public void onNodeEvent(final WearNode event) {
        LogWrap.l();

        // found a connected device.  Does this match the one we paired with during onboarding?
        LogWrap.l(";" + event.getId() + ";");
        if (TextUtils.equals(event.getId(), WearUnlockApp.getInstance().getPairedPebbleAddress())) {
            onRequestLockDevice(WearState.CONNECTED);
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
            onRequestLockDevice(WearState.DISCONNECTED);
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
        switch (state) {
            case CONNECTED:
                // TODO unlock device if we are connected
                logMessage(this, true, "Device connected. Would normally disable password.");
                onUnlockDevice();
                break;
            case UNKNOWN:
                // TODO lock device if we aren't sure
            case DISCONNECTED:
            default:
                // TODO lock device
                logMessage(this, false, "Device disconnected.  Not setting password because of testing.");
                onLockDevice();
                break;
        }

        updateNotification(state);
    }

    private void onLockDevice() {
        LogWrap.l();
    }

    private void onUnlockDevice() {
        LogWrap.l();
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
                notificationText = getString(R.string.notification_pebble_connected_text);
                notificationDrawable = R.drawable.ic_unlocked;
                break;
            case DISCONNECTED:
                // TODO externalize
                notificationText = "Your Android Wear device is disconnected.  Passcode enabled.";
                notificationDrawable = R.drawable.ic_locked;
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


    private void logMessage(Context context, boolean isConnected, String message) {


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


package net.npike.android.wearunlock;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import net.npike.android.util.LogWrap;
import net.npike.android.wearunlock.activity.PrefActivity;
import net.npike.android.wearunlock.provider.LogContract;

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

        // TODO should see if there is an already connected device when starting service.

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        setupAsForeground();

        logMessage(this, true, "Service started.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogWrap.l();
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
            updateNotification(WearState.CONNECTED);

            logMessage(this, true, "Device connected. Would normally disable password.");
        }
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);
        LogWrap.l();

        if (WearUnlockApp.getInstance().isEnabled()) {
            updateNotification(WearState.DISCONNECTED);

            logMessage(this, false, "Device disconnected.  Not setting password because of testing.");
        }
    }

    private void setupAsForeground() {
        LogWrap.l();
        buildNotification(WearState.UNKNOWN);

        startForeground(NOTIFICATION_ID, mNotification);
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
        AsyncQueryHandler asyncQueryHandler = new AsyncQueryHandler(
                context.getContentResolver()) {
        };

        ContentValues cv = new ContentValues();
        cv.put(LogContract.ConnectionEvent.COLUMN_NAME_CONNECTED,
                isConnected ? 1 : 0);
        cv.put(LogContract.ConnectionEvent.COLUMN_NAME_TIME,
                System.currentTimeMillis());
        if (!TextUtils.isEmpty(message)) {
            cv.put(LogContract.ConnectionEvent.COLUMN_NAME_MESSAGE, message);
        }
        asyncQueryHandler.startInsert(0, null,
                LogContract.ConnectionEvent.CONTENT_URI, cv);
    }
}


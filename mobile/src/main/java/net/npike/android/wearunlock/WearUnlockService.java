package net.npike.android.wearunlock;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import net.npike.android.util.LogWrap;
import net.npike.android.wearunlock.provider.LogContract;

/**
 * Created by npike on 6/30/14.
 */
public class WearUnlockService extends WearableListenerService {

    @Override
    public void onCreate() {
        super.onCreate();
        LogWrap.l();

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

        logMessage(this, true, "Device connected. Would normally disable password.");
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);

        LogWrap.l();

        logMessage(this, false, "Device disconnected.  Not setting password because of testing.");
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


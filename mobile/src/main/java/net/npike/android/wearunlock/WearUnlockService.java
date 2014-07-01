package net.npike.android.wearunlock;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import net.npike.android.util.LogWrap;

/**
 * Created by npike on 6/30/14.
 */
public class WearUnlockService extends WearableListenerService {

    @Override
    public void onCreate() {
        super.onCreate();
        LogWrap.l();
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
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);

        LogWrap.l();
    }
}

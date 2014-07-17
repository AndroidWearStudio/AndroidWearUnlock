package net.npike.android.wearunlock.wearutil;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import net.npike.android.util.BusProvider;
import net.npike.android.util.LogWrap;
import net.npike.android.wearunlock.event.WearNode;

import java.util.HashSet;

/**
 * Created by npike on 7/1/14.
 */
public class DiscoveryHelper implements GoogleApiClient.ConnectionCallbacks {

    private static DiscoveryHelper mInstance = null;
    private GoogleApiClient mGoogleAppiClient;

    public static DiscoveryHelper getInstance() {
        if (mInstance == null) {
            mInstance = new DiscoveryHelper();
        }

        return mInstance;
    }

    private DiscoveryHelper() {
        BusProvider.getInstance().register(this);
    }

    public void startDiscovery(Context context) {
        LogWrap.l();
        mGoogleAppiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        LogWrap.l();
                    }
                })
                .addApi(Wearable.API)
                .build();

        mGoogleAppiClient.connect();
    }


    private void processPeers() {
        final Thread t = new Thread() {
            @Override
            public void run() {
                LogWrap.l();
                try {
                    HashSet<String> results = new HashSet<String>();
                    NodeApi.GetConnectedNodesResult nodes =
                            Wearable.NodeApi.getConnectedNodes(mGoogleAppiClient).await();
                    for (Node node : nodes.getNodes()) {
                        LogWrap.l(node.getDisplayName() + " " + node.getId());

                        BusProvider.getInstance().post(new WearNode(node));
                    }
                } finally {

                }
            }
        };
        t.start();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LogWrap.l();


        processPeers();
    }

    @Override
    public void onConnectionSuspended(int i) {
        LogWrap.l();
    }
}

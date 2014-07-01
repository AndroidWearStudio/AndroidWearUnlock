package net.npike.android.wearunlock;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

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
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class WearDiscoveryService extends Service implements GoogleApiClient.ConnectionCallbacks {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_DISCOVER = "net.npike.android.wearunlock.action.DISCOVER";
    private GoogleApiClient mGoogleAppiClient;
    private Looper serviceLooper;
    private int startId;
    private ServiceHandler serviceHandler;


    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            startId = msg.arg1;
            onHandleIntent((Intent) msg.obj);
        }
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionDiscover(Context context) {
        Intent intent = new Intent(context, WearDiscoveryService.class);
        intent.setAction(ACTION_DISCOVER);
        context.startService(intent);
    }


//    public WearDiscoveryService() {
//        super("WearDiscoveryService");
//    }


    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread thread = new HandlerThread("WearDiscoveryService:WorkerThread");
        thread.start();

        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Message message = serviceHandler.obtainMessage();
        message.arg1 = startId;
        message.obj = intent;
        serviceHandler.sendMessage(message);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DISCOVER.equals(action)) {
                handleDiscovery();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleDiscovery() {
        mGoogleAppiClient = new GoogleApiClient.Builder(this)
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

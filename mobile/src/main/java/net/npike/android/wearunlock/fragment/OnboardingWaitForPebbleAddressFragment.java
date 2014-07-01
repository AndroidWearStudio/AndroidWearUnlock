package net.npike.android.wearunlock.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.squareup.otto.Subscribe;

import net.npike.android.OnboardingInterface;
import net.npike.android.util.BusProvider;
import net.npike.android.util.LogWrap;
import net.npike.android.wearunlock.R;
import net.npike.android.wearunlock.WearDiscoveryService;
import net.npike.android.wearunlock.WearUnlockApp;
import net.npike.android.wearunlock.WearUnlockService;
import net.npike.android.wearunlock.event.WearNode;

import java.util.HashSet;

public class OnboardingWaitForPebbleAddressFragment extends Fragment {
    private static final String EXTRA_PEBBLE_ADDRESS = "address";
    private BroadcastReceiver mReceiver;
    private GoogleApiClient mGoogleAppiClient;
    private Handler mHandler;

    public static OnboardingWaitForPebbleAddressFragment getInstance() {
        return new OnboardingWaitForPebbleAddressFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();
    }

    @Override
    public void onResume() {
        super.onResume();

        BusProvider.getInstance().register(this);
//        getActivity().startService(new Intent(getActivity(), WearUnlockService.class));

        LogWrap.l();

        WearDiscoveryService.startActionDiscover(getActivity());

//        mGoogleAppiClient = new GoogleApiClient.Builder(getActivity())
//                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
//                    @Override
//                    public void onConnected(Bundle connectionHint) {
//                        LogWrap.l();
//
//
//                        processPeers();
//                    }
//
//                    @Override
//                    public void onConnectionSuspended(int cause) {
//                        LogWrap.l();
//                    }
//                })
//                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
//                    @Override
//                    public void onConnectionFailed(ConnectionResult result) {
//                        LogWrap.l();
//                    }
//                })
//                .addApi(Wearable.API)
//                .build();
//
//        mGoogleAppiClient.connect();

//		IntentFilter filter = new IntentFilter();
//		filter.addAction("com.getpebble.action.PEBBLE_CONNECTED");
//		filter.addAction("com.getpebble.action.PEBBLE_DISCONNECTED");
//
//		mReceiver = new BroadcastReceiver() {
//
//			@Override
//			public void onReceive(Context context, Intent intent) {
//				final String pebbleAddress = intent
//						.getStringExtra(EXTRA_PEBBLE_ADDRESS);
//
//				WearUnlockApp.getInstance().putPairedPebbleAddress(
//						pebbleAddress);
//
//				try {
//					((OnboardingInterface) getActivity())
//							.onPebbleFound(pebbleAddress);
//				} catch (ClassCastException ccex) {
//
//				}
//			}
//		};
//		getActivity().registerReceiver(mReceiver, filter);

    }

    protected void processPeers() {
        LogWrap.l();

        new Runnable() {
            @Override
            public void run() {
                HashSet<String> results = new HashSet<String>();
                NodeApi.GetConnectedNodesResult nodes =
                        Wearable.NodeApi.getConnectedNodes(mGoogleAppiClient).await();
                for (Node node : nodes.getNodes()) {
                    LogWrap.l(node.getDisplayName() + " " + node.getId());
                }
            }
        }.run();

    }

    @Override
    public void onPause() {
        BusProvider.getInstance().unregister(this);
        super.onPause();
//        try {
//            getActivity().unregisterReceiver(mReceiver);
//        } catch (IllegalArgumentException iae) {
//
//        }
    }

    @Subscribe
    public void onNodeEvent(final WearNode event) {
        LogWrap.l();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((OnboardingInterface) getActivity()).onPebbleFound(event.getId());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater
                .inflate(R.layout.frag_onboarding_waitforpebbleaddress,
                        container, false);
        ProgressBar progressBar = (ProgressBar) v
                .findViewById(R.id.progressBar);

        progressBar.setIndeterminate(true);

        return v;
    }
}

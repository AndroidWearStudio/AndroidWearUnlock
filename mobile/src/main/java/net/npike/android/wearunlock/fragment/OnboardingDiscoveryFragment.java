package net.npike.android.wearunlock.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.otto.Subscribe;

import net.npike.android.OnboardingInterface;
import net.npike.android.util.BusProvider;
import net.npike.android.util.LogWrap;
import net.npike.android.wearunlock.R;
import net.npike.android.wearunlock.WearDiscoveryService;
import net.npike.android.wearunlock.event.WearNode;

public class OnboardingDiscoveryFragment extends Fragment {
    private static final String EXTRA_PEBBLE_ADDRESS = "address";
    private BroadcastReceiver mReceiver;
    private GoogleApiClient mGoogleAppiClient;
    private Handler mHandler;

    public static OnboardingDiscoveryFragment getInstance() {
        return new OnboardingDiscoveryFragment();
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

        LogWrap.l();

        WearDiscoveryService.startActionDiscover(getActivity());
    }



    @Override
    public void onPause() {
        BusProvider.getInstance().unregister(this);
        super.onPause();
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
                .inflate(R.layout.frag_onboarding_discovery,
                        container, false);
        ProgressBar progressBar = (ProgressBar) v
                .findViewById(R.id.progressBar);

        progressBar.setIndeterminate(true);

        return v;
    }
}

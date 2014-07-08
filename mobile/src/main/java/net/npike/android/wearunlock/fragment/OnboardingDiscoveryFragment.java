package net.npike.android.wearunlock.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.otto.Subscribe;

import net.npike.android.util.BindingAdapter;
import net.npike.android.util.BusProvider;
import net.npike.android.util.LogWrap;
import net.npike.android.wearunlock.R;
import net.npike.android.wearunlock.interfaces.OnboardingInterface;
import net.npike.android.wearunlock.service.WearDiscoveryService;
import net.npike.android.wearunlock.event.WearNode;

import java.util.ArrayList;

public class OnboardingDiscoveryFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String EXTRA_PEBBLE_ADDRESS = "address";
    private BroadcastReceiver mReceiver;
    private GoogleApiClient mGoogleAppiClient;
    private Handler mHandler;
    private DeviceAdapter mAdapter;
    private ListView mListViewDevices;
    private ProgressBar mProgressBar;

    public static OnboardingDiscoveryFragment getInstance() {
        return new OnboardingDiscoveryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();
        mAdapter = new DeviceAdapter(getActivity());
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater
                .inflate(R.layout.frag_onboarding_discovery,
                        container, false);
        mListViewDevices = (ListView) v.findViewById(R.id.listViewDevices);
        mProgressBar = (ProgressBar) v
                .findViewById(R.id.progressBar);

        mListViewDevices.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        mListViewDevices.setOnItemClickListener(this);
        mListViewDevices.setAdapter(mAdapter);

        mProgressBar.setIndeterminate(true);

        return v;
    }


    @Subscribe
    public void onNodeEvent(final WearNode event) {
        LogWrap.l();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mAdapter.putItem(event);

                if (mAdapter.getCount() > 0) {
                    mListViewDevices.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mListViewDevices.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ((OnboardingInterface) getActivity()).onPebbleFound(mAdapter.getItem(position).getId());
    }


    public static class DeviceAdapter extends BindingAdapter {

        private final Context mContext;
        private final LayoutInflater inflater;
        private ArrayList<WearNode> mData;

        public DeviceAdapter(final Context context) {
            mContext = context;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void putItem(WearNode item) {
            if (mData == null) {
                mData = new ArrayList<WearNode>();
                mData.add(item);

                return;
            }
            for (WearNode node : mData) {
                if (!node.equals(item.getId())) {
                    mData.add(item);

                    notifyDataSetChanged();
                    break;
                }
            }
        }

        @Override
        public View newView(int type, ViewGroup parent) {
            return inflater.inflate(R.layout.item_device, parent, false);
        }

        @Override
        public void bindView(int position, int type, View view, ViewGroup parent) {
            WearNode item = getItem(position);

            TextView textViewDeviceDisplayName = (TextView) view.findViewById(R.id.textViewDeviceDisplayName);

            textViewDeviceDisplayName.setText(item.getDisplayName());
        }

        @Override
        public int getCount() {
            return mData != null ? mData.size() : 0;
        }

        @Override
        public WearNode getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }


}

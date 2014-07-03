package net.npike.android.wearunlock.activity;

import android.app.Activity;
import android.os.Bundle;

import net.npike.android.OnboardingInterface;
import net.npike.android.wearunlock.R;
import net.npike.android.wearunlock.WearUnlockApp;
import net.npike.android.wearunlock.fragment.OnboardingConfigurePasswordFragment;
import net.npike.android.wearunlock.fragment.OnboardingDiscoveryFragment;
import net.npike.android.wearunlock.fragment.OnboardingRequestDeviceAdminFragment;

public class OnboardingActivity extends Activity implements OnboardingInterface {

	private static final String TAG_CONFIGURE_PASSWORD_FRAG = "configure_password_frag";
	private static final String TAG_REQUEST_ADMIN_FRAG = "request_admin_frag";
	private static final String TAG_WAIT_FOR_PEBBLE_FRAG = "wait_for_pebble_frag";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_onboarding);

		if (savedInstanceState == null) {
			getFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_placeholder,
							OnboardingDiscoveryFragment.getInstance(),
							TAG_WAIT_FOR_PEBBLE_FRAG).commit();
		}
	}

	@Override
	public void onPasswordConfigured() {
		getFragmentManager()
				.beginTransaction()
				.replace(R.id.fragment_placeholder,
						OnboardingRequestDeviceAdminFragment.getInstance(),
						TAG_REQUEST_ADMIN_FRAG).commit();
	}

	@Override
	public void onPebbleFound(String address) {
        WearUnlockApp.getInstance().putPairedPebbleAddress(address);

		getFragmentManager()
				.beginTransaction()
				.replace(R.id.fragment_placeholder,
						OnboardingConfigurePasswordFragment.getInstance(),
						TAG_CONFIGURE_PASSWORD_FRAG).commit();


	}

}

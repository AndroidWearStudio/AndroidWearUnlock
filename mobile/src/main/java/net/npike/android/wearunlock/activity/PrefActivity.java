package net.npike.android.wearunlock.activity;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import net.npike.android.util.LogWrap;
import net.npike.android.wearunlock.BuildConfig;
import net.npike.android.wearunlock.R;
import net.npike.android.wearunlock.WearUnlockApp;
import net.npike.android.wearunlock.service.WearUnlockService;
import net.npike.android.wearunlock.fragment.PasswordChangeFragment;
import net.npike.android.wearunlock.receiver.WearUnlockDeviceAdminReceiver;

public class PrefActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener, OnPreferenceClickListener {

	private static final String TAG_PASSWORD_CHANGE_FRAG = "password_change_frag";

	private OnPreferenceChangeListener mOnPreferenceChangedListenerEnabled = new OnPreferenceChangeListener() {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if (mIgnoreNextEnableRequest) {
				mIgnoreNextEnableRequest = false;
				return true;
			}
			if ((Boolean) newValue) {
				Intent launchIntent = new Intent(PrefActivity.this,
						OnboardingActivity.class);
				startActivity(launchIntent);
			} else {
                WearUnlockService.stopService(PrefActivity.this);
				mDevicePolicyManager.removeActiveAdmin(mDeviceAdminReceiver);
				return true;
			}
			return false;
		}
	};

	private static final String TAG = "PrefActivity";
	private DevicePolicyManager mDevicePolicyManager;
	private ComponentName mDeviceAdminReceiver;
	private SwitchPreference mSwitchPreferenceEnable;
	private boolean mIgnoreNextEnableRequest = false;

	private PreferenceScreen mSetPassword;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		mDeviceAdminReceiver = new ComponentName(this,
				WearUnlockDeviceAdminReceiver.class);

		addPreferencesFromResource(R.xml.preferences);

		Preference version = getPreferenceManager().findPreference(
				getString(R.string.pref_key_version));

		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			version.setSummary(getString(R.string.pref_version_summary,
					packageInfo.versionName, packageInfo.versionCode));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		mSwitchPreferenceEnable = (SwitchPreference) getPreferenceManager()
				.findPreference(getString(R.string.pref_key_enable));
		mSwitchPreferenceEnable
				.setOnPreferenceChangeListener(mOnPreferenceChangedListenerEnabled);

		mSetPassword = (PreferenceScreen) findPreference("key_set_password");
		mSetPassword.setOnPreferenceClickListener(this);

		getPreferenceManager().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);


        if (WearUnlockApp.getInstance().isEnabled()) {
            WearUnlockService.startService(this);
        }
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
        LogWrap.l(key);
		if (key.equalsIgnoreCase(getString(R.string.pref_key_enable))) {
			if (WearUnlockApp.getInstance().isEnabled()) {
				mIgnoreNextEnableRequest = true;
				mSwitchPreferenceEnable.setChecked(true);
			}
		} else if (key.equalsIgnoreCase(getString(R.string.pref_key_enable_notification))) {
            WearUnlockService.stopService(this);
            WearUnlockService.startService(this);
        }
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference == mSetPassword) {
			PasswordChangeFragment.getInstance().show(getFragmentManager(),
					TAG_PASSWORD_CHANGE_FRAG);
		}
		return false;
	}
}

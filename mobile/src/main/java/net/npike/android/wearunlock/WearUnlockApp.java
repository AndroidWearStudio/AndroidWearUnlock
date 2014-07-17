package net.npike.android.wearunlock;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.npike.android.util.CrappyCrypto;

public class WearUnlockApp extends Application {
    private static WearUnlockApp INSTANCE;
    private SharedPreferences mPrefs;
    private CrappyCrypto mCrypto;

    @Override
    public void onCreate() {
        super.onCreate();

        INSTANCE = this;

        mCrypto = new CrappyCrypto();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

    }

    public static WearUnlockApp getInstance() {
        return INSTANCE;
    }

    public boolean isEnabled() {
        return mPrefs.getBoolean(getString(R.string.pref_key_enable), false);
    }

    public void setEnabled(boolean enabled) {
        mPrefs.edit().putBoolean(getString(R.string.pref_key_enable), enabled)
                .commit();
    }

    public boolean shouldShowNotification() {
        return mPrefs.getBoolean(getString(R.string.pref_key_enable_notification), false);
    }

    public String getPassword() {
        try {
            return new String(mCrypto.decrypt((mPrefs.getString(
                    getString(R.string.pref_key_password), "")))).trim();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setPassword(String newPassword) {
        try {
            mPrefs.edit()
                    .putString(getString(R.string.pref_key_password),
                            CrappyCrypto.bytesToHex(mCrypto.encrypt(newPassword))).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPairdAndroidWearId() {
        try {
            return new String(mCrypto.decrypt(mPrefs.getString(
                    getString(R.string.pref_key_wear_id), ""))).trim();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void putPairedAndroidWearId(String address) {
        // mask it because I feel like it.
        try {
            mPrefs.edit()
                    .putString(getString(R.string.pref_key_wear_id),
                            CrappyCrypto.bytesToHex(mCrypto.encrypt(address))).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

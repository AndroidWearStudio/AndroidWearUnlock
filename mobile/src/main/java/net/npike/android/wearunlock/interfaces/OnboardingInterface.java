package net.npike.android.wearunlock.interfaces;

public interface OnboardingInterface {
	public abstract void onPebbleFound(String address);
	public abstract void onPasswordConfigured();
}

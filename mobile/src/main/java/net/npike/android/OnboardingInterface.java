package net.npike.android;

public interface OnboardingInterface {
	public abstract void onPebbleFound(String address);
	public abstract void onPasswordConfigured();
}

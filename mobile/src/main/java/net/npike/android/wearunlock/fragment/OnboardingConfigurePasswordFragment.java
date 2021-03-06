package net.npike.android.wearunlock.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import net.npike.android.wearunlock.R;
import net.npike.android.wearunlock.interfaces.OnboardingInterface;

public class OnboardingConfigurePasswordFragment extends PasswordChangeFragment
        implements OnClickListener {

    private Button mButtonFinished;

    public static OnboardingConfigurePasswordFragment getInstance() {
        return new OnboardingConfigurePasswordFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.frag_onboarding_configure_password, null, false);


        mButtonFinished = (Button) view.findViewById(R.id.buttonFinish);
        mButtonFinished.setOnClickListener(this);

        bindView(view, false);

        return view;
    }

    @Override
    protected void onPasswordChange(boolean confirmed) {
        mButtonFinished.setEnabled(confirmed);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonFinish) {
            // commit password and then start admin activity
            handlePasswordConfirm();

            try {
                ((OnboardingInterface) getActivity()).onPasswordConfigured();
            } catch (ClassCastException ccex) {

            }
        }

    }


}

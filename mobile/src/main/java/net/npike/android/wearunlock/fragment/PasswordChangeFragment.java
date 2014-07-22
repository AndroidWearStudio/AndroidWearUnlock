package net.npike.android.wearunlock.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import net.npike.android.wearunlock.R;
import net.npike.android.wearunlock.WearUnlockApp;

public class PasswordChangeFragment extends DialogFragment {

    private EditText mEditTextPassword;
    private EditText mEditTextPasswordConfirm;
    private TextView mTextViewPasswordStatus;
    private EditText mEditTextCurrentPassword;
    private TextView mTextViewCurrentPassword;

    public static PasswordChangeFragment getInstance() {
        return new PasswordChangeFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = inflater
                .inflate(R.layout.frag_password_change, null, false);
        bindView(view, true);

        builder.setView(view);
        builder.setTitle(R.string.dialog_change_set_password);
        builder.setPositiveButton(R.string.dialog_change_change,
                new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handlePasswordConfirm();

                    }
                }
        );
        builder.setNegativeButton(R.string.dialog_change_cancel, null);

        return builder.create();
    }

    protected void bindView(final View view, boolean requireCurrentPassword) {
        final ScrollView scrollViewPassword = (ScrollView) view.findViewById(R.id.scrollViewPassword);
        mEditTextCurrentPassword = (EditText) view.findViewById(R.id.editTextCurrentPassword);
        mEditTextPassword = (EditText) view.findViewById(R.id.editTextPassword);
        mEditTextPasswordConfirm = (EditText) view
                .findViewById(R.id.editTextPasswordConfirm);
        mTextViewPasswordStatus = (TextView) view
                .findViewById(R.id.textViewPasswordStatus);
        mTextViewCurrentPassword = (TextView) view.findViewById(R.id.textViewCurrentPassword);

        if (requireCurrentPassword) {
            mTextViewCurrentPassword.setVisibility(View.VISIBLE);
            mEditTextCurrentPassword.setVisibility(View.VISIBLE);
        }


        if (scrollViewPassword != null) {
            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int heightDiff = view.getRootView().getHeight() - view.getHeight();
                    if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                        scrollViewPassword.smoothScrollTo(0, mTextViewPasswordStatus.getBottom());

                    }
                }
            });
        }

        if (requireCurrentPassword) {
            toggleNewPassword(false);

            mEditTextCurrentPassword.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // NOOP
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // NOOP
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (TextUtils.equals(mEditTextCurrentPassword.getText().toString(), WearUnlockApp.getInstance().getPassword().trim())) {
                        toggleNewPassword(true);
                        mTextViewPasswordStatus.setText("");
                    } else {
                        toggleNewPassword(false);
                        onPasswordChange(false);
                        mTextViewPasswordStatus
                                .setText(R.string.dialog_password_incorrect);
                    }
                }
            });


        }

        mEditTextPasswordConfirm.addTextChangedListener(new

                                                                TextWatcher() {

                                                                    @Override
                                                                    public void afterTextChanged(Editable s) {
                                                                        if (mEditTextPassword.getText().toString()
                                                                                .equals(mEditTextPasswordConfirm.getText().toString())) {
                                                                            mTextViewPasswordStatus.setText("");

                                                                            onPasswordChange(true);
                                                                        } else {
                                                                            mTextViewPasswordStatus
                                                                                    .setText(R.string.dialog_change_passwords_do_not_match);
                                                                            onPasswordChange(false);

                                                                        }

                                                                    }

                                                                    @Override
                                                                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                                                                  int after) {
                                                                        // NOOP

                                                                    }

                                                                    @Override
                                                                    public void onTextChanged(CharSequence s, int start, int before,
                                                                                              int count) {
                                                                        // NOOP

                                                                    }

                                                                }
        );
    }

    private void toggleNewPassword(boolean b) {
        mEditTextPassword.setEnabled(b);
        mEditTextPasswordConfirm.setEnabled(b);
    }

    protected void onPasswordChange(boolean confirmed) {

    }

    protected void handlePasswordConfirm() {
        // Do the passwords actually match?
        if (mEditTextPassword.getText().toString()
                .equals(mEditTextPasswordConfirm.getText().toString())
                && !TextUtils.isEmpty(mEditTextPassword.getText().toString())
                && !TextUtils.isEmpty(mEditTextPasswordConfirm.getText()
                .toString())) {
            WearUnlockApp.getInstance().setPassword(
                    mEditTextPassword.getText().toString());
        } else {
            Toast.makeText(getActivity(),
                    R.string.dialog_change_password_not_changed,
                    Toast.LENGTH_SHORT).show();
        }
    }
}

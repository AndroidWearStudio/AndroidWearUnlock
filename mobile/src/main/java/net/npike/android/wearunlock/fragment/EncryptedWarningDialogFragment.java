package net.npike.android.wearunlock.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;

import net.npike.android.wearunlock.R;

/**
 * Created by npike on 7/17/14.
 */
public class EncryptedWarningDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getActivity()
                .getString(R.string.dialog_enc_warning_title))
                .setMessage(getActivity().getString(R.string.dialog_enc_warning_message))
                .setPositiveButton(getActivity().getString(R.string.dialog_enc_warning_okay), null);

        return builder.create();
    }
}

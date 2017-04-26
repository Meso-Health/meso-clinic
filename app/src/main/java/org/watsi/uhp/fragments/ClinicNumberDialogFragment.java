package org.watsi.uhp.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.KeyboardManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.SyncableModel;

public class ClinicNumberDialogFragment extends DialogFragment {

    private RadioGroup mClinicNumberRadioGroup;
    private EditText mClinicNumberView;
    private Button mSubmitButton;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_clinic_number, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setMessage(R.string.clinic_number_prompt)
                .setPositiveButton(R.string.clinic_number_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RadioButton selectedRadioButton = (RadioButton) ((AlertDialog)dialog)
                                .findViewById(mClinicNumberRadioGroup.getCheckedRadioButtonId());
                        IdentificationEvent.ClinicNumberTypeEnum clinicNumberType =
                                IdentificationEvent.ClinicNumberTypeEnum.valueOf(
                                        selectedRadioButton.getText().toString().toUpperCase());
                        int clinicNumber = Integer.valueOf(mClinicNumberView.getText().toString());

                        try {
                            ((DetailFragment) getTargetFragment()).completeIdentification(
                                    true, clinicNumberType, clinicNumber);
                        } catch (SyncableModel.UnauthenticatedException e) {
                            Rollbar.reportException(e);
                            Toast.makeText(getActivity(),
                                    "Failed to save identification, contact support.",
                                    Toast.LENGTH_LONG).
                                    show();
                        }
                    }
                });

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                mClinicNumberRadioGroup = (RadioGroup) ((AlertDialog) dialog).findViewById(
                        R.id.radio_group_clinic_number);
                mClinicNumberView = (EditText) ((AlertDialog) dialog).findViewById(
                        R.id.clinic_number_field);
                mSubmitButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                mSubmitButton.setEnabled(false);
                setTextChangedListener();
                KeyboardManager.focusAndShowKeyboard(mClinicNumberView, getContext());
            }
        });

        return dialog;
    }

    private void setTextChangedListener() {
        mClinicNumberView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mClinicNumberView.getText().toString().isEmpty()) {
                    mSubmitButton.setEnabled(false);
                } else {
                    mSubmitButton.setEnabled(true);
                }
            }
        });
    }
}

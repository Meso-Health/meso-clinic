package org.watsi.uhp.listeners;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by michaelliang on 6/13/17.
 */

public class ClinicNumberFormSubmitTextListener implements TextWatcher {
    private EditText mClinicNumberView;
    private Button mSubmitButton;

    public ClinicNumberFormSubmitTextListener(EditText clinicNumberView, Button submitButton) {
        mClinicNumberView = clinicNumberView;
        mSubmitButton = submitButton;
    }

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
}

package org.watsi.uhp.presenters;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.KeyboardManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.IdentificationEvent;

import java.sql.SQLException;

/**
 * Created by michaelliang on 6/6/17.
 */

public class ClinicNumberFormPresenter {
    // Logic
    private IdentificationEvent mUnsavedIdentificationEvent;

    // Activity, context, and view
    private final Activity mActivity;
    private final Context mContext;
    private final View mView;
    private final NavigationManager mNavigationManager;

    // Elements in the view
    private EditText mClinicNumberView;
    private Button mSubmitButton;
    private RadioGroup mClinicNumberRadioGroup;


    public ClinicNumberFormPresenter(View view, Context context, NavigationManager navigationManager, Activity activity, IdentificationEvent unsavedIdentificationEvent) {
        mUnsavedIdentificationEvent = unsavedIdentificationEvent;

        // View
        mView = view;

        // Activity
        mActivity = activity;
        mContext = context;
        mNavigationManager = navigationManager;

        // Elements in the view
        mClinicNumberRadioGroup = (RadioGroup) view.findViewById(R.id.radio_group_clinic_number);
        mClinicNumberView = (EditText) view.findViewById(R.id.clinic_number_field);
        mSubmitButton = (Button) view.findViewById(R.id.clinic_number_save_button);
        mSubmitButton.setEnabled(false);

        // Setting Listeners in the view
        setListeners();

        // Set title on screen
        mActivity.setTitle(R.string.clinic_number_form_fragment_label);

        // Keyboard set up
        KeyboardManager.focusAndShowKeyboard(mClinicNumberView, mContext);
    }

    protected void setListeners() {
        mClinicNumberView.addTextChangedListener(
                createClinicNumberFormSubmitTextWatcher()
        );
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickSubmitButton();
            }
        });
    }

    protected void onClickSubmitButton() {
        // save opd number (database stuff)
        saveIdentification(getSelectedClinicNumberType(), getSelectedClinicNumber());

        // ui stuff
        displayIdentificationSuccessfulToast();

        // navigation stuff
        nagivateToCurrentPatientsFragment();
    }

    // Probably method not needed.
    protected ClinicNumberFormSubmitTextWatcher createClinicNumberFormSubmitTextWatcher() {
        return new ClinicNumberFormSubmitTextWatcher();
    }

    // Where should this go...?
    private class ClinicNumberFormSubmitTextWatcher implements TextWatcher {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            // This part is worth unit testing.
            @Override
            public void afterTextChanged(Editable s) {
                if (mClinicNumberView.getText().toString().isEmpty()) {
                    mSubmitButton.setEnabled(false);
                } else {
                    mSubmitButton.setEnabled(true);
                }
            }
    }

    // UI to value
    protected int getSelectedClinicNumber() {
        return Integer.valueOf(mClinicNumberView.getText().toString());
    }

    // UI to value
    protected IdentificationEvent.ClinicNumberTypeEnum getSelectedClinicNumberType() {
        RadioButton selectedRadioButton = (RadioButton) mClinicNumberRadioGroup.findViewById(mClinicNumberRadioGroup.getCheckedRadioButtonId());
        IdentificationEvent.ClinicNumberTypeEnum clinicNumberType =
                IdentificationEvent.ClinicNumberTypeEnum.valueOf(
                        selectedRadioButton.getText().toString().toUpperCase());
        return clinicNumberType;
    }

    // Activity
    protected String getAuthenticationTokenFromActivity() {
        // TODO: Feels super hacky, probably should create a base class for all presenters
        return ((ClinicActivity) mActivity).getAuthenticationToken();
    }

    protected void saveIdentification(IdentificationEvent.ClinicNumberTypeEnum clinicNumberType, Integer clinicNumber) {
        // Getting stuff from UI
        mUnsavedIdentificationEvent.setClinicNumberType(clinicNumberType);
        mUnsavedIdentificationEvent.setClinicNumber(clinicNumber);
        mUnsavedIdentificationEvent.setOccurredAt(Clock.getCurrentTime());
        mUnsavedIdentificationEvent.setAccepted(true);

        try {
            mUnsavedIdentificationEvent.saveChanges(getAuthenticationTokenFromActivity());
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
        }
    }

    protected void nagivateToCurrentPatientsFragment() {
        mNavigationManager.setCurrentPatientsFragment();
    }

    protected void displayIdentificationSuccessfulToast() {
        Toast.makeText(mContext,
                mUnsavedIdentificationEvent.getMember().getFullName() + " " + mContext.getString(R.string.identification_approved),
                Toast.LENGTH_LONG).
                show();
    }
}

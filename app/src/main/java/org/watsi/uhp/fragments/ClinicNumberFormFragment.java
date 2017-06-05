package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.KeyboardManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;

public class ClinicNumberFormFragment extends BaseFragment {
    private Member mMember;
    private IdentificationEvent.SearchMethodEnum mIdMethod;
    private String mVerificationTier;
    private float mVerificationConfidence;
    private Member mThroughMember;
    private RadioGroup mClinicNumberRadioGroup;
    private EditText mClinicNumberView;
    private Button mSubmitButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clinic_number_form, container, false);
        getActivity().setTitle(R.string.clinic_number_form_fragment_label);
        mMember = (Member) getArguments().getSerializable(NavigationManager.MEMBER_BUNDLE_FIELD);
        mThroughMember = (Member) getArguments().getSerializable(NavigationManager.THROUGH_MEMBER_BUNDLE_FIELD);
        mIdMethod = (IdentificationEvent.SearchMethodEnum) getArguments().getSerializable(NavigationManager.ID_METHOD_BUNDLE_FIELD);
        mVerificationConfidence = (float) getArguments().getSerializable(NavigationManager.VERIFICATION_CONFIDENCE_BUNDLE_FIELD);
        mVerificationTier = (String) getArguments().getSerializable(NavigationManager.VERIFICATION_TIER_BUNDLE_FIELD);

        view.findViewById(R.id.clinic_number_save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClinicNumberSubmissionSuccess();
            }
        });

        // set initial shit
        mClinicNumberRadioGroup = (RadioGroup) view.findViewById(R.id.radio_group_clinic_number);
        mClinicNumberView = (EditText) view.findViewById(R.id.clinic_number_field);
        mSubmitButton = (Button) view.findViewById(R.id.clinic_number_save_button);
        mSubmitButton.setEnabled(false);

        setTextChangedListener();
        KeyboardManager.focusAndShowKeyboard(mClinicNumberView, getContext());
        return view;
    }


    protected void onClinicNumberSubmissionSuccess() {
        // save opd number
        saveIdentification();
        // ui stuff
        successfulCheckin();
    }

    protected void saveIdentification() {
        // saves identification object
        IdentificationEvent idEvent = new IdentificationEvent();
        idEvent.setMember(mMember);
        idEvent.setSearchMethod(mIdMethod);
        idEvent.setThroughMember(mThroughMember);
        idEvent.setClinicNumberType(getSelectedClinicNumberType());
        idEvent.setClinicNumber(getSelectedClinicNumber());
        idEvent.setOccurredAt(Clock.getCurrentTime());
        idEvent.setAccepted(true);
        idEvent.setFingerprintsVerificationConfidence(mVerificationConfidence);
        if (mMember.getPhoto() == null) {
            idEvent.setPhotoVerified(false);
        }
        try {
            idEvent.saveChanges(getAuthenticationToken());
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
        }
    }


    protected void successfulCheckin() {
        getNavigationManager().setCurrentPatientsFragment();
        Toast.makeText(getContext(),
                mMember.getFullName() + " " + getString(R.string.identification_approved),
                Toast.LENGTH_LONG).
                show();
    }

    protected int getSelectedClinicNumber() {
        return Integer.valueOf(mClinicNumberView.getText().toString());
    }

    protected IdentificationEvent.ClinicNumberTypeEnum getSelectedClinicNumberType() {
        RadioButton selectedRadioButton = (RadioButton) mClinicNumberRadioGroup.findViewById(mClinicNumberRadioGroup.getCheckedRadioButtonId());
        IdentificationEvent.ClinicNumberTypeEnum clinicNumberType =
                IdentificationEvent.ClinicNumberTypeEnum.valueOf(
                        selectedRadioButton.getText().toString().toUpperCase());
        return clinicNumberType;
    }

    protected void setTextChangedListener() {
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

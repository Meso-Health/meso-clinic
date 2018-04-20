package org.watsi.uhp.fragments;

import android.view.View;
import android.widget.EditText;

import org.watsi.domain.entities.IdentificationEvent;
import org.watsi.domain.entities.Member;
import org.watsi.uhp.R;
import org.watsi.uhp.managers.LegacyNavigationManager;

public class EnrollmentContactInfoFragment extends FormFragment<Member> {

    private EditText mPhoneNumberView;
    private IdentificationEvent mIdEvent;

    @Override
    int getTitleLabelId() {
        return R.string.enrollment_contact_info_label;
    }

    @Override
    int getFragmentLayoutId() {
        return R.layout.fragment_enrollment_contact_info;
    }

    @Override
    public boolean isFirstStep() {
        return false;
    }

    @Override
    void setUpFragment(View view) {
        mPhoneNumberView = (EditText) view.findViewById(R.id.phone_number);
        mIdEvent = (IdentificationEvent) getArguments().getSerializable(LegacyNavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);
    }

    @Override
    public void nextStep() {
        mSyncableModel.setPhoneNumber(mPhoneNumberView.getText().toString());
        try {
            mSyncableModel.validatePhoneNumber();
            getNavigationManager().setEnrollmentFingerprintFragment(mSyncableModel, mIdEvent);
            mPhoneNumberView.setError(null);
        } catch (AbstractModel.ValidationException e) {
            String errorMessage = getString(R.string.phone_number_validation_error);
            mPhoneNumberView.setError(errorMessage);
        }
    }
}

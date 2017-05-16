package org.watsi.uhp.fragments;

import android.view.View;
import android.widget.EditText;

import org.watsi.uhp.R;
import org.watsi.uhp.models.AbstractModel;

public class EnrollmentContactInfoFragment extends EnrollmentFragment {

    private EditText mPhoneNumberView;

    @Override
    int getTitleLabelId() {
        return R.string.enrollment_contact_info_label;
    }

    @Override
    int getFragmentLayoutId() {
        return R.layout.fragment_enrollment_contact_info;
    }

    @Override
    boolean isLastStep() {
        return false;
    }

    @Override
    void setUpFragment(View view) {
        mPhoneNumberView = (EditText) view.findViewById(R.id.phone_number);
    }

    @Override
    void nextStep() {
        try {
            String phoneNumber = mPhoneNumberView.getText().toString();
            if (phoneNumber.isEmpty()) phoneNumber = null;
            mMember.setPhoneNumber(phoneNumber);
            getNavigationManager().setEnrollmentFingerprintFragment(mMember);
        } catch (AbstractModel.ValidationException e) {
            String errorMessage = getString(R.string.phone_number_validation_error);
            mPhoneNumberView.setError(errorMessage);
        }
    }
}

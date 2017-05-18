package org.watsi.uhp.fragments;

import android.view.View;
import android.widget.EditText;

import org.watsi.uhp.R;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.Member;

public class EnrollmentContactInfoFragment extends FormFragment<Member> {

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
    public boolean isFirstStep() {
        return false;
    }

    @Override
    void setUpFragment(View view) {
        mPhoneNumberView = (EditText) view.findViewById(R.id.phone_number);
    }

    @Override
    void nextStep(View view) {
        try {
            String phoneNumber = mPhoneNumberView.getText().toString();
            if (phoneNumber.isEmpty()) phoneNumber = null;
            mSyncableModel.setPhoneNumber(phoneNumber);
            getNavigationManager().setEnrollmentFingerprintFragment(mSyncableModel);
        } catch (AbstractModel.ValidationException e) {
            String errorMessage = getString(R.string.phone_number_validation_error);
            mPhoneNumberView.setError(errorMessage);
        }
    }
}

package org.watsi.uhp.fragments;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;

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
    void nextStep() {
        try {
            String phoneNumber = mPhoneNumberView.getText().toString();
            if (phoneNumber.isEmpty()) phoneNumber = null;
            mSyncableModel.setPhoneNumber(phoneNumber);
            try {
                MemberDao.update(mSyncableModel);
                getNavigationManager().setEnrollmentFingerprintFragment(mSyncableModel);
            } catch (SQLException e) {
                ExceptionManager.reportException(e);
                Toast.makeText(getContext(), "Failed to save contact information", Toast.LENGTH_LONG).show();
            }
        } catch (AbstractModel.ValidationException e) {
            String errorMessage = getString(R.string.phone_number_validation_error);
            mPhoneNumberView.setError(errorMessage);
        }
    }
}

package org.watsi.uhp.fragments;

import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.Member;

import java.util.Calendar;

public class EnrollNewbornInfoFragment extends EnrollmentFragment {

    private EditText mNameView;
    private EditText mCardIdView;
    private RadioGroup mRadioGroupView;
    private DatePicker mDatePicker;

    @Override
    int getTitleLabelId() {
        return R.string.enroll_newborn_info_label;
    }

    @Override
    int getFragmentLayoutId() {
        return R.layout.fragment_enroll_newborn;
    }

    @Override
    boolean isLastStep() {
        return false;
    }

    @Override
    void nextStep() {
        Member newborn = new Member();
        newborn.setHouseholdId(mMember.getHouseholdId());

        try {
            newborn.setFullName(mNameView.getText().toString());
        } catch (AbstractModel.ValidationException e) {
            mNameView.setError(getString(R.string.name_validation_error));
            return;
        }

        try {
            newborn.setCardId(mCardIdView.getText().toString());
        } catch (AbstractModel.ValidationException e) {
            mCardIdView.setError(getString(R.string.card_id_validation_error));
            return;
        }

        RadioButton selectedRadio =
                (RadioButton) mRadioGroupView.findViewById(mRadioGroupView.getCheckedRadioButtonId());
        if (selectedRadio == null) {
            RadioButton lastRadioButton = (RadioButton) mRadioGroupView.findViewById(R.id.female);
            lastRadioButton.setError(getString(R.string.gender_validation_error));
            return;
        }
        Member.GenderEnum gender = Member.GenderEnum.valueOf(
                selectedRadio.getText().toString().substring(0,1));
        newborn.setGender(gender);

        Calendar cal = Calendar.getInstance();
        cal.set(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
        newborn.setBirthdate(cal.getTime());
        newborn.setBirthdateAccuracy(Member.BirthdateAccuracyEnum.D);

        new NavigationManager(getActivity()).setEnrollNewbornPhotoFragment(newborn);
    }

    @Override
    void setUpFragment(View view) {
        mNameView = (EditText) view.findViewById(R.id.name);
        mCardIdView = (EditText) view.findViewById(R.id.card_id);
        mRadioGroupView = (RadioGroup) view.findViewById(R.id.gender_group);
        mDatePicker = (DatePicker) view.findViewById(R.id.birthdate);

        Calendar today = Calendar.getInstance();
        mDatePicker.updateDate(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
        );
        mDatePicker.setMaxDate(today.getTimeInMillis());

        view.findViewById(R.id.scan_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NavigationManager(getActivity())
                        .setBarcodeFragment(
                                BarcodeFragment.ScanPurposeEnum.NEWBORN, mMember, null);
            }
        });

        String mScannedCardId = getArguments().getString(
                NavigationManager.SCANNED_CARD_ID_BUNDLE_FIELD);
        if (mScannedCardId != null) {
            mCardIdView.getText().append(mScannedCardId);
        }
    }
}

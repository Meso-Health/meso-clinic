package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.Member;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EnrollNewbornInfoFragment extends FormFragment {

    private EditText mNameView;
    private EditText mCardIdView;
    private RadioGroup mRadioGroupView;
    private DatePicker mDatePicker;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy/MM/dd");

    @Override
    int getTitleLabelId() {
        return R.string.enroll_newborn_info_label;
    }

    @Override
    int getFragmentLayoutId() {
        return R.layout.fragment_enroll_newborn;
    }

    @Override
    public boolean isFirstStep() {
        return true;
    }

    @Override
    void nextStep() {
        Member newborn = mMember.createNewborn();

        Bundle attributeBundle = createBundle();

        try {
            newborn.setFullName(attributeBundle.getString(Member.FIELD_NAME_FULL_NAME));
        } catch (AbstractModel.ValidationException e) {
            mNameView.setError(getString(R.string.name_validation_error));
            return;
        }

        String genderString = attributeBundle.getString(Member.FIELD_NAME_GENDER);
        if (genderString == null) {
            RadioButton lastRadioButton = (RadioButton) mRadioGroupView.findViewById(R.id.female);
            lastRadioButton.setError(getString(R.string.gender_validation_error));
            return;
        }
        newborn.setGender(Member.GenderEnum.valueOf(genderString));

        Date birthdate;
        try {
            birthdate = mDateFormat.parse(attributeBundle.getString(Member.FIELD_NAME_BIRTHDATE));
        } catch (ParseException e) {
            ExceptionManager.reportException(e);
            Toast.makeText(getContext(), R.string.birthdate_validation_error, Toast.LENGTH_LONG).show();
            return;
        }

        try {
            newborn.setCardId(mCardIdView.getText().toString());
        } catch (AbstractModel.ValidationException e) {
            mCardIdView.setError(getString(R.string.card_id_validation_error));
            return;
        }

        newborn.setBirthdate(birthdate);

        getNavigationManager().setEnrollNewbornPhotoFragment(newborn);
    }

    @Override
    void setUpFragment(View view) {
        mNameView = (EditText) view.findViewById(R.id.name);
        mCardIdView = (EditText) view.findViewById(R.id.card_id);
        mRadioGroupView = (RadioGroup) view.findViewById(R.id.gender_group);
        mDatePicker = (DatePicker) view.findViewById(R.id.birthdate);

        Bundle sourceParams = getArguments().getBundle(NavigationManager.SOURCE_PARAMS_BUNDLE_FIELD);
        if (sourceParams == null) sourceParams = new Bundle();

        String name = sourceParams.getString(Member.FIELD_NAME_FULL_NAME);
        if (name != null) {
            mNameView.getText().append(name);
        }

        String genderString = sourceParams.getString(Member.FIELD_NAME_GENDER);
        if (genderString != null) {
            Member.GenderEnum gender = Member.GenderEnum.valueOf(genderString);
            if (gender.equals(Member.GenderEnum.M)) {
                ((RadioButton) view.findViewById(R.id.male)).toggle();
            } else {
                ((RadioButton) view.findViewById(R.id.female)).toggle();
            }
        }

        Calendar today = Calendar.getInstance();
        mDatePicker.setMaxDate(today.getTimeInMillis());

        String birthdateString = sourceParams.getString(Member.FIELD_NAME_BIRTHDATE);
        if (birthdateString != null) {
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(mDateFormat.parse(birthdateString));
                mDatePicker.updateDate(
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                );
            } catch (ParseException e) {
                ExceptionManager.reportException(e);
            }
        } else {
            mDatePicker.updateDate(
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH)
            );
        }

        view.findViewById(R.id.scan_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationManager().setBarcodeFragment(
                        BarcodeFragment.ScanPurposeEnum.NEWBORN, mMember, createBundle());
            }
        });

        String mScannedCardId = getArguments().getString(
                NavigationManager.SCANNED_CARD_ID_BUNDLE_FIELD);
        if (mScannedCardId != null) {
            mCardIdView.getText().append(mScannedCardId);
        }
    }

    private Bundle createBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(Member.FIELD_NAME_FULL_NAME, mNameView.getText().toString());

        RadioButton selectedRadio =
                (RadioButton) mRadioGroupView.findViewById(mRadioGroupView.getCheckedRadioButtonId());
        if (selectedRadio != null) {
            bundle.putString(
                    Member.FIELD_NAME_GENDER,
                    selectedRadio.getText().toString().substring(0,1)
            );
        }

        Calendar cal = Calendar.getInstance();
        cal.set(mDatePicker.getYear(), mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
        bundle.putString(Member.FIELD_NAME_BIRTHDATE, mDateFormat.format(cal.getTime()));

        return bundle;
    }
}

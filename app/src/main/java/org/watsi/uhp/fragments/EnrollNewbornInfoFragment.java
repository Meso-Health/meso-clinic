package org.watsi.uhp.fragments;

import android.databinding.DataBindingUtil;
import android.view.View;
import android.widget.DatePicker;

import org.watsi.uhp.R;
import org.watsi.uhp.databinding.FragmentEnrollNewbornBinding;
import org.watsi.uhp.listeners.SetBarcodeFragmentListener;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.view_models.EnrollNewbornViewModel;

import java.util.Calendar;

public class EnrollNewbornInfoFragment extends FormFragment<Member> {

    private IdentificationEvent mIdEvent;
    private View mView;

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
    public void nextStep() {
        getNavigationManager().setEnrollNewbornPhotoFragment(mSyncableModel, mIdEvent);
    }

    @Override
    void setUpFragment(View view) {
        mView = view;

        FragmentEnrollNewbornBinding binding = DataBindingUtil.bind(view);
        EnrollNewbornViewModel enrollNewbornViewModel = new EnrollNewbornViewModel(this, mSyncableModel);
        binding.setMember(enrollNewbornViewModel);


        mIdEvent = (IdentificationEvent) getArguments().getSerializable(NavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);
        setUpDatePicker();
        setUpScanCardListener();
    }

    void setUpScanCardListener() {
        mView.findViewById(R.id.scan_card).setOnClickListener(new SetBarcodeFragmentListener(
                getNavigationManager(), BarcodeFragment.ScanPurposeEnum.NEWBORN,
                mSyncableModel, mIdEvent));
    }


    void setUpDatePicker() {
        DatePicker datePicker = (DatePicker) mView.findViewById(R.id.birthdate);

        Calendar cal = Calendar.getInstance();
        datePicker.setMaxDate(cal.getTimeInMillis());
        if (mSyncableModel.getBirthdate() != null) {
            cal.setTime(mSyncableModel.getBirthdate());
        }

        datePicker.init(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH),
                new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                        Calendar cal = Calendar.getInstance();
                        cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                        mSyncableModel.setBirthdate(cal.getTime());
                    }
                }
        );
    }
}

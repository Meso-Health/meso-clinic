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

import static org.watsi.uhp.helpers.DateTimeHelper.makeCalendarThreeMonthsAgo;
import static org.watsi.uhp.helpers.DateTimeHelper.makeCalendarTomorrow;
import static org.watsi.uhp.helpers.DateTimeHelper.makeCalendarToday;

public class EnrollNewbornInfoFragment extends FormFragment<Member> {

    private IdentificationEvent mIdEvent;
    private View mView;
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
        mDatePicker = (DatePicker) mView.findViewById(R.id.birthdate);

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
        Calendar cal = makeCalendarToday();
        if (mSyncableModel.getBirthdate() != null) {
            cal.setTime(mSyncableModel.getBirthdate());
        } else {
            mSyncableModel.setBirthdate(cal.getTime());
        }

        mDatePicker.init(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH),
                new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                        Calendar cal = makeCalendarToday();
                        cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                        mSyncableModel.setBirthdate(cal.getTime());
                    }
                }
        );
        // This is a policy decision we made during IPM so only newborns born within the last
        // three months can be enrolled via this flow.
        mDatePicker.setMinDate(makeCalendarThreeMonthsAgo().getTimeInMillis());
        mDatePicker.setMaxDate(makeCalendarTomorrow().getTimeInMillis());
    }

    public DatePicker getDatePicker() {
        return mDatePicker;
    }
}

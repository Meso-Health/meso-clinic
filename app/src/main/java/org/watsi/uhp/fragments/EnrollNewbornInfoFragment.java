package org.watsi.uhp.fragments;

import android.databinding.DataBindingUtil;
import android.view.View;
import android.widget.DatePicker;

import org.watsi.domain.entities.IdentificationEvent;
import org.watsi.domain.entities.Member;
import org.watsi.uhp.R;
import org.watsi.uhp.custom_components.NewbornBirthdatePicker;
import org.watsi.uhp.databinding.FragmentEnrollNewbornBinding;
import org.watsi.uhp.listeners.SetBarcodeFragmentListener;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.view_models.EnrollNewbornViewModel;

public class EnrollNewbornInfoFragment extends FormFragment<Member> {

    private IdentificationEvent mIdEvent;
    private View mView;
    private NewbornBirthdatePicker mNewbornBirthdatePicker;

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
        FragmentEnrollNewbornBinding binding = DataBindingUtil.bind(view);
        EnrollNewbornViewModel enrollNewbornViewModel = new EnrollNewbornViewModel(this, mSyncableModel);
        binding.setMember(enrollNewbornViewModel);

        mView = view;
        mIdEvent = (IdentificationEvent) getArguments().getSerializable(NavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);
        mNewbornBirthdatePicker = new NewbornBirthdatePicker((DatePicker) mView.findViewById(R.id.birthdate), mSyncableModel);
        setUpScanCardListener();
    }

    void setUpScanCardListener() {
        mView.findViewById(R.id.scan_card).setOnClickListener(new SetBarcodeFragmentListener(
                getNavigationManager(), BarcodeFragment.ScanPurposeEnum.NEWBORN,
                mSyncableModel, mIdEvent));
    }
}

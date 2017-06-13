package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.presenters.ClinicNumberFormPresenter;

public class ClinicNumberFormFragment extends BaseFragment {
    private ClinicNumberFormPresenter clinicNumberFormPresenter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clinic_number_form, container, false);
        IdentificationEvent unsavedIdentificationEvent = (IdentificationEvent) getArguments().getSerializable(NavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);
        clinicNumberFormPresenter = new ClinicNumberFormPresenter(view, getContext(), getNavigationManager(), getActivity(), unsavedIdentificationEvent);
        return view;
    }
}

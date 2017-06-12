package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.KeyboardManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.presenters.ClinicNumberFormPresenter;

import java.sql.SQLException;

public class ClinicNumberFormFragment extends BaseFragment {
    // Model stuff
    private ClinicNumberFormPresenter clinicNumberFormPresenter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clinic_number_form, container, false);

        // grabbing from NavigationManager the arguments
        IdentificationEvent unsavedIdentificationEvent = (IdentificationEvent) getArguments().getSerializable(NavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);

        // setting the presenter.
        clinicNumberFormPresenter = new ClinicNumberFormPresenter(view, getContext(), getNavigationManager(), getActivity(), unsavedIdentificationEvent);

        return view;
    }
}

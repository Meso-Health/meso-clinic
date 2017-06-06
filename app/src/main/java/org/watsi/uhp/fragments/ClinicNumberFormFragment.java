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
        Member member = (Member) getArguments().getSerializable(NavigationManager.MEMBER_BUNDLE_FIELD);
        Member throughMember = (Member) getArguments().getSerializable(NavigationManager.THROUGH_MEMBER_BUNDLE_FIELD);
        IdentificationEvent.SearchMethodEnum idMethod = (IdentificationEvent.SearchMethodEnum) getArguments().getSerializable(NavigationManager.ID_METHOD_BUNDLE_FIELD);
        float verificationConfidence = (float) getArguments().getSerializable(NavigationManager.VERIFICATION_CONFIDENCE_BUNDLE_FIELD);
        String verificationTier = (String) getArguments().getSerializable(NavigationManager.VERIFICATION_TIER_BUNDLE_FIELD);
        int simprintsResultCode = (Integer) getArguments().getSerializable(NavigationManager.SIMPRINTS_RESULT_CODE_BUNDLE_FIELD);

        // setting the presenter.
        clinicNumberFormPresenter = new ClinicNumberFormPresenter(view, getContext(), getNavigationManager(), getActivity(), member, throughMember, idMethod, verificationTier, verificationConfidence, simprintsResultCode);

        return view;
    }
}

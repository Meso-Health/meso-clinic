package org.watsi.uhp.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.simprints.libsimprints.Verification;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.presenters.DetailPresenter;

import java.sql.SQLException;

public class ClinicNumberFormFragment extends BaseFragment {
    private Member mMember;
    private IdentificationEvent.SearchMethodEnum mIdMethod;
    private String mVerificationTier;
    private float mVerificationScore;
    private Member mThroughMember;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clinic_number_form, container, false);
        getActivity().setTitle(R.string.clinic_number_form_fragment_label);
        mMember = (Member) getArguments().getSerializable(NavigationManager.MEMBER_BUNDLE_FIELD);
        mThroughMember = (Member) getArguments().getSerializable(NavigationManager.THROUGH_MEMBER_BUNDLE_FIELD);
        mIdMethod = (IdentificationEvent.SearchMethodEnum) getArguments().getSerializable(NavigationManager.ID_METHOD_BUNDLE_FIELD);
        mVerificationScore = (float) getArguments().getSerializable(NavigationManager.VERIFICATION_CONFIDENCE_BUNDLE_FIELD);
        mVerificationTier = (String) getArguments().getSerializable(NavigationManager.VERIFICATION_TIER_BUNDLE_FIELD);

        view.findViewById(R.id.clinic_number_save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO put code to save the identification here.
                getNavigationManager().setCurrentPatientsFragment();
                Toast.makeText(getContext(),
                        mMember.getFullName() + " " + getString(R.string.identification_approved),
                        Toast.LENGTH_LONG).
                        show();
            }
        });
        return view;
    }

    public void dialogOnBackOrUpNavigation() {
        new android.app.AlertDialog.Builder(getActivity())
                .setTitle(R.string.exit_form_alert)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        getNavigationManager().setDetailFragment(mMember, mIdMethod, mThroughMember);
                    }
                }).create().show();
    }
}

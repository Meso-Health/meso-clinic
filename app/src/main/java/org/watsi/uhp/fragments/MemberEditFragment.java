package org.watsi.uhp.fragments;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.databinding.FragmentMemberEditBinding;
import org.watsi.uhp.listeners.SetBarcodeFragmentListener;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.view_models.MemberEditViewModel;

import java.sql.SQLException;

public class MemberEditFragment extends FormFragment<Member> {

    @Override
    int getTitleLabelId() {
        return R.string.member_edit_label;
    }

    @Override
    int getFragmentLayoutId() {
        return R.layout.fragment_member_edit;
    }

    @Override
    public boolean isFirstStep() {
        return true;
    }

    @Override
    public void nextStep() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.member_edit_confirmation);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String toastMessage = mSyncableModel.getFullName() + "'s information has been updated.";
                try {
                    mSyncableModel.saveChanges(getAuthenticationToken());
                } catch (SQLException | AbstractModel.ValidationException e) {
                    ExceptionManager.reportException(e, "Failed to save changes to a member that has invalid fields. Member is: " + mSyncableModel.getJson());
                    toastMessage = "Failed to update the member information.";
                }
                IdentificationEvent idEvent = (IdentificationEvent) getArguments().getSerializable(NavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);
                if (idEvent != null) {
                    getNavigationManager().setMemberDetailFragment(mSyncableModel, idEvent);
                } else {
                    getNavigationManager().setMemberDetailFragment(mSyncableModel);
                }
                Toast.makeText(getContext(), toastMessage, Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    void setUpFragment(View view) {
        FragmentMemberEditBinding binding = DataBindingUtil.bind(view);
        MemberEditViewModel memberEditFragmentMemberView = new MemberEditViewModel(this, mSyncableModel);
        binding.setMember(memberEditFragmentMemberView);

        IdentificationEvent idEvent = (IdentificationEvent) getArguments().getSerializable(NavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);

        view.findViewById(R.id.scan_card).setOnClickListener(new SetBarcodeFragmentListener(
                getNavigationManager(), BarcodeFragment.ScanPurposeEnum.MEMBER_EDIT,
                mSyncableModel, idEvent));
    }
}

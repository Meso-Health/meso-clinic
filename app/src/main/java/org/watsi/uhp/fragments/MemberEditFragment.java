package org.watsi.uhp.fragments;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import org.watsi.domain.entities.IdentificationEvent;
import org.watsi.domain.entities.Member;
import org.watsi.domain.repositories.MemberRepository;
import org.watsi.uhp.R;
import org.watsi.uhp.databinding.FragmentMemberEditBinding;
import org.watsi.uhp.listeners.SetBarcodeFragmentListener;
import org.watsi.uhp.managers.LegacyNavigationManager;
import org.watsi.uhp.view_models.MemberEditViewModel;

import javax.inject.Inject;

public class MemberEditFragment extends FormFragment<Member> {

    @Inject
    MemberRepository memberRepository;

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
                String toastMessage = mSyncableModel.getName() + "'s information has been updated.";
                memberRepository.save(mSyncableModel);
                IdentificationEvent idEvent = (IdentificationEvent) getArguments().getSerializable(LegacyNavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);
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

        IdentificationEvent idEvent = (IdentificationEvent) getArguments().getSerializable(LegacyNavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);

        view.findViewById(R.id.scan_card).setOnClickListener(new SetBarcodeFragmentListener(
                getNavigationManager(), BarcodeFragment.ScanPurposeEnum.MEMBER_EDIT,
                mSyncableModel, idEvent));
    }
}

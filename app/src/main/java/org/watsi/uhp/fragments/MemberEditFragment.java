package org.watsi.uhp.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;

public class MemberEditFragment extends FormFragment<Member> {

    private EditText nameView;
    private EditText cardIdView;
    private EditText phoneNumView;

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
    void nextStep(View view) {
        if (valid(nameView, cardIdView, phoneNumView)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(R.string.member_edit_confirmation);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String toastMessage = mSyncableModel.getFullName() + "'s information has been updated.";
                    try {
                        mSyncableModel.saveChanges(getAuthenticationToken());
                    } catch (SQLException e) {
                        ExceptionManager.reportException(e);
                        toastMessage = "Failed to update the member information.";
                    }

                    IdentificationEvent idEvent = (IdentificationEvent) getArguments().getSerializable(NavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);
                    if (idEvent != null) {
                        getNavigationManager().setCheckInMemberDetailFragment((Member) mSyncableModel, idEvent);
                    } else {
                        getNavigationManager().setCurrentMemberDetailFragment((Member) mSyncableModel);
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
    }

    @Override
    void setUpFragment(View view) {
        nameView = (EditText) view.findViewById(R.id.member_name);
        nameView.getText().append(mSyncableModel.getFullName());

        cardIdView = (EditText) view.findViewById(R.id.card_id);
        String mScannedCardId = getArguments().getString(
                NavigationManager.SCANNED_CARD_ID_BUNDLE_FIELD);
        if (mScannedCardId != null) {
            cardIdView.getText().append(mScannedCardId);
        } else if (mSyncableModel.getCardId() != null) {
            cardIdView.getText().append(mSyncableModel.getCardId());
        }

        phoneNumView = (EditText) view.findViewById(R.id.phone_number);
        if (mSyncableModel.getPhoneNumber() != null) {
            phoneNumView.getText().append(mSyncableModel.getPhoneNumber());
        }

        view.findViewById(R.id.scan_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                String idMethodString =
                        getArguments().getString(NavigationManager.ID_METHOD_BUNDLE_FIELD);
                bundle.putString(NavigationManager.ID_METHOD_BUNDLE_FIELD, idMethodString);
                getNavigationManager().setBarcodeFragment(
                        BarcodeFragment.ScanPurposeEnum.MEMBER_EDIT, mSyncableModel, bundle);
            }
        });
    }

    private boolean valid(EditText nameView, EditText cardIdView, EditText phoneNumView) {
        boolean valid = true;

        try {
            mSyncableModel.setFullName(nameView.getText().toString());
        } catch (AbstractModel.ValidationException e) {
            nameView.setError(getString(R.string.name_validation_error));
            valid = false;
        }

        try {
            mSyncableModel.setCardId(cardIdView.getText().toString());
        } catch (AbstractModel.ValidationException e) {
            cardIdView.setError(getString(R.string.card_id_validation_error));
            valid = false;
        }

        try {
            String updatedPhoneNumber = phoneNumView.getText().toString();
            if (updatedPhoneNumber.isEmpty()) updatedPhoneNumber = null;
            mSyncableModel.setPhoneNumber(updatedPhoneNumber);
        } catch (AbstractModel.ValidationException e) {
            phoneNumView.setError(getString(R.string.phone_number_validation_error));
            valid = false;
        }

        return valid;
    }
}

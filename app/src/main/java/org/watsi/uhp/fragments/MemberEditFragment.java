package org.watsi.uhp.fragments;

import android.content.DialogInterface;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.watsi.uhp.BR;
import org.watsi.uhp.R;
import org.watsi.uhp.databinding.FragmentMemberEditBinding;
import org.watsi.uhp.listeners.SetBarcodeFragmentListener;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

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
    void nextStep(View view) {
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
        MemberEditFragmentMemberView memberEditFragmentMemberView = new MemberEditFragmentMemberView(mSyncableModel);
        binding.setMember(memberEditFragmentMemberView);

        Bundle bundle = new Bundle();
        bundle.putString(NavigationManager.ID_METHOD_BUNDLE_FIELD,
                getArguments().getString(NavigationManager.ID_METHOD_BUNDLE_FIELD));
        bundle.putSerializable(NavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD,
                getArguments().getSerializable(NavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD));
        view.findViewById(R.id.scan_card).setOnClickListener(new SetBarcodeFragmentListener(
                getNavigationManager(), BarcodeFragment.ScanPurposeEnum.MEMBER_EDIT,
                mSyncableModel, bundle));
    }

    public class MemberEditFragmentMemberView extends BaseObservable {
        private Member mMember;
        private String fullName;
        private String fullNameError;
        private String phoneNumber;
        private String phoneNumberError;
        private String cardId;
        private String cardIdError;
        private boolean saveEnabled;

        public MemberEditFragmentMemberView(Member member) {
            mMember = member;

            fullName = member.getFullName();
            fullNameError = null;
            phoneNumber = member.getPhoneNumber();
            phoneNumberError = null;
            cardId = member.getCardId();
            cardIdError = null;

            updateSaveButton();
        }

        @Bindable
        public String getFullName() {
            return fullName;
        }

        @Bindable
        public String getFullNameError() {
            return fullNameError;
        }

        @Bindable
        public String getPhoneNumber() {
            return phoneNumber;
        }

        @Bindable
        public String getPhoneNumberError() {
            return phoneNumberError;
        }

        @Bindable
        public String getCardId() {
            return cardId;
        }

        @Bindable
        public String getCardIdError() {
            return cardIdError;
        }

        @Bindable
        public boolean getSaveEnabled() {
            return saveEnabled;
        }

        @Bindable
        public void setFullName(String fullName) {
            this.fullName = fullName;
            notifyPropertyChanged(BR.fullName);
            validateFullName();
            try {
                mMember.setFullName(fullName);
            } catch (AbstractModel.ValidationException e) {
                ExceptionManager.reportException(e);
            }
        }

        @Bindable
        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            notifyPropertyChanged(BR.phoneNumber);
            try {
                mMember.setPhoneNumber(phoneNumber);
            } catch (AbstractModel.ValidationException e) {
                ExceptionManager.reportException(e);
            }
        }

        public void onClickSave() {
            if (validateEverything()) {
                nextStep(getView());
            }
        }

        private boolean validateEverything() {
            if (validateFullName() && validatePhoneNumber() && validateCardId()) {
                saveEnabled = true;
                return true;
            } else {
                saveEnabled = false;
                return false;
            }
        }

        private void updateSaveButton() {
            Log.i("UHP-oman", "updateSaveButton");
            if (validFullName() && validPhoneNumber() && validCardId()) {
                saveEnabled = true;
            } else {
                Log.i("UHP-oman", "aww oman is not a country");
                saveEnabled = false;
            }
            notifyPropertyChanged(BR.saveEnabled);
        }

        private boolean validPhoneNumber() {
            return phoneNumber == null || phoneNumber.isEmpty() || Member.validPhoneNumber(phoneNumber);
        }

        private boolean validCardId() {
            return Member.validCardId(cardId);
        }

        private boolean validFullName() {
            return fullName != null && !fullName.isEmpty();
        }

        private boolean validateFullName() {
            boolean success = true;
            if (validFullName()) {
                this.fullNameError = null;
            } else {
                this.fullNameError = getString(R.string.name_validation_error);
                success = false;
            }
            notifyPropertyChanged(BR.fullNameError);
            updateSaveButton();
            return success;
        }

        private boolean validatePhoneNumber() {
            boolean success = true;
            if (validPhoneNumber()) {
                this.phoneNumberError = null;
            } else {
                this.phoneNumberError = getString(R.string.phone_number_validation_error);
                success = true;
            }
            notifyPropertyChanged(BR.phoneNumberError);
            updateSaveButton();
            return success;
        }

        private boolean validateCardId() {
            boolean success = true;
            if (validCardId()) {
                this.cardIdError = null;
            } else {
                this.cardIdError = getString(R.string.card_id_validation_error);
                success = false;
            }
            notifyPropertyChanged(BR.cardIdError);
            updateSaveButton();
            return success;
        }
    }
}

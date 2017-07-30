package org.watsi.uhp.view_models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.v4.app.Fragment;

import org.watsi.uhp.BR;
import org.watsi.uhp.R;
import org.watsi.uhp.fragments.FormFragment;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.Member;

/**
 * Created by michaelliang on 7/30/17.
 */

public class MemberEditViewModel extends BaseObservable {
    private Member mMember;
    private FormFragment mFormFragment;
    private String fullName;
    private String fullNameError;
    private String phoneNumber;
    private String phoneNumberError;
    private String cardId;
    private String cardIdError;
    private boolean saveEnabled;

    public MemberEditViewModel(FormFragment<Member> formFragment, Member member) {
        mFormFragment = formFragment;
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
    public String getFullName() { return fullName; }

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
        updateSaveButton();
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
            mFormFragment.nextStep(mFormFragment.getView());
        }
    }

    private boolean validateEverything() {
        return validateFullName() && validatePhoneNumber() && validateCardId();
    }

    private void updateSaveButton() {
        saveEnabled = validFullName() && validPhoneNumber() && validCardId();
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
            this.fullNameError = mFormFragment.getString(R.string.name_validation_error);
            success = false;
        }
        notifyPropertyChanged(BR.fullNameError);
        return success;
    }

    private boolean validatePhoneNumber() {
        boolean success = true;
        if (validPhoneNumber()) {
            this.phoneNumberError = null;
        } else {
            this.phoneNumberError = mFormFragment.getString(R.string.phone_number_validation_error);
            success = false;
        }
        notifyPropertyChanged(BR.phoneNumberError);
        return success;
    }

    private boolean validateCardId() {
        boolean success = true;
        if (validCardId()) {
            this.cardIdError = null;
        } else {
            this.cardIdError = mFormFragment.getString(R.string.card_id_validation_error);
            success = false;
        }
        notifyPropertyChanged(BR.cardIdError);
        return success;
    }
}

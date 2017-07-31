package org.watsi.uhp.view_models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.IdRes;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.watsi.uhp.BR;
import org.watsi.uhp.R;
import org.watsi.uhp.fragments.FormFragment;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.Member;

public abstract class MemberViewModel extends BaseObservable {
    private Member mMember;
    private FormFragment mFormFragment;
    private String fullName;
    private String fullNameError;
    private String phoneNumber;
    private String phoneNumberError;
    private String cardId;
    private String cardIdError;
    private boolean saveEnabled;

    public MemberViewModel(FormFragment<Member> formFragment, Member member) {
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

    public abstract void updateSaveButton();

    public abstract void onClickSave();

    public Member getMember() {
        return mMember;
    }

    public FormFragment getFormFragment() { return mFormFragment; }

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

    @Bindable
    public boolean getIsMale() { return getMember().getGender() == Member.GenderEnum.M; }

    @Bindable
    public boolean getIsFemale() { return getMember().getGender() == Member.GenderEnum.F; }

    public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
        RadioButton selectedRadio =
                (RadioButton) radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
        if (selectedRadio.getId() == (R.id.male)) {
            getMember().setGender(Member.GenderEnum.M);
            notifyPropertyChanged(BR.isMale);
            notifyPropertyChanged(BR.isFemale);
        } else {
            getMember().setGender(Member.GenderEnum.F);
            notifyPropertyChanged(BR.isMale);
            notifyPropertyChanged(BR.isFemale);
        }

        updateSaveButton();
    }

    boolean validPhoneNumber() {
        return phoneNumber == null || phoneNumber.isEmpty() || Member.validPhoneNumber(phoneNumber);
    }

    boolean validGender() { return getMember().getGender() != null; }

    boolean validCardId() {
        return Member.validCardId(cardId);
    }

    boolean validFullName() {
        return fullName != null && !fullName.isEmpty();
    }

    boolean validateFullName() {
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

    boolean validatePhoneNumber() {
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

    boolean validateCardId() {
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

    void setSaveEnabled(boolean saveEnabled) {
        this.saveEnabled = saveEnabled;
        notifyPropertyChanged(BR.saveEnabled);
    }
}

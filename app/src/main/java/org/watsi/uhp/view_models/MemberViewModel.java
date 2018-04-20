package org.watsi.uhp.view_models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.IdRes;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.watsi.domain.entities.Member;
import org.watsi.uhp.BR;
import org.watsi.uhp.R;
import org.watsi.uhp.fragments.FormFragment;

public abstract class MemberViewModel extends BaseObservable {
    // Putting these variables here so that unit tests can not depend on android system.
    public static final String PHONE_NUMBER_VALIDATION_ERROR = "Invalid phone number. Phone number must be blank, a 9 digit number, or a 10 digit number starting with 0.";
    public static final String CARD_ID_VALIDATION_ERROR = "Invalid card ID. ID must be 3 letters followed by 6 numbers.";
    public static final String NAME_VALIDATION_ERROR = "Name cannot be blank.";
    private final Member mMember;
    private final FormFragment mFormFragment;

    private String fullNameError;
    private String phoneNumberError;
    private String cardIdError;
    private String genderError;
    private boolean saveEnabled;

    public MemberViewModel(FormFragment<Member> formFragment, Member member) {
        mFormFragment = formFragment;
        mMember = member;

        fullNameError = null;
        phoneNumberError = null;
        cardIdError = null;
        genderError = null;

        setUpViewModel();
    }
    public abstract void setUpViewModel();

    public abstract void updateSaveButton();

    public abstract void onClickSave();

    public Member getMember() {
        return mMember;
    }

    public FormFragment getFormFragment() { return mFormFragment; }

    @Bindable
    public String getFullName() { return mMember.getFullName(); }

    @Bindable
    public String getFullNameError() {
        return fullNameError;
    }

    @Bindable
    public String getPhoneNumber() { return mMember.getPhoneNumber(); }

    @Bindable
    public String getPhoneNumberError() {
        return phoneNumberError;
    }

    @Bindable
    public String getCardId() {
        return mMember.getCardId();
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
    public String getGenderError() { return genderError; }

    @Bindable
    public void setFullName(String fullName) {
        mMember.setFullName(fullName);
        notifyPropertyChanged(BR.fullName);
        validateFullName();
        updateSaveButton();
    }

    @Bindable
    public void setPhoneNumber(String phoneNumber) {
        mMember.setPhoneNumber(phoneNumber);
        notifyPropertyChanged(BR.phoneNumber);
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

    boolean validateFullName() {
        boolean success = true;
        if (mMember.validFullName()) {
            this.fullNameError = null;
        } else {
            this.fullNameError = NAME_VALIDATION_ERROR;
            success = false;
        }
        notifyPropertyChanged(BR.fullNameError);
        return success;
    }

    boolean validatePhoneNumber() {
        boolean success = true;
        if (mMember.validPhoneNumber()) {
            this.phoneNumberError = null;
        } else {
            this.phoneNumberError = PHONE_NUMBER_VALIDATION_ERROR;
            success = false;
        }
        notifyPropertyChanged(BR.phoneNumberError);
        return success;
    }

    boolean validateCardId() {
        boolean success = true;
        if (mMember.validCardId()) {
            this.cardIdError = null;
        } else {
            this.cardIdError = CARD_ID_VALIDATION_ERROR;
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

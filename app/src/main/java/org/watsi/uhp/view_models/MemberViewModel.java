package org.watsi.uhp.view_models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.IdRes;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.watsi.uhp.BR;
import org.watsi.uhp.R;
import org.watsi.uhp.fragments.FormFragment;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.Member;

public abstract class MemberViewModel extends BaseObservable {
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

        updateSaveButton();
    }

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

    boolean validateGender() {
        boolean success = true;
        try {
            mMember.validateGender();
            this.genderError = null;
        } catch (AbstractModel.ValidationException e) {
            this.genderError = mFormFragment.getString(R.string.gender_validation_error);
            success = false;
        }
        notifyPropertyChanged(BR.genderError);
        return success;
    }

    boolean validateFullName() {
        boolean success = true;
        try {
            mMember.validateFullName();
            this.fullNameError = null;
        } catch (AbstractModel.ValidationException e) {
            this.fullNameError = mFormFragment.getString(R.string.name_validation_error);
            success = false;
        }
        notifyPropertyChanged(BR.fullNameError);
        return success;
    }

    boolean validatePhoneNumber() {
        boolean success = true;
        try {
            mMember.validatePhoneNumber();
            this.phoneNumberError = null;
        } catch (AbstractModel.ValidationException e) {
            this.phoneNumberError = mFormFragment.getString(R.string.phone_number_validation_error);
            success = false;
        }
        notifyPropertyChanged(BR.phoneNumberError);
        return success;
    }

    boolean validateCardId() {
        boolean success = true;
        try {
            mMember.validateCardId();
            this.cardIdError = null;
        } catch (AbstractModel.ValidationException e) {
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

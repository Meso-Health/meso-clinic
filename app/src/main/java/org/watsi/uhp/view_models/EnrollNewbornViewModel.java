package org.watsi.uhp.view_models;

import android.databinding.Bindable;
import android.support.annotation.IdRes;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.watsi.uhp.BR;
import org.watsi.uhp.R;
import org.watsi.uhp.fragments.FormFragment;
import org.watsi.uhp.models.Member;

public class EnrollNewbornViewModel extends MemberEditViewModel {

    public EnrollNewbornViewModel(FormFragment<Member> formFragment, Member member) {
        super(formFragment, member);
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

    public void onClickSave() {
        if (validateEverything()) {
            getFormFragment().nextStep(getFormFragment().getView());
        }
    }

    boolean validGender() { return getMember().getGender() != null; }

    void updateSaveButton() {
        setSaveEnabled(validFullName() && validPhoneNumber() && validCardId() && validGender());
    }
}

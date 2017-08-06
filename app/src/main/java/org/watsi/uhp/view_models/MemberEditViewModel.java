package org.watsi.uhp.view_models;

import org.watsi.uhp.fragments.FormFragment;
import org.watsi.uhp.models.Member;

public class MemberEditViewModel extends MemberViewModel {

    public MemberEditViewModel(FormFragment<Member> formFragment, Member member) {
        super(formFragment, member);
    }

    public void updateSaveButton() {
        setSaveEnabled(validateFullName() && validateCardId());
    }

    public void onClickSave() {
        if (validateFullName() && validatePhoneNumber() && validateCardId()) {
            getFormFragment().nextStep();
        }
    }
}
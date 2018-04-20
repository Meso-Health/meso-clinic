package org.watsi.uhp.view_models;

import org.watsi.domain.entities.Member;
import org.watsi.uhp.fragments.FormFragment;

public class MemberEditViewModel extends MemberViewModel {

    public MemberEditViewModel(FormFragment<Member> formFragment, Member member) {
        super(formFragment, member);
    }

    @Override
    public void setUpViewModel() {
        updateSaveButton();
    }

    public void updateSaveButton() {
        setSaveEnabled(getMember().validFullName() && getMember().validCardId());
    }

    public void onClickSave() {
        if (validateFullName() && validatePhoneNumber() && validateCardId()) {
            getFormFragment().nextStep();
        }
    }
}

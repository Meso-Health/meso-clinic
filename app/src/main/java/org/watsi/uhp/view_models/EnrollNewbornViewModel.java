package org.watsi.uhp.view_models;

import org.watsi.uhp.fragments.FormFragment;
import org.watsi.uhp.models.Member;

public class EnrollNewbornViewModel extends MemberViewModel {

    public EnrollNewbornViewModel(FormFragment<Member> formFragment, Member member) {
        super(formFragment, member);
    }

    public void updateSaveButton() {
        setSaveEnabled(validFullName() && validPhoneNumber() && validCardId() && validGender());
    }

    public void onClickSave() {
        if (validateFullName() && validatePhoneNumber() && validateCardId()) {
            getFormFragment().nextStep();
        }
    }
}

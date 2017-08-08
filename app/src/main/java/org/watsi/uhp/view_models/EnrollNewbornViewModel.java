package org.watsi.uhp.view_models;

import org.watsi.uhp.fragments.FormFragment;
import org.watsi.uhp.models.Member;

public class EnrollNewbornViewModel extends MemberViewModel {

    public EnrollNewbornViewModel(FormFragment<Member> formFragment, Member member) {
        super(formFragment, member);
    }

    @Override
    public void setUpViewModel() {
        updateSaveButton();
    }

    public void updateSaveButton() {
        setSaveEnabled(getMember().validFullName() && getMember().validCardId() && getMember().validGender());
    }

    public void onClickSave() {
        if (validateFullName() && validateCardId()) {
            getFormFragment().nextStep();
        }
    }
}

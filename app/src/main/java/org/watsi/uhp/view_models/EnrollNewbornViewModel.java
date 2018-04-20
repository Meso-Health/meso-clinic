package org.watsi.uhp.view_models;

import org.watsi.domain.entities.Member;
import org.watsi.uhp.fragments.FormFragment;

public class EnrollNewbornViewModel extends MemberViewModel {

    public EnrollNewbornViewModel(FormFragment<Member> formFragment, Member member) {
        super(formFragment, member);
    }

    @Override
    public void setUpViewModel() {
        updateSaveButton();
    }

    public void updateSaveButton() {
        setSaveEnabled(getMember().validFullName() &&  Member.validNonNullCardId(getMember().getCardId()) && getMember().validGender());
    }

    public void onClickSave() {
        if (validateFullName() && validateCardId()) {
            getFormFragment().nextStep();
        }
    }
}

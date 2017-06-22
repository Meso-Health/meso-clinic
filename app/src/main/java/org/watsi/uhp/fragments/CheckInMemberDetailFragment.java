package org.watsi.uhp.fragments;

import android.view.View;

import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.presenters.CheckInMemberDetailPresenter;

public class CheckInMemberDetailFragment extends MemberDetailFragment {
    private CheckInMemberDetailPresenter checkInMemberDetailPresenter;

    protected void setUpFragment(View view) {
        String searchMethodString = getArguments().getString(NavigationManager.ID_METHOD_BUNDLE_FIELD);
        IdentificationEvent.SearchMethodEnum idMethod = null;
        if (searchMethodString != null) {
            idMethod = IdentificationEvent.SearchMethodEnum.valueOf(searchMethodString);
        }
        Member throughMember = (Member) getArguments()
                .getSerializable(NavigationManager.THROUGH_MEMBER_BUNDLE_FIELD);

        checkInMemberDetailPresenter = new CheckInMemberDetailPresenter(
                getNavigationManager(),
                getSessionManager(),
                this,
                view,
                getContext(),
                getMember(),
                idMethod,
                throughMember
        );
        checkInMemberDetailPresenter.setUp();
    }

    public IdentificationEvent getIdEvent() {
        return checkInMemberDetailPresenter.getUnsavedIdentificationEvent();
    }
}


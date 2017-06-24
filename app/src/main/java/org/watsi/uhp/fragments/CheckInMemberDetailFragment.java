package org.watsi.uhp.fragments;

import android.content.Intent;
import android.view.Menu;
import android.view.View;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.presenters.CheckInMemberDetailPresenter;
import org.watsi.uhp.presenters.IdentifyMemberDetailPresenter;

public class CheckInMemberDetailFragment extends MemberDetailFragment {
    private CheckInMemberDetailPresenter checkInMemberDetailPresenter;

    protected void setUpFragment(View view) {
        IdentificationEvent idEvent = (IdentificationEvent) getArguments()
                .getSerializable(NavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);

        checkInMemberDetailPresenter = new CheckInMemberDetailPresenter(
                getNavigationManager(),
                getSessionManager(),
                this,
                view,
                getContext(),
                getMember(),
                idEvent
        );
        checkInMemberDetailPresenter.setUp();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    public IdentificationEvent getIdEvent() {
        return checkInMemberDetailPresenter.getIdEvent();
    }
}


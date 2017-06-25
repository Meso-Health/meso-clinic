package org.watsi.uhp.fragments;

import android.content.Intent;
import android.view.Menu;
import android.view.View;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.presenters.CheckInMemberDetailPresenter;

public class CheckInMemberDetailFragment extends MemberDetailFragment {
    private CheckInMemberDetailPresenter checkInMemberDetailPresenter;

    protected void setUpFragment(View view) {
        IdentificationEvent idEvent = (IdentificationEvent) getArguments().getSerializable(NavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);
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
        menu.findItem(R.id.menu_report_member).setVisible(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        checkInMemberDetailPresenter.handleOnActivityResult(requestCode, resultCode, data);
    }

    public IdentificationEvent getIdEvent() {
        return checkInMemberDetailPresenter.getIdEvent();
    }

    public void completeIdentification(IdentificationEvent.ClinicNumberTypeEnum clinicNumberType, int clinicNumber) {
        checkInMemberDetailPresenter.saveIdentificationEventAndCheckIn(clinicNumberType, clinicNumber);
    }

    public void reportMember() {
        checkInMemberDetailPresenter.reportMember();
    }
}


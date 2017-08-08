package org.watsi.uhp.fragments;

import android.content.Intent;
import android.view.Menu;
import android.view.View;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.presenters.CheckInMemberDetailPresenter;
import org.watsi.uhp.presenters.MemberDetailPresenter;

import java.sql.SQLException;

public class CheckInMemberDetailFragment extends MemberDetailFragment {
    CheckInMemberDetailPresenter checkInMemberDetailPresenter;

    @Override
    protected MemberDetailPresenter getPresenter() {
        return checkInMemberDetailPresenter;
    }

    @Override
    protected void setUpFragment(View view) {
        IdentificationEvent idEvent = (IdentificationEvent) getArguments().getSerializable(NavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);
        Member member = (Member) getArguments().getSerializable(NavigationManager.MEMBER_BUNDLE_FIELD);
        checkInMemberDetailPresenter = new CheckInMemberDetailPresenter(
                getNavigationManager(),
                getSessionManager(),
                this,
                view,
                getContext(),
                member,
                idEvent
        );
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

    public void completeIdentification(IdentificationEvent.ClinicNumberTypeEnum clinicNumberType, int clinicNumber) throws SQLException, AbstractModel.ValidationException {
        checkInMemberDetailPresenter.saveIdentificationEventAndCheckIn(clinicNumberType, clinicNumber);
    }

    public void reportMember() {
        checkInMemberDetailPresenter.reportMember();
    }
}


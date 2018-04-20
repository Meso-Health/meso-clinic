package org.watsi.uhp.fragments;

import android.content.Intent;
import android.view.View;

import org.watsi.domain.entities.IdentificationEvent;
import org.watsi.domain.entities.Member;
import org.watsi.domain.repositories.IdentificationEventRepository;
import org.watsi.domain.repositories.MemberRepository;
import org.watsi.uhp.managers.LegacyNavigationManager;
import org.watsi.uhp.presenters.CheckInMemberDetailPresenter;
import org.watsi.uhp.presenters.MemberDetailPresenter;

import java.sql.SQLException;

import javax.inject.Inject;

public class CheckInMemberDetailFragment extends MemberDetailFragment {
    CheckInMemberDetailPresenter checkInMemberDetailPresenter;

    @Inject MemberRepository memberRepository;
    @Inject IdentificationEventRepository identificationEventRepository;

    @Override
    protected MemberDetailPresenter getPresenter() {
        return checkInMemberDetailPresenter;
    }

    @Override
    protected void setUpFragment(View view) {
        IdentificationEvent idEvent = (IdentificationEvent) getArguments().getSerializable(LegacyNavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);
        Member member = (Member) getArguments().getSerializable(LegacyNavigationManager.MEMBER_BUNDLE_FIELD);
        checkInMemberDetailPresenter = new CheckInMemberDetailPresenter(
                getNavigationManager(),
                getSessionManager(),
                this,
                view,
                getContext(),
                member,
                idEvent,
                identificationEventRepository,
                memberRepository
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        checkInMemberDetailPresenter.handleOnActivityResult(requestCode, resultCode, data);
    }

    public void completeIdentification(IdentificationEvent.ClinicNumberType clinicNumberType, int clinicNumber) throws SQLException, AbstractModel.ValidationException {
        checkInMemberDetailPresenter.saveIdentificationEventAndCheckIn(clinicNumberType, clinicNumber);
    }
}

package org.watsi.uhp.fragments;

import android.content.Intent;
import android.view.Menu;
import android.view.View;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.presenters.IdentifyMemberDetailPresenter;

public class IdentifyMemberDetailFragment extends MemberDetailFragment {
    private IdentifyMemberDetailPresenter identifyMemberDetailPresenter;

    protected void setUpFragment(View view) {
        String searchMethodString = getArguments().getString(NavigationManager.ID_METHOD_BUNDLE_FIELD);
        IdentificationEvent.SearchMethodEnum idMethod = null;
        if (searchMethodString != null) {
            idMethod = IdentificationEvent.SearchMethodEnum.valueOf(searchMethodString);
        }
        Member throughMember = (Member) getArguments()
                .getSerializable(NavigationManager.THROUGH_MEMBER_BUNDLE_FIELD);

        identifyMemberDetailPresenter = new IdentifyMemberDetailPresenter(
                getNavigationManager(),
                getSessionManager(),
                this,
                view,
                getContext(),
                getMember(),
                idMethod,
                throughMember
        );
        identifyMemberDetailPresenter.setUp();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.menu_check_in_without_fingerprints).setVisible(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        identifyMemberDetailPresenter.handleOnActivityResult(requestCode, resultCode, data);
    }

    public IdentificationEvent getIdEvent() {
        return identifyMemberDetailPresenter.getUnsavedIdentificationEvent();
    }

    public void completeIdentificationWithoutFingerprints() {
        identifyMemberDetailPresenter.completeIdentificationWithoutFingerprints();
    }
}


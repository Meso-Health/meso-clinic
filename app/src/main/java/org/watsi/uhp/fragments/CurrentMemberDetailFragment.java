package org.watsi.uhp.fragments;

import android.view.View;

import org.watsi.uhp.presenters.CurrentMemberDetailPresenter;

/**
 * Created by michaelliang on 6/12/17.
 */

public class CurrentMemberDetailFragment extends MemberDetailFragment {
    private CurrentMemberDetailPresenter currentMemberDetailPresenter;

    protected void setUpFragment(View view) {
        currentMemberDetailPresenter = new CurrentMemberDetailPresenter(getNavigationManager(),
                view, getContext(), getMember());
        currentMemberDetailPresenter.setUp();
    }
}

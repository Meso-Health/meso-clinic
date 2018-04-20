package org.watsi.uhp.fragments;

import android.view.Menu;
import android.view.View;

import org.watsi.domain.entities.Member;
import org.watsi.domain.repositories.BillableRepository;
import org.watsi.domain.repositories.IdentificationEventRepository;
import org.watsi.domain.repositories.MemberRepository;
import org.watsi.uhp.R;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.presenters.CurrentMemberDetailPresenter;
import org.watsi.uhp.presenters.MemberDetailPresenter;

import javax.inject.Inject;

public class CurrentMemberDetailFragment extends MemberDetailFragment {
    private CurrentMemberDetailPresenter currentMemberDetailPresenter;

    @Inject MemberRepository memberRepository;
    @Inject BillableRepository billableRepository;
    @Inject IdentificationEventRepository identificationEventRepository;

    @Override
    protected MemberDetailPresenter getPresenter() {
        return currentMemberDetailPresenter;
    }

    @Override
    protected void setUpFragment(View view) {
        Member member = (Member) getArguments().getSerializable(NavigationManager.MEMBER_BUNDLE_FIELD);
        currentMemberDetailPresenter = new CurrentMemberDetailPresenter(
                getNavigationManager(),
                view,
                getContext(),
                member,
                identificationEventRepository,
                billableRepository,
                memberRepository);
        currentMemberDetailPresenter.setUp();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_dismiss_member).setVisible(true);
    }

    public void dismissMember() {
        currentMemberDetailPresenter.dismissMember();
    }
}

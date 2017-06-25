package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.presenters.MemberDetailPresenter;

/**
 * Created by michaelliang on 6/12/17.
 */

public abstract class MemberDetailFragment extends BaseFragment {
    MemberDetailPresenter memberDetailPresenter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member_detail, container, false);

        Member member = (Member) getArguments().getSerializable(NavigationManager.MEMBER_BUNDLE_FIELD);
        memberDetailPresenter = new MemberDetailPresenter(view, getContext(), member, getNavigationManager());
        memberDetailPresenter.setUp();

        setUpMenuAndWindow();
        setUpFragment(view);

        return view;
    }

    protected void setUpFragment(View view) {
        // no-op.
    }

    protected void setUpMenuAndWindow() {
        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        getActivity().setTitle(R.string.detail_fragment_label);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_member_edit).setVisible(true);
        menu.findItem(R.id.menu_enroll_newborn).setVisible(true);
        if (memberDetailPresenter.getMember().isAbsentee()) {
            menu.findItem(R.id.menu_complete_enrollment).setVisible(true);
        }
    }

    public Member getMember() {
        return memberDetailPresenter.getMember();
    }

}

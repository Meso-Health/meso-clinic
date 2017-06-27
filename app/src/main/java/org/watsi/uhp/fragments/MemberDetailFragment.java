package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import org.watsi.uhp.R;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.presenters.MemberDetailPresenter;

public abstract class MemberDetailFragment extends BaseFragment {
    MemberDetailPresenter memberDetailPresenter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member_detail, container, false);
        setUpFragment(view);

        memberDetailPresenter = getPresenter();
        memberDetailPresenter.setUp();

        setUpMenuAndWindow();
        return view;
    }

    protected abstract MemberDetailPresenter getPresenter();

    protected abstract void setUpFragment(View view);

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

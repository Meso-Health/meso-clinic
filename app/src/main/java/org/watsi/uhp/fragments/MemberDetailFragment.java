package org.watsi.uhp.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.presenters.MemberDetailPresenter;

import java.sql.SQLException;

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

        // Prepare fragment stuff
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

        // These should appear whenever you're in the detail view.
        menu.findItem(R.id.menu_member_edit).setVisible(true);
        menu.findItem(R.id.menu_enroll_newborn).setVisible(true);
        menu.findItem(R.id.menu_report_member).setVisible(true);

        // This should only appear if member is an absentee.
        if (memberDetailPresenter.getMember().isAbsentee()) {
            menu.findItem(R.id.menu_complete_enrollment).setVisible(true);
        }
    }

    public Member getMember() {
        return memberDetailPresenter.getMember();
    }

    public void reportMember(IdentificationEvent idEvent) {
        idEvent.setClinicNumberType(null);
        idEvent.setClinicNumber(null);
        idEvent.setAccepted(false);
        idEvent.setOccurredAt(Clock.getCurrentTime());

        final IdentificationEvent finalIdEvent = idEvent;
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.reject_identity_alert)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        try {
                            finalIdEvent.saveChanges(((ClinicActivity) getContext()).getAuthenticationToken());
                            getNavigationManager().setCurrentPatientsFragment();
                            Toast.makeText(getContext(),
                                    getMember().getFullName() + " " + getContext().getString(R.string.identification_rejected),
                                    Toast.LENGTH_LONG).
                                    show();
                        } catch (SQLException e) {
                            ExceptionManager.reportException(e);
                        }
                    }
                }).create().show();
    }
}

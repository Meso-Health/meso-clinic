package org.watsi.uhp.managers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.fragments.CurrentMemberDetailFragment;
import org.watsi.uhp.fragments.MemberDetailFragment;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

public class MenuNavigationManager {
    private SessionManager mSessionManager;
    private NavigationManager mNavigationManager;
    private ClinicActivity mClinicActivity;


    public MenuNavigationManager(ClinicActivity clinicActivity) {
        mSessionManager = clinicActivity.getSessionManager();
        mNavigationManager = clinicActivity.getNavigationManager();
        mClinicActivity = clinicActivity;
    }

    public boolean nextStep(Fragment currentFragment, MenuItem menuItem) {
        Member member = getMemberFromFragmentIfExists(currentFragment);
        switch (menuItem.getItemId()) {
            case R.id.menu_dismiss_member:
                dismissMember(currentFragment);
                break;
            case R.id.menu_member_edit:
                editMember(currentFragment);
                break;
            case R.id.menu_enroll_newborn:
                Member newborn = member.createNewborn();
                IdentificationEvent idEvent = new IdentificationEvent(newborn,
                        IdentificationEvent.SearchMethodEnum.THROUGH_HOUSEHOLD, member);
                mNavigationManager.setEnrollNewbornInfoFragment(newborn, idEvent);
                break;
            case R.id.menu_version:
                mNavigationManager.setVersionFragment();
                break;
            case R.id.menu_logout:
                confirmBeforelogout(currentFragment);
                break;
        }
        return true;
    }

    Member getMemberFromFragmentIfExists(Fragment fragment) {
        Member member = null;
        if (fragment instanceof MemberDetailFragment) {
            member = ((MemberDetailFragment) fragment).getMember();
        }
        return member;
    }

    void dismissMember(Fragment fragment) {
        if (fragment instanceof CurrentMemberDetailFragment) {
            ((CurrentMemberDetailFragment) fragment).dismissMember();
        } else {
            ExceptionManager.reportErrorMessage("Dismiss member menu button reached from " +
                    fragment.getClass().toString());
        }
    }

    void editMember(Fragment fragment) {
        if (fragment instanceof MemberDetailFragment) {
            ((MemberDetailFragment) fragment).navigateToMemberEditFragment();
        } else {
            ExceptionManager.reportErrorMessage("Edit member menu button reached from " +
                    fragment.getClass().toString());
        }
    }

    void confirmBeforelogout(Fragment fragment) {
        new AlertDialog.Builder(fragment.getActivity())
                .setTitle(R.string.log_out_alert)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        mSessionManager.logout(mClinicActivity);
                    }
                }).create().show();
    }

    public NavigationManager getNavigationManager() {
        return mNavigationManager;
    }
}

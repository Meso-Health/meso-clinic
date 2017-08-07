package org.watsi.uhp.managers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.fragments.CheckInMemberDetailFragment;
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
            case R.id.menu_logout:
                confirmBeforelogout(currentFragment);
                break;
            case R.id.menu_member_edit:
                navigateToMemberEditFragment(currentFragment, member);
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
            case R.id.menu_complete_enrollment:
                navigateToCompleteEnrollmentFragment(currentFragment, member);
                break;
            case R.id.menu_report_member:
                reportMember(currentFragment);
                break;
        }
        return true;
    }

    protected Member getMemberFromFragmentIfExists(Fragment fragment) {
        Member member = null;
        if (fragment instanceof MemberDetailFragment) {
            member = ((MemberDetailFragment) fragment).getMember();
        }
        return member;
    }

    protected void reportMember(Fragment fragment) {
        if (fragment instanceof CheckInMemberDetailFragment) {
            ((CheckInMemberDetailFragment) fragment).reportMember();
        } else {
            ExceptionManager.reportErrorMessage("Attempted to report member after check in.");
        }
    }

    protected void navigateToCompleteEnrollmentFragment(Fragment fragment, Member member) {
        if (fragment instanceof CheckInMemberDetailFragment) {
            getNavigationManager().setEnrollmentMemberPhotoFragment(member, ((CheckInMemberDetailFragment) fragment).getIdEvent());
        } else if (fragment instanceof CurrentMemberDetailFragment) {
            getNavigationManager().setEnrollmentMemberPhotoFragment(member, null);
        } else {
            ExceptionManager.reportErrorMessage("Complete enrollment menu button reached from fragment that's not a MemberDetailFragment");
        }
    }

    protected void navigateToMemberEditFragment(Fragment fragment, Member member) {
        if (fragment instanceof CheckInMemberDetailFragment) {
            CheckInMemberDetailFragment checkInMemberDetailFragment = (CheckInMemberDetailFragment) fragment;
            getNavigationManager().setMemberEditFragment(member, checkInMemberDetailFragment.getIdEvent());
        } else if (fragment instanceof CurrentMemberDetailFragment) {
            getNavigationManager().setMemberEditFragment(member, null);
        } else {
            ExceptionManager.reportErrorMessage("MemberEdit menu button reached from fragment not in [CheckInMemberDetailFragment, CurrentMemberDetailFragment]");
        }
    }

    protected void confirmBeforelogout(Fragment fragment) {
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

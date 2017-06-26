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
import org.watsi.uhp.models.Member;

/**
 * Created by michaelliang on 6/26/17.
 */

public class MenuNavigationManager {
    private MenuItem mMenuItem;
    private Fragment mFragment;
    private SessionManager mSessionManager;
    private NavigationManager mNavigationManager;
    private ClinicActivity mClinicActivity;
    private Member mMember;


    public MenuNavigationManager(Fragment fragment, ClinicActivity clinicActivity, MenuItem menuItem) {
        mFragment = fragment;
        mMenuItem = menuItem;
        mSessionManager = clinicActivity.getSessionManager();
        mNavigationManager = clinicActivity.getNavigationManager();
        mClinicActivity = clinicActivity;

        setMemberIfExists();
    }

    protected void setMemberIfExists() {
        if (mFragment instanceof MemberDetailFragment) {
            mMember = ((MemberDetailFragment) mFragment).getMember();
        }
    }

    public boolean nextStep() {
        switch (mMenuItem.getItemId()) {
            case R.id.menu_logout:
                confirmBeforelogout();
                break;
            case R.id.menu_member_edit:
                navigateToMemberEditFragment();
                break;
            case R.id.menu_enroll_newborn:
                mNavigationManager.setEnrollNewbornInfoFragment(mMember, null, null);
                break;
            case R.id.menu_version:
                mNavigationManager.setVersionFragment();
                break;
            case R.id.menu_complete_enrollment:
                navigateToCompleteEnrollmentFragment();
                break;
            case R.id.menu_report_member:
                reportMember();
                break;
        }
        return true;
    }

    private void reportMember() {
        if (mFragment instanceof CheckInMemberDetailFragment) {
            ((CheckInMemberDetailFragment) mFragment).reportMember();
        } else {
            ExceptionManager.reportException(new IllegalStateException("Attempted to report member after check in."));
        }
    }

    private void navigateToCompleteEnrollmentFragment() {
        if (mFragment instanceof CheckInMemberDetailFragment) {
            mNavigationManager.setEnrollmentMemberPhotoFragment(mMember, ((CheckInMemberDetailFragment) mFragment).getIdEvent());
        } else if (mFragment instanceof CurrentMemberDetailFragment) {
            mNavigationManager.setEnrollmentMemberPhotoFragment(mMember, null);
        } else {
            ExceptionManager.reportMessage("Complete enrollment menu button reached from fragment that's not a MemberDetailFragment");
        }
    }

    private void navigateToMemberEditFragment() {
        if (mFragment instanceof CheckInMemberDetailFragment) {
            CheckInMemberDetailFragment checkInMemberDetailFragment = (CheckInMemberDetailFragment) mFragment;
            mNavigationManager.setMemberEditFragment(
                    mMember,
                    checkInMemberDetailFragment.getIdEvent(),
                    null
            );
        } else if (mFragment instanceof CurrentMemberDetailFragment) {
            mNavigationManager.setMemberEditFragment(
                    mMember,
                    null,
                    null);
        } else {
            ExceptionManager.reportMessage("MemberEdit menu button reached from fragment not in [CheckInMemberDetailFragment, CurrentMemberDetailFragment]");
        }
    }

    private void confirmBeforelogout() {
        new AlertDialog.Builder(mFragment.getActivity())
                .setTitle(R.string.log_out_alert)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        mSessionManager.logout(mClinicActivity);
                    }
                }).create().show();
    }
}

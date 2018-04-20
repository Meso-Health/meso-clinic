package org.watsi.uhp.managers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import org.watsi.domain.entities.IdentificationEvent;
import org.watsi.domain.entities.Member;
import org.watsi.uhp.R;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.fragments.CurrentMemberDetailFragment;
import org.watsi.uhp.fragments.MemberDetailFragment;
import org.watsi.uhp.fragments.ReceiptFragment;

public class MenuNavigationManager {

    private SessionManager mSessionManager;
    private LegacyNavigationManager mNavigationManager;
    private ClinicActivity mClinicActivity;

    public MenuNavigationManager(ClinicActivity clinicActivity) {
        mSessionManager = clinicActivity.getSessionManager();
        mNavigationManager = clinicActivity.getNavigationManager();
        mClinicActivity = clinicActivity;
    }

    public boolean nextStep(Fragment currentFragment, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_dismiss_member:
                dismissMember(currentFragment);
                break;
            case R.id.menu_member_edit:
                editMember(currentFragment);
                break;
            case R.id.menu_enroll_newborn:
                Member member = getMemberFromFragmentIfExists(currentFragment);
                Member newborn = member.createNewborn();
                IdentificationEvent idEvent = new IdentificationEvent(newborn,
                        IdentificationEvent.SearchMethod.THROUGH_HOUSEHOLD, member);
                mNavigationManager.setEnrollNewbornInfoFragment(newborn, idEvent);
                break;
            case R.id.menu_version:
                mNavigationManager.setVersionFragment();
                break;
            case R.id.menu_logout:
                confirmBeforeLogout(currentFragment);
                break;
            case R.id.menu_submit_without_copayment:
                ReceiptFragment receiptFragment = (ReceiptFragment) currentFragment;
                receiptFragment.getEncounter().setCopaymentPaid(false);
                receiptFragment.nextStep();
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

    private void dismissMember(Fragment fragment) {
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

    void confirmBeforeLogout(Fragment fragment) {
        new AlertDialog.Builder(fragment.getActivity())
                .setTitle(R.string.log_out_alert)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        mSessionManager.logout(mClinicActivity);
                    }
                }).create().show();
    }

    public LegacyNavigationManager getNavigationManager() {
        return mNavigationManager;
    }
}

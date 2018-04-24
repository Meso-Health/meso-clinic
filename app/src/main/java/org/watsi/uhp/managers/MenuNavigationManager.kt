package org.watsi.uhp.managers

import android.app.AlertDialog
import android.os.Bundle
import android.view.MenuItem

import org.watsi.domain.entities.Member
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.fragments.CheckInMemberDetailFragment.Companion.PARAM_MEMBER
import org.watsi.uhp.fragments.CurrentPatientsFragment
import org.watsi.uhp.fragments.EnrollNewbornInfoFragment
import org.watsi.uhp.fragments.MemberEditFragment
import org.watsi.uhp.fragments.VersionAndSyncFragment

class MenuNavigationManager(private val sessionManager: SessionManager,
                            private val navigationManager: NavigationManager,
                            private val clinicActivity: ClinicActivity) {

    fun handle(arguments: Bundle, menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menu_dismiss_member -> {
                // TODO: create dismissed IdentificationEvent
                navigationManager.popTo(CurrentPatientsFragment())
            }
            R.id.menu_member_edit -> {
                val member = arguments.getSerializable(PARAM_MEMBER) as Member
                navigationManager.goTo(MemberEditFragment.forMember(member))
            }
            R.id.menu_enroll_newborn -> {
                val member = arguments.getSerializable(PARAM_MEMBER) as Member
                navigationManager.goTo(EnrollNewbornInfoFragment.forParent(member))
            }
            R.id.menu_version -> {
                navigationManager.goTo(VersionAndSyncFragment())
            }
            R.id.menu_logout -> {
                AlertDialog.Builder(clinicActivity)
                        .setTitle(R.string.log_out_alert)
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes) { _, _ ->
                            sessionManager.logout(clinicActivity)
                        }.create().show()

            }
            R.id.menu_submit_without_copayment -> {
                // TODO: handle submit without copayment
            }
        }
        return true
    }
}

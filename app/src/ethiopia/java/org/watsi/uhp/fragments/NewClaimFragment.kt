package org.watsi.uhp.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.ethiopia.fragment_new_claim.membership_number
import kotlinx.android.synthetic.ethiopia.fragment_new_claim.start_button
import org.watsi.device.managers.Logger
import org.watsi.device.managers.SessionManager
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.managers.NavigationManager
import javax.inject.Inject

class NewClaimFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var logger: Logger

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(context.getString(R.string.new_claim_fragment_label), null)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_new_claim, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        start_button.setOnClickListener {
            val membershipNumber = membership_number.getText().toString()

            if (membershipNumber.isBlank()) {
                // TODO: disable button when membership number field is blank
                logger.error("no membership number")
            } else {
                navigationManager.goTo(MemberInformationFragment.withMembershipNumber(membershipNumber))
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu?.let {
            it.findItem(R.id.menu_logout).isVisible = true
            it.findItem(R.id.menu_version).isVisible = true
            it.findItem(R.id.menu_switch_language).isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_version -> {
                navigationManager.goTo(StatusFragment())
            }
            R.id.menu_logout -> {
                AlertDialog.Builder(activity)
                        .setTitle(R.string.log_out_alert)
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes) { _, _ ->
                            sessionManager.logout()
                            (activity as ClinicActivity).navigateToAuthenticationActivity()
                        }.create().show()
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

}

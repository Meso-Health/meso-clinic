package org.watsi.uhp.fragments

import android.app.AlertDialog
import android.app.job.JobScheduler
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.ethiopia.fragment_home.check_in_button
import kotlinx.android.synthetic.ethiopia.fragment_home.language_button
import kotlinx.android.synthetic.ethiopia.fragment_home.pending_button
import kotlinx.android.synthetic.ethiopia.fragment_home.pending_indicator
import kotlinx.android.synthetic.ethiopia.fragment_home.prepare_button
import kotlinx.android.synthetic.ethiopia.fragment_home.returned_button
import kotlinx.android.synthetic.ethiopia.fragment_home.returned_indicator
import kotlinx.android.synthetic.ethiopia.fragment_home.status_button
import me.philio.pinentry.PinEntryView
import org.watsi.device.managers.SessionManager
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.HomeViewModel
import javax.inject.Inject

class HomeFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: HomeViewModel

    private var snackbarMessageToShow: String? = null

    companion object {
        const val PARAM_SNACKBAR_MESSAGE = "snackbar_message"

        fun withSnackbarMessage(message: String): HomeFragment {
            val fragment = HomeFragment()
            fragment.arguments = Bundle().apply {
                putString(PARAM_SNACKBAR_MESSAGE, message)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(HomeViewModel::class.java)
        viewModel.getObservable().observe(this, Observer {
            it?.let {
                if (sessionManager.userHasPermission(SessionManager.Permissions.WORKFLOW_CLAIMS_PREPARATION)) {
                    if (it.pendingClaimsCount > 0) {
                        pending_indicator.visibility = View.VISIBLE
                    } else {
                        pending_indicator.visibility = View.GONE
                    }
                    if (it.returnedClaimsCount > 0) {
                        returned_indicator.visibility = View.VISIBLE
                    } else {
                        returned_indicator.visibility = View.GONE
                    }
                }
            }
        })
        snackbarMessageToShow = arguments?.getString(PARAM_SNACKBAR_MESSAGE)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(context.getString(R.string.home_fragment_label), null)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        check_in_button.setOnClickListener {
            navigationManager.goTo(SearchFragment())
        }

        prepare_button.setOnClickListener {
            navigationManager.goTo(CheckedInPatientsFragment())
        }


        language_button.setOnClickListener {
            (activity as ClinicActivity).localeManager.toggleLocale(activity)
        }

        status_button.setOnClickListener {
            navigationManager.goTo(StatusFragment())
        }

        if (sessionManager.userHasPermission(SessionManager.Permissions.WORKFLOW_CLAIMS_PREPARATION)) {
            pending_button.visibility = View.VISIBLE
            pending_button.setOnClickListener {
                confirmSecurityPinAndNavigate(PendingClaimsFragment())
            }

            returned_button.visibility = View.VISIBLE
            returned_button.setOnClickListener {
                confirmSecurityPinAndNavigate(ReturnedClaimsFragment())
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu?.let {
            it.findItem(R.id.menu_logout).isVisible = true
            it.findItem(R.id.menu_delete_claim).isVisible = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_logout -> {
                AlertDialog.Builder(activity)
                        .setTitle(R.string.log_out_alert)
                        .setMessage(R.string.log_out_alert_message)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.yes) { _, _ ->
                            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                            jobScheduler.cancelAll()
                            sessionManager.logout()
                            (activity as ClinicActivity).navigateToAuthenticationActivity()
                        }.create().show()
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun confirmSecurityPinAndNavigate(fragment: Fragment) {
        val securityPinView = LayoutInflater.from(context).inflate(R.layout.dialog_security_pin, null, true)
        val pinView = securityPinView.findViewById<PinEntryView>(R.id.security_pin)
        val errorText = securityPinView.findViewById<TextInputLayout>(R.id.error_text)
        val dialog = AlertDialog.Builder(activity)
                .setTitle(R.string.pending_claims_dialog_label)
                .setView(securityPinView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.submit, null)
                .create() as AlertDialog

        // have to apply custom onClickListener so that we can verify PIN before dismissing dialog
        dialog.setOnShowListener {
            val submitBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            submitBtn.setOnClickListener {
                errorText.error = ""
                sessionManager.currentUser()?.securityPin?.let { pin ->
                    if (pin == pinView.text.toString()) {
                        dialog.dismiss()
                        navigationManager.goTo(fragment)
                    }
                }
                errorText.error = getString(R.string.pending_claims_dialog_error_message)
            }
        }
        dialog.show()
    }
}

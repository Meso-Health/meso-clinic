package org.watsi.uhp.fragments

import android.app.AlertDialog
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.ethiopia.fragment_home.current_patients
import kotlinx.android.synthetic.ethiopia.fragment_home.empty_container
import kotlinx.android.synthetic.ethiopia.fragment_home.patients_container
import kotlinx.android.synthetic.ethiopia.fragment_home.search_button
import me.philio.pinentry.PinEntryView
import org.threeten.bp.Clock
import org.watsi.device.managers.Logger
import org.watsi.device.managers.SessionManager
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.adapters.MemberAdapter
import org.watsi.uhp.helpers.RecyclerViewHelper
import org.watsi.uhp.helpers.SnackbarHelper
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.HomeViewModel
import javax.inject.Inject

class HomeFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    @Inject lateinit var clock: Clock

    lateinit var viewModel: HomeViewModel
    lateinit var menuStateObservable: LiveData<HomeViewModel.MenuState>
    lateinit var memberAdapter: MemberAdapter

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
        menuStateObservable = viewModel.getMenuStateObservable()
        menuStateObservable.observe(this, Observer {
            it?.let {
                activity.invalidateOptionsMenu()
            }
        })

        viewModel.getMemberStateObservable().observe(this, Observer {
            it?.let { memberState ->
                val checkedInMembers = memberState.checkedInMembers
                if (checkedInMembers.isEmpty()) {
                    empty_container.visibility = View.VISIBLE
                    patients_container.visibility = View.GONE
                } else {
                    empty_container.visibility = View.GONE
                    patients_container.visibility = View.VISIBLE
                    memberAdapter.setMembers(checkedInMembers)
                }
            }
        })

        snackbarMessageToShow = arguments?.getString(PARAM_SNACKBAR_MESSAGE)

        memberAdapter = MemberAdapter(
            onItemSelect = { memberRelation: MemberWithIdEventAndThumbnailPhoto ->
                if (memberRelation.identificationEvent != null) {
                    memberRelation.identificationEvent?.let {
                        navigationManager.goTo(EditMemberFragment.forParams(
                            member = memberRelation.member,
                            searchMethod = it.searchMethod
                        ))
                    }
                } else {
                    logger.error(
                        "Member shown on HomeFragment has no corresponding IdentificationEvent",
                        mapOf("memberId" to memberRelation.member.id.toString())
                    )
                }
            }
        )
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(context.getString(R.string.home_fragment_label), null)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        RecyclerViewHelper.setRecyclerView(current_patients, memberAdapter, context, false)

        search_button.setOnClickListener {
            navigationManager.goTo(SearchFragment())
        }

        snackbarMessageToShow?.let { snackbarMessage ->
            SnackbarHelper.show(search_button, context, snackbarMessage)
            snackbarMessageToShow = null
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        var returnedClaimsMenuTitle = context.getString(R.string.menu_returned_claims_without_number)
        var pendingClaimsMenuTitle = context.getString(R.string.menu_pending_claims_without_number)

        menuStateObservable.value?.let {
            if (it.returnedClaimsCount > 0) {
                returnedClaimsMenuTitle = context.getString(R.string.menu_returned_claims_with_number, it.returnedClaimsCount)
            }
            if (it.pendingClaimsCount > 0) {
                pendingClaimsMenuTitle = context.getString(R.string.menu_pending_claims_with_number, it.pendingClaimsCount)
            }
        }

        menu?.let {
            it.findItem(R.id.menu_returned_claims).isVisible = true
            it.findItem(R.id.menu_returned_claims).title = returnedClaimsMenuTitle
            it.findItem(R.id.menu_pending_claims).isVisible = true
            it.findItem(R.id.menu_pending_claims).title = pendingClaimsMenuTitle
            it.findItem(R.id.menu_logout).isVisible = true
            it.findItem(R.id.menu_status).isVisible = true
            it.findItem(R.id.menu_switch_language).isVisible = true
            it.findItem(R.id.menu_edit_claim).isVisible = false
            it.findItem(R.id.menu_delete_claim).isVisible = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_pending_claims -> {
                confirmSecurityPinAndNavigate(PendingClaimsFragment())
            }
            R.id.menu_returned_claims -> {
                confirmSecurityPinAndNavigate(ReturnedClaimsFragment())
            }
            R.id.menu_status -> {
                navigationManager.goTo(StatusFragment())
            }
            R.id.menu_logout -> {
                AlertDialog.Builder(activity)
                        .setTitle(R.string.log_out_alert)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.yes) { _, _ ->
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
                sessionManager.currentToken()?.user?.securityPin?.let { pin ->
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

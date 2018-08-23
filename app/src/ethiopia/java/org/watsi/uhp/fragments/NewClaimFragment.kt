package org.watsi.uhp.fragments

import android.app.AlertDialog
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.ethiopia.fragment_new_claim.household_member_number
import kotlinx.android.synthetic.ethiopia.fragment_new_claim.household_number
import kotlinx.android.synthetic.ethiopia.fragment_new_claim.kebele_number
import kotlinx.android.synthetic.ethiopia.fragment_new_claim.member_status
import kotlinx.android.synthetic.ethiopia.fragment_new_claim.membership_number_layout
import kotlinx.android.synthetic.ethiopia.fragment_new_claim.region_number
import kotlinx.android.synthetic.ethiopia.fragment_new_claim.start_button
import kotlinx.android.synthetic.ethiopia.fragment_new_claim.woreda_number
import org.watsi.device.managers.Logger
import org.watsi.device.managers.SessionManager
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.helpers.LayoutHelper
import org.watsi.uhp.helpers.SnackbarHelper
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.NewClaimViewModel
import javax.inject.Inject

class NewClaimFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var keyboardManager: KeyboardManager
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    lateinit var viewModel: NewClaimViewModel
    lateinit var viewStateObservable: LiveData<NewClaimViewModel.ViewState>

    private var snackbarMessageToShow: String? = null

    companion object {
        const val PARAM_SNACKBAR_MESSAGE = "snackbar_message"

        fun withSnackbarMessage(message: String): NewClaimFragment {
            val fragment = NewClaimFragment()
            fragment.arguments = Bundle().apply {
                putString(PARAM_SNACKBAR_MESSAGE, message)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(NewClaimViewModel::class.java)
        viewStateObservable = viewModel.getViewStateObservable()
        viewStateObservable.observe(this, Observer {
            it?.let {
                membership_number_layout.error = it.error
            }
        })

        snackbarMessageToShow = arguments?.getString(PARAM_SNACKBAR_MESSAGE)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(context.getString(R.string.new_claim_fragment_label), null)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_new_claim, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        region_number.addTextChangedListener(LayoutHelper.OnChangedListener {
            text -> viewModel.onRegionNumberChange(text)
        })

        woreda_number.addTextChangedListener(LayoutHelper.OnChangedListener {
            text -> viewModel.onWoredaNumberChange(text)
        })

        kebele_number.addTextChangedListener(LayoutHelper.OnChangedListener {
            text -> viewModel.onKebeleNumberChange(text)
        })

        member_status.setUpSpinner(
            NewClaimViewModel.memberStatusList,
            NewClaimViewModel.memberStatusList.first(),
            { selectedString ->
                viewModel.onMemberStatusChange(selectedString)
            }
        )

        household_number.addTextChangedListener(LayoutHelper.OnChangedListener {
            text -> viewModel.onHouseholdNumberChange(text)
        })

        household_member_number.addTextChangedListener(LayoutHelper.OnChangedListener {
            text -> viewModel.onHouseholdMemberNumberChange(text)
        })

        start_button.setOnClickListener {
            viewStateObservable.value?.let {
                if (viewModel.getMembershipNumberError(it).isBlank()) {
                    val membershipNumber = viewModel.getMembershipNumber(it)
                    navigationManager.popTo(MemberInformationFragment.withMembershipNumber(membershipNumber))
                } else {
                    viewModel.setMembershipNumberError(viewModel.getMembershipNumberError(it))
                }
            }
        }

        /* Hide keyboard if no text inputs have focus */
        val textFields = listOf(region_number, woreda_number, kebele_number, household_number, household_member_number)
        textFields.forEach {
            it.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
                if (textFields.all { !it.hasFocus() }) {
                    keyboardManager.hideKeyboard(view)
                }
            }
        }

        snackbarMessageToShow?.let { snackbarMessage ->
            SnackbarHelper.show(start_button, context, snackbarMessage)
            snackbarMessageToShow = null
        }
    }

    private fun getNumberOfReturnedClaims(): Int {
        // TODO: actually return count of returned encounters
        return 5
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val returnedClaimsMenuTitle = if (this.getNumberOfReturnedClaims() > 0) {
            context.getString(R.string.menu_returned_claims_with_number, this.getNumberOfReturnedClaims())
        } else {
            context.getString(R.string.menu_returned_claims_without_number)
        }

        menu?.let {
            it.findItem(R.id.menu_returned_claims).isVisible = true
            it.findItem(R.id.menu_returned_claims).title = returnedClaimsMenuTitle
            it.findItem(R.id.menu_logout).isVisible = true
            it.findItem(R.id.menu_version).isVisible = true
            it.findItem(R.id.menu_switch_language).isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_returned_claims -> {
                navigationManager.goTo(ReturnedClaimsFragment())
            }
            R.id.menu_version -> {
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
}

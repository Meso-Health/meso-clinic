package org.watsi.uhp.fragments

import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.ethiopia.fragment_new_claim.region_number
import kotlinx.android.synthetic.ethiopia.fragment_new_claim.woreda_number
import kotlinx.android.synthetic.ethiopia.fragment_new_claim.kebele_number
import kotlinx.android.synthetic.ethiopia.fragment_new_claim.member_status
import kotlinx.android.synthetic.ethiopia.fragment_new_claim.household_number
import kotlinx.android.synthetic.ethiopia.fragment_new_claim.household_member_number
import kotlinx.android.synthetic.ethiopia.fragment_new_claim.start_button
import org.watsi.device.managers.SessionManager
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.helpers.LayoutHelper
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.NewClaimViewModel
import javax.inject.Inject

class NewClaimFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var keyboardManager: KeyboardManager
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: NewClaimViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(NewClaimViewModel::class.java)
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

        kebele_number.setOnEditorActionListener() { v, actionId, event ->
            if (v == null) false
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                if (v == kebele_number) {
                    v?.clearFocus()
                    keyboardManager.hideKeyboard(v)
                    member_status.performClick()
                    true
                }
            }
            false
        }

        member_status.setUpSpinner(
            // TODO: move member status list as something set in viewModel so it can just be pulled from there
            listOf("P", "S"),
            // TODO: initialChoice should be listInViewModel.first()
            "P",
            { selected -> viewModel.onMemberStatusChange(selected) },
            "P",
            { viewModel.onMemberStatusChange("P") }

        )

        household_number.addTextChangedListener(LayoutHelper.OnChangedListener {
            text -> viewModel.onHouseholdNumberChange(text)
        })

        household_member_number.addTextChangedListener(LayoutHelper.OnChangedListener {
            text -> viewModel.onHouseholdMemberNumberChange(text)
        })
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

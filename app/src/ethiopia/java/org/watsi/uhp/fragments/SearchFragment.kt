package org.watsi.uhp.fragments

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.ethiopia.fragment_search.household_member_number
import kotlinx.android.synthetic.ethiopia.fragment_search.household_number
import kotlinx.android.synthetic.ethiopia.fragment_search.kebele_number
import kotlinx.android.synthetic.ethiopia.fragment_search.member_status
import kotlinx.android.synthetic.ethiopia.fragment_search.membership_number_layout
import kotlinx.android.synthetic.ethiopia.fragment_search.region_number
import kotlinx.android.synthetic.ethiopia.fragment_search.start_button
import kotlinx.android.synthetic.ethiopia.fragment_search.woreda_number
import org.watsi.device.managers.Logger
import org.watsi.device.managers.SessionManager
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.helpers.LayoutHelper
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.SearchViewModel
import javax.inject.Inject

class SearchFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var keyboardManager: KeyboardManager
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    lateinit var viewModel: SearchViewModel
    lateinit var formStateObservable: LiveData<SearchViewModel.FormState>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SearchViewModel::class.java)
        formStateObservable = viewModel.getFormStateObservable()
        formStateObservable.observe(this, Observer {
            it?.let {
                membership_number_layout.error = it.error
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(context.getString(R.string.search_fragment_label), null)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        region_number.addTextChangedListener(LayoutHelper.OnChangedListener { text ->
            viewModel.onRegionNumberChange(text)
            if (text.length == 2) {
                woreda_number.requestFocus()
            }
        })

        woreda_number.addTextChangedListener(LayoutHelper.OnChangedListener { text ->
            viewModel.onWoredaNumberChange(text)
            if (text.length == 2) {
                kebele_number.requestFocus()
            }
        })

        kebele_number.addTextChangedListener(LayoutHelper.OnChangedListener { text ->
            viewModel.onKebeleNumberChange(text)
            if (text.length == 2) {
                household_number.requestFocus()
            }
        })

        member_status.setUpSpinner(
            SearchViewModel.memberStatusList,
            SearchViewModel.memberStatusList.first(),
            { selectedString ->
                viewModel.onMemberStatusChange(selectedString)
            }
        )

        household_number.addTextChangedListener(LayoutHelper.OnChangedListener { text ->
            viewModel.onHouseholdNumberChange(text)
            if (text.length == 6) {
                household_member_number.requestFocus()
            }
        })

        household_member_number.addTextChangedListener(LayoutHelper.OnChangedListener {
            text -> viewModel.onHouseholdMemberNumberChange(text)
        })

        start_button.setOnClickListener {
            formStateObservable.value?.let {
                if (!viewModel.membershipNumberHasError(it)) {
                    val membershipNumber = viewModel.getMembershipNumber(it)
                    navigationManager.popTo(MemberInformationFragment.withMembershipNumber(membershipNumber))
                } else {
                    viewModel.setMembershipNumberError(getString(R.string.invalid_membership_error))
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
    }
}

package org.watsi.uhp.fragments

import android.app.Activity
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.ethiopia.fragment_search.household_member_number
import kotlinx.android.synthetic.ethiopia.fragment_search.household_number
import kotlinx.android.synthetic.ethiopia.fragment_search.kebele_number
import kotlinx.android.synthetic.ethiopia.fragment_search.member_status
import kotlinx.android.synthetic.ethiopia.fragment_search.membership_number_layout
import kotlinx.android.synthetic.ethiopia.fragment_search.region_number
import kotlinx.android.synthetic.ethiopia.fragment_search.search_button
import kotlinx.android.synthetic.ethiopia.fragment_search.woreda_number
import org.watsi.device.managers.Logger
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.usecases.FindHouseholdIdByMembershipNumberUseCase
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.activities.SearchByMemberCardActivity
import org.watsi.uhp.helpers.LayoutHelper
import org.watsi.uhp.helpers.SnackbarHelper
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.SearchViewModel
import java.util.UUID
import javax.inject.Inject

class SearchFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var keyboardManager: KeyboardManager
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var findHouseholdIdByMembershipNumberUseCase: FindHouseholdIdByMembershipNumberUseCase
    @Inject lateinit var logger: Logger
    lateinit var viewModel: SearchViewModel
    lateinit var formStateObservable: LiveData<SearchViewModel.FormState>

    companion object {
        const val SCAN_CARD_INTENT = 1
    }

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
        (activity as ClinicActivity).setSoftInputModeToResize()
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

        search_button.setOnClickListener { view ->
            formStateObservable.value?.let {
                if (!viewModel.membershipNumberHasError(it)) {
                    val membershipNumber = viewModel.getMembershipNumber(it)
                    findHouseholdIdByMembershipNumberUseCase.execute(membershipNumber).subscribe( {
                        navigationManager.goTo(HouseholdFragment.forParams(
                            it, IdentificationEvent.SearchMethod.SEARCH_MEMBERSHIP_NUMBER)
                        )
                    }, { err ->
                        logger.error(err)
                        SnackbarHelper.showError(view, context, err.localizedMessage)
                    }, {
                        navigationManager.goTo(MemberNotFoundFragment.forMembershipNumber(membershipNumber))
                    })
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

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.search, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_search_card -> {
                startActivityForResult(Intent(activity, SearchByMemberCardActivity::class.java), SCAN_CARD_INTENT)
            }
            R.id.menu_search_by_name -> {
                navigationManager.goTo(MemberSearchFragment())
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        return when (resultCode) {
            Activity.RESULT_OK -> {
                val householdId = data?.getSerializableExtra(SearchByMemberCardActivity.MEMBER_RESULT_KEY) as UUID
                navigationManager.goTo(HouseholdFragment.forParams(
                    householdId, IdentificationEvent.SearchMethod.SCAN_BARCODE)
                )
            }
            Activity.RESULT_CANCELED -> { }
            else -> {
                logger.error("QrCodeActivity.parseResult called with resultCode: $resultCode")
            }
        }
    }
}

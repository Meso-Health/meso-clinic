package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.ethiopia.fragment_claims_list.claims_list
import kotlinx.android.synthetic.ethiopia.fragment_claims_list.total_claims_label
import kotlinx.android.synthetic.ethiopia.fragment_claims_list.total_price_label
import org.watsi.device.managers.Logger
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.usecases.LoadReturnedClaimsUseCase
import org.watsi.uhp.R
import org.watsi.uhp.R.plurals.returned_claims_count
import org.watsi.uhp.R.string.returned_claims_count_empty
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.adapters.ClaimListItemAdapter
import org.watsi.uhp.flowstates.EncounterFlowState
import org.watsi.uhp.helpers.RecyclerViewHelper
import org.watsi.uhp.helpers.SnackbarHelper
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.utils.CurrencyUtil
import org.watsi.uhp.viewmodels.SearchableClaimsListViewModel
import javax.inject.Inject

class ReturnedClaimsFragment : DaggerFragment() {
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    @Inject lateinit var loadReturnedClaimsUseCase: LoadReturnedClaimsUseCase

    lateinit var viewModel: SearchableClaimsListViewModel
    lateinit var claimsAdapter: ClaimListItemAdapter

    private var snackbarMessageToShow: String? = null

    companion object {
        const val PARAM_SNACKBAR_MESSAGE = "snackbar_message"

        fun withSnackbarMessage(message: String): ReturnedClaimsFragment {
            val fragment = ReturnedClaimsFragment()
            fragment.arguments = Bundle().apply {
                putString(PARAM_SNACKBAR_MESSAGE, message)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SearchableClaimsListViewModel::class.java)
        setAndObserveViewModel()

        snackbarMessageToShow = arguments?.getString(PARAM_SNACKBAR_MESSAGE)
        claimsAdapter = ClaimListItemAdapter(
            onClaimSelected = { encounterRelation ->
                navigationManager.goTo(ReceiptFragment.forEncounter(
                    EncounterFlowState.fromEncounterWithExtras(encounterRelation)
                ))
            }
        )
    }

    fun setAndObserveViewModel() {
        val observable = viewModel.getObservable(loadReturnedClaimsUseCase)
        observable.observe(this, Observer {
            it?.let { viewState ->
                updateClaims(viewState.visibleClaims)
            }
        })
    }

    private fun updateClaims(returnedClaims: List<EncounterWithExtras>) {
        total_claims_label.text = if (returnedClaims.isEmpty()) {
            getString(returned_claims_count_empty)
        } else {
            resources.getQuantityString(
                returned_claims_count, returnedClaims.size, returnedClaims.size
            )
        }

        total_price_label.text = CurrencyUtil.formatMoneyWithCurrency(context, returnedClaims.sumBy { it.price() })

        claimsAdapter.setClaims(returnedClaims)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(
            context.getString(org.watsi.uhp.R.string.returned_claims_fragment_label),
            org.watsi.uhp.R.drawable.ic_arrow_back_white_24dp
        )
        setHasOptionsMenu(true)
        return inflater?.inflate(org.watsi.uhp.R.layout.fragment_claims_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        RecyclerViewHelper.setRecyclerView(claims_list, claimsAdapter, context, true)
        snackbarMessageToShow?.let { snackbarMessage ->
            SnackbarHelper.show(claims_list, context, snackbarMessage)
            snackbarMessageToShow = null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.claims_search, menu)

        val searchItem = menu.findItem(R.id.search_claims)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(query: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                searchView.clearFocus()
                viewModel.filterClaimsBySearchText(query)
                return false
            }
        })

        searchView.setOnCloseListener(object : SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                viewModel.filterClaimsBySearchText("")
                return false
            }
        })
        searchView.isIconified = true
    }

    override fun onResume() {
        super.onResume()

        // this is required for when the user back navigates into this screen
        // the observable does not trigger, so we need to set the adapter from the viewModel
        viewModel.getClaims()?.let { updateClaims(it.first) }
    }
}

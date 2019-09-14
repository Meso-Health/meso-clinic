package org.watsi.uhp.fragments

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.ethiopia.fragment_claims_list.claims_list
import kotlinx.android.synthetic.ethiopia.fragment_claims_list.select_all_checkbox
import kotlinx.android.synthetic.ethiopia.fragment_claims_list.submit_button
import kotlinx.android.synthetic.ethiopia.fragment_claims_list.total_claims_label
import kotlinx.android.synthetic.ethiopia.fragment_claims_list.total_price_label
import org.threeten.bp.Clock
import org.watsi.device.managers.Logger
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.usecases.LoadPendingClaimsUseCase
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.adapters.ClaimListItemAdapter
import org.watsi.uhp.flowstates.EncounterFlowState
import org.watsi.uhp.helpers.RecyclerViewHelper
import org.watsi.uhp.helpers.SnackbarHelper
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.utils.CurrencyUtil
import org.watsi.uhp.viewmodels.SearchableClaimsListViewModel
import javax.inject.Inject

class PendingClaimsFragment : DaggerFragment() {
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    @Inject lateinit var loadPendingClaimsUseCase: LoadPendingClaimsUseCase
    @Inject lateinit var clock: Clock

    lateinit var viewModel: SearchableClaimsListViewModel
    lateinit var claimsAdapter: ClaimListItemAdapter
    lateinit var observable: LiveData<SearchableClaimsListViewModel.ViewState>

    private var snackbarMessageToShow: String? = null

    companion object {
        const val PARAM_SNACKBAR_MESSAGE = "snackbar_message"

        fun withSnackbarMessage(message: String): PendingClaimsFragment {
            val fragment = PendingClaimsFragment()
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
            clock = clock,
            onClaimSelected = { encounterRelation ->
                navigationManager.goTo(ReceiptFragment.forEncounter(
                    EncounterFlowState.fromEncounterWithExtras(encounterRelation)
                ))
            },
            onCheck = { encounterRelation ->
                viewModel.updateSelectedClaims(encounterRelation)
            }
        )
    }

    private fun setAndObserveViewModel() {
        observable = viewModel.getObservable(loadPendingClaimsUseCase)
        observable.observe(this, Observer {
            it?.let { viewState ->
                updateClaims(viewState.visibleClaims, viewState.selectedClaims)

                if (viewState.visibleClaims.count() > 0 && viewState.visibleClaims.count() == viewState.claims.count()) {
                    submit_button.visibility = View.VISIBLE
                } else {
                    submit_button.visibility = View.GONE
                }

                submit_button.isEnabled = viewState.selectedClaims.count() > 0
                // require there to be claims to avoid flashing on initial load
                select_all_checkbox.isChecked = viewState.claims.count() > 0 &&
                        viewState.claims == viewState.selectedClaims
            }
        })
    }

    private fun updateClaims(
        visibleClaims: List<EncounterWithExtras>,
        selectedClaims: List<EncounterWithExtras>
    ) {
        total_claims_label.text = if (visibleClaims.isEmpty()) {
            getString(R.string.pending_claims_count_empty)
        } else {
            resources.getQuantityString(
                R.plurals.pending_claims_count, visibleClaims.size, visibleClaims.size
            )
        }

        total_price_label.text = CurrencyUtil.formatMoneyWithCurrency(context, visibleClaims.sumBy { it.price() })

        claimsAdapter.setClaims(visibleClaims, selectedClaims)
    }

    private fun submitAll() {
        viewModel.submitSelected().subscribe({
            SnackbarHelper.show(claims_list, context, getString(R.string.claims_submitted))
        }, {
            logger.error(it)
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(
            context.getString(org.watsi.uhp.R.string.pending_claims_fragment_label),
            org.watsi.uhp.R.drawable.ic_arrow_back_white_24dp
        )
        setHasOptionsMenu(true)
        return inflater?.inflate(org.watsi.uhp.R.layout.fragment_claims_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        RecyclerViewHelper.setRecyclerView(claims_list, claimsAdapter, context, false)

        select_all_checkbox.visibility = View.VISIBLE
        // intercept touch event so we can manage checked state via ViewState
        select_all_checkbox.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                viewModel.toggleSelectAll(!select_all_checkbox.isChecked)
                true
            } else {
                false
            }
        }

        submit_button.setOnClickListener {
            AlertDialog.Builder(activity)
                    .setTitle(R.string.temp_submit_selected_claims)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.submit_encounter_button) { _, _ ->
                        submitAll()
                    }.create().show()
        }

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
        viewModel.getClaims()?.let { updateClaims(it.first, it.second) }
    }
}

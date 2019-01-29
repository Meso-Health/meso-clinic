package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.ethiopia.fragment_claims_list.claims_list
import kotlinx.android.synthetic.ethiopia.fragment_claims_list.submit_all_button
import kotlinx.android.synthetic.ethiopia.fragment_claims_list.total_claims_label
import kotlinx.android.synthetic.ethiopia.fragment_claims_list.total_price_label
import org.watsi.device.managers.Logger
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.adapters.ClaimListItemAdapter
import org.watsi.uhp.flowstates.EncounterFlowState
import org.watsi.uhp.helpers.RecyclerViewHelper
import org.watsi.uhp.helpers.SnackbarHelper
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.utils.CurrencyUtil
import org.watsi.uhp.viewmodels.PendingClaimsViewModel
import javax.inject.Inject

class PendingClaimsFragment : DaggerFragment() {
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger

    lateinit var viewModel: PendingClaimsViewModel
    lateinit var claimsAdapter: ClaimListItemAdapter

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

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PendingClaimsViewModel::class.java)
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

    private fun setAndObserveViewModel() {
        viewModel.getObservable().observe(this, Observer {
            it?.let { viewState ->
                updateClaims(viewState.visibleClaims)

                if (viewState.visibleClaims.count() > 0 && viewState.visibleClaims.count() == viewState.claims.count()) {
                    submit_all_button.visibility = View.VISIBLE
                } else {
                    submit_all_button.visibility = View.GONE
                }
            }
        })
    }

    private fun updateClaims(pendingClaims: List<EncounterWithExtras>) {
        total_claims_label.text = if (pendingClaims.isEmpty()) {
            getString(R.string.pending_claims_count_empty)
        } else {
            resources.getQuantityString(
                R.plurals.pending_claims_count, pendingClaims.size, pendingClaims.size
            )
        }

        total_price_label.text = CurrencyUtil.formatMoney(pendingClaims.sumBy { it.price() })

        claimsAdapter.setClaims(pendingClaims)
    }

    private fun submitAll() {
        viewModel.submitAll().subscribe({
            SnackbarHelper.show(claims_list, context, getString(R.string.all_encounter_submitted))
        }, {
            logger.error(it)
        })
    }

    private fun filterClaimsBySearchText(text: String) {
        viewModel.filterClaimsBySearchText(text)
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

        submit_all_button.setOnClickListener {
            AlertDialog.Builder(activity)
                    /* TODO: We did not have the necessary translations to support this. When they come in, put this code back.
                    .setTitle(resources.getQuantityString(
                        R.plurals.submit_all_form_alert, claimsAdapter.itemCount, claimsAdapter.itemCount)
                    )
                    */
                    .setTitle(R.string.temp_submit_all_claims)
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
        setAndObserveViewModel()
    }
}

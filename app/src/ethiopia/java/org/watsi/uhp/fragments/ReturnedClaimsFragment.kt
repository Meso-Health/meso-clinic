package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import org.watsi.uhp.R.plurals.returned_claims_count
import org.watsi.uhp.R.string.returned_claims_count_empty
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.ethiopia.fragment_returned_claims.returned_claims_list
import kotlinx.android.synthetic.ethiopia.fragment_returned_claims.total_claims_label
import kotlinx.android.synthetic.ethiopia.fragment_returned_claims.total_price_label
import org.watsi.device.managers.Logger
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.adapters.ReturnedClaimListItemAdapter
import org.watsi.uhp.flowstates.EncounterFlowState
import org.watsi.uhp.helpers.RecyclerViewHelper
import org.watsi.uhp.helpers.SnackbarHelper
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.utils.CurrencyUtil
import org.watsi.uhp.viewmodels.ReturnedClaimsViewModel
import javax.inject.Inject

class ReturnedClaimsFragment : DaggerFragment() {
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger

    lateinit var viewModel: ReturnedClaimsViewModel
    lateinit var returnedClaimsAdapter: ReturnedClaimListItemAdapter

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

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ReturnedClaimsViewModel::class.java)
        val observable = viewModel.getObservable()
        observable.observe(this, Observer {
            it?.let { viewState ->
                updateReturnedClaims(viewState.returnedEncounters)
            }
        })

        snackbarMessageToShow = arguments?.getString(PARAM_SNACKBAR_MESSAGE)
    }

    private fun updateReturnedClaims(returnedClaims: List<EncounterWithExtras>) {
        total_claims_label.text = if (returnedClaims.isEmpty()) {
            getString(returned_claims_count_empty)
        } else {
            resources.getQuantityString(
                returned_claims_count, returnedClaims.size, returnedClaims.size
            )
        }

        total_price_label.text = CurrencyUtil.formatMoney(returnedClaims.sumBy { it.price() })

        returnedClaimsAdapter.setReturnedClaimsItems(returnedClaims)
        RecyclerViewHelper.setRecyclerView(returned_claims_list, returnedClaimsAdapter, context, true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(
            context.getString(org.watsi.uhp.R.string.returned_claims_fragment_label), org.watsi.uhp.R.drawable.ic_arrow_back_white_24dp
        )
        (activity as ClinicActivity).setSoftInputModeToResize()
        setHasOptionsMenu(false)
        return inflater?.inflate(org.watsi.uhp.R.layout.fragment_returned_claims, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        returnedClaimsAdapter = ReturnedClaimListItemAdapter(
            onReturnedClaimSelected = { encounterRelation ->
                navigationManager.goTo(ReceiptFragment.forEncounter(
                    EncounterFlowState.fromEncounterWithExtras(encounterRelation)
                ))
            }
        )

        snackbarMessageToShow?.let { snackbarMessage ->
            SnackbarHelper.show(returned_claims_list, context, snackbarMessage)
            snackbarMessageToShow = null
        }
    }

    override fun onResume() {
        super.onResume()

        // this is required for when the user back navigates into this screen
        // the observable does not trigger, so we need to set the adapter from the viewModel
        viewModel.getReturnedClaims()?.let { returnedClaims ->
            updateReturnedClaims(returnedClaims)
        }
    }

    override fun onPause() {
        super.onPause()
        (activity as ClinicActivity).setSoftInputModeToPan()
    }

}

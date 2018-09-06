package org.watsi.uhp.fragments

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import dagger.android.support.DaggerFragment
import io.reactivex.Single
import kotlinx.android.synthetic.main.fragment_spinner_line_item.billable_spinner
import kotlinx.android.synthetic.main.fragment_spinner_line_item.container
import kotlinx.android.synthetic.main.fragment_spinner_line_item.line_item_count
import kotlinx.android.synthetic.main.fragment_spinner_line_item.line_items_list
import kotlinx.android.synthetic.main.fragment_spinner_line_item.save_button
import kotlinx.android.synthetic.main.fragment_spinner_line_item.select_billable_box
import org.threeten.bp.Clock
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Billable
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.adapters.EncounterItemAdapter
import org.watsi.uhp.flowstates.EncounterFlowState
import org.watsi.uhp.helpers.RecyclerViewHelper
import org.watsi.uhp.helpers.SnackbarHelper
import org.watsi.uhp.helpers.scrollToBottom
import org.watsi.uhp.helpers.setBottomPadding
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.SpinnerLineItemViewModel
import java.util.UUID
import javax.inject.Inject

class SpinnerLineItemFragment : DaggerFragment(), NavigationManager.HandleOnBack {
    @Inject lateinit var clock: Clock
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var keyboardManager: KeyboardManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger

    lateinit var viewModel: SpinnerLineItemViewModel
    lateinit var observable: LiveData<SpinnerLineItemViewModel.ViewState>
    lateinit var billableAdapter: ArrayAdapter<BillablePresenter>
    lateinit var encounterItemAdapter: EncounterItemAdapter
    lateinit var showSaveButtonRunnable: Runnable
    lateinit var billableType: Billable.Type
    lateinit var encounterFlowState: EncounterFlowState

    companion object {
        const val PARAM_ENCOUNTER = "encounter"
        const val PARAM_BILLABLE_TYPE = "billable_type"
        const val SHOW_BUTTON_DELAY_TIME_IN_MS = 200L

        fun forEncounter(type: Billable.Type, encounter: EncounterFlowState): SpinnerLineItemFragment {
            val fragment = SpinnerLineItemFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_ENCOUNTER, encounter)
                putSerializable(PARAM_BILLABLE_TYPE, type)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        encounterFlowState = arguments.getSerializable(PARAM_ENCOUNTER) as EncounterFlowState
        billableType = arguments.getSerializable(PARAM_BILLABLE_TYPE) as Billable.Type

        billableAdapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SpinnerLineItemViewModel::class.java)
        observable = viewModel.getObservable(encounterFlowState, billableType)
        observable.observe(this, Observer {
            it?.let { viewState ->
                val billableOptions = viewState.selectableBillables.map {
                    BillablePresenter(it, context)
                }.toMutableList()
                billableOptions.add(0, BillablePresenter(null, context))
                billableAdapter.clear()
                billableAdapter.addAll(billableOptions)
                billable_spinner.setSelection(0)

                updateLineItems(viewState.encounterFlowState)
            }
        })
    }

    private fun updateLineItems(encounterFlowState: EncounterFlowState) {
        val lineItems = encounterFlowState.getEncounterItemsOfType(billableType)
        line_item_count.text = resources.getQuantityString(
                R.plurals.encounter_item_count, lineItems.size, lineItems.size)
        encounterItemAdapter.setEncounterItems(lineItems)
    }

    override fun onResume() {
        super.onResume()

        // this is required for when the user back navigates into this screen
        // the observable does not trigger, so we need to set the adapter from the viewModel
        viewModel.getEncounterFlowState()?.let { encounterFlowState ->
            updateLineItems(encounterFlowState)
        }
    }

    private fun titleResource(): Int {
        return when (billableType) {
            Billable.Type.SERVICE -> R.string.service
            Billable.Type.LAB -> R.string.lab
            else -> 0
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(
                context.getString(titleResource()), R.drawable.ic_arrow_back_white_24dp)
        (activity as ClinicActivity).setSoftInputModeToResize()
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_spinner_line_item, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        showSaveButtonRunnable = Runnable({
            save_button?.let { it.visibility = View.VISIBLE }
        })

        container.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                keyboardManager.hideKeyboard(v)
                onHideKeyboard()
            }
        }

        encounterItemAdapter = EncounterItemAdapter(
                onQuantitySelected = {
                    onShowKeyboard()
                },
                onQuantityChanged = { encounterItemId: UUID, newQuantity: Int? ->
                    if (newQuantity == null || newQuantity == 0) {
                        SnackbarHelper.show(save_button, context, R.string.error_blank_or_zero_quantity)
                    } else {
                        viewModel.setItemQuantity(encounterItemId, newQuantity)
                    }
                },
                onRemoveEncounterItem = { encounterItemId: UUID ->
                    viewModel.removeItem(encounterItemId)
                },
                onPriceTap = null
        )

        RecyclerViewHelper.setRecyclerView(
            recyclerView = line_items_list,
            adapter = encounterItemAdapter,
            context = context,
            onSwipe = { position: Int -> encounterItemAdapter.removeAt(position) }
        )

        billable_spinner.adapter = billableAdapter
        billable_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                /* no-op */
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                billableAdapter.getItem(position).billable?.let { viewModel.addItem(it) }
                line_items_list.scrollToBottom()
            }
        }

        save_button.setOnClickListener {
            viewModel.getEncounterFlowState()?.let { encounterFlowState ->
                when (billableType) {
                    Billable.Type.SERVICE -> {
                        navigationManager.goTo(SpinnerLineItemFragment.forEncounter(
                                Billable.Type.LAB, encounterFlowState))
                    }
                    Billable.Type.LAB -> {
                        navigationManager.goTo(DrugAndSupplyFragment.forEncounter(encounterFlowState))
                    }
                    else -> {
                        logger.error("Unsupported Billable.Type for SpinnerLineItemFragment")
                    }
                }
            } ?: run {
                logger.error("EncounterFlowState not set")
            }
        }
    }

    /**
     * Android has no native method to detect whether the keyboard is showing, so this is a proxy
     * method that should be called in places where we assume the keyboard has appeared.
     */
    private fun onShowKeyboard(hidePanel: Boolean = true) {
        line_items_list.setBottomPadding(0)

        if (hidePanel) {
            select_billable_box.visibility = View.GONE
        }

        // If the delayed "show button" task has not run at this point, make sure we stop it
        // so it doesn't run and show the button. This handles race conditions like the case
        // where a user deselects an EditText (triggering the "show button" delayed task)
        // and quickly reselects another EditText (before the delayed task has run).
        save_button.removeCallbacks(showSaveButtonRunnable)
        save_button.visibility = View.GONE
    }

    /**
     * Android has no native method to detect whether the keyboard has been hidden, so this is a proxy
     * method that should be called in places where we assume the keyboard has been hidden.
     */
    private fun onHideKeyboard() {
        line_items_list.setBottomPadding(context.resources.getDimensionPixelSize(R.dimen.scrollingFragmentBottomPadding))

        select_billable_box.visibility = View.VISIBLE

        if (save_button.visibility != View.VISIBLE) {
            // Delay showing the button to prevent jumpy visual behavior.
            save_button.postDelayed(showSaveButtonRunnable, SHOW_BUTTON_DELAY_TIME_IN_MS)
        }
    }

    override fun onBack(): Single<Boolean> {
        return Single.fromCallable {
            viewModel.getEncounterFlowState()?.encounterItemRelations?.let {
                encounterFlowState.encounterItemRelations = it
            }
            true
        }
    }

    override fun onPause() {
        super.onPause()
        (activity as ClinicActivity).setSoftInputModeToPan()
    }

    /**
     * Used to customize toString behavior for use in an ArrayAdapter
     */
    data class BillablePresenter(val billable: Billable?, val context: Context) {
        override fun toString(): String = billable?.name ?: context.getString(R.string.select_prompt)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navigationManager.goBack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

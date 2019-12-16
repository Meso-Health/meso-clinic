package org.watsi.uhp.fragments

import android.app.AlertDialog
import android.app.SearchManager
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.database.MatrixCursor
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SimpleCursorAdapter
import dagger.android.support.DaggerFragment
import io.reactivex.Single
import kotlinx.android.synthetic.uganda.fragment_encounter.add_billable_prompt
import kotlinx.android.synthetic.uganda.fragment_encounter.billable_spinner
import kotlinx.android.synthetic.uganda.fragment_encounter.drug_search
import kotlinx.android.synthetic.uganda.fragment_encounter.encounter_item_count
import kotlinx.android.synthetic.uganda.fragment_encounter.fragment_encounter_container
import kotlinx.android.synthetic.uganda.fragment_encounter.line_items_list
import kotlinx.android.synthetic.uganda.fragment_encounter.save_button
import kotlinx.android.synthetic.uganda.fragment_encounter.select_billable_box
import kotlinx.android.synthetic.uganda.fragment_encounter.select_lab_result_box
import kotlinx.android.synthetic.uganda.fragment_encounter.select_type_box
import org.threeten.bp.Clock
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.LabResult
import org.watsi.domain.utils.titleize
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.adapters.EncounterItemAdapter
import org.watsi.uhp.flowstates.EncounterFlowState
import org.watsi.uhp.helpers.LayoutHelper
import org.watsi.uhp.helpers.QueryHelper
import org.watsi.uhp.helpers.RecyclerViewHelper
import org.watsi.uhp.helpers.SnackbarHelper
import org.watsi.uhp.helpers.SwipeHandler
import org.watsi.uhp.helpers.scrollToBottom
import org.watsi.uhp.helpers.setBottomPadding
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.EncounterViewModel
import java.util.UUID
import javax.inject.Inject

class EncounterFragment : DaggerFragment(), NavigationManager.HandleOnBack {

    @Inject lateinit var clock: Clock
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var keyboardManager: KeyboardManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger

    lateinit var viewModel: EncounterViewModel
    lateinit var observable: LiveData<EncounterViewModel.ViewState>
    lateinit var encounterItemAdapter: EncounterItemAdapter
    lateinit var swipeHandler: SwipeHandler
    lateinit var encounterFlowState: EncounterFlowState
    lateinit var showSaveButtonRunnable: Runnable

    companion object {
        const val PARAM_ENCOUNTER = "encounter"
        const val SHOW_BUTTON_DELAY_TIME_IN_MS = 200L

        fun forEncounter(encounter: EncounterFlowState): EncounterFragment {
            val fragment = EncounterFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_ENCOUNTER, encounter)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        encounterFlowState = arguments.getSerializable(PARAM_ENCOUNTER) as EncounterFlowState

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EncounterViewModel::class.java)
        observable = viewModel.getObservable(encounterFlowState)
        viewModel.getBillableTypeObservable().observe( this, Observer {
            it?.let { billableTypes ->
                select_type_box.setUpWithPrompt(
                    choices = billableTypes.map { it.toString().titleize() },
                    initialChoice = null,
                    onItemSelected = { index ->
                        viewModel.selectType(billableTypes[index])
                    },
                    promptString = getString(R.string.prompt_category),
                    onPromptSelected = { viewModel.selectType(null) }
                )
            }
        })
        observable.observe(this, Observer {
            it?.let { viewState ->
                when (viewState.type) {
                    null -> {
                        billable_spinner.visibility = View.GONE
                        drug_search.visibility = View.GONE
                        select_lab_result_box.visibility = View.GONE
                    }
                    Billable.Type.DRUG -> {
                        billable_spinner.visibility = View.GONE
                        val cursor = buildSearchResultCursor(viewState.selectableBillables.map { it.billable })
                        drug_search.suggestionsAdapter.changeCursor(cursor)
                        drug_search.visibility = View.VISIBLE
                    }
                    else -> {
                        val billableOptions = viewState.selectableBillables
                        drug_search.visibility = View.GONE

                        billable_spinner.visibility = View.VISIBLE
                        if (viewState.billableWithPriceSchedule == null) {
                            billable_spinner.setUpWithPrompt(
                                choices = billableOptions.map { it.billable.name },
                                initialChoice = null,
                                onItemSelected = { index ->
                                    val selectedBillable = billableOptions[index]
                                    viewModel.onSelectedBillable(selectedBillable)
                                },
                                promptString = "Select...",
                                onPromptSelected = { viewModel.onSelectedBillable(null) }
                            )
                        }
                    }
                }

                if (viewModel.requiresLabResult()) {
                    select_lab_result_box.visibility = View.VISIBLE
                    val labResultChoices = LabResult.malariaTestResults()
                    select_lab_result_box.setUpWithPrompt(
                        choices = labResultChoices,
                        initialChoice = null,
                        onItemSelected = { index: Int ->
                            viewModel.onLabResultChange(labResultChoices[index])
                        },
                        promptString = "Select a lab result...",
                        onPromptSelected = { /* no-op */ }
                    )
                } else {
                    select_lab_result_box.visibility = View.GONE
                }
                updateLineItems(viewState.encounterFlowState)
            }
        })
    }

    private fun updateLineItems(encounterFlowState: EncounterFlowState) {
        val lineItems = encounterFlowState.encounterItemRelations
        encounter_item_count.text = resources.getQuantityString(
            R.plurals.encounter_item_count, lineItems.size, lineItems.size)
        encounterItemAdapter.setEncounterItems(lineItems)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(context.getString(R.string.encounter_fragment_label), R.drawable.ic_arrow_back_white_24dp)
        (activity as ClinicActivity).setSoftInputModeToResize()
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_encounter, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        showSaveButtonRunnable = Runnable({
            save_button?.let { it.visibility = View.VISIBLE }
        })

        fragment_encounter_container.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                keyboardManager.hideKeyboard(v)
                onHideKeyboard()
            }
        }

        val onPriceTap = { encounterItemId: UUID ->
            viewModel.currentEncounterItems()?.let { encounterItemRelationsFromViewModel ->
                encounterFlowState.encounterItemRelations = encounterItemRelationsFromViewModel
                navigationManager.goTo(
                    EditPriceFragment.forEncounterItem(encounterItemId, encounterFlowState)
                )
            } ?: run {
                logger.error("EncounterFlowState not set")
            }
        }

        val onSurgicalScoreTap = { encounterItemId: UUID ->
            viewModel.currentEncounterItems()?.let { encounterItemRelationsFromViewModel ->
                encounterFlowState.encounterItemRelations = encounterItemRelationsFromViewModel
                launchSurgicalScoreModal(encounterItemId)
            } ?: run {
                logger.error("EncounterFlowState not set")
            }
        }

        encounterItemAdapter = EncounterItemAdapter(
                onQuantitySelected = {
                    swipeHandler.disableSwipe()
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
                onPriceTap = onPriceTap,
                onSurgicalScoreTap = onSurgicalScoreTap
        )


        swipeHandler = SwipeHandler(context, onSwipe = { position: Int ->
            AlertDialog.Builder(activity)
                    .setTitle(getString(R.string.delete_items_confirmation))
                    .setPositiveButton(R.string.yes) { _, _ ->
                        encounterItemAdapter.removeAt(position)
                    }
                    .setNegativeButton(R.string.cancel) { _, _ ->
                        // This call is necessary to force a redraw of the adapter. If its not included
                        // the line item will stay red with the trash icon as it was at the end of the swipe
                        encounterItemAdapter.notifyDataSetChanged()
                    }
                    .setCancelable(false)
                    .create().show()
        })

        RecyclerViewHelper.setRecyclerView(
            recyclerView = line_items_list,
            adapter = encounterItemAdapter,
            context = context,
            swipeHandler = swipeHandler
        )

        drug_search.suggestionsAdapter = SimpleCursorAdapter(
                activity, R.layout.item_billable_search_suggestion, null,
                arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2),
                intArrayOf(R.id.text1, R.id.text2), 0)
        drug_search.setOnQueryTextListener(QueryHelper.ThrottledQueryListener(
            drug_search,
            { query: String -> viewModel.updateQuery(query) }
        ))
        drug_search.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) { onShowKeyboard(hidePanel = false) }
        }
        drug_search.setOnSuggestionListener(object : android.widget.SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean = true

            override fun onSuggestionClick(position: Int): Boolean {
                observable.value?.selectableBillables?.get(position)?.let {
                    viewModel.addItem(it)
                    line_items_list.scrollToBottom()
                    drug_search.setQuery("", false)
                }
                return true
            }
        })

        add_billable_prompt.setOnClickListener {
            encounterFlowState.encounterItemRelations = viewModel.currentEncounterItems().orEmpty()
            navigationManager.goTo(AddNewBillableFragment.forEncounter(encounterFlowState))
        }

        save_button.setOnClickListener {
            viewModel.currentEncounterItems()?.let { encounterItems ->
                if (encounterItems.isEmpty()) {
                    SnackbarHelper.show(save_button, context, R.string.no_line_items_snackbar_message)
                } else {
                    encounterFlowState.encounterItemRelations = encounterItems
                    navigationManager.goTo(DiagnosisFragment.forEncounter(encounterFlowState))
                }
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
            select_type_box.visibility = View.GONE
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

        if (select_type_box.visibility != View.VISIBLE) {
            select_type_box.visibility = View.VISIBLE
            select_billable_box.visibility = View.VISIBLE
        }

        if (save_button.visibility != View.VISIBLE) {
            // Delay showing the button to prevent jumpy visual behavior.
            save_button.postDelayed(showSaveButtonRunnable, SHOW_BUTTON_DELAY_TIME_IN_MS)
        }
    }

    override fun onPause() {
        super.onPause()
        (activity as ClinicActivity).setSoftInputModeToPan()
    }

    private fun buildSearchResultCursor(billables: List<Billable>): MatrixCursor {
        val cursorColumns = arrayOf("_id", SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2, "id")
        val cursor = MatrixCursor(cursorColumns)
        billables.forEach {
            cursor.addRow(arrayOf(it.id.mostSignificantBits, it.name, it.details(), it.id.toString()))
        }
        return cursor
    }

    override fun onBack(): Single<Boolean> {
        return Single.fromCallable {
            viewModel.updateEncounterWithLineItems(encounterFlowState)
            true
        }
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
    private fun launchSurgicalScoreModal(encounterItemId: UUID) {
        val builder = android.support.v7.app.AlertDialog.Builder(context)

        val items= arrayOf(
            "1: Failure",
            "2",
            "3",
            "4",
            "5: Success"
        )

        val checkedScore = viewModel.getSurgicalScore(encounterItemId) ?: 0
        builder.setTitle(R.string.surgical_score_prompt)
            .setSingleChoiceItems(items, checkedScore - 1, { _, which ->
                viewModel.setSurgicalScore(encounterItemId, which + 1)
            }).setPositiveButton(R.string.modal_save, { dialog, _ ->
                (dialog as android.support.v7.app.AlertDialog).dismiss()
            })
            .setNegativeButton(R.string.modal_delete, { dialogInterface, _ ->
                viewModel.setSurgicalScore(encounterItemId, null)
                (dialogInterface as android.support.v7.app.AlertDialog).dismiss()
            })

        val dialog = builder.create()

        dialog.show()
    }
}

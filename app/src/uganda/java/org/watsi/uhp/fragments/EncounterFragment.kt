package org.watsi.uhp.fragments

import android.app.SearchManager
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.database.MatrixCursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SimpleCursorAdapter
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.uganda.fragment_encounter.add_billable_prompt
import kotlinx.android.synthetic.uganda.fragment_encounter.billable_spinner
import kotlinx.android.synthetic.uganda.fragment_encounter.drug_search
import kotlinx.android.synthetic.uganda.fragment_encounter.encounter_item_count
import kotlinx.android.synthetic.uganda.fragment_encounter.fragment_encounter_container
import kotlinx.android.synthetic.uganda.fragment_encounter.line_items_list
import kotlinx.android.synthetic.uganda.fragment_encounter.save_button
import kotlinx.android.synthetic.uganda.fragment_encounter.select_billable_box
import kotlinx.android.synthetic.uganda.fragment_encounter.select_type_box
import kotlinx.android.synthetic.uganda.fragment_encounter.type_spinner
import org.threeten.bp.Clock
import org.watsi.domain.entities.Billable
import org.watsi.domain.utils.titleize
import org.watsi.uhp.R
import org.watsi.uhp.R.string.prompt_category
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.adapters.EncounterItemAdapter
import org.watsi.uhp.flowstates.EncounterFlowState
import org.watsi.uhp.helpers.RecyclerViewHelper
import org.watsi.uhp.helpers.SnackbarHelper
import org.watsi.uhp.helpers.scrollToBottom
import org.watsi.uhp.helpers.setBottomMargin
import org.watsi.uhp.helpers.setBottomPadding
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.EncounterViewModel
import java.util.UUID
import javax.inject.Inject

class EncounterFragment : DaggerFragment() {

    @Inject lateinit var clock: Clock
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var keyboardManager: KeyboardManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: EncounterViewModel
    lateinit var observable: LiveData<EncounterViewModel.ViewState>
    lateinit var billableTypeAdapter: ArrayAdapter<String>
    lateinit var billableAdapter: ArrayAdapter<BillablePresenter>
    lateinit var encounterItemAdapter: EncounterItemAdapter
    lateinit var encounterFlowState: EncounterFlowState


    companion object {
        const val PARAM_ENCOUNTER = "encounter"

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

        val billableTypeOptions = Billable.Type.values()
                .map { it.toString().titleize() }
                .toMutableList()
        billableTypeOptions.add(0, getString(prompt_category))
        billableTypeAdapter = ArrayAdapter(
                activity, android.R.layout.simple_list_item_1, billableTypeOptions)
        billableAdapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EncounterViewModel::class.java)
        observable = viewModel.getObservable(encounterFlowState.encounter.id,
                                             encounterFlowState.encounterItems)
        observable.observe(this, Observer {
            it?.let { viewState ->
                if (viewState.type == null) {
                    type_spinner.setSelection(0)
                }
                when (viewState.type) {
                    null -> {
                        billable_spinner.visibility = View.GONE
                        drug_search.visibility = View.GONE
                    }
                    Billable.Type.DRUG -> {
                        billable_spinner.visibility = View.GONE
                        val cursor = buildSearchResultCursor(viewState.selectableBillables)
                        drug_search.suggestionsAdapter.changeCursor(cursor)
                        drug_search.visibility = View.VISIBLE
                    }
                    else -> {
                        val billableOptions = viewState.selectableBillables.map {
                            BillablePresenter(it)
                        }.toMutableList()
                        billableOptions.add(0, BillablePresenter(null))
                        billableAdapter.clear()
                        billableAdapter.addAll(billableOptions)
                        billable_spinner.setSelection(0)
                        billable_spinner.visibility = View.VISIBLE
                        drug_search.visibility = View.GONE
                    }
                }
                viewState.let {
                    encounter_item_count.text = resources.getQuantityString(
                            R.plurals.encounter_item_count, it.encounterItems.size, it.encounterItems.size)
                    encounterItemAdapter.setEncounterItems(it.encounterItems)
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(context.getString(R.string.encounter_fragment_label), R.drawable.ic_clear_white_24dp)
        (activity as ClinicActivity).setSoftInputModeToNothing()
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_encounter, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        fragment_encounter_container.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                keyboardManager.hideKeyboard(v)
                select_type_box.visibility = View.VISIBLE
                select_billable_box.visibility = View.VISIBLE

                line_items_list.setBottomPadding(context.resources.getDimensionPixelSize(R.dimen.scrollingFragmentBottomPadding))
                line_items_list.setBottomMargin(0)
            }
        }

        encounterItemAdapter = EncounterItemAdapter(
                onQuantitySelected = {
                    select_type_box.visibility = View.GONE
                    select_billable_box.visibility = View.GONE

                    // manually resize recyclerView when keyboard is shown since neither adjustPan
                    // nor adjustResize accomplishes the intended behavior.
                    line_items_list.setBottomPadding(0)
                    line_items_list.setBottomMargin(context.resources.getDimensionPixelSize(R.dimen.numericKeyboardHeight))
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
                keyboardManager = keyboardManager
        )

        RecyclerViewHelper.setRecyclerView(line_items_list, encounterItemAdapter, context)

        type_spinner.adapter = billableTypeAdapter
        val typeSpinnerListener =  object : AdapterView.OnItemSelectedListener, View.OnTouchListener {
            var userSelected = false

            override fun onNothingSelected(parent: AdapterView<*>?) {
                /* no-op */
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = if (position > 0) {
                    Billable.Type.valueOf(billableTypeAdapter.getItem(position).toUpperCase())
                } else {
                    null
                }
                viewModel.selectType(selectedType)

                // Distinguish between user-initiated select events and automatically triggered
                // ones (e.g. resuming fragment on back press)
                if (userSelected) {
                    billable_spinner.performClick()
                    userSelected = false
                }
            }

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                userSelected = true
                return false
            }
        }
        type_spinner.onItemSelectedListener = typeSpinnerListener
        type_spinner.setOnTouchListener(typeSpinnerListener)

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

        drug_search.suggestionsAdapter = SimpleCursorAdapter(
                activity, R.layout.item_billable_search_suggestion, null,
                arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2),
                intArrayOf(R.id.text1, R.id.text2), 0)
        drug_search.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.updateQuery(it) }
                return true
            }
        })
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
            encounterFlowState.encounterItems = viewModel.currentEncounterItems().orEmpty()
            navigationManager.goTo(AddNewBillableFragment.forEncounter(encounterFlowState))
        }

        save_button.setOnClickListener {
            viewModel.currentEncounterItems()?.let { encounterItems ->
                if (encounterItems.isEmpty()) {
                    SnackbarHelper.show(save_button, context, R.string.no_line_items_snackbar_message)
                } else {
                    encounterFlowState.encounterItems = encounterItems
                    navigationManager.goTo(DiagnosisFragment.forEncounter(encounterFlowState))
                }
            }
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
            cursor.addRow(arrayOf(it.id.mostSignificantBits, it.name, it.dosageDetails(), it.id.toString()))
        }
        return cursor
    }

    /**
     * Used to customize toString behavior for use in an ArrayAdapter
     */
    data class BillablePresenter(val billable: Billable?) {
        override fun toString(): String = billable?.name ?: "Select..."
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

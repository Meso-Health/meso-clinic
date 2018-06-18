package org.watsi.uhp.fragments

import android.app.SearchManager
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.database.MatrixCursor
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SimpleCursorAdapter
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_encounter.add_billable_prompt
import kotlinx.android.synthetic.main.fragment_encounter.billable_spinner
import kotlinx.android.synthetic.main.fragment_encounter.drug_search
import kotlinx.android.synthetic.main.fragment_encounter.encounter_item_count
import kotlinx.android.synthetic.main.fragment_encounter.line_items_list
import kotlinx.android.synthetic.main.fragment_encounter.save_button
import kotlinx.android.synthetic.main.fragment_encounter.type_spinner
import org.threeten.bp.Clock
import org.watsi.domain.entities.Billable
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.uhp.R
import org.watsi.uhp.R.string.prompt_category
import org.watsi.uhp.adapters.EncounterItemAdapter
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

    companion object {
        const val PARAM_ENCOUNTER = "encounter"

        fun forEncounter(encounter: EncounterWithItemsAndForms): EncounterFragment {
            val fragment = EncounterFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_ENCOUNTER, encounter)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val encounter = arguments.getSerializable(PARAM_ENCOUNTER) as EncounterWithItemsAndForms

        val billableTypeOptions = Billable.Type.values()
                .map { it.toString().toLowerCase().capitalize() }
                .toMutableList()
        billableTypeOptions.add(0, getString(prompt_category))
        billableTypeAdapter = ArrayAdapter(
                activity, android.R.layout.simple_list_item_1, billableTypeOptions)
        billableAdapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EncounterViewModel::class.java)
        observable = viewModel.getObservable(encounter)
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
                viewState.encounter.let {
                    encounter_item_count.text = resources.getQuantityString(
                            R.plurals.encounter_item_count, it.encounterItems.size, it.encounterItems.size)
                    encounterItemAdapter.setEncounterItems(it.encounterItems)
                }
            }
        })

        encounterItemAdapter = EncounterItemAdapter(
                onQuantityChanged = { encounterItemId: UUID, newQuantity: Int ->
                    viewModel.setItemQuantity(encounterItemId, newQuantity)
                },
                onRemoveEncounterItem = { encounterItemId: UUID ->
                    viewModel.removeItem(encounterItemId)
                },
                keyboardManager = keyboardManager
        )
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.encounter_fragment_label)
        return inflater?.inflate(R.layout.fragment_encounter, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        type_spinner.adapter = billableTypeAdapter
        type_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
            }
        }

        billable_spinner.adapter = billableAdapter
        billable_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                /* no-op */
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                billableAdapter.getItem(position).billable?.let { viewModel.addItem(it) }
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
                    drug_search.setQuery("", false)
                }
                return true
            }
        })

        line_items_list.adapter = encounterItemAdapter
        line_items_list.layoutManager = LinearLayoutManager(activity)

        add_billable_prompt.setOnClickListener {
            viewModel.currentEncounter()?.let {
                navigationManager.goTo(AddNewBillableFragment.forEncounter(it))
            }
        }

        save_button.setOnClickListener {
            viewModel.currentEncounter()?.let {
                // TODO: should we allow proceeding with no encounter items?
                navigationManager.goTo(DiagnosisFragment.forEncounter(it))
            }
        }
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
}

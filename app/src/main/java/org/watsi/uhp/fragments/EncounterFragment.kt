package org.watsi.uhp.fragments

import android.app.SearchManager
import android.database.MatrixCursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SimpleCursorAdapter
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_encounter.add_billable_prompt
import kotlinx.android.synthetic.main.fragment_encounter.backdate_encounter
import kotlinx.android.synthetic.main.fragment_encounter.billable_spinner
import kotlinx.android.synthetic.main.fragment_encounter.category_spinner
import kotlinx.android.synthetic.main.fragment_encounter.drug_search
import kotlinx.android.synthetic.main.fragment_encounter.line_items_list
import kotlinx.android.synthetic.main.fragment_encounter.save_button
import org.threeten.bp.Clock
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Encounter

import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.relations.EncounterItemWithBillable
import org.watsi.domain.repositories.BillableRepository
import org.watsi.uhp.R
import org.watsi.uhp.adapters.EncounterItemAdapter
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.runnables.ScrollToBottomRunnable
import java.io.Serializable
import java.util.UUID

import javax.inject.Inject

class EncounterFragment : DaggerFragment() {

    @Inject lateinit var clock: Clock
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var billableRepository: BillableRepository

    lateinit var identificationEvent: IdentificationEvent

    companion object {
        const val PARAM_IDENTIFICATION_EVENT = "identification_event"
        const val PARAM_ENCOUNTER_ITEMS = "encounter_items"
        const val PARAM_BILLABLE = "billable"

        fun forIdentificationEvent(idEvent: IdentificationEvent,
                                   encounterItems: List<EncounterItemWithBillable> = emptyList(),
                                   billable: Billable? = null): EncounterFragment {
            val fragment = EncounterFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_IDENTIFICATION_EVENT, idEvent)
                putSerializable(PARAM_ENCOUNTER_ITEMS, encounterItems as Serializable)
                putSerializable(PARAM_BILLABLE, billable)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        identificationEvent = arguments.getSerializable(PARAM_IDENTIFICATION_EVENT) as IdentificationEvent
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.encounter_fragment_label)
        return inflater?.inflate(R.layout.fragment_encounter, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        line_items_list.adapter = EncounterItemAdapter(
                context, R.layout.item_encounter_item_list, emptyList())

        val prompt = getString(R.string.prompt_category)
        val categoriesArray = arrayOf(prompt).union(Billable.Type.values().map { it.toString() }).toMutableList()

        val categoriesAdapter = ArrayAdapter<String>(
                activity, android.R.layout.simple_spinner_dropdown_item, categoriesArray)

        category_spinner.adapter = categoriesAdapter
        category_spinner.tag = "category"
        category_spinner.onItemSelectedListener = CategoryListener()

        drug_search.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                drug_search.suggestionsAdapter = if (newText != null && newText.length > 3) {
                    createBillableCursorAdapter(newText)
                } else {
                    null
                }
                return true
            }
        })
        drug_search.setOnSuggestionListener(object : android.widget.SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean = true

            override fun onSuggestionClick(position: Int): Boolean {
                val cursor = drug_search.suggestionsAdapter.getItem(position) as MatrixCursor
                val uuidString = cursor.getString(cursor.getColumnIndex("id"))
                val billable = billableRepository.find(UUID.fromString(uuidString))
                // TODO: add to encounter item list
                drug_search.clearFocus()
                drug_search.setQuery("", false)
                scrollToBottomOfList()
                return true
            }
        })
        drug_search.queryHint = getString(R.string.search_drug_hint)

        add_billable_prompt.setOnClickListener {
            // TODO: pass current encounter item list
            navigationManager.goTo(AddNewBillableFragment.forIdentificationEvent(
                    identificationEvent, emptyList()))
        }

        backdate_encounter.setOnClickListener {
            // TODO: open backdate encounter dialog
        }

        save_button.setOnClickListener {
            val encounter = Encounter(id = UUID.randomUUID(),
                                      memberId = identificationEvent.memberId,
                                      identificationEventId = identificationEvent.id,
                                      occurredAt = clock.instant(),
                                      backdatedOccurredAt = null)
            navigationManager.goTo(DiagnosisFragment.forEncounter(encounter))
        }
    }

    private fun createBillableCursorAdapter(query: String): SimpleCursorAdapter {
        val cursorColumns = arrayOf("_id", SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2, "id")
        val cursor = MatrixCursor(cursorColumns)

        billableRepository.fuzzySearchDrugsByName(query).forEach { billable ->
            cursor.addRow(arrayOf(billable.id.mostSignificantBits,
                                  billable.name,
                                  billable.dosageDetails(),
                                  billable.id.toString()))
        }

        return SimpleCursorAdapter(
                activity,
                R.layout.item_billable_search_suggestion,
                cursor,
                arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2),
                intArrayOf(R.id.text1, R.id.text2),
                0)
    }

    private fun createBillableAdapter(category: Billable.Type): ArrayAdapter<Billable> {
        val promptString = "Select a " + category.toString().toLowerCase() + "..."
        val promptBillable = Billable(UUID.randomUUID(), Billable.Type.DRUG, null, null, 0, promptString)
        val billables = arrayOf(promptBillable).union(billableRepository.findByType(category)).toList()
        return ArrayAdapter(activity, android.R.layout.simple_spinner_dropdown_item, billables)
    }

    private fun scrollToBottomOfList() {
        line_items_list.post(ScrollToBottomRunnable(line_items_list))
    }

    inner class CategoryListener : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?)  {/* no-op */ }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            drug_search.visibility = View.GONE
            billable_spinner.visibility = View.GONE

            if (position != 0) {
                val category = Billable.Type.valueOf(parent?.getItemAtPosition(position).toString())
                if (category == Billable.Type.DRUG) {
                    drug_search.visibility = View.VISIBLE
                    KeyboardManager.focusAndForceShowKeyboard(drug_search, activity)
                } else {
                    billable_spinner.adapter = createBillableAdapter(category)
                    billable_spinner.onItemSelectedListener = BillableListener()
                    billable_spinner.visibility = View.VISIBLE
                    billable_spinner.performClick()
                }
            }
        }
    }

    inner class BillableListener : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) { /* no-op */ }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (position != 0) {
                val billable = parent?.adapter?.getItem(position)
                // TODO: add to encounter item list
                billable_spinner.setSelection(0)
                scrollToBottomOfList()
            }
        }
    }
}

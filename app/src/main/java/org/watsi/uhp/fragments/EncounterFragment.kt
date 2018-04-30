package org.watsi.uhp.fragments

import android.app.AlertDialog
import android.app.SearchManager
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.database.MatrixCursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.SimpleCursorAdapter
import android.widget.TimePicker
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_encounter.add_billable_prompt
import kotlinx.android.synthetic.main.fragment_encounter.backdate_encounter
import kotlinx.android.synthetic.main.fragment_encounter.billable_spinner
import kotlinx.android.synthetic.main.fragment_encounter.drug_search
import kotlinx.android.synthetic.main.fragment_encounter.line_items_list
import kotlinx.android.synthetic.main.fragment_encounter.save_button
import kotlinx.android.synthetic.main.fragment_encounter.type_spinner
import org.threeten.bp.Clock
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.watsi.domain.entities.Billable

import org.watsi.domain.entities.IdentificationEvent
import org.watsi.uhp.R
import org.watsi.uhp.R.string.prompt_category
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.EncounterViewModel
import java.io.Serializable

import javax.inject.Inject

class EncounterFragment : DaggerFragment() {

    @Inject lateinit var clock: Clock
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: EncounterViewModel
    lateinit var observable: LiveData<EncounterViewModel.ViewState>
    lateinit var billableTypeAdapter: ArrayAdapter<String>
    lateinit var billableAdapter: ArrayAdapter<BillablePresenter>
    lateinit var lineItemAdapter: ArrayAdapter<LineItemPresenter>

    companion object {
        const val PARAM_IDENTIFICATION_EVENT = "identification_event"
        const val PARAM_LINE_ITEMS = "line_items"

        fun forIdentificationEvent(idEvent: IdentificationEvent,
                                   lineItems: List<Pair<Billable, Int>> = emptyList()): EncounterFragment {
            val fragment = EncounterFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_IDENTIFICATION_EVENT, idEvent)
                // TODO: this Serializable cast is probably broken
                putSerializable(PARAM_LINE_ITEMS, lineItems as Serializable)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val billableTypeOptions = Billable.Type.values().map { it.toString() }.toMutableList()
        billableTypeOptions.add(0, getString(prompt_category))
        billableTypeAdapter = ArrayAdapter(
                activity, android.R.layout.simple_list_item_1, billableTypeOptions)
        billableAdapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1)
        lineItemAdapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EncounterViewModel::class.java)
        val lineItemsFromArgs = arguments.getSerializable(PARAM_LINE_ITEMS) as List<Pair<Billable, Int>>?
        observable = viewModel.getObservable(lineItemsFromArgs ?: emptyList())
        observable.observe(this, Observer {
            it?.let { viewState ->
                viewState.backdatedOccurredAt?.let {
                    backdate_encounter.text = it.toString() // TODO: format
                }
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

                viewState.lineItems.let {
                    // ideally only do this if the line items change or alternatively could choose
                    // to only notify after both clear & add if there is a UI flash
                    lineItemAdapter.clear()
                    lineItemAdapter.addAll(it.map { LineItemPresenter(it.first, it.second) })
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.encounter_fragment_label)
        return inflater?.inflate(R.layout.fragment_encounter, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val identificationEvent =
                arguments.getSerializable(PARAM_IDENTIFICATION_EVENT) as IdentificationEvent

        type_spinner.adapter = billableTypeAdapter
        type_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                /* no-op */
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = if (position > 0) {
                    Billable.Type.valueOf(billableTypeAdapter.getItem(position))
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

        line_items_list.adapter = lineItemAdapter

        add_billable_prompt.setOnClickListener {
            navigationManager.goTo(AddNewBillableFragment.forIdentificationEvent(
                    identificationEvent, viewModel.currentLineItems()))
        }

        backdate_encounter.setOnClickListener {
            launchBackdateDialog()
        }

        save_button.setOnClickListener {
            val encounterWithItemAndForms =
                    viewModel.buildEncounterWithItemsAndForms(identificationEvent)

            if (encounterWithItemAndForms == null) {
                // TODO: do not allow submitting without any encounter items
            } else {
                navigationManager.goTo(DiagnosisFragment.forEncounter(encounterWithItemAndForms))
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

    private fun launchBackdateDialog() {
        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_backdate_encounter, null)
        val datePicker = dialogView.findViewById<View>(R.id.date_picker) as DatePicker
        val timePicker = dialogView.findViewById<View>(R.id.time_picker) as TimePicker

        datePicker.maxDate = clock.instant().toEpochMilli()

        observable.value?.backdatedOccurredAt?.let {
            val ldt = LocalDateTime.ofInstant(it, ZoneId.systemDefault())
            datePicker.updateDate(ldt.year, ldt.monthValue, ldt.dayOfMonth)
            timePicker.hour = ldt.hour
            timePicker.minute = ldt.minute
        }

        val builder = AlertDialog.Builder(context)
        builder.setView(dialogView)

        val dialog = builder.create()

        dialogView.findViewById<View>(R.id.done).setOnClickListener {
            val backdatedOccurredAt = LocalDateTime.of(datePicker.year,
                                                   datePicker.month,
                                                   datePicker.dayOfMonth,
                                                   timePicker.hour,
                                                   timePicker.minute
            ).toInstant(ZoneOffset.UTC) // TODO: fix offset calculation
            viewModel.updateBackdatedOccurredAt(backdatedOccurredAt)
            dialog.dismiss()
        }

        dialogView.findViewById<View>(R.id.cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * Used to customize toString behavior for use in an ArrayAdapter
     */
    data class BillablePresenter(val billable: Billable?) {
        override fun toString(): String = billable?.name ?: "Select..."
    }

    data class LineItemPresenter(val billable: Billable, val quantity: Int) {
        override fun toString(): String {
            return "${billable.name} - $quantity"
        }
    }
}

package org.watsi.uhp.fragments

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_encounter.add_billable_prompt
import kotlinx.android.synthetic.main.fragment_encounter.billable_spinner
import kotlinx.android.synthetic.main.fragment_encounter.drug_search
import kotlinx.android.synthetic.main.fragment_encounter.line_items_list
import kotlinx.android.synthetic.main.fragment_encounter.save_button
import kotlinx.android.synthetic.main.fragment_encounter.type_spinner
import org.threeten.bp.Clock
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterItem

import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.relations.EncounterItemWithBillable
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.uhp.R
import org.watsi.uhp.R.string.prompt_category
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.EncounterViewModel
import java.io.Serializable
import java.util.UUID

import javax.inject.Inject

class EncounterFragment : DaggerFragment() {

    @Inject lateinit var clock: Clock
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: EncounterViewModel
    lateinit var observable: LiveData<EncounterViewModel.ViewState>
    lateinit var billableTypeOptions: Array<String>
    lateinit var billableAdapter: ArrayAdapter<BillablePresenter>
    lateinit var lineItemAdapter: ArrayAdapter<LineItemPresenter>

    companion object {
        const val PARAM_IDENTIFICATION_EVENT = "identification_event"
        const val PARAM_LINE_ITEMS = "line_items"
        const val PARAM_BILLABLE = "billable"

        fun forIdentificationEvent(idEvent: IdentificationEvent,
                                   lineItems: List<Pair<Billable, Int>> = emptyList(),
                                   billable: Billable? = null): EncounterFragment {
            val fragment = EncounterFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_IDENTIFICATION_EVENT, idEvent)
                // TODO: this Serializable cast is probably broken
                putSerializable(PARAM_LINE_ITEMS, lineItems as Serializable)
                putSerializable(PARAM_BILLABLE, billable)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EncounterViewModel::class.java)
        val previousLineItems = arguments.getSerializable(PARAM_LINE_ITEMS) as List<Pair<Billable, Int>>?
        observable = viewModel.getObservable(previousLineItems ?: emptyList())
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
                    lineItemAdapter.clear()
                    lineItemAdapter.addAll(it.map { LineItemPresenter(it.first, it.second) })
                }
            }
        })
        val billableTypes = Billable.Type.values()
        billableTypeOptions = arrayOf(getString(prompt_category)).union(billableTypes.map {
            it.toString()
        }).toTypedArray()
        billableAdapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1)
        lineItemAdapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.encounter_fragment_label)
        return inflater?.inflate(R.layout.fragment_encounter, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val identificationEvent =
                arguments.getSerializable(PARAM_IDENTIFICATION_EVENT) as IdentificationEvent


        type_spinner.adapter = ArrayAdapter<String>(
                activity, android.R.layout.simple_spinner_item, billableTypeOptions)

        type_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                /* no-op */
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = if (position > 0) {
                    Billable.Type.valueOf(billableTypeOptions[position])
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

        line_items_list.adapter = lineItemAdapter

        add_billable_prompt.setOnClickListener {
            // TODO: pass current encounter item list
            navigationManager.goTo(AddNewBillableFragment.forIdentificationEvent(
                    identificationEvent, emptyList()))
        }

        save_button.setOnClickListener {
            val encounter = Encounter(id = UUID.randomUUID(),
                    memberId = identificationEvent.memberId,
                    identificationEventId = identificationEvent.id,
                    occurredAt = clock.instant(),
                    backdatedOccurredAt = observable.value?.backdatedOccurredAt)
            val encounterItems = observable.value?.lineItems?.map {
                val encounterItem = EncounterItem(
                        UUID.randomUUID(), encounter.id, it.first.id, it.second)
                EncounterItemWithBillable(encounterItem, it.first)
            }

            if (encounterItems == null) {
                // TODO: do not allow submitting without any encounter items
            } else {
                navigationManager.goTo(DiagnosisFragment.forEncounter(
                        EncounterWithItemsAndForms(encounter, encounterItems, emptyList())))
            }
        }
    }

//
//        drug_search.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean = true
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                drug_search.suggestionsAdapter = if (newText != null && newText.length > 3) {
//                    createBillableCursorAdapter(newText)
//                } else {
//                    null
//                }
//                return true
//            }
//        })
//        drug_search.setOnSuggestionListener(object : android.widget.SearchView.OnSuggestionListener {
//            override fun onSuggestionSelect(position: Int): Boolean = true
//
//            override fun onSuggestionClick(position: Int): Boolean {
//                val cursor = drug_search.suggestionsAdapter.getItem(position) as MatrixCursor
//                val uuidString = cursor.getString(cursor.getColumnIndex("id"))
//                val billable = billableRepository.find(UUID.fromString(uuidString))
//                // TODO: add to encounter item list
//                drug_search.clearFocus()
//                drug_search.setQuery("", false)
//                scrollToBottomOfList()
//                return true
//            }
//        })
//        drug_search.queryHint = getString(R.string.search_drug_hint)
//
//
//        backdate_encounter.setOnClickListener {
//            launchBackdateDialog()
//        }


//    private fun createBillableCursorAdapter(query: String): SimpleCursorAdapter {
//        val cursorColumns = arrayOf("_id", SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2, "id")
//        val cursor = MatrixCursor(cursorColumns)
//
//        billableRepository.fuzzySearchDrugsByName(query).forEach { billable ->
//            cursor.addRow(arrayOf(billable.id.mostSignificantBits,
//                                  billable.name,
//                                  billable.dosageDetails(),
//                                  billable.id.toString()))
//        }
//
//        return SimpleCursorAdapter(
//                activity,
//                R.layout.item_billable_search_suggestion,
//                cursor,
//                arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2),
//                intArrayOf(R.id.text1, R.id.text2),
//                0)
//    }
//
//    private fun scrollToBottomOfList() {
//        line_items_list.post(ScrollToBottomRunnable(line_items_list))
//    }
//
//    private fun launchBackdateDialog() {
//        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_backdate_encounter, null)
//
//        val datePicker = dialogView.findViewById<View>(R.id.date_picker) as DatePicker
//        val yesterday = Calendar.getInstance()
//        yesterday.add(Calendar.DAY_OF_MONTH, -1)
//        datePicker.maxDate = yesterday.timeInMillis
//
//        val timePicker = dialogView.findViewById<View>(R.id.time_picker) as TimePicker
//
//
//        backdatedOccurredAt?.let {
//            val ldt = LocalDateTime.ofInstant(it, ZoneId.systemDefault())
//            datePicker.updateDate(ldt.year, ldt.monthValue, ldt.dayOfMonth)
//            timePicker.currentHour = ldt.hour
//            timePicker.currentMinute = ldt.minute
//        }
//
//        val builder = AlertDialog.Builder(context)
//        builder.setView(dialogView)
//
//        val dialog = builder.create()
//
//        dialogView.findViewById<View>(R.id.done).setOnClickListener {
//            backdatedOccurredAt = LocalDateTime.of(
//                    datePicker.year,
//                    datePicker.month,
//                    datePicker.dayOfMonth,
//                    timePicker.currentHour,
//                    timePicker.currentMinute
//            ).toInstant(ZoneOffset.UTC) // TODO: fix offset calculation
//            // TODO: update backdated link text
//            dialog.dismiss()
//        }
//
//        dialogView.findViewById<View>(R.id.cancel).setOnClickListener {
//            dialog.dismiss()
//        }
//
//        dialog.show()
//    }
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

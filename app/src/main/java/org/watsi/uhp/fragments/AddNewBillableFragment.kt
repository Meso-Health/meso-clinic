package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_add_new_billable.composition_container
import kotlinx.android.synthetic.main.fragment_add_new_billable.composition_spinner
import kotlinx.android.synthetic.main.fragment_add_new_billable.name_field
import kotlinx.android.synthetic.main.fragment_add_new_billable.price_field
import kotlinx.android.synthetic.main.fragment_add_new_billable.save_button
import kotlinx.android.synthetic.main.fragment_add_new_billable.type_spinner
import kotlinx.android.synthetic.main.fragment_add_new_billable.unit_container
import kotlinx.android.synthetic.main.fragment_add_new_billable.unit_field
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.relations.EncounterItemWithBillable
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.utils.titleize
import org.watsi.uhp.R
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.AddNewBillableViewModel
import java.util.UUID
import javax.inject.Inject

class AddNewBillableFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: AddNewBillableViewModel
    lateinit var compositionAdapter: ArrayAdapter<String>

    companion object {
        const val PARAM_ENCOUNTER = "encounter"

        fun forEncounter(encounter: EncounterWithItemsAndForms): AddNewBillableFragment {
            val fragment = AddNewBillableFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_ENCOUNTER, encounter)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        compositionAdapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(AddNewBillableViewModel::class.java)
        viewModel.getObservable().observe(this, Observer {
            it?.let { viewState ->
                compositionAdapter.clear()
                compositionAdapter.add("") // This provides a default empty option that is stored as null.
                compositionAdapter.addAll(viewState.compositions.map { it.capitalize() }.sorted())

                if (viewState.type != null && Billable.requiresQuantity(viewState.type)) {
                    unit_container.visibility = View.VISIBLE
                    composition_container.visibility = View.VISIBLE
                } else {
                    unit_container.visibility = View.GONE
                    composition_container.visibility = View.GONE
                }

                save_button.isEnabled = viewState.isValid
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.add_new_billable_fragment_label)
        return inflater?.inflate(R.layout.fragment_add_new_billable, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val billableTypes = Billable.Type.values()
        val billableStrings = billableTypes.map { it.toString().titleize() }
        type_spinner.adapter = ArrayAdapter<String>(activity,
                                                    android.R.layout.simple_list_item_1,
                                                    billableStrings)
        // pre-select drug because its been the most commonly created Billable type
        type_spinner.setSelection(billableTypes.indexOf(Billable.Type.DRUG))
        type_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                /* no-op */
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.updateType(billableTypes[position])
            }
        }

        name_field.addTextChangedListener(TextChangedListener { viewModel.updateName(it) })
        unit_field.addTextChangedListener(TextChangedListener { viewModel.updateUnit(it) })
        price_field.addTextChangedListener(TextChangedListener { viewModel.updatePrice(it.toIntOrNull()) })

        composition_spinner.adapter = compositionAdapter
        composition_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                /* no-op */
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.updateComposition(composition_spinner.adapter.getItem(position) as String)
            }
        }

        save_button.setOnClickListener {
            viewModel.getBillable()?.let { billable ->
                val encounter = arguments.getSerializable(PARAM_ENCOUNTER) as EncounterWithItemsAndForms
                val updatedEncounterItems = encounter.encounterItems.toMutableList()
                val newBillableEncounterItem = EncounterItem(
                        UUID.randomUUID(), encounter.encounter.id, billable.id, 1)
                updatedEncounterItems.add(EncounterItemWithBillable(newBillableEncounterItem, billable))

                navigationManager.popTo(EncounterFragment.forEncounter(
                        encounter.copy(encounterItems = updatedEncounterItems)))
            }
        }
    }

    class TextChangedListener(private val handleTextChange: (String) -> Unit) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            /* no-op */
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            /* no-op */
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            handleTextChange(s.toString())
        }
    }
}

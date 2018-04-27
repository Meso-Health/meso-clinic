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
import kotlinx.android.synthetic.main.fragment_add_new_billable.composition_field
import kotlinx.android.synthetic.main.fragment_add_new_billable.composition_spinner
import kotlinx.android.synthetic.main.fragment_add_new_billable.name_field
import kotlinx.android.synthetic.main.fragment_add_new_billable.price_field
import kotlinx.android.synthetic.main.fragment_add_new_billable.save_button
import kotlinx.android.synthetic.main.fragment_add_new_billable.type_spinner
import kotlinx.android.synthetic.main.fragment_add_new_billable.unit_field
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.IdentificationEvent

import org.watsi.uhp.R
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.AddNewBillableViewModel
import java.io.Serializable

import javax.inject.Inject

class AddNewBillableFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: AddNewBillableViewModel
    lateinit var compositionAdapter: ArrayAdapter<String>

    companion object {
        const val PARAM_IDENTIFICATION_EVENT = "identification_event"
        const val PARAM_LINE_ITEMS = "line_items"

        fun forIdentificationEvent(idEvent: IdentificationEvent,
                                   lineItems: List<Pair<Billable, Int>>): AddNewBillableFragment {
            val fragment = AddNewBillableFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_IDENTIFICATION_EVENT, idEvent)
                putSerializable(PARAM_LINE_ITEMS, lineItems as Serializable)
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
                compositionAdapter.addAll(viewState.compositions)

                if (viewState.type != null && Billable.requiresQuantity(viewState.type)) {
                    unit_field.visibility = View.VISIBLE
                    composition_field.visibility = View.VISIBLE
                } else {
                    unit_field.visibility = View.GONE
                    composition_field.visibility = View.GONE
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.add_new_billable_fragment_label)
        return inflater?.inflate(R.layout.fragment_add_new_billable, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {

        val billableTypes = Billable.Type.values()
        type_spinner.adapter = ArrayAdapter<Billable.Type>(activity,
                                                         android.R.layout.simple_list_item_1,
                                                         billableTypes)
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
        price_field.addTextChangedListener(TextChangedListener { viewModel.updatePrice(it.toInt()) })

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
            // TODO: handle missing fields
            viewModel.getBillable()?.let { billable ->
                val identificationEvent =
                        arguments.getSerializable(PARAM_IDENTIFICATION_EVENT) as IdentificationEvent
                val lineItems = arguments.getSerializable(PARAM_LINE_ITEMS) as List<Pair<Billable, Int>>

                navigationManager.popTo(EncounterFragment.forIdentificationEvent(
                        identificationEvent, lineItems, billable))
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

package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.uganda.fragment_add_new_billable.composition_container
import kotlinx.android.synthetic.uganda.fragment_add_new_billable.composition_field
import kotlinx.android.synthetic.uganda.fragment_add_new_billable.name_field
import kotlinx.android.synthetic.uganda.fragment_add_new_billable.price_field
import kotlinx.android.synthetic.uganda.fragment_add_new_billable.save_button
import kotlinx.android.synthetic.uganda.fragment_add_new_billable.type_spinner
import kotlinx.android.synthetic.uganda.fragment_add_new_billable.unit_container
import kotlinx.android.synthetic.uganda.fragment_add_new_billable.unit_field
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.domain.utils.titleize
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.flowstates.EncounterFlowState
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.AddNewBillableViewModel
import org.watsi.uhp.views.SpinnerField
import java.util.UUID
import javax.inject.Inject

class AddNewBillableFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: AddNewBillableViewModel
    lateinit var encounterFlowState: EncounterFlowState


    companion object {
        const val PARAM_ENCOUNTER = "encounter"

        fun forEncounter(encounter: EncounterFlowState): AddNewBillableFragment {
            val fragment = AddNewBillableFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_ENCOUNTER, encounter)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        encounterFlowState = arguments.getSerializable(PARAM_ENCOUNTER) as EncounterFlowState

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(AddNewBillableViewModel::class.java)
        viewModel.getObservable().observe(this, Observer {
            it?.let { viewState ->
                when (viewState.type) {
                    Billable.Type.DRUG -> {
                        unit_container.visibility = View.VISIBLE
                        composition_container.visibility = View.VISIBLE
                    }
                    Billable.Type.VACCINE -> {
                        unit_container.visibility = View.VISIBLE
                        composition_container.visibility = View.GONE
                    }
                    else -> {
                        unit_container.visibility = View.GONE
                        composition_container.visibility = View.GONE
                    }
                }

                save_button.isEnabled = viewState.isValid
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(context.getString(R.string.add_new_billable_fragment_label), R.drawable.ic_arrow_back_white_24dp)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_add_new_billable, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val billableTypes = Billable.Type.values()

        type_spinner.setUpWithoutPrompt(
            adapter = SpinnerField.createAdapter(context, billableTypes.map { it.toString().titleize() }),
            initialChoiceIndex = 0,
            onItemSelected = { selectedString: String ->
                Billable.Type.valueOf(selectedString.toUpperCase()).let {
                        type -> viewModel.updateType(type)
                }
            }
        )

        name_field.addTextChangedListener(TextChangedListener { viewModel.updateName(it) })
        unit_field.addTextChangedListener(TextChangedListener { viewModel.updateUnit(it) })
        price_field.addTextChangedListener(TextChangedListener { viewModel.updatePrice(it.toIntOrNull()) })
        composition_field.addTextChangedListener(TextChangedListener { viewModel.updateComposition(it) })

        save_button.setOnClickListener {
            viewModel.getBillable()?.let { billableWithPrice ->
                val newBillableEncounterItem = EncounterItem(
                    id = UUID.randomUUID(),
                    encounterId = encounterFlowState.encounter.id,
                    quantity = 1,
                    priceScheduleId = billableWithPrice.priceSchedule.id,
                    priceScheduleIssued = true
                )
                encounterFlowState.encounterItemRelations =
                        encounterFlowState.encounterItemRelations.plus(
                            EncounterItemWithBillableAndPrice(
                                encounterItem = newBillableEncounterItem,
                                billableWithPriceSchedule = billableWithPrice,
                                labResult = null
                            )
                        )
                navigationManager.popTo(EncounterFragment.forEncounter(encounterFlowState))
            }
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

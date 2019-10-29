package org.watsi.uhp.fragments

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_edit_price.billable_details
import kotlinx.android.synthetic.main.fragment_edit_price.billable_name
import kotlinx.android.synthetic.main.fragment_edit_price.price_indicator
import kotlinx.android.synthetic.main.fragment_edit_price.quantity
import kotlinx.android.synthetic.main.fragment_edit_price.save_button
import kotlinx.android.synthetic.main.fragment_edit_price.stockout_check_box
import kotlinx.android.synthetic.main.fragment_edit_price.total_price
import kotlinx.android.synthetic.main.fragment_edit_price.unit_price
import org.threeten.bp.Instant
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.PriceSchedule
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.flowstates.EncounterFlowState
import org.watsi.uhp.helpers.LayoutHelper
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.utils.CurrencyUtil
import org.watsi.uhp.viewmodels.EditPriceViewModel
import java.util.UUID
import javax.inject.Inject

class EditPriceFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var encounterFlowState: EncounterFlowState
    lateinit var encounterItemId: UUID
    lateinit var viewModel: EditPriceViewModel
    lateinit var observable: LiveData<EditPriceViewModel.ViewState>
    lateinit var billableType: Billable.Type

    companion object {
        const val PARAM_ENCOUNTER = "encounter"
        const val PARAM_ENCOUNTER_ID = "encounter_item_id"

        fun forEncounterItem(encounterItemId: UUID, encounter: EncounterFlowState): EditPriceFragment {
            val fragment = EditPriceFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_ENCOUNTER_ID, encounterItemId)
                putSerializable(PARAM_ENCOUNTER, encounter)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        encounterFlowState = arguments.getSerializable(PARAM_ENCOUNTER) as EncounterFlowState
        encounterItemId = arguments.getSerializable(PARAM_ENCOUNTER_ID) as UUID
        val encounterItemRelation = encounterFlowState.encounterItemRelations.find {
            it.encounterItem.id == encounterItemId
        }!!
        billableType = encounterItemRelation.billableWithPriceSchedule.billable.type
        val initialPrice = encounterItemRelation.billableWithPriceSchedule.priceSchedule.price

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EditPriceViewModel::class.java)
        observable = viewModel.getObservable(encounterItemRelation)
        observable.observe(this, Observer {
            it?.let { viewState ->
                viewState.billable?.let { billable ->
                    billable_name.text = billable.name
                    billable.details()?.let { billable_details.text = it }
                }

                price_indicator.setPrice(viewState.unitPrice, initialPrice)

                // don't change value if focused because that means the user is editing the field
                // and we don't want to reset the cursor position by calling setText
                if (!unit_price.isFocused) {
                    unit_price.setText(CurrencyUtil.formatMoney(viewState.unitPrice))
                }
                if (!quantity.isFocused) {
                    quantity.setText(viewState.quantity.toString())
                }
                if (!total_price.isFocused) {
                    total_price.setText(CurrencyUtil.formatMoney(viewState.totalPrice))
                }

                stockout_check_box.isChecked = viewState.stockout
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(context.getString(R.string.edit_price_fragment_label), R.drawable.ic_arrow_back_white_24dp)
        return inflater?.inflate(R.layout.fragment_edit_price, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        if (Billable.canEditPrice(billableType)) {
            // This logic uses a TextWatcher to update the ViewModel when valid numbers are entered into
            // the inputs and we require a separate OnFocusChangeListener to handle resetting the
            // the value of the field to a previously set value if an invalid number is entered.
            // We can't include the reset behavior in the TextWatcher because that causes an
            // awkward UX behavior where you aren't able to clear the input while editing because
            // null is an invalid value.
            unit_price.addTextChangedListener(LayoutHelper.OnChangedListener { s ->
                s.toBigDecimalOrNull()?.let {
                    viewModel.updateUnitPrice(CurrencyUtil.parseMoney(it.toString()))
                }
            })

            unit_price.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus && unit_price.text.toString().toBigDecimalOrNull() == null) {
                    observable.value?.let { viewState ->
                        unit_price.setText(CurrencyUtil.formatMoney(viewState.unitPrice))
                    }
                }
            }

            total_price.addTextChangedListener(LayoutHelper.OnChangedListener { s ->
                s.toBigDecimalOrNull()?.let {
                    viewModel.updateTotalPrice(CurrencyUtil.parseMoney(it.toString()))
                }
            })

            total_price.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus && total_price.text.toString().toBigDecimalOrNull() == null) {
                    observable.value?.let { viewState ->
                        total_price.setText(CurrencyUtil.formatMoney(viewState.totalPrice))
                    }
                }
            }
        } else {
            unit_price.isEnabled = false
            total_price.isEnabled = false
        }

        quantity.addTextChangedListener(LayoutHelper.OnChangedListener { s ->
            s.toIntOrNull()?.let { viewModel.updateQuantity(it) }
        })

        quantity.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && quantity.text.toString().toIntOrNull() == null) {
                observable.value?.let { viewState ->
                    quantity.setText(viewState.quantity.toString())
                }
            }
        }

        stockout_check_box.text = getString(R.string.mark_as_stockout, billableType)
        stockout_check_box.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateStockout(isChecked)
        }

        save_button.setOnClickListener {
            observable.value?.let { viewState ->
                val updatedEncounterItems = encounterFlowState.encounterItemRelations.map { encounterItemRelation ->
                    if (encounterItemRelation.encounterItem.id == encounterItemId) {
                        // handle case of 0 quantity by setting it back to original quantity
                        val qty = if (viewState.quantity == 0) {
                            encounterItemRelation.encounterItem.quantity
                        } else {
                            viewState.quantity
                        }

                        // Issue a new price schedule if the price has changed
                        val priceScheduleIssued = encounterItemRelation.billableWithPriceSchedule.priceSchedule.price != viewState.unitPrice
                        val prevPriceSchedule = if (encounterItemRelation.encounterItem.priceScheduleIssued) {
                            // If the priceScheduleIssued field is already set, you are are replacing the new price
                            // rather than creating a new link in the list. Use the same previousPriceScheduleId
                            encounterItemRelation.billableWithPriceSchedule.prevPriceSchedule
                        } else {
                            encounterItemRelation.billableWithPriceSchedule.priceSchedule
                        }

                        val billableWithPriceSchedule = if (priceScheduleIssued) {
                            encounterItemRelation.billableWithPriceSchedule.copy(
                                priceSchedule = PriceSchedule(
                                    id = UUID.randomUUID(),
                                    billableId = encounterItemRelation.billableWithPriceSchedule.billable.id,
                                    issuedAt = Instant.now(),
                                    price = viewState.unitPrice,
                                    previousPriceScheduleModelId = prevPriceSchedule?.id
                                ),
                                prevPriceSchedule = prevPriceSchedule
                            )
                        } else {
                            encounterItemRelation.billableWithPriceSchedule
                        }

                        encounterItemRelation.copy(
                            encounterItem = encounterItemRelation.encounterItem.copy(
                                quantity = qty,
                                priceScheduleId = billableWithPriceSchedule.priceSchedule.id,
                                priceScheduleIssued = priceScheduleIssued,
                                stockout = viewState.stockout
                            ),
                            billableWithPriceSchedule = billableWithPriceSchedule
                        )
                    } else {
                        encounterItemRelation
                    }
                }
                encounterFlowState.encounterItemRelations = updatedEncounterItems
                navigationManager.goBack()
            }
        }
    }
}

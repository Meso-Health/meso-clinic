package org.watsi.uhp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_add_new_billable.list_of_compositions
import kotlinx.android.synthetic.main.fragment_add_new_billable.name_field
import kotlinx.android.synthetic.main.fragment_add_new_billable.price_field
import kotlinx.android.synthetic.main.fragment_add_new_billable.save_button
import kotlinx.android.synthetic.main.fragment_add_new_billable.type_field
import kotlinx.android.synthetic.main.fragment_add_new_billable.unit_field
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.IdentificationEvent

import org.watsi.domain.repositories.BillableRepository
import org.watsi.uhp.R
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.NavigationManager
import java.io.Serializable
import java.util.UUID

import javax.inject.Inject

class AddNewBillableFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var billableRepository: BillableRepository

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

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.add_new_billable_fragment_label)
        return inflater?.inflate(R.layout.fragment_add_new_billable, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {

        list_of_compositions.setCompositionChoices(billableRepository.uniqueCompositions())

        save_button.setOnClickListener {
            // TODO: handle empty field values
            val billable = Billable(id = UUID.randomUUID(),
                                    type = Billable.Type.valueOf(type_field.selectedItem.toString()),
                                    composition = list_of_compositions.text.toString(),
                                    unit = unit_field.text.toString(),
                                    price = price_field.text.toString().toInt(),
                                    name = name_field.text.toString())

            val identificationEvent =
                    arguments.getSerializable(PARAM_IDENTIFICATION_EVENT) as IdentificationEvent
            val lineItems = arguments.getSerializable(PARAM_LINE_ITEMS) as List<Pair<Billable, Int>>

            KeyboardManager.hideKeyboard(view, context)
            navigationManager.popTo(EncounterFragment.forIdentificationEvent(
                    identificationEvent, lineItems, billable))
        }
    }
}

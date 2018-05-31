package org.watsi.uhp.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_receipt.diagnoses_label
import kotlinx.android.synthetic.main.fragment_receipt.diagnoses_list
import kotlinx.android.synthetic.main.fragment_receipt.encounter_items_label
import kotlinx.android.synthetic.main.fragment_receipt.encounter_items_list
import kotlinx.android.synthetic.main.fragment_receipt.forms_label
import kotlinx.android.synthetic.main.fragment_receipt.save_button
import kotlinx.android.synthetic.main.fragment_receipt.total_price
import org.watsi.device.managers.Logger

import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.usecases.CreateEncounterUseCase
import org.watsi.uhp.R
import org.watsi.uhp.R.plurals.diagnosis_count
import org.watsi.uhp.R.plurals.forms_attached_label
import org.watsi.uhp.R.plurals.receipt_line_item_count
import org.watsi.uhp.R.string.encounter_submitted
import org.watsi.uhp.R.string.price_with_currency
import org.watsi.uhp.adapters.EncounterItemAdapter
import org.watsi.uhp.managers.NavigationManager

import javax.inject.Inject

class ReceiptFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var createEncounterUseCase: CreateEncounterUseCase
    @Inject lateinit var logger: Logger

    lateinit var encounter: EncounterWithItemsAndForms

    companion object {
        const val PARAM_ENCOUNTER = "encounter"

        fun forEncounter(encounter: EncounterWithItemsAndForms): ReceiptFragment {
            val fragment = ReceiptFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_ENCOUNTER, encounter)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        encounter = arguments.getSerializable(EncounterFormFragment.PARAM_ENCOUNTER) as EncounterWithItemsAndForms
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.receipt_fragment_label)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_receipt, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        diagnoses_label.text = resources.getQuantityString(
                diagnosis_count, encounter.diagnoses.size, encounter.diagnoses.size)
        encounter_items_label.text = resources.getQuantityString(
                receipt_line_item_count, encounter.encounterItems.size, encounter.encounterItems.size)
        total_price.text = getString(price_with_currency, encounter.price().toString()) // TODO: format
        forms_label.text = resources.getQuantityString(
                forms_attached_label, encounter.encounterForms.size, encounter.encounterForms.size)

        diagnoses_list.text = encounter.diagnoses.map { it.description }.joinToString(", ")

        encounter_items_list.adapter = EncounterItemAdapter(encounter.encounterItems)
        encounter_items_list.layoutManager = LinearLayoutManager(activity)

        save_button.setOnClickListener {
            submitEncounter(true)
        }
    }

    private fun submitEncounter(copaymentPaid: Boolean) {
        createEncounterUseCase.execute(encounter.copy(encounter.encounter.copy(copaymentPaid = copaymentPaid)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    navigationManager.popTo(CurrentPatientsFragment())
                    Toast.makeText(activity, getString(encounter_submitted), Toast.LENGTH_LONG).show()
                }, {
                    logger.error(it)
                })
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.menu_submit_without_copayment).isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_submit_without_copayment -> {
                submitEncounter(false)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

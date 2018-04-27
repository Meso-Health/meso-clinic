package org.watsi.uhp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_receipt.save_button

import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.repositories.EncounterRepository
import org.watsi.uhp.R
import org.watsi.uhp.managers.NavigationManager

import javax.inject.Inject

class ReceiptFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var encounterRepository: EncounterRepository

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
        // TODO: populate view

        save_button.setOnClickListener {
            encounterRepository.create(encounter)
            navigationManager.popTo(CurrentPatientsFragment())
            Toast.makeText(activity, "Claim submitted", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.menu_submit_without_copayment).isVisible = true
    }
}

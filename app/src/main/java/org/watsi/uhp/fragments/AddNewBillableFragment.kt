package org.watsi.uhp.fragments

import android.databinding.DataBindingUtil
import android.view.View

import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.repositories.BillableRepository
import org.watsi.uhp.R
import org.watsi.uhp.custom_components.BillableCompositionInput
import org.watsi.uhp.databinding.FragmentAddNewBillableBinding
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.view_models.BillableViewModel

import javax.inject.Inject

class AddNewBillableFragment : FormFragment<Encounter>() {
    private var mBillableViewModel: BillableViewModel? = null
    private var mCompositionNameTextView: BillableCompositionInput? = null

    @Inject
    internal var billableRepository: BillableRepository? = null

    internal override fun getTitleLabelId(): Int {
        return R.string.add_new_billable_fragment_label
    }

    internal override fun getFragmentLayoutId(): Int {
        return R.layout.fragment_add_new_billable
    }

    override fun isFirstStep(): Boolean {
        return false
    }

    override fun nextStep() {
        val billable = mBillableViewModel!!.billable
        billable.setCreatedDuringEncounter(true)

        val encounterItem = EncounterItem()
        encounterItem.setBillable(billable)

        mSyncableModel.getEncounterItems().add(encounterItem)
        KeyboardManager.hideKeyboard(view, context)
        navigationManager.setEncounterFragment(mSyncableModel)
    }

    internal override fun setUpFragment(view: View) {
        val binding = DataBindingUtil.bind<FragmentAddNewBillableBinding>(view)
        mBillableViewModel = BillableViewModel(this)
        binding!!.billable = mBillableViewModel

        mCompositionNameTextView = view.findViewById<View>(R.id.list_of_compositions) as BillableCompositionInput
        mCompositionNameTextView!!.setCompositionChoices(billableRepository!!.uniqueCompositions())
    }
}

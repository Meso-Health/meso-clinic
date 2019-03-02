package org.watsi.uhp.fragments

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.ethiopia.fragment_visit_type.next_button
import kotlinx.android.synthetic.ethiopia.fragment_visit_type.referral_check_box
import kotlinx.android.synthetic.ethiopia.fragment_visit_type.referral_form
import kotlinx.android.synthetic.ethiopia.fragment_visit_type.visit_type_spinner
import org.watsi.domain.entities.Encounter
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.flowstates.EncounterFlowState
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.VisitTypeViewModel
import javax.inject.Inject

class VisitTypeFragment : DaggerFragment() {
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var encounterFlowState: EncounterFlowState
    lateinit var viewModel: VisitTypeViewModel
    lateinit var observable: LiveData<VisitTypeViewModel.ViewState>

    companion object {
        const val PARAM_ENCOUNTER = "encounter"

        fun forEncounter(encounter: EncounterFlowState): VisitTypeFragment {
            val fragment = VisitTypeFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_ENCOUNTER, encounter)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        encounterFlowState = arguments.getSerializable(PARAM_ENCOUNTER) as EncounterFlowState
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(VisitTypeViewModel::class.java)
        viewModel.getObservable(encounterFlowState).observe(this, Observer {
            it?.let { viewState ->
                setErrors(viewState.validationErrors)

                if (viewState.referralBoxChecked) {
                    referral_check_box.isChecked = true
                    referral_form.visibility = View.VISIBLE
                } else {
                    referral_check_box.isChecked = false
                    referral_form.visibility = View.GONE
                }
            }
        })
    }

    fun setErrors(errors: Map<String, Int>) {
        // TODO
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(getString(R.string.visit_type_fragment_label), R.drawable.ic_arrow_back_white_24dp)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_visit_type, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val visitTypes = Encounter.VISIT_TYPE_CHOICES

        visit_type_spinner.setUpSpinner(
            visitTypes,
            encounterFlowState.encounter.visitType ?: visitTypes[0],
            { selectedVisitType ->
                viewModel.onSelectVisitType(selectedVisitType)
            }
        )

        referral_check_box.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onToggleReferralCheckBox(isChecked)
        }

        next_button.setOnClickListener {
            viewModel.validateAndUpdateEncounterFlowState(encounterFlowState)
            navigationManager.goTo(DiagnosisFragment.forEncounter(encounterFlowState))
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
}

package org.watsi.uhp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.ethiopia.fragment_member_information.next_button
import kotlinx.android.synthetic.ethiopia.fragment_visit_type.visit_type_spinner
import org.watsi.domain.entities.Encounter
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.flowstates.EncounterFlowState
import org.watsi.uhp.managers.NavigationManager
import javax.inject.Inject

class VisitTypeFragment : DaggerFragment() {
    @Inject lateinit var navigationManager: NavigationManager
    lateinit var encounterFlowState: EncounterFlowState

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
        encounterFlowState = arguments.getSerializable(DiagnosisFragment.PARAM_ENCOUNTER) as EncounterFlowState
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
            { selectedString ->
                encounterFlowState.encounter = encounterFlowState.encounter.copy(visitType = selectedString)
            }
        )

        next_button.setOnClickListener {
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

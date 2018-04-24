package org.watsi.uhp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_diagnosis.diagnosis_fuzzy_search_input
import kotlinx.android.synthetic.main.fragment_diagnosis.save_button
import kotlinx.android.synthetic.main.fragment_diagnosis.selected_diagnosis_list
import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.entities.Encounter
import org.watsi.domain.relations.EncounterWithItemsAndForms

import org.watsi.domain.repositories.DiagnosisRepository
import org.watsi.uhp.R
import org.watsi.uhp.managers.NavigationManager

import javax.inject.Inject

class DiagnosisFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var diagnosisRepository: DiagnosisRepository

    lateinit var diagnosesList: MutableList<Diagnosis>
    lateinit var adapter: ArrayAdapter<Diagnosis>
    lateinit var encounter: Encounter

    companion object {
        const val PARAM_ENCOUNTER = "encounter"

        fun forEncounter(encounter: Encounter): DiagnosisFragment {
            val fragment = DiagnosisFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_ENCOUNTER, encounter)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        encounter = arguments.getSerializable(PARAM_ENCOUNTER) as Encounter
        diagnosesList = mutableListOf()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.diagnosis_fragment_label)
        return inflater?.inflate(R.layout.fragment_diagnosis, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        diagnosis_fuzzy_search_input.setDiagnosisChosenListener(this, diagnosisRepository)

        adapter = ArrayAdapter<Diagnosis>(
                activity, android.R.layout.simple_list_item_1, diagnosesList)
        selected_diagnosis_list.adapter = adapter
        // TODO: support removing diagnoses

        save_button.setOnClickListener {
            navigationManager.goTo(EncounterFormFragment.forEncounter(
                    EncounterWithItemsAndForms(encounter, emptyList(), emptyList())))
        }
    }

    fun onDiagnosisChosen(diagnoiss: Diagnosis) {
        diagnosesList.add(diagnoiss)
        adapter.notifyDataSetChanged()
    }
}

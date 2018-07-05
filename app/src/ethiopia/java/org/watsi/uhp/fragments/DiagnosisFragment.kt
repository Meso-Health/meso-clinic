package org.watsi.uhp.fragments

import android.app.SearchManager
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.database.MatrixCursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleCursorAdapter
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_diagnosis.diagnoses_count
import kotlinx.android.synthetic.main.fragment_diagnosis.diagnosis_search
import kotlinx.android.synthetic.main.fragment_diagnosis.save_button
import kotlinx.android.synthetic.main.fragment_diagnosis.selected_diagnosis_list
import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.uhp.R
import org.watsi.uhp.adapters.DiagnosisAdapter
import org.watsi.uhp.helpers.RecyclerViewHelper
import org.watsi.uhp.helpers.scrollToBottom
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.DiagnosisViewModel
import javax.inject.Inject

class DiagnosisFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var diagnosisAdapter: DiagnosisAdapter
    lateinit var viewModel: DiagnosisViewModel
    lateinit var observable: LiveData<DiagnosisViewModel.ViewState>

    companion object {
        const val PARAM_ENCOUNTER = "encounter"

        fun forEncounter(encounter: EncounterWithItemsAndForms): DiagnosisFragment {
            val fragment = DiagnosisFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_ENCOUNTER, encounter)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(DiagnosisViewModel::class.java)
        observable = viewModel.getObservable()
        observable.observe(this, Observer {
            it?.let { viewState ->
                val cursor = buildSuggestionsCursor(viewState.suggestedDiagnoses)
                diagnosis_search.suggestionsAdapter.changeCursor(cursor)

                diagnosisAdapter.setDiagnoses(viewState.selectedDiagnoses)
                diagnoses_count.text = resources.getQuantityString(
                    R.plurals.diagnosis_count, viewState.selectedDiagnoses.size, viewState.selectedDiagnoses.size)
            }
        })

        diagnosisAdapter = DiagnosisAdapter(
            onRemoveDiagnosis = { diagnosis: Diagnosis ->
                viewModel.removeDiagnosis(diagnosis)
            }
        )
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.diagnosis_fragment_label)
        return inflater?.inflate(R.layout.fragment_diagnosis, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        diagnosis_search.suggestionsAdapter = SimpleCursorAdapter(
            activity, R.layout.item_billable_search_suggestion, null,
            arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1), intArrayOf(R.id.text1), 0)
        diagnosis_search.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.updateQuery(it) }
                return true
            }
        })
        diagnosis_search.setOnSuggestionListener(object : android.widget.SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean = true

            override fun onSuggestionClick(position: Int): Boolean {
                observable.value?.suggestedDiagnoses?.get(position)?.let {
                    viewModel.addDiagnosis(it)
                    diagnosis_search.setQuery("", false)
                    diagnosis_search.clearFocus()
                    selected_diagnosis_list.scrollToBottom()
                }
                return true
            }
        })

        RecyclerViewHelper.setRecyclerView(selected_diagnosis_list, diagnosisAdapter, context)

        save_button.setOnClickListener {
            val encounterRelation = arguments.getSerializable(PARAM_ENCOUNTER) as EncounterWithItemsAndForms
            navigationManager.goTo(ReceiptFragment.forEncounter(
                    encounter = viewModel.updateEncounterWithDiagnoses(encounterRelation)))
        }
    }

    private fun buildSuggestionsCursor(diagnoses: List<Diagnosis>): MatrixCursor {
        val cursorColumns = arrayOf("_id", SearchManager.SUGGEST_COLUMN_TEXT_1, "id")
        val cursor = MatrixCursor(cursorColumns)
        diagnoses.forEach {
            cursor.addRow(arrayOf(it.id, it.description, it.id.toString()))
        }
        return cursor
    }
}

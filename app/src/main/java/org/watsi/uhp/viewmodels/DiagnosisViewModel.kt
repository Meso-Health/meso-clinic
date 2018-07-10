package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.relations.MutableEncounterWithItemsAndForms
import org.watsi.domain.repositories.DiagnosisRepository
import javax.inject.Inject

class DiagnosisViewModel @Inject constructor(
        diagnosisRepository: DiagnosisRepository,
        private val logger: Logger
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()
    private var diagnoses: List<Diagnosis> = emptyList()
    private var uniqueDescriptions: List<String> = emptyList()

    init {
        diagnosisRepository.all().subscribe({
            diagnoses = it
            uniqueDescriptions = diagnoses.map { it.description }.distinct()
        }, {
            logger.error(it)
        })
    }

    fun getObservable(initialDiagnosis: List<Diagnosis> = emptyList()): LiveData<ViewState> {
        observable.value = ViewState(selectedDiagnoses = initialDiagnosis)
        return observable
    }


    fun addDiagnosis(diagnosis: Diagnosis) {
        val diagnoses = observable.value?.selectedDiagnoses.orEmpty().toMutableList()
        if (!diagnoses.contains(diagnosis)) {
            diagnoses.add(diagnosis)
        }
        observable.value = observable.value?.copy(
                selectedDiagnoses = diagnoses,
                suggestedDiagnoses = emptyList()
        )
    }

    fun removeDiagnosis(diagnosis: Diagnosis) {
        val diagnoses = observable.value?.selectedDiagnoses.orEmpty().minus(diagnosis)
        observable.value = observable.value?.copy(
                selectedDiagnoses = diagnoses,
                suggestedDiagnoses = emptyList()
        )
    }

    fun updateQuery(query: String) {
        if (query.length > 2) {
            val topMatchingDescriptions = FuzzySearch.extractTop(query, uniqueDescriptions, 6, 60)

            // This sorts the fuzzy search results by decreasing score, increasing alphabetical order.
            topMatchingDescriptions.sortWith(Comparator { o1, o2 ->
                if (o2.score == o1.score)
                    o1.string.compareTo(o2.string)
                else
                    Integer.compare(o2.score, o1.score)
            })

            val matchingDescriptionDiagnoses = topMatchingDescriptions.map { result ->
                diagnoses.find { it.description == result.string }
            }.filterNotNull()
            val matchingSearchAliasesDiagnoses = diagnoses.filter { it.searchAliases.contains(query) }

            val suggestedDiagnoses = (matchingSearchAliasesDiagnoses + matchingDescriptionDiagnoses)
                    .distinct()
                    .filterNot { diagnosis ->
                        observable.value?.selectedDiagnoses.orEmpty().map { it.id }.contains(diagnosis.id)
                    }
            observable.value = observable.value?.copy(suggestedDiagnoses = suggestedDiagnoses)
        } else {
            observable.value = observable.value?.copy(suggestedDiagnoses = emptyList())
        }
    }

    fun updateEncounterWithDiagnoses(encounterRelation: MutableEncounterWithItemsAndForms) {
        val diagnoses = observable.value?.selectedDiagnoses.orEmpty()
        encounterRelation.encounter = encounterRelation.encounter.copy(diagnoses = diagnoses.map { it.id })
        encounterRelation.diagnoses = diagnoses
    }

    data class ViewState(val selectedDiagnoses: List<Diagnosis> = emptyList(),
                         val suggestedDiagnoses: List<Diagnosis> = emptyList())
}

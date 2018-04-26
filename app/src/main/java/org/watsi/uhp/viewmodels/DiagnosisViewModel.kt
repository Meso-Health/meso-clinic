package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.repositories.DiagnosisRepository
import javax.inject.Inject

class DiagnosisViewModel @Inject constructor(diagnosisRepository: DiagnosisRepository) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()
    private val diagnoses = diagnosisRepository.all()
    private val uniqueDescriptions = diagnoses.map { it.description }.distinct()

    fun getObservable(): LiveData<ViewState> {
        observable.value = ViewState()
        return observable
    }

    fun addDiagnosis(diagnosis: Diagnosis) {
        val diagnoses = observable.value?.selectedDiagnoses?.toMutableList() ?: mutableListOf()
        diagnoses.add(diagnosis)
        observable.value = observable.value?.copy(selectedDiagnoses = diagnoses,
                                                  suggestedDiagnoses = emptyList())
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

            val suggestedDiagnoses = (matchingSearchAliasesDiagnoses + matchingDescriptionDiagnoses).distinct()

            observable.value = observable.value?.copy(suggestedDiagnoses = suggestedDiagnoses)
        } else {
            observable.value = observable.value?.copy(suggestedDiagnoses = emptyList())
        }
    }

    data class ViewState(val selectedDiagnoses: List<Diagnosis> = emptyList(),
                         val suggestedDiagnoses: List<Diagnosis> = emptyList())
}

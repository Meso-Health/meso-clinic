package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.repositories.DiagnosisRepository
import org.watsi.uhp.flowstates.EncounterFlowState
import javax.inject.Inject

class DiagnosisViewModel @Inject constructor(
        private val diagnosisRepository: DiagnosisRepository,
        private val logger: Logger
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()
    private var diagnoses: List<Diagnosis> = emptyList()
    private var uniqueDescriptions: List<String> = emptyList()


    fun getObservable(initialDiagnosis: List<Diagnosis> = emptyList()): LiveData<ViewState> {
        diagnosisRepository.all().subscribe({ diagnoses ->
            uniqueDescriptions = diagnoses.map { it.description }.distinct()
            if (diagnoses.isEmpty()) {
                observable.postValue(ViewState(
                    diagnosesExistOnDevice = false
                ))
            } else {
                observable.postValue(ViewState(
                    selectedDiagnoses = initialDiagnosis,
                    suggestedDiagnoses = emptyList()
                ))
            }
        }, {
            logger.error(it)
        })
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
        Completable.fromCallable {
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
                observable.postValue(observable.value?.copy(suggestedDiagnoses = suggestedDiagnoses))
            } else {
                observable.postValue(observable.value?.copy(suggestedDiagnoses = emptyList()))
            }
        }.subscribeOn(Schedulers.computation()).subscribe()
    }

    fun updateEncounterWithDiagnoses(encounterFlowState: EncounterFlowState) {
        val diagnoses = observable.value?.selectedDiagnoses.orEmpty()
        encounterFlowState.encounter = encounterFlowState.encounter.copy(diagnoses = diagnoses.map { it.id })
        encounterFlowState.diagnoses = diagnoses
    }

    data class ViewState(
        val diagnosesExistOnDevice: Boolean? = null,
        val selectedDiagnoses: List<Diagnosis> = emptyList(),
        val suggestedDiagnoses: List<Diagnosis> = emptyList()
    )
}

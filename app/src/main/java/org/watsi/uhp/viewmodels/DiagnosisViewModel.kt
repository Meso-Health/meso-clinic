package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.repositories.DiagnosisRepository
import org.watsi.uhp.flowstates.EncounterFlowState
import org.watsi.uhp.utils.FuzzySearchUtil
import javax.inject.Inject

class DiagnosisViewModel @Inject constructor(
        diagnosisRepository: DiagnosisRepository,
        private val logger: Logger
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()
    private var diagnoses: List<Diagnosis> = emptyList()
    private var uniqueDescriptions: List<String> = emptyList()

    init {
        diagnosisRepository.allActive().subscribe({
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
        Completable.fromCallable {
            if (query.length > 2) {
                val topMatchingDescriptions = FuzzySearchUtil.topMatches(query, uniqueDescriptions, 6)

                val matchingDescriptionDiagnoses = topMatchingDescriptions.map { result ->
                    diagnoses.find { it.description == result }
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

    data class ViewState(val selectedDiagnoses: List<Diagnosis> = emptyList(),
                         val suggestedDiagnoses: List<Diagnosis> = emptyList())
}

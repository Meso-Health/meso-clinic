package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.threeten.bp.Clock
import org.watsi.device.managers.Logger
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.usecases.LoadClaimsUseCase
import org.watsi.domain.usecases.SubmitClaimUseCase
import javax.inject.Inject

class SearchableClaimsListViewModel @Inject constructor(
    private val submitClaimUseCase: SubmitClaimUseCase,
    private val logger: Logger,
    private val clock: Clock
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()

    fun getObservable(loadClaimsUseCase: LoadClaimsUseCase): LiveData<ViewState> {
        observable.value = ViewState()

        loadClaimsUseCase.execute().subscribe({ claims ->
            observable.postValue(ViewState(
                claims = claims,
                visibleClaims = claims,
                selectedClaims = emptyList()
            ))
        }, {
            logger.error(it)
        })

        return observable
    }

    fun filterClaimsBySearchText(filterText: String) {
        observable.value?.let { viewState ->
            if (filterText.length > 2) {
                val filteredClaims = viewState.claims.filter {
                    val medicalRecordNumber = it.member.medicalRecordNumber
                    val membershipNumber = it.member.membershipNumber
                    (membershipNumber != null && membershipNumber.contains(filterText, ignoreCase = true)) ||
                            (medicalRecordNumber != null && medicalRecordNumber.contains(filterText, ignoreCase = true))
                }
                observable.value = viewState.copy(visibleClaims = filteredClaims)
            } else {
                observable.value = viewState.copy(visibleClaims = viewState.claims)
            }
        }
    }

    fun getClaims(): Pair<List<EncounterWithExtras>, List<EncounterWithExtras>>? {
        return observable.value?.let {
            Pair(it.claims, it.selectedClaims)
        }
    }

    fun updateSelectedClaims(encounter: EncounterWithExtras) {
        observable.value?.let { viewState ->
            val currentSelectedClaims = viewState.selectedClaims
            if (currentSelectedClaims.contains(encounter)) {
                observable.value = viewState.copy(selectedClaims = currentSelectedClaims.minus(encounter))
            } else {
                observable.value = viewState.copy(selectedClaims = currentSelectedClaims.plus(encounter))
            }
        }
    }

    fun toggleSelectAll(selected: Boolean) {
        observable.value?.let { viewState ->
            if (selected) {
                observable.value = viewState.copy(selectedClaims = viewState.claims)
            } else {
                observable.value = viewState.copy(selectedClaims = emptyList())
            }
        }
    }

    fun submitSelected(): Completable {
        return observable.value?.let { viewState ->
            Completable.fromCallable {
                viewState.selectedClaims.map { encounterWithExtras ->
                    submitClaimUseCase.execute(
                        encounterWithExtras,
                        clock
                    ).blockingAwait()
                }
            // prevent further UI action (e.g. button pressed again) while claims are being submitted
            }.observeOn(AndroidSchedulers.mainThread())
        } ?: Completable.never()
    }

    data class ViewState(
        val claims: List<EncounterWithExtras> = emptyList(),
        val visibleClaims: List<EncounterWithExtras> = emptyList(),
        val selectedClaims: List<EncounterWithExtras> = emptyList()
    )
}

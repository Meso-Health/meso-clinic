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
                visibleClaims = claims
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

    fun getClaims(): List<EncounterWithExtras>? = observable.value?.claims

    fun submitAll(): Completable {
        return observable.value?.let { viewState ->
            Completable.fromCallable {
                viewState.claims.map { encounterWithExtras ->
                    submitClaimUseCase.execute(
                        encounterWithExtras.toEncounterWithItemsAndForms(),
                        clock
                    ).blockingAwait()
                }
            // prevent further UI action (e.g. button pressed again) while claims are being submitted
            }.observeOn(AndroidSchedulers.mainThread())
        } ?: Completable.never()
    }

    data class ViewState(
        val claims: List<EncounterWithExtras> = emptyList(),
        val visibleClaims: List<EncounterWithExtras> = emptyList()
    )
}
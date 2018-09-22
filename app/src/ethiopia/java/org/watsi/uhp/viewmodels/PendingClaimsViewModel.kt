package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.watsi.device.managers.Logger
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.usecases.LoadPendingClaimsUseCase
import javax.inject.Inject

class PendingClaimsViewModel @Inject constructor(
    private val loadPendingClaimsUseCase: LoadPendingClaimsUseCase,
    private val logger: Logger
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()

    fun getObservable(): LiveData<ViewState> {
        observable.value = ViewState()

        loadPendingClaimsUseCase.execute().subscribe({
            observable.postValue(ViewState(it))
        }, {
            logger.error(it)
        })

        return observable
    }

    fun getClaims(): List<EncounterWithExtras>? = observable.value?.claims

    data class ViewState(val claims: List<EncounterWithExtras> = emptyList())
}

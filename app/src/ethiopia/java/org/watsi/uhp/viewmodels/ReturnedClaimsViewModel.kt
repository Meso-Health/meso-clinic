package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.watsi.device.managers.Logger
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.usecases.LoadReturnedClaimsUseCase
import javax.inject.Inject

class ReturnedClaimsViewModel @Inject constructor(
    private val loadReturnedClaimsUseCase: LoadReturnedClaimsUseCase,
    private val logger: Logger
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()

    fun getObservable(): LiveData<ViewState> {
        observable.value = ViewState()
        loadReturnedClaimsUseCase.execute().subscribe({
            observable.postValue(ViewState(it))
        }, {
            logger.error(it)
        })

        return observable
    }

    fun getReturnedClaims(): List<EncounterWithExtras>? = observable.value?.returnedEncounters

    data class ViewState(val returnedEncounters: List<EncounterWithExtras> = emptyList())
}

package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.watsi.device.managers.Logger
import org.watsi.domain.relations.EncounterWithExtras
import javax.inject.Inject

class ReturnedClaimsViewModel @Inject constructor(
    private val logger: Logger
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()

    fun getObservable(): LiveData<ViewState> {

        // TODO: Add Use Case to fetch returned encounters (Pivotal story #159841519)
        return observable
    }

    fun getReturnedClaims(): List<EncounterWithExtras>? = observable.value?.returnedEncounters

    data class ViewState(val returnedEncounters: List<EncounterWithExtras> = emptyList())
}

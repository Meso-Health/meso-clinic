package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import io.reactivex.Flowable
import org.watsi.domain.usecases.LoadPendingClaimsCountUseCase
import org.watsi.domain.usecases.LoadReturnedClaimsCountUseCase
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val loadPendingClaimsCountUseCase: LoadPendingClaimsCountUseCase,
    private val loadReturnedClaimsCountUseCase: LoadReturnedClaimsCountUseCase
) : ViewModel() {

    fun getObservable(): LiveData<ViewState> {
        val flowables = listOf(
            loadPendingClaimsCountUseCase.execute(),
            loadReturnedClaimsCountUseCase.execute()
        )

        return LiveDataReactiveStreams.fromPublisher(
            Flowable.combineLatest(flowables, { results ->
                ViewState(
                    pendingClaimsCount = results[0] as Int,
                    returnedClaimsCount = results[1] as Int
                )
            })
        )
    }

    data class ViewState(
        val pendingClaimsCount: Int = 0,
        val returnedClaimsCount: Int = 0
    )
}

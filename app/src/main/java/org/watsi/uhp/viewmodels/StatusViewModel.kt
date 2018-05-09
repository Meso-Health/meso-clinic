package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Flowable
import org.threeten.bp.Instant
import org.watsi.device.managers.PreferencesManager
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.usecases.FetchStatusUseCase
import org.watsi.domain.usecases.SyncStatusUseCase
import javax.inject.Inject

class StatusViewModel @Inject constructor (
        private val syncStatusUseCase: SyncStatusUseCase,
        private val fetchStatusUseCase: FetchStatusUseCase,
        private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val viewStateObservable = MutableLiveData<ViewState>()

    init {
        viewStateObservable.value = ViewState()
    }

    fun getObservable(): LiveData<ViewState> {
        val flowables = listOf(
                syncStatusUseCase.execute(),
                fetchStatusUseCase.execute(),
                preferencesManager.getMemberLastFetched(),
                preferencesManager.getBillablesLastFetched(),
                preferencesManager.getDiagnosesLastFetched()
        )

        return LiveDataReactiveStreams.fromPublisher(
            Flowable.zip(flowables, { results ->
                ViewState(results[0] as DeltaRepository.SyncStatus,
                          results[1] as Int,
                          results[2] as Instant, // last member fetched at
                          results[3] as Instant, // last billable fetched at
                          results[4] as Instant  // last diagnoses fetched at
                )
            })
        )
    }

    data class ViewState(val syncStatus: DeltaRepository.SyncStatus = DeltaRepository.SyncStatus(),
                         val photosToFetchCount: Int? = null,
                         val membersUpdatedAt: Instant? = null,
                         val billablesUpdatedAt: Instant? = null,
                         val diagnosesUpdatedAt: Instant? = null)
}
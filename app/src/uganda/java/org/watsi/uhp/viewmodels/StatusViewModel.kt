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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StatusViewModel @Inject constructor (
    private val syncStatusUseCase: SyncStatusUseCase,
    private val fetchStatusUseCase: FetchStatusUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val networkViewStateObservable = MutableLiveData<NetworkViewState>()

    fun getObservable(): LiveData<ViewState> {
        val flowables = listOf(
            fetchStatusUseCase.execute(),
            syncStatusUseCase.execute(),
            Flowable.interval(0, 1, TimeUnit.MINUTES) // allows status screen timestamps to keep updating
        )

        return LiveDataReactiveStreams.fromPublisher(
            Flowable.combineLatest(flowables) { results ->
                ViewState(
                    dataFetchedAt = preferencesManager.getDataLastFetched(),
                    photoFetchedAt = preferencesManager.getPhotoLastFetched(),
                    membersFetchedAt = preferencesManager.getMemberLastFetched(),
                    billablesFetchedAt = preferencesManager.getBillablesLastFetched(),
                    diagnosesFetchedAt = preferencesManager.getDiagnosesLastFetched(),
                    memberPhotosFetchedAt = preferencesManager.getMemberPhotosLastFetched(),
                    photosToFetchCount = results[0] as Int,
                    dataSyncedAt = preferencesManager.getDataLastSynced(),
                    photoSyncedAt = preferencesManager.getPhotoLastSynced(),
                    syncStatus = results[1] as DeltaRepository.SyncStatus
                )
            }
        )
    }

    fun getNetworkObservable(): LiveData<NetworkViewState> {
        networkViewStateObservable.value = NetworkViewState()
        return networkViewStateObservable
    }

    fun updateFetchDataStatus(isRunning: Boolean, fetchErrors: List<String> = emptyList()) {
        networkViewStateObservable.value = networkViewStateObservable.value?.copy(isDataFetching = isRunning, dataFetchErrors = fetchErrors)
    }

    fun updateFetchPhotosStatus(isRunning: Boolean, fetchErrors: List<String> = emptyList()) {
        networkViewStateObservable.value = networkViewStateObservable.value?.copy(isPhotoFetching = isRunning, photoFetchErrors = fetchErrors)
    }

    fun updateSyncDataStatus(isRunning: Boolean, dataSyncErrors: List<String> = emptyList()) {
        networkViewStateObservable.value = networkViewStateObservable.value?.copy(isDataSyncing = isRunning, dataSyncErrors = dataSyncErrors)
    }

    fun updateSyncPhotosStatus(isRunning: Boolean, photoSyncErrors: List<String> = emptyList()) {
        networkViewStateObservable.value = networkViewStateObservable.value?.copy(isPhotoSyncing = isRunning, photoSyncErrors = photoSyncErrors)
    }

    data class NetworkViewState(
        val isDataFetching: Boolean = false,
        val isPhotoFetching: Boolean = false,
        val isDataSyncing: Boolean = false,
        val isPhotoSyncing: Boolean = false,
        val dataFetchErrors: List<String> = emptyList(),
        val photoFetchErrors: List<String> = emptyList(),
        val dataSyncErrors: List<String> = emptyList(),
        val photoSyncErrors: List<String> = emptyList()
    )

    data class ViewState(
        val dataFetchedAt: Instant,
        val photoFetchedAt: Instant,
        val membersFetchedAt: Instant,
        val billablesFetchedAt: Instant,
        val diagnosesFetchedAt: Instant,
        val memberPhotosFetchedAt: Instant,
        val photosToFetchCount: Int,
        val dataSyncedAt: Instant,
        val photoSyncedAt: Instant,
        val syncStatus: DeltaRepository.SyncStatus
    )
}

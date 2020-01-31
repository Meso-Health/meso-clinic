package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import org.watsi.domain.usecases.FetchHouseholdIdByCardIdUseCase
import org.watsi.domain.usecases.FetchHouseholdIdByMembershipNumberUseCase
import java.util.UUID
import javax.inject.Inject

class DownloadHouseholdViewModel @Inject constructor(
    private val fetchHouseholdIdByCardIdUseCase: FetchHouseholdIdByCardIdUseCase,
    private val fetchHouseholdIdByMembershipNumberUseCase: FetchHouseholdIdByMembershipNumberUseCase
) : ViewModel() {

    fun getObservableByCardId(cardId: String): LiveData<ViewState> {
        return LiveDataReactiveStreams.fromPublisher(
            fetchHouseholdIdByCardIdUseCase.execute(cardId).map { ViewState(it) }
                .onErrorReturn { ViewState(null) }
        )
    }

    fun getObservableByMembershipNumber(membershipNumber: String): LiveData<ViewState> {
        return LiveDataReactiveStreams.fromPublisher(
            fetchHouseholdIdByMembershipNumberUseCase.execute(membershipNumber).map { ViewState(it) }
                .onErrorReturn { ViewState(null) }
        )
    }

    data class ViewState(val householdId: UUID?)
}

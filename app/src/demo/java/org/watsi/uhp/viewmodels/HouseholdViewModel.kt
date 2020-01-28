package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.domain.usecases.LoadHouseholdMembersUseCase
import java.util.UUID
import javax.inject.Inject

class HouseholdViewModel @Inject constructor(
    private val loadHouseholdMembersUseCase: LoadHouseholdMembersUseCase
) : ViewModel() {

    fun getObservable(householdId: UUID): LiveData<ViewState> {
        return LiveDataReactiveStreams.fromPublisher(
            loadHouseholdMembersUseCase.execute(householdId).map { ViewState(it) }
        )
    }

    data class ViewState(val householdMembers: List<MemberWithIdEventAndThumbnailPhoto>)
}

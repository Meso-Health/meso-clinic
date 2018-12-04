package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import io.reactivex.Flowable
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.domain.usecases.LoadCheckedInMembersUseCase
import org.watsi.domain.usecases.LoadPendingClaimsCountUseCase
import org.watsi.domain.usecases.LoadReturnedClaimsCountUseCase
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val loadPendingClaimsCountUseCase: LoadPendingClaimsCountUseCase,
    private val loadReturnedClaimsCountUseCase: LoadReturnedClaimsCountUseCase,
    private val loadCheckedInMembersUseCase: LoadCheckedInMembersUseCase
) : ViewModel() {

    fun getMenuStateObservable(): LiveData<MenuState> {
        val flowables = listOf(
            loadPendingClaimsCountUseCase.execute(),
            loadReturnedClaimsCountUseCase.execute()
        )

        return LiveDataReactiveStreams.fromPublisher(
            Flowable.combineLatest(flowables, { results ->
                MenuState(
                    pendingClaimsCount = results[0] as Int,
                    returnedClaimsCount = results[1] as Int
                )
            })
        )
    }

    fun getMemberStateObservable(): LiveData<MemberState> {
        return LiveDataReactiveStreams.fromPublisher(
            loadCheckedInMembersUseCase.execute().map { MemberState(it) }
        )
    }

    data class MenuState(val pendingClaimsCount: Int = 0, val returnedClaimsCount: Int = 0)

    data class MemberState(val checkedInMembers: List<MemberWithIdEventAndThumbnailPhoto>)
}

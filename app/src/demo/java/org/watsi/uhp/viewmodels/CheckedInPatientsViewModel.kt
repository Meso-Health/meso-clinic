package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.domain.usecases.LoadCheckedInMembersUseCase
import javax.inject.Inject

class CheckedInPatientsViewModel @Inject constructor(
    private val loadCheckedInMembersUseCase: LoadCheckedInMembersUseCase
) : ViewModel() {

    fun getMemberStateObservable(): LiveData<MemberState> {
        return LiveDataReactiveStreams.fromPublisher(
            loadCheckedInMembersUseCase.execute().map { MemberState(it) }
        )
    }

    data class MemberState(val checkedInMembers: List<MemberWithIdEventAndThumbnailPhoto>)
}

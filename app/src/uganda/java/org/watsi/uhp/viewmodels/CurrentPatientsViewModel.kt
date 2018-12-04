package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.domain.usecases.LoadCheckedInMembersUseCase
import javax.inject.Inject

class CurrentPatientsViewModel @Inject constructor(
    private val loadCheckedInMembersUseCase: LoadCheckedInMembersUseCase
) : ViewModel() {

    private val observable: LiveData<ViewState>

    init {
        val transformedFlowable = loadCheckedInMembersUseCase.execute().map { ViewState(it) }
        observable = LiveDataReactiveStreams.fromPublisher(transformedFlowable)
    }

    fun getObservable(): LiveData<ViewState> = observable

    data class ViewState(val checkedInMembers: List<MemberWithIdEventAndThumbnailPhoto>)
}

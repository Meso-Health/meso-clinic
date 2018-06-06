package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import io.reactivex.Maybe
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.domain.repositories.IdentificationEventRepository
import org.watsi.domain.repositories.MemberRepository
import javax.inject.Inject

class CurrentPatientsViewModel @Inject constructor(
        memberRepository: MemberRepository
) : ViewModel() {

    private val observable: LiveData<ViewState>

    init {
        val transformedFlowable = memberRepository.checkedInMembers().map { ViewState(it) }
        observable = LiveDataReactiveStreams.fromPublisher(transformedFlowable)
    }

    fun getObservable(): LiveData<ViewState> = observable

    data class ViewState(val checkedInMembers: List<MemberWithIdEventAndThumbnailPhoto>)
}

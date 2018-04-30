package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import io.reactivex.Maybe
import io.reactivex.Single
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.IdentificationEventRepository
import org.watsi.domain.repositories.MemberRepository
import javax.inject.Inject

class CurrentPatientsViewModel @Inject constructor(
        memberRepository: MemberRepository,
        private val identificationEventRepository: IdentificationEventRepository
) : ViewModel() {

    private val observable: LiveData<ViewState>

    init {
        val transformedFlowable = memberRepository.checkedInMembers().map { ViewState(it) }
        observable = LiveDataReactiveStreams.fromPublisher(transformedFlowable)
    }

    fun getObservable(): LiveData<ViewState> = observable

    fun getIdentificationEvent(member: Member): Maybe<IdentificationEvent> {
        return identificationEventRepository.openCheckIn(member.id)
    }

    data class ViewState(val checkedInMembers: List<Member>)
}

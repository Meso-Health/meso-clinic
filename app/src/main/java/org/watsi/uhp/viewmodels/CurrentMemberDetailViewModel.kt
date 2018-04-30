package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.MemberRepository
import java.util.UUID
import javax.inject.Inject

class CurrentMemberDetailViewModel @Inject constructor(
        private val memberRepository: MemberRepository) : ViewModel() {

    fun getObservable(memberId: UUID): LiveData<ViewState> {
        val transformedFlowable = memberRepository.find(memberId).map { ViewState(it) }
        return LiveDataReactiveStreams.fromPublisher(transformedFlowable)
    }

    data class ViewState(val member: Member)
}

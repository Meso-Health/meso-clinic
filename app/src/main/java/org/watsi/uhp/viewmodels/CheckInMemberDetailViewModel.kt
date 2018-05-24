package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import io.reactivex.Flowable
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Photo
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.domain.relations.MemberWithThumbnail
import org.watsi.domain.usecases.LoadHouseholdMembersUseCase
import org.watsi.domain.usecases.LoadMemberUseCase
import javax.inject.Inject

class CheckInMemberDetailViewModel @Inject constructor(
        private val loadMemberUseCase: LoadMemberUseCase,
        private val loadHouseholdMembersUseCase: LoadHouseholdMembersUseCase,
        private val logger: Logger
) : ViewModel() {

    fun getObservable(member: Member): LiveData<ViewState> {
        val flowables = listOf(
                loadMemberUseCase.execute(member.id),
                loadHouseholdMembersUseCase.execute(member)
        )

        val zippedFlowables = Flowable.zip(flowables, { results ->
            val memberWithThumbnail = results[0] as MemberWithThumbnail
            ViewState(
                        member = member,
                        memberThumbnail = memberWithThumbnail.photo,
                        householdMembers = results[1] as List<MemberWithIdEventAndThumbnailPhoto>)
        }).onErrorReturn {
            logger.error(it)
            ViewState()
        }.startWith(
                ViewState(member)
        )
        return LiveDataReactiveStreams.fromPublisher(zippedFlowables)
    }

    data class ViewState(val member: Member? = null,
                         val memberThumbnail: Photo? = null,
                         val householdMembers: List<MemberWithIdEventAndThumbnailPhoto>? = null)
}
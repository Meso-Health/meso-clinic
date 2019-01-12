package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import io.reactivex.Flowable
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Photo
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.domain.usecases.IsMemberCheckedInUseCase
import org.watsi.domain.usecases.LoadHouseholdMembersUseCase
import javax.inject.Inject

class CheckInMemberDetailViewModel @Inject constructor(
        private val isMemberCheckedInUseCase: IsMemberCheckedInUseCase,
        private val loadHouseholdMembersUseCase: LoadHouseholdMembersUseCase,
        private val logger: Logger
) : ViewModel() {

    fun getObservable(member: Member): LiveData<ViewState> {
        // TODO: properly handle case where member has no household
        val flowables = listOf(
                loadHouseholdMembersUseCase.execute(member.householdId!!),
                isMemberCheckedInUseCase.execute(member.id)
        )

        val zippedFlowables = Flowable.zip(flowables, { results ->
            val householdMembers = results[0] as List<MemberWithIdEventAndThumbnailPhoto>
            val selfMember = householdMembers.find { it -> it.member.id == member.id }!!
            val isMemberCheckedIn = results[1] as Boolean

            ViewState(
                    member = selfMember.member,
                    memberThumbnail = selfMember.thumbnailPhoto,
                    isMemberCheckedIn = isMemberCheckedIn,
                    householdMembers = householdMembers.minus(selfMember)
            )
        }).onErrorReturn {
            logger.error(it)
            ViewState()
        }.startWith(ViewState(member))
        return LiveDataReactiveStreams.fromPublisher(zippedFlowables)
    }

    data class ViewState(val member: Member? = null,
                         val memberThumbnail: Photo? = null,
                         val isMemberCheckedIn: Boolean? = null,
                         val householdMembers: List<MemberWithIdEventAndThumbnailPhoto> = emptyList())
}

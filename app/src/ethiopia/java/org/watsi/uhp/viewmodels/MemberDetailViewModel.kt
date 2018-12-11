package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import io.reactivex.Flowable
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.MemberWithThumbnail
import org.watsi.domain.usecases.IsMemberCheckedInUseCase
import org.watsi.domain.usecases.LoadMemberUseCase
import javax.inject.Inject

class MemberDetailViewModel @Inject constructor(
    private val loadMemberUseCase: LoadMemberUseCase,
    private val isMemberCheckedInUseCase: IsMemberCheckedInUseCase,
    private val logger: Logger
) : ViewModel() {

    fun getObservable(member: Member): LiveData<ViewState> {
        val flowables = listOf(
            loadMemberUseCase.execute(member.id),
            isMemberCheckedInUseCase.execute(member.id)
        )

        val zippedFlowables = Flowable.zip(flowables, { results ->
            val memberWithThumbnail = results[0] as MemberWithThumbnail
            val isMemberCheckedIn = results[1] as Boolean
            ViewState(memberWithThumbnail, isMemberCheckedIn)
        }).onErrorReturn {
            logger.error(it)
            ViewState()
        }.startWith(ViewState(MemberWithThumbnail(member, null)))

        return LiveDataReactiveStreams.fromPublisher(zippedFlowables)
    }

    data class ViewState(
        val memberWithThumbnail: MemberWithThumbnail? = null,
        val isMemberCheckedIn: Boolean? = null
    )
}

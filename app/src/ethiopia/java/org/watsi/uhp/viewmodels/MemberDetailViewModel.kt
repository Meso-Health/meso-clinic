package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.MemberWithThumbnail
import org.watsi.domain.usecases.LoadMemberUseCase
import javax.inject.Inject

class MemberDetailViewModel @Inject constructor(
    private val loadMemberUseCase: LoadMemberUseCase
) : ViewModel() {

    fun getObservable(member: Member): LiveData<MemberWithThumbnail> {
        return LiveDataReactiveStreams.fromPublisher(
            loadMemberUseCase.execute(member.id).startWith(MemberWithThumbnail(member, null))
        )
    }
}

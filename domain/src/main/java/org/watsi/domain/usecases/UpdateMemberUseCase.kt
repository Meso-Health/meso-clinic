package org.watsi.domain.usecases

import io.reactivex.Completable
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.MemberRepository

class UpdateMemberUseCase(private val memberRepository: MemberRepository) {

    fun execute(member: Member): Completable {
        return memberRepository.find(member.id).flatMapCompletable { previous ->
            val deltas = member.diff(previous).toMutableList()
            if (deltas.any { it.field == "photoId" }) {
                // use member ID in photo delta because it allows a more simple pattern
                // for querying the delta and creating the sync request
                deltas.add(Delta(
                        action = Delta.Action.ADD,
                        modelName = Delta.ModelName.PHOTO,
                        modelId = member.id))
            }
            memberRepository.save(member, deltas)
        }
    }
}

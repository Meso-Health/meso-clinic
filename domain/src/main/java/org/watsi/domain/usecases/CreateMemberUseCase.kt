package org.watsi.domain.usecases

import io.reactivex.Completable
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.MemberRepository

class CreateMemberUseCase(private val memberRepository: MemberRepository) {

    fun execute(member: Member, submitNow: Boolean): Completable {
        val deltas = mutableListOf<Delta>()
        if (submitNow) {
            deltas.add(
                Delta(
                    action = Delta.Action.ADD,
                    modelName = Delta.ModelName.MEMBER,
                    modelId = member.id
                )
            )

            if (member.photoId != null) {
                // TODO: If this Delta is not created here, it will NEVER be created.
                // This is because we have no way of knowing which photos are new after they are persisted.
                // This works for now because Ethiopia is the only case where we delay creating Deltas and they
                // don't take member photos

                // use member ID in photo delta because it allows a more simple pattern
                // for querying the delta and creating the sync request
                deltas.add(
                    Delta(
                        action = Delta.Action.ADD,
                        modelName = Delta.ModelName.PHOTO,
                        modelId = member.id
                    )
                )
            }
        }

        return memberRepository.upsert(member, deltas)
    }
}

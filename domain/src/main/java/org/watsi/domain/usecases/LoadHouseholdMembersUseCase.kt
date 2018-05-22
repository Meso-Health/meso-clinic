package org.watsi.domain.usecases

import io.reactivex.Flowable
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.MemberWithThumbnail
import org.watsi.domain.repositories.MemberRepository
import java.util.UUID

class LoadHouseholdMembersUseCase(private val memberRepository: MemberRepository) {
    fun execute(member: Member): Flowable<List<MemberWithThumbnail>> {
        return memberRepository.remainingHouseholdMembers(member)
    }
}
package org.watsi.domain.usecases

import io.reactivex.Flowable
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.domain.repositories.MemberRepository

class LoadHouseholdMembersUseCase(private val memberRepository: MemberRepository) {
    fun execute(member: Member): Flowable<List<MemberWithIdEventAndThumbnailPhoto>> {
        return memberRepository.remainingHouseholdMembers(member)
    }
}

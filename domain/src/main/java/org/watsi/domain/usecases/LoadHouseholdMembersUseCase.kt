package org.watsi.domain.usecases

import io.reactivex.Flowable
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.domain.repositories.MemberRepository
import java.util.UUID

class LoadHouseholdMembersUseCase(private val memberRepository: MemberRepository) {
    fun execute(householdId: UUID): Flowable<List<MemberWithIdEventAndThumbnailPhoto>> {
        return memberRepository.findHouseholdMembers(householdId)
    }
}

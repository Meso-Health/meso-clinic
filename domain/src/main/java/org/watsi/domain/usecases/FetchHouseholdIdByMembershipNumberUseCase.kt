package org.watsi.domain.usecases

import io.reactivex.Flowable
import org.watsi.domain.repositories.MemberRepository
import java.util.UUID

class FetchHouseholdIdByMembershipNumberUseCase(private val memberRepository: MemberRepository) {

    fun execute(membershipNumber: String): Flowable<UUID> {
        return memberRepository.fetchHouseholdIdByMembershipNumber(membershipNumber)
    }
}

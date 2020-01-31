package org.watsi.domain.usecases

import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.repositories.MemberRepository
import java.util.UUID

class FindHouseholdIdByMembershipNumberUseCase(private val memberRepository: MemberRepository) {
    fun execute(membershipNumber: String): Maybe<UUID> {
        return memberRepository.findHouseholdIdByMembershipNumber(membershipNumber).subscribeOn(Schedulers.io())
    }
}

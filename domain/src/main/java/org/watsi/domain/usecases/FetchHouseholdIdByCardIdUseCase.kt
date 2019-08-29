package org.watsi.domain.usecases

import io.reactivex.Single
import org.watsi.domain.repositories.MemberRepository
import java.util.UUID

class FetchHouseholdIdByCardIdUseCase(private val memberRepository: MemberRepository) {

    fun execute(cardId: String): Single<UUID> {
        return memberRepository.fetchHouseholdIdByCardId(cardId)
    }
}

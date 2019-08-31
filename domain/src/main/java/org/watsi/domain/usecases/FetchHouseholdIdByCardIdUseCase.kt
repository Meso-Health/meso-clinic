package org.watsi.domain.usecases

import io.reactivex.Flowable
import org.watsi.domain.repositories.MemberRepository
import java.util.UUID

class FetchHouseholdIdByCardIdUseCase(private val memberRepository: MemberRepository) {

    fun execute(cardId: String): Flowable<UUID> {
        return memberRepository.fetchHouseholdIdByCardId(cardId)
    }
}

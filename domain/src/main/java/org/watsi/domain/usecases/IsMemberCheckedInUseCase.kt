package org.watsi.domain.usecases

import io.reactivex.Flowable
import org.watsi.domain.repositories.MemberRepository
import java.util.UUID

class IsMemberCheckedInUseCase(private val memberRepository: MemberRepository) {

    fun execute(memberId: UUID): Flowable<Boolean> {
        return memberRepository.isMemberCheckedIn(memberId)
    }
}
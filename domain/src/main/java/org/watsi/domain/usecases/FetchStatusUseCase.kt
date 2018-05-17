package org.watsi.domain.usecases

import io.reactivex.Flowable
import org.watsi.domain.repositories.MemberRepository

class FetchStatusUseCase(private val memberRepository: MemberRepository) {
    fun execute(): Flowable<Int> {
        return memberRepository.withPhotosToFetchCount()
    }
}

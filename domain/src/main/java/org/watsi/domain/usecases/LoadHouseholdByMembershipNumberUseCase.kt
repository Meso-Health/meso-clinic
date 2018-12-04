package org.watsi.domain.usecases

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.domain.repositories.MemberRepository

class LoadHouseholdByMembershipNumberUseCase(private val memberRepository: MemberRepository) {
    fun execute(parameter: String): Flowable<List<MemberWithIdEventAndThumbnailPhoto>> {
        return memberRepository.findHouseholdByMembershipNumber(parameter).subscribeOn(Schedulers.io())
    }
}


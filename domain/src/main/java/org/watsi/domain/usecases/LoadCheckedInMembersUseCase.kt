package org.watsi.domain.usecases

import io.reactivex.Flowable
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.domain.repositories.MemberRepository

class LoadCheckedInMembersUseCase(private val memberRepository: MemberRepository) {

    fun execute(): Flowable<List<MemberWithIdEventAndThumbnailPhoto>> {
        return memberRepository.checkedInMembers()
    }
}

package org.watsi.domain.usecases

import io.reactivex.Flowable
import org.watsi.domain.relations.MemberWithThumbnail
import org.watsi.domain.repositories.MemberRepository
import java.util.UUID

class LoadMemberUseCase(private val memberRepository: MemberRepository) {
    fun execute(parameter: UUID): Flowable<MemberWithThumbnail> {
        return memberRepository.findMemberWithThumbnailFlowable(parameter)
    }
}

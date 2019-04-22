package org.watsi.domain.usecases

import io.reactivex.Completable
import org.watsi.domain.repositories.MemberRepository

class FetchMembersPhotosUseCase(private val memberRepository: MemberRepository) {

    fun execute(): Completable {
        return memberRepository.downloadPhotos()
    }
}

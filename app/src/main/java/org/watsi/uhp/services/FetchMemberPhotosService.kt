package org.watsi.uhp.services

import io.reactivex.Completable
import org.watsi.domain.repositories.MemberRepository
import javax.inject.Inject

class FetchMemberPhotosService : BaseService() {

    @Inject lateinit var memberRepository: MemberRepository

    override fun executeTasks(): Completable {
        return memberRepository.downloadPhotos()
    }
}

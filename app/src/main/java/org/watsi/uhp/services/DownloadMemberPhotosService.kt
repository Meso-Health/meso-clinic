package org.watsi.uhp.services

import org.watsi.domain.repositories.MemberRepository

import javax.inject.Inject

/**
 * Service class to handle downloading member photos
 */
class DownloadMemberPhotosService : AbstractSyncJobService() {

    @Inject lateinit var memberRepository: MemberRepository

    override fun performSync(): Boolean {
        memberRepository.fetchPhotos()
        return true
    }
}

package org.watsi.uhp.services

import android.app.job.JobParameters
import org.watsi.domain.repositories.MemberRepository

import javax.inject.Inject

/**
 * Service class to handle downloading member photos
 */
class DownloadMemberPhotosService : DaggerJobService() {

    @Inject lateinit var memberRepository: MemberRepository

    override fun onStartJob(params: JobParameters?): Boolean {
        memberRepository.fetchPhotos()
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }
}

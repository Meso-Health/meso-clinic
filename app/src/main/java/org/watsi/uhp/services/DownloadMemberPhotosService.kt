package org.watsi.uhp.services

import android.app.job.JobParameters
import org.watsi.device.managers.Logger
import org.watsi.domain.repositories.MemberRepository

import javax.inject.Inject

/**
 * Service class to handle downloading member photos
 */
class DownloadMemberPhotosService : DaggerJobService() {

    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var logger: Logger

    override fun onStartJob(params: JobParameters?): Boolean {
        memberRepository.downloadPhotos().subscribe({
            jobFinished(params, false)
        }, {
            logger.error(it)
            jobFinished(params, true)
        })
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }
}

package org.watsi.uhp.services

import android.app.job.JobParameters
import io.reactivex.Completable
import org.watsi.device.managers.Logger
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.DiagnosisRepository
import org.watsi.domain.repositories.MemberRepository

import javax.inject.Inject

/**
 * Service class that polls the UHP API and updates the device with updated member and billables data
 */
class FetchService : DaggerJobService() {

    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var billableRepository: BillableRepository
    @Inject lateinit var diagnosisRepository: DiagnosisRepository
    @Inject lateinit var logger: Logger

    override fun onStartJob(params: JobParameters?): Boolean {
        Completable.concatArray(memberRepository.fetch(),
                                billableRepository.fetch(),
                                diagnosisRepository.fetch()).subscribe({
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

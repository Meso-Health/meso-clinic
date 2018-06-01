package org.watsi.uhp.services

import android.app.job.JobParameters
import io.reactivex.CompletableObserver
import io.reactivex.disposables.Disposable
import org.watsi.device.managers.Logger
import org.watsi.domain.repositories.MemberRepository

import javax.inject.Inject

/**
 * Service class to handle downloading member photos
 */
class FetchMemberPhotosService : DaggerJobService() {

    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var logger: Logger
    private lateinit var disposable: Disposable

    override fun onStartJob(params: JobParameters): Boolean {
        memberRepository.downloadPhotos().subscribe(SyncObserver(params))
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        disposable.dispose()
        return true
    }

    inner class SyncObserver(private val params: JobParameters) : CompletableObserver {
        override fun onComplete() {
            jobFinished(params, false)
        }

        override fun onSubscribe(d: Disposable) {
            disposable = d
        }

        override fun onError(e: Throwable) {
            logger.error(e)
            jobFinished(params, true)
        }
    }
}

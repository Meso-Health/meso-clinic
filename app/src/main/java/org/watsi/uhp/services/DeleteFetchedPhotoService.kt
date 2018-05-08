package org.watsi.uhp.services

import android.app.job.JobParameters
import org.watsi.device.managers.Logger
import org.watsi.domain.repositories.PhotoRepository
import javax.inject.Inject

open class DeleteFetchedPhotoService : DaggerJobService() {

    @Inject lateinit var photoRepository: PhotoRepository
    @Inject lateinit var logger: Logger

    override fun onStartJob(params: JobParameters?): Boolean {
        photoRepository.deleteSynced().subscribe({
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

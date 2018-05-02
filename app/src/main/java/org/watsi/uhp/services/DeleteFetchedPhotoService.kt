package org.watsi.uhp.services

import android.app.job.JobParameters
import org.watsi.domain.repositories.PhotoRepository
import javax.inject.Inject

open class DeleteFetchedPhotoService : DaggerJobService() {

    @Inject lateinit var photoRepository: PhotoRepository

    override fun onStartJob(params: JobParameters?): Boolean {
        photoRepository.deleteSynced()
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }
}

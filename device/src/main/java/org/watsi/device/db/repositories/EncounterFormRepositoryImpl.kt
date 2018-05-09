package org.watsi.device.db.repositories

import io.reactivex.Completable
import okhttp3.MediaType
import okhttp3.RequestBody
import org.watsi.device.api.CoverageApi
import org.watsi.device.db.daos.EncounterFormDao
import org.watsi.device.db.daos.PhotoDao
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.Delta
import org.watsi.domain.repositories.EncounterFormRepository

class EncounterFormRepositoryImpl(private val encounterFormDao: EncounterFormDao,
                                  private val photoDao: PhotoDao,
                                  private val api: CoverageApi,
                                  private val sessionManager: SessionManager
) : EncounterFormRepository {

    override fun sync(deltas: List<Delta>): Completable {
        //TODO: finish implementing
        return Completable.complete()
    }
}

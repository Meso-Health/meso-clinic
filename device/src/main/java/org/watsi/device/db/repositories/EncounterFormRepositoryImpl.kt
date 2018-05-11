package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import org.watsi.device.api.CoverageApi
import org.watsi.device.db.daos.EncounterFormDao
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.Delta
import org.watsi.domain.relations.EncounterFormWithEncounterAndPhoto
import org.watsi.domain.repositories.EncounterFormRepository
import java.util.UUID

class EncounterFormRepositoryImpl(private val encounterFormDao: EncounterFormDao,
                                  private val api: CoverageApi,
                                  private val sessionManager: SessionManager
) : EncounterFormRepository {

    override fun find(id: UUID): Single<EncounterFormWithEncounterAndPhoto> {
        return encounterFormDao.find(id)
                .map { it.toEncounterFormWithEncounterAndPhoto() }
                .subscribeOn(Schedulers.io())
    }

    override fun sync(delta: Delta): Completable {
        val authToken = sessionManager.currentToken()!!

        return find(delta.modelId).flatMapCompletable {
            val requestBody = RequestBody.create(MediaType.parse("image/jpg"), it.photo.bytes)
            api.patchEncounterForm(authToken.getHeaderString(), it.encounter.id, requestBody)
        }
    }
}

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
import org.watsi.domain.relations.EncounterFormWithPhoto
import org.watsi.domain.repositories.EncounterFormRepository
import java.util.UUID

class EncounterFormRepositoryImpl(private val encounterFormDao: EncounterFormDao,
                                  private val api: CoverageApi,
                                  private val sessionManager: SessionManager
) : EncounterFormRepository {

    override fun find(id: UUID): Single<EncounterFormWithPhoto> {
        return encounterFormDao.find(id).map { it.toEncounterFormWithPhoto() }
                .subscribeOn(Schedulers.io())
    }

    override fun sync(delta: Delta): Completable {
        return sessionManager.currentToken()?.let { token ->
            find(delta.modelId).flatMapCompletable { encounterFormModel ->
                val requestBody = RequestBody.create(MediaType.parse("image/jpg"), encounterFormModel.photo.bytes)
                api.patchEncounterForm(token.getHeaderString(),
                        encounterFormModel.encounterForm.encounterId, requestBody)
            }.subscribeOn(Schedulers.io())
        } ?: Completable.complete()
    }
}

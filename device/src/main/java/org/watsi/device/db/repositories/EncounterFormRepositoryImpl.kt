package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import org.threeten.bp.Clock
import org.watsi.device.api.CoverageApi
import org.watsi.device.db.daos.EncounterFormDao
import org.watsi.device.db.models.EncounterFormModel
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.Delta
import org.watsi.domain.relations.EncounterFormWithPhoto
import org.watsi.domain.repositories.EncounterFormRepository
import java.util.UUID

class EncounterFormRepositoryImpl(private val encounterFormDao: EncounterFormDao,
                                  private val api: CoverageApi,
                                  private val sessionManager: SessionManager,
                                  private val clock: Clock
) : EncounterFormRepository {

    override fun find(id: UUID): Single<EncounterFormWithPhoto> {
        return encounterFormDao.find(id).map { it.toEncounterFormWithPhoto() }
                .subscribeOn(Schedulers.io())
    }

    private fun create(encounterFormModel: EncounterFormModel): Completable {
        return Completable.fromAction {
            encounterFormDao.update(encounterFormModel)
        }.subscribeOn(Schedulers.io())
    }

    override fun sync(delta: Delta): Completable {
        return sessionManager.currentAuthenticationToken()?.let { token ->
            find(delta.modelId).flatMapCompletable { encounterFormWithPhoto ->
                val requestBody = RequestBody.create(MediaType.parse("image/jpg"), encounterFormWithPhoto.photo.bytes)
                Completable.concatArray(
                    api.patchEncounterForm(token.getHeaderString(),
                            encounterFormWithPhoto.encounterForm.encounterId, requestBody),
                    create(EncounterFormModel.fromEncounterForm(encounterFormWithPhoto.encounterForm.copy(photoId =
                    null), clock))
                )

            }.subscribeOn(Schedulers.io())
        } ?: Completable.complete()
    }
}

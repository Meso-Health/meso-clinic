package org.watsi.device.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.device.db.daos.PhotoDao
import org.watsi.device.db.models.PhotoModel
import java.util.UUID

object PhotoModelFactory {

    fun build(id: UUID = UUID.randomUUID(),
              bytes: ByteArray? = ByteArray(1, { 0xa }),
              createdAt: Instant? = null,
              updatedAt: Instant? = null,
              clock: Clock = Clock.systemUTC()) : PhotoModel {
        val now = Instant.now(clock)
        return PhotoModel(id = id,
                          bytes = bytes,
                          createdAt = createdAt ?: now,
                          updatedAt = updatedAt ?: now)
    }

    fun create(photoDao: PhotoDao,
               id: UUID = UUID.randomUUID(),
               bytes: ByteArray? = ByteArray(1, { 0xa }),
               createdAt: Instant? = null,
               updatedAt: Instant? = null,
               clock: Clock = Clock.systemUTC()) : PhotoModel {
        val model = build(id = id,
                          bytes = bytes,
                          createdAt = createdAt,
                          updatedAt = updatedAt,
                          clock = clock)
        photoDao.insert(model)
        return model
    }
}

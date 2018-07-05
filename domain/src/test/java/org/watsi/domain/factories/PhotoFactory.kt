package org.watsi.domain.factories

import org.watsi.domain.entities.Photo
import java.util.UUID

object PhotoFactory {

    fun build(id: UUID = UUID.randomUUID(),
              bytes: ByteArray = ByteArray(1, { 0xa })) : Photo {
        return Photo(id = id, bytes = bytes)
    }
}

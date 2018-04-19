package org.watsi.domain.entities

import java.util.UUID

data class Photo(val id: UUID,
                 val url: String,
                 val deleted: Boolean)

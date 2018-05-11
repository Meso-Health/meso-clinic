package org.watsi.domain.entities

import java.util.Arrays
import java.util.Objects
import java.util.UUID

data class Photo(val id: UUID,
                 val bytes: ByteArray,
                 val deleted: Boolean = false) {

    override fun equals(other: Any?): Boolean{
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Photo

        return id == other.id &&
                Arrays.equals(bytes, other.bytes) &&
                deleted == other.deleted
    }

    override fun hashCode(): Int{
        return Objects.hash(id, Arrays.hashCode(bytes), deleted)
    }
}

package org.watsi.device.db.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import org.watsi.domain.relations.MemberWithRawPhoto

data class MemberWithRawPhotoModel(
        @Embedded var memberModel: MemberModel? = null,
        @Relation(parentColumn = "photoId", entityColumn = "id", entity = PhotoModel::class)
        var photoModels: List<PhotoModel>? = null) {

    fun toMemberWithRawPhoto(): MemberWithRawPhoto {
        memberModel?.toMember()?.let { member ->
            photoModels?.firstOrNull()?.toPhoto()?.let { photo ->
                return MemberWithRawPhoto(member, photo)
            }
            throw IllegalStateException("PhotoModel cannot be null")
        }
        throw IllegalStateException("MemberModel cannot be null")
    }
}

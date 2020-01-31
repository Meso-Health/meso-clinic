package org.watsi.device.db.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import org.watsi.domain.relations.MemberWithThumbnail

data class MemberWithThumbnailPhotoModel(
    @Embedded var memberModel: MemberModel? = null,
    @Relation(parentColumn = "thumbnailPhotoId", entityColumn = "id", entity = PhotoModel::class)
    var photoModel: List<PhotoModel>? = null) {

    fun toMemberWithThumbnailPhoto(): MemberWithThumbnail {
        memberModel?.toMember()?.let { member ->
            return MemberWithThumbnail(
                member,
                photoModel?.firstOrNull()?.toPhoto()
            )
        }
        throw IllegalStateException("MemberModel cannot be null")
    }
}

package org.watsi.device.db.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto

data class MemberWithIdEventAndThumbnailPhotoModel(
        @Embedded var memberModel: MemberModel? = null,
        @Relation(parentColumn = "id", entityColumn = "memberId")
        var identificationEventModels: List<IdentificationEventModel>? = null,
        @Relation(parentColumn = "thumbnailPhotoId", entityColumn = "id")
        var photoModels: List<PhotoModel>? = null) {

    fun toMemberWithIdEventAndThumbnailPhoto(): MemberWithIdEventAndThumbnailPhoto {
        memberModel?.let { memberModel ->
            return MemberWithIdEventAndThumbnailPhoto(
                    memberModel.toMember(),
                    identificationEventModels?.firstOrNull()?.toIdentificationEvent(),
                    photoModels?.first()?.toPhoto())
        }
        throw IllegalStateException("MemberModel cannot be null")
    }
}

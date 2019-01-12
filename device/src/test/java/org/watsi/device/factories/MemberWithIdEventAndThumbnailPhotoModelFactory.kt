package org.watsi.device.factories

import org.watsi.device.db.models.IdentificationEventModel
import org.watsi.device.db.models.MemberModel
import org.watsi.device.db.models.MemberWithIdEventAndThumbnailPhotoModel

object MemberWithIdEventAndThumbnailPhotoModelFactory {
    fun build(memberModel: MemberModel, idEvent: IdentificationEventModel? = null): MemberWithIdEventAndThumbnailPhotoModel {
        val photoModelList = memberModel.thumbnailPhotoId?.let { id -> listOf(
            PhotoModelFactory.build(id, createdAt = memberModel.createdAt, updatedAt = memberModel.updatedAt)
        ) }
        val idEventList = idEvent?.let { listOf(it) }
        return MemberWithIdEventAndThumbnailPhotoModel(memberModel, idEventList, photoModelList)
    }
}

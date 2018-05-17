package org.watsi.device.factories

import org.watsi.device.db.daos.MemberDao
import org.watsi.device.db.daos.PhotoDao
import org.watsi.device.db.models.MemberModel
import org.watsi.device.db.relations.MemberWithThumbnailModel

object MemberWithThumbnailModelFactory {
    fun build(memberModel: MemberModel): MemberWithThumbnailModel {
        val photoId = memberModel.thumbnailPhotoId
        return if (photoId != null) {
            val photoModel = PhotoModelFactory.build(photoId, createdAt = memberModel.createdAt, updatedAt = memberModel.updatedAt)
            MemberWithThumbnailModel(memberModel, listOf(photoModel))
        } else {
            MemberWithThumbnailModel(memberModel, null)
        }
    }

    fun create(memberDao: MemberDao, photoDao: PhotoDao, memberModel: MemberModel): MemberWithThumbnailModel {
        val modelWithThumbnailModel = build(memberModel)
        modelWithThumbnailModel.memberModel?.let { memberModel ->
            memberDao.insert(memberModel)
        }
        modelWithThumbnailModel.photoModels?.map { photoModel ->
            photoDao.insert(photoModel)
        }
        return modelWithThumbnailModel
    }
}

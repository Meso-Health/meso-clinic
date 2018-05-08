package org.watsi.device.db.daos

import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit
import org.watsi.device.factories.EncounterFormModelFactory
import org.watsi.device.factories.MemberModelFactory
import org.watsi.device.factories.PhotoModelFactory

class PhotoDaoTest : DaoBaseTest() {

    @Test
    fun canBeDeleted() {
        val hourAgo = Instant.now().minus(1, ChronoUnit.HOURS)

        val fullSizePhoto = PhotoModelFactory.create(photoDao, createdAt = hourAgo)
        MemberModelFactory.create(memberDao, photoId = fullSizePhoto.id)
        val thumbnailPhoto = PhotoModelFactory.create(photoDao, createdAt = hourAgo)
        MemberModelFactory.create(memberDao, thumbnailPhotoId = thumbnailPhoto.id)
        val encounterFormPhoto = PhotoModelFactory.create(photoDao, createdAt = hourAgo)
        EncounterFormModelFactory.create(encounterFormDao, photoId = encounterFormPhoto.id)
        PhotoModelFactory.create(photoDao) // recently created photo
        val shouldBeDeleted = PhotoModelFactory.create(photoDao, createdAt = hourAgo)

        photoDao.canBeDeleted().test().assertValue(listOf(shouldBeDeleted))
    }
}

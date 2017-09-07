package org.watsi.uhp.database

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.watsi.uhp.managers.Clock
import org.watsi.uhp.models.Encounter
import org.watsi.uhp.models.EncounterForm
import org.watsi.uhp.models.Member
import org.watsi.uhp.models.Photo
import java.util.*

@RunWith(RobolectricTestRunner::class)
class PhotoDaoTest {

    @Before
    fun setup() {
        DatabaseHelper.init(RuntimeEnvironment.application)
    }

    @Test
    fun canBeDeleted() {
        val justCreatedPhoto = Photo()
        justCreatedPhoto.url = "justCreatedPhoto"
        justCreatedPhoto.create()

        val photoSyncedMember = Photo()
        photoSyncedMember.url = "photoSyncedMember"
        photoSyncedMember.create()
        createMember(photoSyncedMember, false)

        val photoSyncedEncounterForm = Photo()
        photoSyncedEncounterForm.url = "photoSyncedEncounterForm"
        photoSyncedEncounterForm.create()
        createEncounterForm(photoSyncedEncounterForm, false)

        val deletedPhotoWithSyncedMember = Photo()
        deletedPhotoWithSyncedMember.deleted = true
        deletedPhotoWithSyncedMember.url = "deletedPhoto"
        deletedPhotoWithSyncedMember.create()
        createMember(deletedPhotoWithSyncedMember, false)

        val photoDirtyMember = Photo()
        photoDirtyMember.url = "photoDirtyMember"
        photoDirtyMember.create()
        createMember(photoDirtyMember, true)

        val photoDirtyEncounterForm = Photo()
        photoDirtyEncounterForm.url = "photoDirtyEncounterForm"
        photoDirtyEncounterForm.create()
        createEncounterForm(photoDirtyEncounterForm, true)

        val hourAgo = Calendar.getInstance()
        hourAgo.time = Clock.getCurrentTime()
        hourAgo.add(Calendar.HOUR, -1)
        Clock.setTime(hourAgo.time)

        val orphanedOldPhoto = Photo()
        orphanedOldPhoto.url = "orphanedOldPhoto"
        orphanedOldPhoto.create()

        val canBeDeletedPhotoIds = PhotoDao.canBeDeleted().map { it.id }

        assertFalse(canBeDeletedPhotoIds.contains(deletedPhotoWithSyncedMember.id))
        assertFalse(canBeDeletedPhotoIds.contains(justCreatedPhoto.id))
        assertFalse(canBeDeletedPhotoIds.contains(photoDirtyMember.id))
        assertFalse(canBeDeletedPhotoIds.contains(photoDirtyEncounterForm.id))
        assertTrue(canBeDeletedPhotoIds.contains(photoSyncedMember.id))
        assertTrue(canBeDeletedPhotoIds.contains(photoSyncedEncounterForm.id))
        assertTrue(canBeDeletedPhotoIds.contains(orphanedOldPhoto.id))
    }

    private fun createMember(photo: Photo, dirty: Boolean) {
        val member = Member()
        member.id = UUID.randomUUID()
        member.localMemberPhoto = photo
        member.fullName = "Foo"
        member.cardId = "RWI000000"
        member.birthdateAccuracy = Member.BirthdateAccuracyEnum.Y
        member.birthdate = Clock.getCurrentTime()
        member.gender = Member.GenderEnum.M

        if (dirty) {
            member.saveChanges("foo")
        } else {
            MemberDao.create(member)
        }
    }

    private fun createEncounterForm(photo: Photo, dirty: Boolean) {
        val form = EncounterForm()
        form.id = UUID.randomUUID()
        form.photo = photo
        val encounter = Encounter()
        encounter.id = UUID.randomUUID()
        form.encounter = encounter

        if (dirty) {
            form.saveChanges("foo")
        } else {
            DatabaseHelper.getHelper().getDao(EncounterForm::class.java).create(form)
        }
    }
}
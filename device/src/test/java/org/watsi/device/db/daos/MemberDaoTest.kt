package org.watsi.device.db.daos

import org.junit.Test
import org.watsi.device.db.models.MemberWithIdEventAndThumbnailPhotoModel
import org.watsi.device.factories.DeltaModelFactory
import org.watsi.device.factories.EncounterModelFactory
import org.watsi.device.factories.IdentificationEventModelFactory
import org.watsi.device.factories.MemberModelFactory
import org.watsi.device.factories.MemberWithThumbnailModelFactory
import org.watsi.device.factories.PhotoModelFactory
import org.watsi.domain.entities.Delta
import java.util.UUID

class MemberDaoTest : DaoBaseTest() {

    @Test
    fun checkedInMembers() {
        val memberThumbnailPhoto = PhotoModelFactory.create(photoDao)
        val memberWithOpenCheckIn = MemberModelFactory.create(
                memberDao, thumbnailPhotoId = memberThumbnailPhoto.id)
        val memberWithDismissedCheckIn = MemberModelFactory.create(memberDao)
        val memberWithEncounter = MemberModelFactory.create(memberDao)
        MemberModelFactory.create(memberDao)

        // open identification event
        val openCheckIn = IdentificationEventModelFactory.create(identificationEventDao,
                memberId = memberWithOpenCheckIn.id,
                dismissed = false)

        // dismissed identification event
        IdentificationEventModelFactory.create(identificationEventDao,
                memberId = memberWithDismissedCheckIn.id,
                dismissed = true)

        // open identification event but with corresponding encounter
        val idEventWithEncounter = IdentificationEventModelFactory.create(identificationEventDao,
                memberId = memberWithEncounter.id,
                dismissed = false)
        EncounterModelFactory.create(encounterDao, identificationEventId = idEventWithEncounter.id)

        val memberWithOpenCheckInRelation = MemberWithIdEventAndThumbnailPhotoModel(
                memberWithOpenCheckIn,
                listOf(openCheckIn),
                listOf(memberThumbnailPhoto))
        memberDao.checkedInMembers().test().assertValue(listOf(memberWithOpenCheckInRelation))
    }

    @Test
    fun isMemberCheckedIn() {
        val memberThumbnailPhoto = PhotoModelFactory.create(photoDao)
        val memberWithOpenCheckIn = MemberModelFactory.create(
                memberDao, thumbnailPhotoId = memberThumbnailPhoto.id)
        val memberWithDismissedCheckIn = MemberModelFactory.create(memberDao)
        val memberWithEncounter = MemberModelFactory.create(memberDao)
        MemberModelFactory.create(memberDao)

        // open identification event
        val openCheckIn = IdentificationEventModelFactory.create(identificationEventDao,
                memberId = memberWithOpenCheckIn.id,
                dismissed = false)

        // dismissed identification event
        IdentificationEventModelFactory.create(identificationEventDao,
                memberId = memberWithDismissedCheckIn.id,
                dismissed = true)

        // open identification event but with corresponding encounter
        val idEventWithEncounter = IdentificationEventModelFactory.create(identificationEventDao,
                memberId = memberWithEncounter.id,
                dismissed = false)
        EncounterModelFactory.create(encounterDao, identificationEventId = idEventWithEncounter.id)

        memberDao.isMemberCheckedIn(memberWithOpenCheckIn.id).test().assertValue(true)
        memberDao.isMemberCheckedIn(memberWithDismissedCheckIn.id).test().assertValue(false)
        memberDao.isMemberCheckedIn(memberWithEncounter.id).test().assertValue(false)
    }

    @Test
    fun findHouseholdIdByMembershipNumber() {
        val householdId = UUID.randomUUID()
        val membershipNumber = "01/01/01/P/10"
        val memberModel = MemberModelFactory.build(householdId = householdId, membershipNumber = membershipNumber)
        val memberWithThumbnailModel = MemberWithThumbnailModelFactory.create(memberDao, photoDao, memberModel)

        memberDao.findHouseholdIdByMembershipNumber(membershipNumber)
            .test()
            .assertValue(householdId)
    }

    @Test
    fun findHouseholdIdByMembershipNumberDuplicate() {
        val householdId1 = UUID.randomUUID()
        val householdId2 = UUID.randomUUID()
        val membershipNumber = "01/01/01/P/10"
        val memberModel1 = MemberModelFactory.build(householdId = householdId1, membershipNumber = membershipNumber)
        val memberWithThumbnailModel1 = MemberWithThumbnailModelFactory.create(memberDao, photoDao, memberModel1)
        val memberModel2 = MemberModelFactory.build(householdId = householdId2, membershipNumber = membershipNumber)
        val memberWithThumbnailModel2 = MemberWithThumbnailModelFactory.create(memberDao, photoDao, memberModel2)

        memberDao.findHouseholdIdByMembershipNumber(membershipNumber)
            .test()
            .assertValue(householdId2)
    }

    @Test
    fun findHouseholdMembers() {
        val householdId = UUID.randomUUID()
        val householdMembers = (1..3).map {
            MemberWithIdEventAndThumbnailPhotoModel(
                memberModel = MemberModelFactory.create(memberDao, householdId = householdId),
                identificationEventModels = emptyList()
            )
        }
        MemberModelFactory.create(memberDao)

        memberDao.findHouseholdMembers(householdId)
            .test()
            .assertValue(householdMembers)
    }

    @Test
    fun needPhotoDownload() {
        val needsPhoto = MemberModelFactory.create(memberDao, photoUrl = "foo", thumbnailPhotoId = null)
        // photo downloaded
        MemberModelFactory.create(memberDao, photoUrl = "foo", thumbnailPhotoId = UUID.randomUUID())
        // does not have photo
        MemberModelFactory.create(memberDao, photoUrl = null)

        memberDao.needPhotoDownload().test().assertValue(listOf(needsPhoto))
    }

    @Test
    fun delete() {
        val model1 = MemberModelFactory.create(memberDao)
        val model2 = MemberModelFactory.create(memberDao)

        memberDao.delete(listOf(model1.id))

        memberDao.all().test().assertValue(listOf(model2))
    }

    @Test
    fun unsynced() {
        val unsyncedMember = MemberModelFactory.create(memberDao)
        val syncedMember = MemberModelFactory.create(memberDao)
        MemberModelFactory.create(memberDao)

        DeltaModelFactory.create(deltaDao,
                modelName = Delta.ModelName.MEMBER, modelId = unsyncedMember.id, synced = false)
        DeltaModelFactory.create(deltaDao,
                modelName = Delta.ModelName.MEMBER, modelId = syncedMember.id, synced = true)

        memberDao.unsynced().test().assertValue(listOf(unsyncedMember))
    }

    @Test
    fun needPhotoDownloadCount() {
        // awaiting photo download
        MemberModelFactory.create(memberDao, photoUrl = "foo", thumbnailPhotoId = null)
        // photo downloaded
        MemberModelFactory.create(memberDao, photoUrl = "foo", thumbnailPhotoId = UUID.randomUUID())
        // does not have photo
        MemberModelFactory.create(memberDao, photoUrl = null)

        memberDao.needPhotoDownloadCount().test().assertValue(1)
    }

    @Test
    fun findFlowableMemberWithThumbnail() {
        val memberId = UUID.randomUUID()
        val memberModel = MemberModelFactory.build(id = memberId)
        val memberWithThumbnailModel = MemberWithThumbnailModelFactory.create(memberDao, photoDao, memberModel)

        memberDao.findFlowableMemberWithThumbnail(memberId)
                .test()
                .assertValue(memberWithThumbnailModel)

        memberDao.findFlowableMemberWithThumbnail(UUID.randomUUID())
                .test()
                .assertEmpty()
    }

    @Test
    fun byIds() {
        val photoModel = PhotoModelFactory.create(photoDao)
        val memberModel = MemberModelFactory.create(memberDao, thumbnailPhotoId = photoModel.id)
        val idEventModel = IdentificationEventModelFactory.create(
                identificationEventDao, memberId = memberModel.id)

        val expectedRelationModel = MemberWithIdEventAndThumbnailPhotoModel(
                memberModel, listOf(idEventModel), listOf(photoModel))

        memberDao.byIds(listOf(memberModel.id)).test().assertValue(listOf(expectedRelationModel))
    }
}

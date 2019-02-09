package org.watsi.device.db.daos

import org.junit.Test
import org.threeten.bp.Instant
import org.watsi.device.db.models.MemberWithIdEventAndThumbnailPhotoModel
import org.watsi.device.factories.DeltaModelFactory
import org.watsi.device.factories.EncounterModelFactory
import org.watsi.device.factories.IdentificationEventModelFactory
import org.watsi.device.factories.MemberModelFactory
import org.watsi.device.factories.MemberWithThumbnailModelFactory
import org.watsi.device.factories.PhotoModelFactory
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Member.ArchivedReason
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
    fun findHouseholdIdByCardId() {
        val householdId = UUID.randomUUID()
        val cardId = "ETH345987"
        val memberModel = MemberModelFactory.build(householdId = householdId, cardId = cardId)
        MemberWithThumbnailModelFactory.create(memberDao, photoDao, memberModel)

        memberDao.findHouseholdIdByCardId(cardId)
            .test()
            .assertValue(householdId)
    }

    @Test
    fun findHouseholdIdByCardIdNoHousehold() {
        val cardId = "ETH345987"
        val memberModel = MemberModelFactory.build(householdId = null, cardId = cardId)
        MemberWithThumbnailModelFactory.create(memberDao, photoDao, memberModel)

        memberDao.findHouseholdIdByCardId(cardId)
            .test()
            .assertNoValues()
    }

    @Test
    fun findHouseholdIdByCardIdDuplicate() {
        val householdId1 = UUID.randomUUID()
        val householdId2 = UUID.randomUUID()
        val cardId = "ETH345987"
        val memberModel1 = MemberModelFactory.build(householdId = householdId1, cardId = cardId)
        MemberWithThumbnailModelFactory.create(memberDao, photoDao, memberModel1)
        val memberModel2 = MemberModelFactory.build(householdId = householdId2, cardId = cardId)
        MemberWithThumbnailModelFactory.create(memberDao, photoDao, memberModel2)

        memberDao.findHouseholdIdByCardId(cardId)
            .test()
            .assertValue(householdId2)
    }

    @Test
    fun findHouseholdIdByCardIdUnarchived() {
        val householdId = UUID.randomUUID()
        val cardId = "ETH345987"
        val archivedCardId = "ETH345988"
        val memberModel = MemberModelFactory.build(householdId = householdId, cardId = cardId)
        val archivedMemberModel = MemberModelFactory.build(householdId = householdId, cardId = archivedCardId, archivedReason = ArchivedReason.DEATH, archivedAt = Instant.now())
        MemberWithThumbnailModelFactory.create(memberDao, photoDao, memberModel)
        MemberWithThumbnailModelFactory.create(memberDao, photoDao, archivedMemberModel)

        memberDao.findHouseholdIdByCardIdUnarchived(cardId)
            .test()
            .assertValue(householdId)

        memberDao.findHouseholdIdByCardIdUnarchived(archivedCardId)
            .test()
            .assertNoValues()
    }

    @Test
    fun findHouseholdIdByMembershipNumber() {
        val householdId = UUID.randomUUID()
        val membershipNumber = "01/01/01/P/10"
        val memberModel = MemberModelFactory.build(householdId = householdId, membershipNumber = membershipNumber)
        MemberWithThumbnailModelFactory.create(memberDao, photoDao, memberModel)

        memberDao.findHouseholdIdByMembershipNumber(membershipNumber)
            .test()
            .assertValue(householdId)
    }

    @Test
    fun findHouseholdIdByMembershipNumberNoHousehold() {
        val membershipNumber = "01/01/01/P/10"
        val memberModel = MemberModelFactory.build(householdId = null, membershipNumber = membershipNumber)
        MemberWithThumbnailModelFactory.create(memberDao, photoDao, memberModel)

        memberDao.findHouseholdIdByMembershipNumber(membershipNumber)
            .test()
            .assertNoValues()
    }

    @Test
    fun findHouseholdIdByMembershipNumberDuplicate() {
        val householdId1 = UUID.randomUUID()
        val householdId2 = UUID.randomUUID()
        val membershipNumber = "01/01/01/P/10"
        val memberModel1 = MemberModelFactory.build(householdId = householdId1, membershipNumber = membershipNumber)
        MemberWithThumbnailModelFactory.create(memberDao, photoDao, memberModel1)
        val memberModel2 = MemberModelFactory.build(householdId = householdId2, membershipNumber = membershipNumber)
        MemberWithThumbnailModelFactory.create(memberDao, photoDao, memberModel2)

        memberDao.findHouseholdIdByMembershipNumber(membershipNumber)
            .test()
            .assertValue(householdId2)
    }

    @Test
    fun findHouseholdIdByMembershipNumberUnarchived() {
        val householdId = UUID.randomUUID()
        val membershipNumber = "01/01/01/P/10"
        val archivedMembershipNumber = "01/01/01/P/11"
        val memberModel = MemberModelFactory.build(householdId = householdId, membershipNumber = membershipNumber)
        val archivedMemberModel = MemberModelFactory.build(householdId = householdId, membershipNumber = archivedMembershipNumber, archivedAt = Instant.now(), archivedReason = ArchivedReason.DEATH)
        MemberWithThumbnailModelFactory.create(memberDao, photoDao, memberModel)
        MemberWithThumbnailModelFactory.create(memberDao, photoDao, archivedMemberModel)

        memberDao.findHouseholdIdByMembershipNumberUnarchived(membershipNumber)
            .test()
            .assertValue(householdId)

        memberDao.findHouseholdIdByMembershipNumberUnarchived(archivedMembershipNumber)
            .test()
            .assertNoValues()
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
    fun findHouseholdMembersUnarchived() {
        val householdId = UUID.randomUUID()
        val householdMembers = (1..3).map {
            MemberWithIdEventAndThumbnailPhotoModel(
                memberModel = MemberModelFactory.create(memberDao, householdId = householdId),
                identificationEventModels = emptyList()
            )
        }
        MemberModelFactory.create(memberDao)
        MemberWithIdEventAndThumbnailPhotoModel(
            memberModel = MemberModelFactory.create(memberDao, householdId = householdId, archivedAt = Instant.now(), archivedReason = ArchivedReason.DEATH),
            identificationEventModels = emptyList()
        )

        memberDao.findHouseholdMembersUnarchived(householdId)
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
        // is archived
        MemberModelFactory.create(memberDao, photoUrl = "foo", thumbnailPhotoId = null, archivedAt = Instant.now(), archivedReason = ArchivedReason.DEATH)

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
        // is archived
        MemberModelFactory.create(memberDao, photoUrl = "foo", thumbnailPhotoId = null, archivedAt = Instant.now(), archivedReason = ArchivedReason.DEATH)

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
    fun findMembersByIds() {
        val memberModel1 = MemberModelFactory.create(memberDao)
        val memberModel2 = MemberModelFactory.create(memberDao)
        val memberModel3 = MemberModelFactory.create(memberDao)
        val idNotInDB = UUID.randomUUID()

        memberDao.findMembersByIds(listOf(memberModel1.id, idNotInDB, memberModel2.id)).test()
                .assertValue { value -> value.sortedBy { it.createdAt } == listOf(memberModel1, memberModel2) }
    }

    @Test
    fun findMemberRelationsByIds() {
        val photoModel = PhotoModelFactory.create(photoDao)
        val memberModel = MemberModelFactory.create(memberDao, thumbnailPhotoId = photoModel.id)
        val idEventModel = IdentificationEventModelFactory.create(
                identificationEventDao, memberId = memberModel.id)

        val expectedRelationModel = MemberWithIdEventAndThumbnailPhotoModel(
                memberModel, listOf(idEventModel), listOf(photoModel))

        memberDao.findMemberRelationsByIds(listOf(memberModel.id)).test().assertValue(listOf(expectedRelationModel))
    }

    @Test
    fun all_returnsAllMembersWithHouseholdIds() {
        val memberWithHousehold = MemberModelFactory.create(memberDao, householdId = UUID.randomUUID())
        MemberModelFactory.create(memberDao, householdId = null)

        memberDao.all().test().assertValue(listOf(memberWithHousehold))
    }

    @Test
    fun allUnarchived_returnsAllUnarchivedMembersWithHouseholdIds() {
        val memberWithHousehold = MemberModelFactory.create(memberDao, householdId = UUID.randomUUID())
        val archivedMember = MemberModelFactory.create(memberDao, householdId = UUID.randomUUID(), archivedReason = ArchivedReason.DEATH, archivedAt = Instant.now())
        MemberModelFactory.create(memberDao, householdId = null)

        memberDao.allUnarchived().test().assertValue(listOf(memberWithHousehold))
    }
}

package org.watsi.device.db.daos

import org.junit.Test
import org.threeten.bp.Instant
import org.watsi.device.factories.DeltaModelFactory
import org.watsi.device.factories.EncounterModelFactory
import org.watsi.device.factories.IdentificationEventModelFactory
import org.watsi.device.factories.MemberModelFactory
import org.watsi.device.factories.PhotoModelFactory
import org.watsi.domain.entities.Delta

class IdentificationEventDaoTest : DaoBaseTest() {

    @Test
    fun unsynced() {
        val member1 = MemberModelFactory.create(memberDao)
        val member2 = MemberModelFactory.create(memberDao)
        val member3 = MemberModelFactory.create(memberDao)
        val unsyncedIdentificationEvent = IdentificationEventModelFactory.create(identificationEventDao, memberId = member1.id)
        val syncedIdentificationEvent = IdentificationEventModelFactory.create(identificationEventDao, memberId = member2.id)
        IdentificationEventModelFactory.create(identificationEventDao, memberId = member3.id)

        DeltaModelFactory.create(deltaDao,
            modelName = Delta.ModelName.IDENTIFICATION_EVENT, modelId = unsyncedIdentificationEvent.id, synced = false)
        DeltaModelFactory.create(deltaDao,
            modelName = Delta.ModelName.IDENTIFICATION_EVENT, modelId = syncedIdentificationEvent.id, synced = true)

        identificationEventDao.unsynced().test().assertValue(listOf(unsyncedIdentificationEvent))
    }

    @Test
    fun activeCheckIns() {
        val memberThumbnailPhoto = PhotoModelFactory.create(photoDao)
        val memberWithIdEvent = MemberModelFactory.create(
            memberDao, thumbnailPhotoId = memberThumbnailPhoto.id)
        val memberWithDismissedIdEvent = MemberModelFactory.create(memberDao)
        val memberWithPartialEncounter = MemberModelFactory.create(memberDao)
        val memberWithEncounter = MemberModelFactory.create(memberDao)
        val first = Instant.parse("2018-05-21T10:15:30.000Z")
        val second = Instant.parse("2018-05-30T12:30:00.000Z")
        val third = Instant.parse("2018-06-05T12:30:00.000Z")
        val fourth = Instant.parse("2018-06-10T12:30:00.000Z")

        // id event with no encounter
        val idEventWithNoEncounter = IdentificationEventModelFactory.create(
            identificationEventDao,
            memberId = memberWithIdEvent.id,
            dismissed = false,
            occurredAt = first
        )

        // dismissed id event with no encounter
        IdentificationEventModelFactory.create(identificationEventDao,
            memberId = memberWithDismissedIdEvent.id,
            dismissed = true
        )

        // id event with partial encounter
        val idEventWithPartialEncounter = IdentificationEventModelFactory.create(
            identificationEventDao,
            memberId = memberWithPartialEncounter.id,
            dismissed = false,
            occurredAt = second
        )
        EncounterModelFactory.create(encounterDao, memberDao, identificationEventId = idEventWithPartialEncounter.id, preparedAt = null)

        // id event with encounter
        val idEventWithEncounter = IdentificationEventModelFactory.create(
            identificationEventDao,
            memberId = memberWithEncounter.id,
            dismissed = false
        )
        EncounterModelFactory.create(encounterDao, memberDao, identificationEventId = idEventWithEncounter.id)

        val secondIdEventWithNoEncounter = IdentificationEventModelFactory.create(
            identificationEventDao,
            memberId = memberWithIdEvent.id,
            dismissed = false,
            occurredAt = third
        )

        val thirdIdEventWithEncounter = IdentificationEventModelFactory.create(
            identificationEventDao,
            memberId = memberWithIdEvent.id,
            dismissed = false,
            occurredAt = fourth
        )
        EncounterModelFactory.create(encounterDao, memberDao, identificationEventId = thirdIdEventWithEncounter.id)

        identificationEventDao.activeCheckIns().test().assertValue(listOf(
            idEventWithNoEncounter,
            idEventWithPartialEncounter,
            secondIdEventWithNoEncounter
        ))
    }
}

package org.watsi.device.db.daos

import org.junit.Test
import org.watsi.device.factories.DeltaModelFactory
import org.watsi.device.factories.EncounterModelFactory
import org.watsi.device.factories.IdentificationEventModelFactory
import org.watsi.device.factories.MemberModelFactory
import org.watsi.domain.entities.Delta

class IdentificationEventDaoTest : DaoBaseTest() {

    @Test
    fun openCheckIn_memberWithNoIdEvent() {
        val member = MemberModelFactory.create(memberDao)

        identificationEventDao.openCheckIn(member.id).test().assertNoValues()
    }
    
    @Test
    fun openCheckIn_memberWithIdEventAndNoEncounter() {
        val member = MemberModelFactory.create(memberDao)
        val idEventWithNoEncounter = IdentificationEventModelFactory.create(
            identificationEventDao,
            memberId = member.id,
            dismissed = false
        )

        identificationEventDao.openCheckIn(member.id).test().assertValue(idEventWithNoEncounter)
    }

    @Test
    fun openCheckIn_memberWithDismissedIdEventAndNoEncounter() {
        val member = MemberModelFactory.create(memberDao)
        IdentificationEventModelFactory.create(identificationEventDao,
            memberId = member.id,
            dismissed = true
        )

        identificationEventDao.openCheckIn(member.id).test().assertNoValues()
    }

    @Test
    fun openCheckIn_memberWithIdEventAndPartialEncounter() {
        val member = MemberModelFactory.create(memberDao)
        val idEventWithPartialEncounter = IdentificationEventModelFactory.create(
            identificationEventDao,
            memberId = member.id,
            dismissed = false
        )
        EncounterModelFactory.create(encounterDao, memberDao,
            identificationEventId = idEventWithPartialEncounter.id,
            memberId = member.id,
            preparedAt = null
        )

        identificationEventDao.openCheckIn(member.id).test().assertValue(idEventWithPartialEncounter)
    }

    @Test
    fun openCheckIn_memberWithDismissedIdEventAndPartialEncounter() {
        val member = MemberModelFactory.create(memberDao)
        val dismissedIdEventWithPartialEncounter = IdentificationEventModelFactory.create(
            identificationEventDao,
            memberId = member.id,
            dismissed = true
        )
        EncounterModelFactory.create(encounterDao, memberDao,
            identificationEventId = dismissedIdEventWithPartialEncounter.id,
            memberId = member.id,
            preparedAt = null
        )

        identificationEventDao.openCheckIn(member.id).test().assertNoValues()
    }

    @Test
    fun openCheckIn_memberWithIdEventAndEncounter() {
        val member = MemberModelFactory.create(memberDao)
        val idEventWithEncounter = IdentificationEventModelFactory.create(
            identificationEventDao,
            memberId = member.id,
            dismissed = false
        )
        EncounterModelFactory.create(encounterDao, memberDao,
            identificationEventId = idEventWithEncounter.id,
            memberId = member.id
        )

        identificationEventDao.openCheckIn(member.id).test().assertNoValues()
    }

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
}

package org.watsi.device.db.daos

import org.junit.Test
import org.watsi.device.factories.DeltaModelFactory
import org.watsi.device.factories.EncounterModelFactory
import org.watsi.device.factories.IdentificationEventModelFactory
import org.watsi.device.factories.MemberModelFactory
import org.watsi.domain.entities.Delta

class IdentificationEventDaoTest : DaoBaseTest() {

    @Test
    fun openCheckIn() {
        val memberWithOpenCheckIn = MemberModelFactory.create(memberDao)
        val memberWithDismissedCheckIn = MemberModelFactory.create(memberDao)
        val memberWithEncounter = MemberModelFactory.create(memberDao)
        val memberWithNoCheckIn = MemberModelFactory.create(memberDao)

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
            dismissed = false
        )
        EncounterModelFactory.create(encounterDao, memberDao,
            identificationEventId = idEventWithEncounter.id
        )

        identificationEventDao.openCheckIn(memberWithOpenCheckIn.id).test().assertValue(openCheckIn)
        identificationEventDao.openCheckIn(memberWithDismissedCheckIn.id).test().assertNoValues()
        identificationEventDao.openCheckIn(memberWithEncounter.id).test().assertNoValues()
        identificationEventDao.openCheckIn(memberWithNoCheckIn.id).test().assertNoValues()
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

package org.watsi.device.db.daos

import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit
import org.watsi.device.db.models.EncounterWithExtrasModel
import org.watsi.device.factories.DeltaModelFactory
import org.watsi.device.factories.EncounterModelFactory
import org.watsi.device.factories.MemberModelFactory
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Encounter
import java.util.UUID

class EncounterDaoTest : DaoBaseTest() {
    @Test
    fun pending() {
        val encounterModel1 = EncounterModelFactory.create(encounterDao, memberDao, submittedAt = null)
        val encounterModel2 = EncounterModelFactory.create(encounterDao, memberDao, submittedAt = null)
        EncounterModelFactory.create(encounterDao, memberDao, submittedAt = Instant.now())
        EncounterModelFactory.create(encounterDao, memberDao, submittedAt = Instant.now().minus(1, ChronoUnit.HOURS))

        assertEquals(encounterDao.pending().test().values().first().map { it.encounterModel },
            listOf(encounterModel1, encounterModel2)
        )
    }

    @Test
    fun pendingCount() {
        EncounterModelFactory.create(encounterDao, memberDao, submittedAt = Instant.now())
        EncounterModelFactory.create(encounterDao, memberDao, submittedAt = null)

        val pendingCount = encounterDao.pendingCount().test().values().first()

        assertEquals(1, pendingCount)
    }

    @Test
    fun returned() {
        val encounterModel1 = EncounterModelFactory.create(
            encounterDao,
            memberDao,
            adjudicationState = Encounter.AdjudicationState.RETURNED
        )
        val encounterModel2 = EncounterModelFactory.create(
            encounterDao,
            memberDao,
            adjudicationState = Encounter.AdjudicationState.RETURNED
        )
        EncounterModelFactory.create(
            encounterDao,
            memberDao,
            adjudicationState = Encounter.AdjudicationState.PENDING
        )
        EncounterModelFactory.create(
            encounterDao,
            memberDao,
            adjudicationState = Encounter.AdjudicationState.REVISED
        )

        assertEquals(encounterDao.returned().test().values().first().map { it.encounterModel },
            listOf(encounterModel1, encounterModel2)
        )
    }

    @Test
    fun returnedCount() {
        EncounterModelFactory.create(encounterDao, memberDao, adjudicationState = Encounter.AdjudicationState.RETURNED)
        EncounterModelFactory.create(encounterDao, memberDao, adjudicationState = Encounter.AdjudicationState.RETURNED)
        EncounterModelFactory.create(encounterDao, memberDao, adjudicationState = Encounter.AdjudicationState.PENDING)
        EncounterModelFactory.create(encounterDao, memberDao, adjudicationState = Encounter.AdjudicationState.REVISED)

        val returnedCount = encounterDao.returnedCount().test().values().first()

        assertEquals(2, returnedCount)
    }

    @Test
    fun update_noExistingRecords() {
        val encounterModel1 = EncounterModelFactory.build(memberId = UUID.randomUUID())
        assertEquals(encounterDao.update(listOf(encounterModel1)), 0)
    }

    @Test
    fun update_existingRecords() {
        val encounterModel1 = EncounterModelFactory.create(encounterDao, memberDao)
        assertEquals(encounterDao.update(listOf(encounterModel1)), 1)
    }

    @Test
    fun returnedIds() {
        val encounterModel1 = EncounterModelFactory.create(encounterDao, memberDao, adjudicationState = Encounter.AdjudicationState.RETURNED)
        EncounterModelFactory.create(encounterDao, memberDao, adjudicationState = Encounter.AdjudicationState.REVISED)
        EncounterModelFactory.create(encounterDao, memberDao, adjudicationState = Encounter.AdjudicationState.PENDING)
        val encounterModel4 = EncounterModelFactory.create(encounterDao, memberDao, adjudicationState = Encounter.AdjudicationState.RETURNED)
        assertEquals(encounterDao.returnedIds().test().values().first(), listOf(encounterModel1.id, encounterModel4.id))
    }

    @Test
    fun revisedIds() {
        val encounterModel1 = EncounterModelFactory.create(encounterDao, memberDao)
        EncounterModelFactory.create(encounterDao, memberDao, revisedEncounterId = encounterModel1.id)
        val encounterModel3 = EncounterModelFactory.create(encounterDao, memberDao)
        EncounterModelFactory.create(encounterDao, memberDao, revisedEncounterId = encounterModel3.id)
        EncounterModelFactory.create(encounterDao, memberDao, revisedEncounterId = encounterModel3.id)
        assertEquals(encounterDao.revisedIds().test().values().first(), listOf(encounterModel1.id, encounterModel3.id))
    }

    @Test
    fun unsynced() {
        val unsyncedMember = MemberModelFactory.create(memberDao)
        val unsyncedEncounter = EncounterModelFactory.create(encounterDao, memberDao, memberId = unsyncedMember.id)
        val syncedMember = MemberModelFactory.create(memberDao)
        val syncedEncounter = EncounterModelFactory.create(encounterDao, memberDao, memberId = syncedMember.id)

        EncounterModelFactory.create(encounterDao, memberDao)

        DeltaModelFactory.create(deltaDao,
            modelName = Delta.ModelName.ENCOUNTER, modelId = unsyncedEncounter.id, synced = false)
        DeltaModelFactory.create(deltaDao,
            modelName = Delta.ModelName.ENCOUNTER, modelId = syncedEncounter.id, synced = true)

        val unsyncedEncounterRelation = EncounterWithExtrasModel(
            encounterModel = unsyncedEncounter,
            memberModel = listOf(unsyncedMember),
            encounterFormModels = emptyList(),
            encounterItemWithBillableAndPriceModels = emptyList(),
            referralModels = emptyList()
        )

        encounterDao.unsynced().test().assertValue(listOf(unsyncedEncounterRelation))
    }

    @Test
    fun encountersForMemberBetween() {
        val member = MemberModelFactory.create(memberDao)
        val t1 = Instant.now()
        val t2 = t1.plusSeconds(86400)
        val before = t1.minusSeconds(500)
        val during = t1.plusSeconds(1000)
        val after = t2.plusSeconds(2000)

        // memberEncounterBefore
        EncounterModelFactory.create(encounterDao, memberDao, occurredAt = before, memberId = member.id)
        // memberEncounterAfter
        EncounterModelFactory.create(encounterDao, memberDao, occurredAt = after, memberId = member.id)
        val memberEncounterDuring = EncounterModelFactory.create(encounterDao, memberDao,
            occurredAt = during, memberId = member.id)
        // otherMemberEncounterDuring
        EncounterModelFactory.create(encounterDao, memberDao, occurredAt = during)

        encounterDao.encountersForMemberBetween(member.id, t1, t2).test().assertValue(listOf(memberEncounterDuring.id))
    }
}

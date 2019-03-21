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

class EncounterDaoTest : DaoBaseTest() {

    @Test
    fun pending() {
        val encounterModel1 = EncounterModelFactory.create(encounterDao, submittedAt = null)
        val encounterModel2 = EncounterModelFactory.create(encounterDao, submittedAt = null)
        EncounterModelFactory.create(encounterDao, submittedAt = Instant.now())
        EncounterModelFactory.create(encounterDao, submittedAt = Instant.now().minus(1, ChronoUnit.HOURS))

        assertEquals(encounterDao.pending().test().values().first().map { it.encounterModel },
            listOf(encounterModel1, encounterModel2)
        )
    }

    @Test
    fun pendingCount() {
        EncounterModelFactory.create(encounterDao, submittedAt = Instant.now())
        EncounterModelFactory.create(encounterDao, submittedAt = null)

        val pendingCount = encounterDao.pendingCount().test().values().first()

        assertEquals(1, pendingCount)
    }

    @Test
    fun returned() {
        val encounterModel1 = EncounterModelFactory.create(
            encounterDao,
            adjudicationState = Encounter.AdjudicationState.RETURNED
        )
        val encounterModel2 = EncounterModelFactory.create(
            encounterDao,
            adjudicationState = Encounter.AdjudicationState.RETURNED
        )
        EncounterModelFactory.create(
            encounterDao,
            adjudicationState = Encounter.AdjudicationState.PENDING
        )
        EncounterModelFactory.create(
            encounterDao,
            adjudicationState = Encounter.AdjudicationState.REVISED
        )

        assertEquals(encounterDao.returned().test().values().first().map { it.encounterModel },
            listOf(encounterModel1, encounterModel2)
        )
    }

    @Test
    fun returnedCount() {
        EncounterModelFactory.create(encounterDao, adjudicationState = Encounter.AdjudicationState.RETURNED)
        EncounterModelFactory.create(encounterDao, adjudicationState = Encounter.AdjudicationState.RETURNED)
        EncounterModelFactory.create(encounterDao, adjudicationState = Encounter.AdjudicationState.PENDING)
        EncounterModelFactory.create(encounterDao, adjudicationState = Encounter.AdjudicationState.REVISED)

        val returnedCount = encounterDao.returnedCount().test().values().first()

        assertEquals(2, returnedCount)
    }

    @Test
    fun update_noExistingRecords() {
        val encounterModel1 = EncounterModelFactory.build()
        assertEquals(encounterDao.update(listOf(encounterModel1)), 0)
    }

    @Test
    fun update_existingRecords() {
        val encounterModel1 = EncounterModelFactory.create(encounterDao)
        assertEquals(encounterDao.update(listOf(encounterModel1)), 1)
    }

    @Test
    fun returnedIds() {
        val encounterModel1 = EncounterModelFactory.create(encounterDao, adjudicationState = Encounter.AdjudicationState.RETURNED)
        val encounterModel2 = EncounterModelFactory.create(encounterDao, adjudicationState = Encounter.AdjudicationState.REVISED)
        val encounterModel3 = EncounterModelFactory.create(encounterDao, adjudicationState = Encounter.AdjudicationState.PENDING)
        val encounterModel4 = EncounterModelFactory.create(encounterDao, adjudicationState = Encounter.AdjudicationState.RETURNED)
        assertEquals(encounterDao.returnedIds().test().values().first(), listOf(encounterModel1.id, encounterModel4.id))
    }

    @Test
    fun revisedIds() {
        val encounterModel1 = EncounterModelFactory.create(encounterDao)
        val encounterModel2 = EncounterModelFactory.create(encounterDao, revisedEncounterId = encounterModel1.id)
        val encounterModel3 = EncounterModelFactory.create(encounterDao)
        val encounterModel4 = EncounterModelFactory.create(encounterDao, revisedEncounterId = encounterModel3.id)
        val encounterModel5 = EncounterModelFactory.create(encounterDao, revisedEncounterId = encounterModel3.id)
        assertEquals(encounterDao.revisedIds().test().values().first(), listOf(encounterModel1.id, encounterModel3.id))
    }

    @Test
    fun unsynced() {
        val unsyncedMember = MemberModelFactory.create(memberDao)
        val unsyncedEncounter = EncounterModelFactory.create(encounterDao = encounterDao, memberId = unsyncedMember.id)
        val syncedMember = MemberModelFactory.create(memberDao)
        val syncedEncounter = EncounterModelFactory.create(encounterDao = encounterDao, memberId = syncedMember.id)

        EncounterModelFactory.create(encounterDao)

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
}

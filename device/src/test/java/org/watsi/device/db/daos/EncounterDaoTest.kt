package org.watsi.device.db.daos

import junit.framework.Assert.assertEquals
import org.junit.Test
import org.watsi.device.factories.EncounterItemModelFactory
import org.watsi.device.factories.EncounterModelFactory
import org.watsi.domain.entities.Encounter

class EncounterDaoTest : DaoBaseTest() {

    @Test
    fun returned() {
        val encounterModel1 = EncounterModelFactory.create(encounterDao, adjudicationState = Encounter.AdjudicationState.RETURNED)
        val encounterModel2 = EncounterModelFactory.create(encounterDao, adjudicationState = Encounter.AdjudicationState.RETURNED)
        EncounterModelFactory.create(encounterDao, adjudicationState = Encounter.AdjudicationState.PENDING)
        EncounterModelFactory.create(encounterDao, adjudicationState = Encounter.AdjudicationState.REVISED)
        val encounterItemModel1 = EncounterItemModelFactory.create(encounterItemDao, encounterId = encounterModel1.id)
        val encounterItemModel2 = EncounterItemModelFactory.create(encounterItemDao, encounterId = encounterModel1.id)
        val encounterItemModel3 = EncounterItemModelFactory.create(encounterItemDao, encounterId = encounterModel2.id)

        val returnedEncountersWithItem = encounterDao.returned().test().values().first()

        assertEquals(returnedEncountersWithItem[0].encounterModel, encounterModel1)
        assertEquals(returnedEncountersWithItem[0].encounterItemModels, listOf(encounterItemModel1, encounterItemModel2))

        assertEquals(returnedEncountersWithItem[1].encounterModel, encounterModel2)
        assertEquals(returnedEncountersWithItem[1].encounterItemModels, listOf(encounterItemModel3))
    }
}

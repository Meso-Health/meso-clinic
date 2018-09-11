package org.watsi.device.db.daos

import org.junit.Assert.assertEquals
import org.junit.Test
import org.watsi.device.factories.BillableModelFactory
import org.watsi.device.factories.EncounterItemModelFactory
import org.watsi.device.factories.EncounterItemWithBillableAndPriceModelFactory
import org.watsi.device.factories.EncounterModelFactory
import org.watsi.device.factories.MemberModelFactory
import org.watsi.device.factories.PriceScheduleModelFactory
import org.watsi.device.factories.PriceScheduleWithBillableModelFactory
import org.watsi.domain.entities.Encounter

class EncounterDaoTest : DaoBaseTest() {

    @Test
    fun returned() {
        val member1 = MemberModelFactory.create(memberDao)
        val member2 = MemberModelFactory.create(memberDao)
        val member3 = MemberModelFactory.create(memberDao)
        val member4 = MemberModelFactory.create(memberDao)

        val encounterModel1 = EncounterModelFactory.create(
            encounterDao, memberId = member1.id,
            adjudicationState = Encounter.AdjudicationState.RETURNED
        )
        val encounterModel2 = EncounterModelFactory.create(
            encounterDao, memberId = member2.id,
            adjudicationState = Encounter.AdjudicationState.RETURNED
        )
        EncounterModelFactory.create(
            encounterDao, memberId = member3.id,
            adjudicationState = Encounter.AdjudicationState.PENDING
        )
        EncounterModelFactory.create(
            encounterDao, memberId = member4.id,
            adjudicationState = Encounter.AdjudicationState.REVISED
        )

        val billable1 = BillableModelFactory.build()
        val billable2 = BillableModelFactory.build()
        val billable3 = BillableModelFactory.build()

        val priceSchedule1 = PriceScheduleModelFactory.build(billableId = billable1.id)
        val priceSchedule2 = PriceScheduleModelFactory.build(billableId = billable2.id)
        val priceSchedule3 = PriceScheduleModelFactory.build(billableId = billable3.id)

        val encounterItemModel1 = EncounterItemModelFactory.build(
            encounterId = encounterModel1.id,
            billableId = billable1.id,
            priceScheduleId = priceSchedule1.id
        )
        val encounterItemModel2 = EncounterItemModelFactory.build(
            encounterId = encounterModel1.id,
            billableId = billable2.id,
            priceScheduleId = priceSchedule2.id
        )
        val encounterItemModel3 = EncounterItemModelFactory.build(
            encounterId = encounterModel2.id,
            billableId = billable3.id,
            priceScheduleId = priceSchedule3.id
        )


        val encounterItemRelationModel1 =
            EncounterItemWithBillableAndPriceModelFactory.create(
                billableDao,
                priceScheduleDao,
                encounterItemDao,
                PriceScheduleWithBillableModelFactory.build(billable1, priceSchedule1),
                encounterItemModel1
            )
        val encounterItemRelationModel2 =
            EncounterItemWithBillableAndPriceModelFactory.create(
                billableDao,
                priceScheduleDao,
                encounterItemDao,
                PriceScheduleWithBillableModelFactory.build(billable2, priceSchedule2),
                encounterItemModel2
            )
        val encounterItemRelationModel3 =
            EncounterItemWithBillableAndPriceModelFactory.create(
                billableDao,
                priceScheduleDao,
                encounterItemDao,
                PriceScheduleWithBillableModelFactory.build(billable3, priceSchedule3),
                encounterItemModel3
            )

        val returnedEncountersWithMemberAndItemsAndForms =
            encounterDao.returned().test().values().first()

        assertEquals(
            returnedEncountersWithMemberAndItemsAndForms[0].encounterModel,
            encounterModel1
        )
        assertEquals(
            returnedEncountersWithMemberAndItemsAndForms[0].memberModel?.first(),
            member1
        )
        assertEquals(
            returnedEncountersWithMemberAndItemsAndForms[0].encounterItemWithBillableAndPriceModels,
            listOf(encounterItemRelationModel1, encounterItemRelationModel2)
        )

        assertEquals(
            returnedEncountersWithMemberAndItemsAndForms[1].encounterModel,
            encounterModel2
        )
        assertEquals(
            returnedEncountersWithMemberAndItemsAndForms[1].memberModel?.first(),
            member2
        )
        assertEquals(
            returnedEncountersWithMemberAndItemsAndForms[1].encounterItemWithBillableAndPriceModels,
            listOf(encounterItemRelationModel3)
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
}

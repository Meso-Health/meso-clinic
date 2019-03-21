package org.watsi.uhp.flowstates

import org.junit.Assert.assertEquals
import org.junit.Test
import org.watsi.domain.entities.Billable
import org.watsi.domain.factories.BillableFactory
import org.watsi.domain.factories.BillableWithPriceScheduleFactory
import org.watsi.domain.factories.EncounterFactory
import org.watsi.domain.factories.EncounterItemFactory
import org.watsi.domain.factories.EncounterItemWithBillableAndPriceFactory
import org.watsi.domain.factories.MemberFactory
import org.watsi.domain.factories.PriceScheduleFactory
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice

class EncounterFlowStateTest {

    @Test
    fun price() {
        val encounterItem1 = EncounterItemFactory.build(quantity = 2)
        val billable1 = BillableFactory.build()
        val billableWithPrice1 = BillableWithPriceScheduleFactory.build(
            billable1,
            PriceScheduleFactory.build(billableId = billable1.id, price = 150)
        )
        val encounterItemRelation1 =
            EncounterItemWithBillableAndPrice(encounterItem1, billableWithPrice1)

        val encounterItem2 = EncounterItemFactory.build(quantity = 4)
        val billable2 = BillableFactory.build()
        val billableWithPrice2 = BillableWithPriceScheduleFactory.build(
            billable2,
            PriceScheduleFactory.build(billableId = billable1.id, price = 50)
        )
        val encounterItemRelation2 =
            EncounterItemWithBillableAndPrice(encounterItem2, billableWithPrice2)

        val encounter = EncounterFactory.build()
        val member = MemberFactory.build(id = encounter.memberId)
        val encounterFlowState = EncounterFlowState(
            encounter = encounter,
            encounterItemRelations = listOf(encounterItemRelation1, encounterItemRelation2),
            encounterForms = emptyList(),
            diagnoses = emptyList(),
            member = member,
            referral = null
        )

        assertEquals(500, encounterFlowState.price())
    }

    @Test
    fun getEncounterItemsOfType() {
        val drugEncounterItem = EncounterItemWithBillableAndPriceFactory.build(
                billableWithPrice = BillableWithPriceScheduleFactory.build(
                    BillableFactory.build(type = Billable.Type.DRUG)
                )
            )
        val serviceEncounterItem = EncounterItemWithBillableAndPriceFactory.build(
            billableWithPrice = BillableWithPriceScheduleFactory.build(
                BillableFactory.build(type = Billable.Type.SERVICE)
            )
        )
        val encounter = EncounterFactory.build()
        val member = MemberFactory.build(id = encounter.memberId)
        val encounterFlowState = EncounterFlowState(
            encounter = encounter,
            encounterItemRelations = listOf(drugEncounterItem, serviceEncounterItem),
            encounterForms = emptyList(),
            diagnoses = emptyList(),
            member = member,
            referral = null
        )

        assertEquals(listOf(drugEncounterItem), encounterFlowState.getEncounterItemsOfType(Billable.Type.DRUG))
    }
}

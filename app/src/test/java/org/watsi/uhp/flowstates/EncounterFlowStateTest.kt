package org.watsi.uhp.flowstates

import org.junit.Assert.assertEquals
import org.junit.Test
import org.watsi.domain.entities.Billable
import org.watsi.domain.factories.BillableFactory
import org.watsi.domain.factories.EncounterFactory
import org.watsi.domain.factories.EncounterItemFactory
import org.watsi.domain.factories.EncounterItemWithBillableAndPriceFactory
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice

class EncounterWithItemsAndFormsTest {

    @Test
    fun price() {
        val encounterItem1 = EncounterItemFactory.build(quantity = 2)
        val billable1 = BillableFactory.build(price = 150)
        val encounterItemRelation1 = EncounterItemWithBillableAndPrice(encounterItem1, billable1)

        val encounterItem2 = EncounterItemFactory.build(quantity = 4)
        val billable2 = BillableFactory.build(price = 50)
        val encounterItemRelation2 = EncounterItemWithBillableAndPrice(encounterItem2, billable2)

        val encounter = EncounterFactory.build()
        val encounterWithItemsAndForms = EncounterFlowState(
                encounter, listOf(encounterItemRelation1, encounterItemRelation2),
                emptyList(), emptyList())

        assertEquals(500, encounterWithItemsAndForms.price())
    }

    @Test
    fun getEncounterItemsOfType() {
        val drugEncounterItem = EncounterItemWithBillableAndPriceFactory.build(
                billable = BillableFactory.build(type = Billable.Type.DRUG))
        val serviceEncounterItem = EncounterItemWithBillableAndPriceFactory.build(
                billable = BillableFactory.build(type = Billable.Type.SERVICE))
        val encounterFlowState = EncounterFlowState(EncounterFactory.build(),
                listOf(drugEncounterItem, serviceEncounterItem), emptyList(), emptyList())

        assertEquals(listOf(drugEncounterItem), encounterFlowState.getEncounterItemsOfType(Billable.Type.DRUG))
    }
}

package org.watsi.uhp.database

import org.junit.Assert.*
import org.junit.Test
import org.watsi.uhp.managers.Clock
import org.watsi.uhp.models.*
import java.util.*

class EncounterItemDaoTest: DaoTest() {

    @Test
    fun fromEncounter() {
        val encounter = Encounter()
        encounter.addEncounterItem(EncounterItem(createBillable("foo"), 2))
        encounter.addEncounterItem(EncounterItem(createBillable("bar"), 3))
        encounter.occurredAt = Clock.getCurrentTime()
        val member = Member()
        member.id = UUID.randomUUID()
        encounter.member = member
        val identificationEvent = IdentificationEvent(member, IdentificationEvent.SearchMethodEnum.SEARCH_ID, null)
        identificationEvent.id = UUID.randomUUID()
        encounter.identificationEvent = identificationEvent
        encounter.saveChanges(null)

        val encounterItems = EncounterItemDao.fromEncounter(encounter)

        assertEquals(encounterItems.size, 2)
        val firstItem: EncounterItem? = encounterItems.find { it.id == (encounter.encounterItems[0].id) }
        assertNotNull(firstItem)
        assertEquals(firstItem!!.quantity, 2)
        assertEquals(firstItem.encounterId, encounter.id)
        assertEquals(firstItem.billable.id, encounter.encounterItems[0].billable.id)
        assertEquals(firstItem.billable.name, "foo")

        val secondItem: EncounterItem? = encounterItems.find { it.id == (encounter.encounterItems[1].id) }
        assertNotNull(secondItem)
        assertEquals(secondItem!!.quantity, 3)
        assertEquals(secondItem.encounterId, encounter.id)
        assertEquals(secondItem.billable.id, encounter.encounterItems[1].billable.id)
        assertEquals(secondItem.billable.name, "bar")
    }

    @Test
    fun getDefaultEncounterItems_opd() {
        createDefaultBillables()

        val defaultEncounterItems =EncounterItemDao.getDefaultEncounterItems(
                IdentificationEvent.ClinicNumberTypeEnum.OPD)

        assertEquals(defaultEncounterItems.size, 2)
        assertEquals(defaultEncounterItems.get(0).billable.name, "Consultation")
        assertEquals(defaultEncounterItems.get(1).billable.name, "Medical Form")
    }

    @Test
    fun getDefaultEncounterItems_delivery() {
        createDefaultBillables()

        val defaultEncounterItems =EncounterItemDao.getDefaultEncounterItems(
                IdentificationEvent.ClinicNumberTypeEnum.DELIVERY)

        assertTrue(defaultEncounterItems.isEmpty())
    }

    private fun createDefaultBillables() {
        createBillable("Consultation")
        createBillable("Medical Form")
    }

    private fun createBillable(name: String): Billable {
        val billable = Billable()
        billable.name = name
        billable.type = Billable.TypeEnum.SERVICE
        billable.price = 100
        billable.generateId()
        BillableDao.create(billable)
        return billable
    }
}
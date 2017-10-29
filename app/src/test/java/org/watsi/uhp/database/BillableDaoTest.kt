package org.watsi.uhp.database

import org.junit.Assert.*
import org.junit.Test
import org.watsi.uhp.models.Billable

class BillableDaoTest: DaoTest() {

    @Test
    fun findByName() {
        val billable1 = createBillable("foo")
        val billable2 = createBillable("foo")
        val billable3 = createBillable("bar")

        val queriedBillableIds = BillableDao.findBillablesByName("foo").map { it.id }

        assertTrue(queriedBillableIds.contains(billable1.id))
        assertTrue(queriedBillableIds.contains(billable2.id))
        assertFalse(queriedBillableIds.contains(billable3.id))
    }

    @Test
    fun allUniqueDrugNames() {
        createBillable("foo", Billable.TypeEnum.DRUG)
        createBillable("bar", Billable.TypeEnum.DRUG)
        createBillable("baz", Billable.TypeEnum.SUPPLY)

        val uniqueDrugNames = BillableDao.allUniqueDrugNames()

        assertEquals(uniqueDrugNames.size, 2)
        assertTrue(uniqueDrugNames.contains("foo"))
        assertTrue(uniqueDrugNames.contains("bar"))
    }

    @Test
    fun fuzzySearchDrugs() {
        val billable1 = createBillable("Aminophylline", Billable.TypeEnum.DRUG)
        val billable2 = createBillable("Amitriptyline", Billable.TypeEnum.DRUG)
        createBillable("Cough Linctus", Billable.TypeEnum.DRUG)
        createBillable("Aminophylline", Billable.TypeEnum.SUPPLY)

        val matchingBillableIds = BillableDao.fuzzySearchDrugs("aminophlyne").map { it.id }

        assertEquals(matchingBillableIds.size, 2)
        assertTrue(matchingBillableIds.contains(billable1.id))
        assertTrue(matchingBillableIds.contains(billable2.id))
    }

    @Test
    fun getBillablesByType() {
        val billable1 = createBillable("foo", Billable.TypeEnum.SUPPLY)
        val billable2 = createBillable("bar", Billable.TypeEnum.SUPPLY)
        createBillable("baz", Billable.TypeEnum.DRUG)

        val supplyBillableIds = BillableDao.getBillablesByType(Billable.TypeEnum.SUPPLY).map { it.id }

        assertEquals(supplyBillableIds.size, 2)
        assertTrue(supplyBillableIds.contains(billable1.id))
        assertTrue(supplyBillableIds.contains(billable2.id))
    }

    @Test
    fun getUniqueBillableCompositions() {
        createBillable("foo", Billable.TypeEnum.DRUG, "tablet")
        createBillable("bar", Billable.TypeEnum.DRUG, "fluid")
        createBillable("baz", Billable.TypeEnum.DRUG, "tablet")
        createBillable("surgical gloves", Billable.TypeEnum.SUPPLY, "pair")

        val uniqueCompositions = BillableDao.getUniqueBillableCompositions()

        assertEquals(uniqueCompositions.size, 3)
        assertTrue(uniqueCompositions.contains("fluid"))
        assertTrue(uniqueCompositions.contains("tablet"))
        assertTrue(uniqueCompositions.contains("pair"))
    }

    private fun createBillable(name: String,
                               type: Billable.TypeEnum = Billable.TypeEnum.DRUG,
                               composition: String? = null): Billable {
        val billable = Billable()
        billable.name = name
        billable.type = type
        billable.price = 100
        billable.composition = composition
        billable.generateId()
        billable.create()
        return billable
    }
}
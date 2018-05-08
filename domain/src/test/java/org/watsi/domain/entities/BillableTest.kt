package org.watsi.domain.entities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.watsi.domain.factories.BillableFactory

class BillableTest {

    @Test
    fun requiresQuantity() {
        assert(Billable.requiresQuantity(Billable.Type.DRUG))
        assert(Billable.requiresQuantity(Billable.Type.SUPPLY))
        assert(Billable.requiresQuantity(Billable.Type.VACCINE))
        assertFalse(Billable.requiresQuantity(Billable.Type.SERVICE))
        assertFalse(Billable.requiresQuantity(Billable.Type.LAB))
    }

    @Test
    fun dosageDetails_noComposition() {
        val billable = BillableFactory.build(composition = null)

        assertNull(billable.dosageDetails())
    }

    @Test
    fun dosageDetails_compositionNoUnit() {
        val billable = BillableFactory.build(composition = "capsule", unit = null)

        assertEquals("capsule", billable.dosageDetails())
    }

    @Test
    fun dosageDetails_compositionAndUnit() {
        val billable = BillableFactory.build(composition = "capsule", unit = "100mg")

        assertEquals("100mg capsule", billable.dosageDetails())
    }
}

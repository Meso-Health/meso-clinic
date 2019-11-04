package org.watsi.domain.entities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.watsi.domain.factories.BillableFactory

class BillableTest {

    @Test
    fun details_noCompositionOrUnit() {
        val billable = BillableFactory.build(composition = null, unit = null)

        assertNull(billable.details())
    }

    @Test
    fun details_compositionNoUnit() {
        val billable = BillableFactory.build(composition = "capsule", unit = null)

        assertEquals("capsule", billable.details())
    }

    @Test
    fun details_compositionAndUnit() {
        val billable = BillableFactory.build(composition = "capsule", unit = "100mg")

        assertEquals("100mg capsule", billable.details())
    }

    @Test
    fun details_unitNoComposition() {
        val billable = BillableFactory.build(unit = "100mg")

        assertEquals("100mg", billable.details())
    }
}

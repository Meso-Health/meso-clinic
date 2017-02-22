package org.watsi.uhp.models;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class BillableTest {

    private Billable billable;

    @Before
    public void setup() {
        billable = new Billable();
    }

    @Test
    public void toString_displaysDescriptive() throws Exception {
        billable.setName("Foo");
        billable.setUnit("Tablet");
        billable.setAmount("30g");

        assertEquals(billable.toString(), "Foo - 30g Tablet");
    }
}

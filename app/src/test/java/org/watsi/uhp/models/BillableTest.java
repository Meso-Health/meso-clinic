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
    public void getDisplayName() throws Exception {
        billable.setName("Foo");
        billable.setUnit("Tablet");
        billable.setAmount("30g");

        assertEquals(billable.getDisplayName(), "Foo - 30g Tablet");
    }
}

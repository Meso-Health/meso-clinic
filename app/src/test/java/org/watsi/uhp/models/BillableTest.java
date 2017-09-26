package org.watsi.uhp.models;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class BillableTest {

    private Billable billable;

    @Before
    public void setup() {
        billable = new Billable();
    }

    @Test
    public void toString_displaysDescriptive() throws Exception {
        billable.setName("Foo");
        billable.setComposition("Tablet");
        billable.setUnit("30g");

        assertEquals(billable.toString(), "Foo - 30g Tablet");
    }

    @Test
    public void valid_nullName_isInValid() throws Exception {
        billable.setPrice(1000);
        billable.setType(Billable.TypeEnum.SERVICE);
        assertFalse(billable.valid());
    }

    @Test
    public void valid_service() throws Exception {
        billable.setName("Service name");
        billable.setPrice(1000);
        billable.setType(Billable.TypeEnum.SERVICE);
        assertTrue(billable.valid());
    }

    @Test
    public void valid_supply() throws Exception {
        billable.setName("Supply name");
        billable.setPrice(1000);
        billable.setType(Billable.TypeEnum.SUPPLY);
        assertTrue(billable.valid());
    }

    @Test
    public void valid_drug() throws Exception {
        billable.setName("Drug name");
        billable.setPrice(1000);
        billable.setType(Billable.TypeEnum.DRUG);
        assertFalse(billable.valid());
        billable.setComposition("Syrup");
        assertFalse(billable.valid());
        billable.setUnit("100 mg");
        assertTrue(billable.valid());
    }

    @Test
    public void valid_vaccine() throws Exception {
        billable.setName("Vaccine name");
        billable.setPrice(1000);
        billable.setType(Billable.TypeEnum.VACCINE);
        assertFalse(billable.valid());
        billable.setUnit("100 mg");
        assertTrue(billable.valid());
    }
}

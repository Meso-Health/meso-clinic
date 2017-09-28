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
    public void valid_nullName_notValid() throws Exception {
        billable.setPrice(1000);
        billable.setType(Billable.TypeEnum.SERVICE);
        assertFalse(billable.valid());
    }

    @Test
    public void valid_serviceHasNameAndPrice_isValid() throws Exception {
        billable.setName("Service name");
        billable.setPrice(1000);
        billable.setType(Billable.TypeEnum.SERVICE);
        assertTrue(billable.valid());
    }

    @Test
    public void valid_supplyHasNameAndPrice_valid() throws Exception {
        billable.setName("Supply name");
        billable.setPrice(1000);
        billable.setType(Billable.TypeEnum.SUPPLY);
        assertTrue(billable.valid());
    }

    @Test
    public void valid_drugNoCompositionOrUnit_invalid() throws Exception {
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
    public void valid_drugNoUnit_invalid() throws Exception {
        billable.setName("Drug name");
        billable.setPrice(1000);
        billable.setType(Billable.TypeEnum.DRUG);
        billable.setComposition("Syrup");
        assertFalse(billable.valid());
        billable.setUnit("100 mg");
        assertTrue(billable.valid());
    }

    @Test
    public void valid_drugNoComposition_invalid() throws Exception {
        billable.setName("Drug name");
        billable.setPrice(1000);
        billable.setType(Billable.TypeEnum.DRUG);
        billable.setUnit("100 mg");
        assertFalse(billable.valid());
    }

    @Test
    public void valid_drugHasNameAndPriceAndCompositionAndUnit_valid() throws Exception {
        billable.setName("Drug name");
        billable.setPrice(1000);
        billable.setType(Billable.TypeEnum.DRUG);
        billable.setComposition("Syrup");
        billable.setUnit("100 mg");
        assertTrue(billable.valid());
    }

    @Test
    public void valid_vaccineHasNameAndPriceAndUnit_valid() throws Exception {
        billable.setName("Vaccine name");
        billable.setPrice(1000);
        billable.setType(Billable.TypeEnum.VACCINE);
        billable.setUnit("100 mg");
        assertTrue(billable.valid());
    }

    @Test
    public void valid_vaccineNoUnits_invalid() throws Exception {
        billable.setName("Vaccine name");
        billable.setPrice(1000);
        billable.setType(Billable.TypeEnum.VACCINE);
        assertFalse(billable.valid());
    }
}

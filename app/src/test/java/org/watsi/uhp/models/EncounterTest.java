package org.watsi.uhp.models;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class EncounterTest {

    private Encounter encounter;

    @Before
    public void setup() {
        encounter = new Encounter();
    }

    @Test
    public void price_noItems() throws Exception {
        assertEquals(encounter.price(), 0);
    }

    @Test
    public void price_withItems() throws Exception {
        List<EncounterItem> encounterItems = new ArrayList<>();
        EncounterItem ei1 = new EncounterItem();
        Billable b1 = new Billable();
        b1.setPrice(500);
        ei1.setQuantity(2);
        ei1.setBillable(b1);
        encounterItems.add(ei1);

        EncounterItem ei2 = new EncounterItem();
        Billable b2 = new Billable();
        b2.setPrice(1400);
        ei2.setQuantity(4);
        ei2.setBillable(b2);
        encounterItems.add(ei2);

        encounter.setEncounterItems(encounterItems);
        assertEquals(encounter.price(), 6600);
    }
}

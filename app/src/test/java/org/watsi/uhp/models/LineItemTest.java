package org.watsi.uhp.models;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class LineItemTest {

    private LineItem lineItem;

    @Before
    public void setup() {
        lineItem = new LineItem();
    }

    @Test
    public void increaseQuantity() throws Exception {
        lineItem.setQuantity(1);
        lineItem.increaseQuantity();
        assertEquals(lineItem.getQuantity(), 2);
    }

    @Test
    public void decreaseQuantity_whenQuantityIsNotGreaterThanOne() throws Exception {
        lineItem.setQuantity(1);
        lineItem.decreaseQuantity();
        assertEquals(lineItem.getQuantity(), 1);
    }

    @Test
    public void decreaseQuantity_whenQuantityIsGreaterThanOne() throws Exception {
        lineItem.setQuantity(3);
        lineItem.decreaseQuantity();
        assertEquals(lineItem.getQuantity(), 2);
    }
}

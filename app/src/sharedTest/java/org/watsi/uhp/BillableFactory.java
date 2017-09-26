package org.watsi.uhp;

import org.watsi.uhp.models.Billable;

public class BillableFactory extends Billable {

    public BillableFactory(String name, Billable.TypeEnum type, String unit, String composition,
                           int price) {
        generateId();
        setName(name);
        setType(type);
        setUnit(unit);
        setComposition(composition);
        setPrice(price);
    }

    public BillableFactory(String name, Billable.TypeEnum type, int price) {
        generateId();
        setName(name);
        setType(type);
        setPrice(price);
    }
}

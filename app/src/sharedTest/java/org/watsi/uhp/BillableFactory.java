package org.watsi.uhp;

import org.watsi.uhp.models.Billable;

class BillableFactory extends Billable {

    BillableFactory(String name, Billable.TypeEnum type, String unit, String composition,
                           int price) {
        generateId();
        setName(name);
        setType(type);
        setUnit(unit);
        setComposition(composition);
        setPrice(price);
        setCreatedDuringEncounter(false);
    }

    BillableFactory(String name, Billable.TypeEnum type, int price) {
        generateId();
        setName(name);
        setType(type);
        setPrice(price);
        setCreatedDuringEncounter(false);
    }
}

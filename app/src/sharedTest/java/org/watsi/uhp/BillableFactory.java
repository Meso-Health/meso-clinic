package org.watsi.uhp;

import org.watsi.uhp.models.Billable;

import java.sql.SQLException;

public class BillableFactory {
    public static Billable createBillable(String name, Billable.TypeEnum type, String unit,
              String composition, int price, boolean requiresLabResult) throws SQLException {
        Billable billable = new Billable();
        billable.generateId();
        billable.setName(name);
        billable.setType(type);
        billable.setUnit(unit);
        billable.setComposition(composition);
        billable.setPrice(price);
        billable.setCreatedDuringEncounter(false);
        billable.setRequiresLabResult(requiresLabResult);
        billable.create();
        return billable;
    }

    public static Billable createBillable(String name, Billable.TypeEnum type, int price,
              boolean requiresLabResult) throws SQLException {
        Billable billable = new Billable();
        billable.generateId();
        billable.setName(name);
        billable.setType(type);
        billable.setPrice(price);
        billable.setCreatedDuringEncounter(false);
        billable.setRequiresLabResult(requiresLabResult);
        billable.create();
        return billable;
    }
}

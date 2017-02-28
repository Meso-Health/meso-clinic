package org.watsi.uhp.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

@DatabaseTable(tableName = LineItem.TABLE_NAME)
public class LineItem extends AbstractModel {

    public static final String TABLE_NAME = "line_items";
    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_ENCOUNTER_ID = "encounter_id";
    public static final String FIELD_NAME_BILLABLE_ID = "billable_id";
    public static final String FIELD_NAME_QUANTITY = "quantity";

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private UUID mId;

    @DatabaseField(columnName = FIELD_NAME_ENCOUNTER_ID, foreign = true)
    private Encounter mEncounter;

    @DatabaseField(columnName = FIELD_NAME_BILLABLE_ID, foreign = true)
    private Billable mBillable;

    @DatabaseField(columnName = FIELD_NAME_QUANTITY)
    private int mQuantity = 1;

    public LineItem() {
        super();
    }

    public LineItem(Billable billable, int quantity) {
        setBillable(billable);
        setQuantity(quantity);
    }

    public Encounter getEncounter() {
        return mEncounter;
    }

    public void setEncounter(Encounter encounter) {
        this.mEncounter = encounter;
    }

    public Billable getBillable() {
        return mBillable;
    }

    public void setBillable(Billable billable) {
        this.mBillable = billable;
    }

    public int getQuantity() {
        return mQuantity;
    }

    public void setQuantity(Integer quantity) {
        this.mQuantity = quantity;
    }
}
